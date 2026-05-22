package net.steve.darkfantasy.worldgen.dimension;

import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.block.custom.TwilightPortalBlock;
import net.steve.darkfantasy.event.TwilightPortalIgnitionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Twilight Forest portal teleporter. Auto-builds a flat 5×5 bookshelf-framed nether-style
 * portal on the ground at the landing site so the player has a way home. The frame is
 * X-axis-oriented (spans east/west, faces north/south) by convention; the player lands
 * two blocks south of it, facing into the portal.
 */
public final class TwilightTeleporter {
    private TwilightTeleporter() {}

    public static @Nullable TeleportTransition createTransition(Entity entity, ServerLevel destination, BlockPos sourcePortalPos) {
        BlockPos landingBlock;
        if (destination.dimension() == ModDimensions.TWILIGHT_FOREST) {
            landingBlock = ensureTwilightPortal(destination, sourcePortalPos.getX(), sourcePortalPos.getZ());
        } else {
            landingBlock = findOverworldLanding(destination, sourcePortalPos);
        }

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
     * Idempotently builds a 5×5 bookshelf frame with an ignited portal at the heightmap
     * surface for {@code (x, z)}. The frame is X-axis-oriented and sits at z = {@code z};
     * the player lands two blocks south (z + 2). Returns the player's landing position.
     */
    private static BlockPos ensureTwilightPortal(ServerLevel level, int x, int z) {
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        // Frame bottom-left origin: 2 west of the center column, 1 below the player's feet,
        // so the bottom row of bookshelves embeds into the surface (vanilla nether-portal style).
        BlockPos origin = new BlockPos(x - 2, groundY - 1, z);
        BlockPos landingPos = new BlockPos(x, groundY, z + 2);

        // If the center interior portal block is already lit, the portal exists — reuse it.
        BlockPos interiorCenter = origin.offset(2, 2, 0);
        if (level.getBlockState(interiorCenter).is(ModBlocks.TWILIGHT_PORTAL.get())) {
            return landingPos;
        }

        // Clear the frame interior + a 2-block-deep approach corridor in front of it, so
        // a tree or hillside can't suffocate the player on arrival.
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = 0; dx < TwilightPortalIgnitionHandler.FRAME_SIZE; dx++) {
            for (int dy = 0; dy < TwilightPortalIgnitionHandler.FRAME_SIZE; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    // dz == 0 is the frame plane itself — bookshelves overwrite below.
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

    private static BlockPos findOverworldLanding(ServerLevel level, BlockPos sourcePortalPos) {
        int targetX = sourcePortalPos.getX();
        int targetZ = sourcePortalPos.getZ();

        // The overworld portal is identified by an active twilight portal block in its center.
        // Scan downward at the same X/Z to find one, then land 2 blocks south of it (frame plane + 2).
        int topY = Math.min(level.getMaxY(),
                level.getHeight(Heightmap.Types.MOTION_BLOCKING, targetX, targetZ));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = topY; y >= level.getMinY(); y--) {
            cursor.set(targetX, y, targetZ);
            if (level.getBlockState(cursor).is(ModBlocks.TWILIGHT_PORTAL.get())) {
                return new BlockPos(targetX, y - 1, targetZ + 2);
            }
        }

        int safeY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        return new BlockPos(targetX, safeY, targetZ);
    }
}
