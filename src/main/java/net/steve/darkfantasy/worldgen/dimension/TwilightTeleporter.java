package net.steve.darkfantasy.worldgen.dimension;

import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.block.custom.TwilightPortalBlock;
import net.steve.darkfantasy.event.TwilightPortalIgnitionHandler;
import net.steve.darkfantasy.init.ModPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Twilight Forest portal teleporter. Mirrors vanilla's nether-portal flow ({@link
 * net.minecraft.world.level.portal.PortalForcer}):
 *
 * <ol>
 *   <li>Compute an approximate exit position by clamping the source X/Y/Z to the
 *       destination world border. The Twilight Forest is 1:1 with the overworld so no
 *       coordinate scaling is applied.</li>
 *   <li>Ask the destination's {@link PoiManager} for the closest existing twilight portal
 *       within {@link #SEARCH_RADIUS} blocks (same radius as vanilla overworld lookup).
 *       If one exists, land adjacent to it.</li>
 *   <li>Otherwise, spiral out from the exit X/Z looking for a column whose surface can
 *       host a clean 5×5 bookshelf frame and a 2-block approach corridor without clipping
 *       into terrain. Build there.</li>
 *   <li>If no clean column is found anywhere in the search square, fall back to building
 *       at the original X/Z so the player isn't stranded.</li>
 * </ol>
 */
public final class TwilightTeleporter {
    private TwilightTeleporter() {}

    /** Same radius vanilla uses for nether→overworld portal lookups. */
    private static final int SEARCH_RADIUS = 128;

    /** Spiral radius (chunks) when searching for a clean column to build a new portal. */
    private static final int PLACEMENT_SPIRAL_RADIUS = 16;

    public static @Nullable TeleportTransition createTransition(Entity entity, ServerLevel destination, BlockPos sourcePortalPos) {
        WorldBorder border = destination.getWorldBorder();
        BlockPos approxExitPos = border.clampToBounds(
                sourcePortalPos.getX(), sourcePortalPos.getY(), sourcePortalPos.getZ());

        // Force the destination chunks around the approximate exit to load so the POI
        // manager indexes any pre-existing portal blocks. PortalForcer does the same.
        PoiManager poiManager = destination.getPoiManager();
        poiManager.ensureLoadedAndValid(destination, approxExitPos, SEARCH_RADIUS);

        BlockPos landingPos;
        Optional<BlockPos> existing = findClosestExistingPortal(destination, poiManager, approxExitPos, border);
        if (existing.isPresent()) {
            landingPos = landingFromExistingPortal(destination, existing.get());
        } else {
            landingPos = createNewPortal(destination, approxExitPos.getX(), approxExitPos.getZ());
        }

        Vec3 position = Vec3.atBottomCenterOf(landingPos);
        return new TeleportTransition(
                destination,
                position,
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND);
    }

    // ---- Find existing portal via POI --------------------------------------

    private static Optional<BlockPos> findClosestExistingPortal(ServerLevel level, PoiManager poiManager, BlockPos approxExitPos, WorldBorder border) {
        return poiManager.getInSquare(
                        type -> type.is(ModPoiTypes.TWILIGHT_PORTAL.getKey()),
                        approxExitPos,
                        SEARCH_RADIUS,
                        PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(border::isWithinBounds)
                // Guard against stale POI records — confirm the block is still a portal.
                .filter(pos -> level.getBlockState(pos).is(ModBlocks.TWILIGHT_PORTAL.get()))
                .min(Comparator.comparingDouble(p -> p.distSqr(approxExitPos)));
    }

    /**
     * Compute a safe landing block adjacent to an existing portal frame. The interior is
     * 3 blocks tall, so the bottom-most portal block sits at ground level + 1. We grab
     * any portal block from the POI and walk down to the bottom of the column, then step
     * one block out from the frame plane based on the portal's axis.
     */
    private static BlockPos landingFromExistingPortal(ServerLevel level, BlockPos anyPortalPos) {
        // Walk down to the lowest portal block in the column so we're consistent regardless
        // of which of the 9 interior blocks the POI returned.
        BlockPos.MutableBlockPos cursor = anyPortalPos.mutable();
        while (cursor.getY() > level.getMinY()
                && level.getBlockState(cursor.below()).is(ModBlocks.TWILIGHT_PORTAL.get())) {
            cursor.move(Direction.DOWN);
        }
        BlockPos bottom = cursor.immutable();
        Direction.Axis axis = level.getBlockState(bottom).getValue(TwilightPortalBlock.AXIS);

        // Frame plane lies along `axis`; step 2 along the perpendicular horizontal so we're
        // clear of the bookshelves. The bottom interior portal sits one block above ground.
        int groundY = bottom.getY();
        if (axis == Direction.Axis.X) {
            return new BlockPos(bottom.getX(), groundY, bottom.getZ() + 2);
        }
        return new BlockPos(bottom.getX() + 2, groundY, bottom.getZ());
    }

    // ---- Build new portal --------------------------------------------------

    /**
     * Spiral out from the target X/Z looking for the closest column with terrain that can
     * cleanly host a 5×5 bookshelf frame on its surface. Builds there. Falls back to the
     * original X/Z if nothing suitable is found within {@link #PLACEMENT_SPIRAL_RADIUS}.
     *
     * <p>Each chunk the spiral touches is force-loaded to {@code ChunkStatus.FULL} on first
     * visit — {@link net.minecraft.world.level.Level#getHeight} only returns real heightmap
     * values for FULL chunks, and {@link PoiManager#ensureLoadedAndValid} only loads to
     * EMPTY. Without this force-load, every column reads {@code level.getMinY()} as its
     * ground height and the portal ends up at the bottom of the world.
     */
    private static BlockPos createNewPortal(ServerLevel level, int targetX, int targetZ) {
        BlockPos origin = new BlockPos(targetX, 64, targetZ);

        // We always build along the X axis so the player lands on the +Z side of the frame.
        Direction.Axis axis = Direction.Axis.X;

        // Track which chunks we've already upgraded to FULL so we don't redundantly upgrade.
        Set<Long> loaded = new HashSet<>();

        for (BlockPos.MutableBlockPos columnPos : BlockPos.spiralAround(
                origin, PLACEMENT_SPIRAL_RADIUS, Direction.EAST, Direction.SOUTH)) {
            ensureChunkFull(level, columnPos.getX(), columnPos.getZ(), loaded);
            int groundY = level.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnPos.getX(), columnPos.getZ());
            if (canHostFrame(level, columnPos.getX(), groundY, columnPos.getZ(), axis)) {
                return buildFrameAt(level, columnPos.getX(), groundY, columnPos.getZ(), axis);
            }
        }

        // Nothing clean nearby — force a build at the original target so the player isn't
        // dropped into the void. The fallback's chunk is already FULL from the spiral above
        // (the spiral starts at targetX,targetZ), but call ensureChunkFull again to be safe.
        ensureChunkFull(level, targetX, targetZ, loaded);
        int fallbackGroundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        return buildFrameAt(level, targetX, fallbackGroundY, targetZ, axis);
    }

    /** Upgrade the chunk containing {@code (x, z)} to {@code ChunkStatus.FULL} if we haven't already. */
    private static void ensureChunkFull(ServerLevel level, int x, int z, Set<Long> alreadyLoaded) {
        int chunkX = SectionPos.blockToSectionCoord(x);
        int chunkZ = SectionPos.blockToSectionCoord(z);
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        if (alreadyLoaded.add(key)) {
            level.getChunk(chunkX, chunkZ); // defaults to ChunkStatus.FULL, blocking
        }
    }

    /**
     * True iff the column at {@code (x, z)} with surface {@code groundY} can host the 5×5
     * frame on its {@code axis}-aligned plane, with a 2-block-deep approach corridor in
     * the +perpendicular direction, all without clipping into existing terrain. Also
     * requires solid ground under the player's landing spot.
     */
    private static boolean canHostFrame(ServerLevel level, int x, int groundY, int z, Direction.Axis axis) {
        if (groundY <= level.getMinY() + 2 || groundY >= level.getMaxY() - TwilightPortalIgnitionHandler.FRAME_SIZE) {
            return false;
        }

        // Frame origin: 2 west/north of the center column, 1 below the player's feet so the
        // bottom row of bookshelves embeds into the surface (matches existing geometry).
        BlockPos origin = axis == Direction.Axis.X
                ? new BlockPos(x - 2, groundY - 1, z)
                : new BlockPos(x, groundY - 1, z - 2);

        // 5×5 frame plane + 2-block-deep corridor in front of it must all be replaceable.
        for (int dw = 0; dw < TwilightPortalIgnitionHandler.FRAME_SIZE; dw++) {
            for (int dh = 1; dh < TwilightPortalIgnitionHandler.FRAME_SIZE; dh++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos check = axis == Direction.Axis.X
                            ? origin.offset(dw, dh, dz)
                            : origin.offset(dz, dh, dw);
                    if (!level.getBlockState(check).canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        // The two blocks beneath the player's landing spot must be solid ground.
        BlockPos landingUnder = axis == Direction.Axis.X
                ? new BlockPos(x, groundY - 1, z + 2)
                : new BlockPos(x + 2, groundY - 1, z);
        return level.getBlockState(landingUnder).isSolid();
    }

    /**
     * Stamps the 5×5 bookshelf frame at the given surface position, lights the portal,
     * and returns the player's landing block (two blocks out from the frame on the
     * approach side). Matches the geometry of {@link TwilightPortalIgnitionHandler}.
     */
    private static BlockPos buildFrameAt(ServerLevel level, int x, int groundY, int z, Direction.Axis axis) {
        BlockPos origin = axis == Direction.Axis.X
                ? new BlockPos(x - 2, groundY - 1, z)
                : new BlockPos(x, groundY - 1, z - 2);
        BlockPos landingPos = axis == Direction.Axis.X
                ? new BlockPos(x, groundY, z + 2)
                : new BlockPos(x + 2, groundY, z);

        // Clear the frame interior + 2-deep approach corridor.
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dw = 0; dw < TwilightPortalIgnitionHandler.FRAME_SIZE; dw++) {
            for (int dh = 0; dh < TwilightPortalIgnitionHandler.FRAME_SIZE; dh++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos pos = axis == Direction.Axis.X
                            ? origin.offset(dw, dh, dz)
                            : origin.offset(dz, dh, dw);
                    level.setBlock(pos, air, 3);
                }
            }
        }

        // Ensure solid ground under the player.
        for (int dz = 1; dz <= 2; dz++) {
            BlockPos under = axis == Direction.Axis.X
                    ? origin.offset(2, 0, dz)
                    : origin.offset(dz, 0, 2);
        }

        // Lay the 16 perimeter enchanted bookshelves on the frame plane.
        BlockState bookshelf = ModBlocks.ENCHANTED_BOOKSHELF.get().defaultBlockState();
        for (int dw = 0; dw < TwilightPortalIgnitionHandler.FRAME_SIZE; dw++) {
            for (int dh = 0; dh < TwilightPortalIgnitionHandler.FRAME_SIZE; dh++) {
                boolean isPerimeter = dw == 0 || dw == TwilightPortalIgnitionHandler.FRAME_SIZE - 1
                        || dh == 0 || dh == TwilightPortalIgnitionHandler.FRAME_SIZE - 1;
                if (isPerimeter) {
                    BlockPos pos = axis == Direction.Axis.X
                            ? origin.offset(dw, dh, 0)
                            : origin.offset(0, dh, dw);
                    level.setBlock(pos, bookshelf, 3);
                }
            }
        }

        // Ignite the interior. Portal blocks placed last so updateShape sees a finished frame.
        TwilightPortalIgnitionHandler.ignitePortal(level, origin, axis);

        return landingPos;
    }
}
