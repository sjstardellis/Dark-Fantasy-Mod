package net.steve.darkfantasy.worldgen.dimension;

import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.block.custom.TwilightPortalBlock;
import net.steve.darkfantasy.event.TwilightPortalIgnitionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Twilight Forest portal teleporter. Symmetric in both directions: whichever dimension the
 * player is arriving in, we first look for an existing twilight portal at the source's X/Z
 * column and reuse it; only if none is present do we stamp a fresh 5×5 bookshelf frame at
 * the surface. The auto-built frame is X-axis-oriented (faces N/S) and the player lands
 * two blocks south of it.
 */
public final class TwilightTeleporter {
    private TwilightTeleporter() {}

    public static @Nullable TeleportTransition createTransition(Entity entity, ServerLevel destination, BlockPos sourcePortalPos) {
        BlockPos landingBlock = ensurePortalAt(destination, sourcePortalPos.getX(), sourcePortalPos.getZ());

        Vec3 position = Vec3.atBottomCenterOf(landingBlock);
        return new TeleportTransition(
                destination,
                position,
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND);
    }

    /**
     * Reuses an existing twilight portal at {@code (x, z)} if present, or builds a new
     * 5×5 frame at the surface there. Returns the player's landing position adjacent to
     * the frame. Works in both directions — outbound to the Twilight Forest *and* return
     * to the overworld — so a player-built portal in either dimension auto-builds its
     * counterpart in the other.
     */
    private static BlockPos ensurePortalAt(ServerLevel level, int x, int z) {
        // createTransition runs before the destination chunk has been generated. Force
        // the chunk to FULL status so the heightmap is computed and getBlockState reads
        // real terrain, not the min-Y default for an unloaded chunk.
        level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));

        BlockPos existing = findExistingPortalColumn(level, x, z);
        if (existing != null) {
            return landingFromExisting(level, existing);
        }

        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return buildNewPortal(level, x, groundY, z);
    }

    /**
     * Scan the full Y column at {@code (x, z)} for an active twilight portal block.
     * The scan is heightmap-independent because a previously-built frame's bookshelves
     * raise the heightmap by 4 — so an idempotency check that probes a heightmap-relative
     * position would miss the existing portal column and we'd stack a second frame on
     * top.
     */
    private static @Nullable BlockPos findExistingPortalColumn(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = level.getMaxY(); y >= level.getMinY(); y--) {
            cursor.set(x, y, z);
            if (level.getBlockState(cursor).is(ModBlocks.TWILIGHT_PORTAL.get())) {
                return cursor.immutable();
            }
        }
        return null;
    }

    /**
     * Compute a safe landing block adjacent to an existing portal frame. The top portal
     * block sits at {@code groundY + 2} (interior is 3 tall starting at {@code groundY}),
     * so ground level for the player is two blocks below the topmost portal block we found.
     * Direction depends on the frame's axis — X-axis faces N/S so we land south,
     * Z-axis faces E/W so we land east.
     */
    private static BlockPos landingFromExisting(ServerLevel level, BlockPos topPortalPos) {
        Direction.Axis axis = level.getBlockState(topPortalPos).getValue(TwilightPortalBlock.AXIS);
        int groundY = topPortalPos.getY() - 2;
        if (axis == Direction.Axis.X) {
            return new BlockPos(topPortalPos.getX(), groundY, topPortalPos.getZ() + 2);
        }
        return new BlockPos(topPortalPos.getX() + 2, groundY, topPortalPos.getZ());
    }

    private static BlockPos buildNewPortal(ServerLevel level, int x, int groundY, int z) {
        // Frame bottom-left origin: 2 west of the center column, 1 below the player's feet
        // so the bottom row of bookshelves embeds into the surface.
        BlockPos origin = new BlockPos(x - 2, groundY - 1, z);
        BlockPos landingPos = new BlockPos(x, groundY, z + 2);

        // Clear the frame interior + a 2-block-deep approach corridor in front of it, so
        // a tree or hillside can't suffocate the player on arrival.
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = 0; dx < TwilightPortalIgnitionHandler.FRAME_SIZE; dx++) {
            for (int dy = 0; dy < TwilightPortalIgnitionHandler.FRAME_SIZE; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    level.setBlock(origin.offset(dx, dy, dz), air, 3);
                }
            }
        }

        // Ensure solid ground beneath the player's landing spot.
        for (int dz = 1; dz <= 2; dz++) {
            BlockPos under = origin.offset(2, -1, dz);
            if (!level.getBlockState(under).isSolid()) {
                level.setBlock(under, Blocks.DIRT.defaultBlockState(), 3);
            }
        }

        // Lay the 16 perimeter bookshelves.
        BlockState bookshelf = Blocks.BOOKSHELF.defaultBlockState();
        for (int dx = 0; dx < TwilightPortalIgnitionHandler.FRAME_SIZE; dx++) {
            for (int dy = 0; dy < TwilightPortalIgnitionHandler.FRAME_SIZE; dy++) {
                boolean isPerimeter = dx == 0 || dx == 4 || dy == 0 || dy == 4;
                if (isPerimeter) {
                    level.setBlock(origin.offset(dx, dy, 0), bookshelf, 3);
                }
            }
        }

        // Ignite the interior. Portal blocks placed last so updateShape sees a finished frame.
        TwilightPortalIgnitionHandler.ignitePortal(level, origin, Direction.Axis.X);

        return landingPos;
    }
}
