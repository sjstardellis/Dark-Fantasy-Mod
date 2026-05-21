package net.steve.darkfantasy.worldgen.dimension;

import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.event.TwilightPortalIgnitionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Twilight Forest portal teleporter. Lands the player on the natural ground (rolling
 * overworld-style terrain in the Twilight Forest) and auto-builds a return portal on
 * first arrival so the player is never stranded.
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
     * Idempotently builds the return portal on the Twilight Forest surface. Returns the
     * spot the player should land on (one cardinal step west of the amethyst anchor).
     */
    private static BlockPos ensureTwilightPortal(ServerLevel level, int x, int z) {
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos amethystPos = new BlockPos(x, groundY, z);
        BlockPos landingPos = amethystPos.offset(-1, 1, 0);

        if (level.getBlockState(amethystPos).is(Blocks.AMETHYST_BLOCK)) {
            return landingPos;
        }

        // Clear 3×3 × 5-tall airspace so trees / hills don't block the portal or smother the player.
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    level.setBlock(amethystPos.offset(dx, dy, dz), air, 3);
                }
            }
        }

        BlockState bookshelf = Blocks.BOOKSHELF.defaultBlockState();
        level.setBlock(amethystPos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        for (int[] offset : TwilightPortalIgnitionHandler.REQUIRED_BOOKSHELVES) {
            level.setBlock(amethystPos.offset(offset[0], offset[1], offset[2]), bookshelf, 3);
        }
        BlockPos glowstonePos = amethystPos.offset(
                TwilightPortalIgnitionHandler.GLOWSTONE_POS[0],
                TwilightPortalIgnitionHandler.GLOWSTONE_POS[1],
                TwilightPortalIgnitionHandler.GLOWSTONE_POS[2]);
        level.setBlock(glowstonePos, Blocks.GLOWSTONE.defaultBlockState(), 3);

        // Portal blocks last so updateShape sees the completed frame.
        BlockState portal = ModBlocks.TWILIGHT_PORTAL.get().defaultBlockState();
        for (int[] offset : TwilightPortalIgnitionHandler.PORTAL_POSITIONS) {
            level.setBlock(amethystPos.offset(offset[0], offset[1], offset[2]), portal, 3);
        }

        return landingPos;
    }

    private static BlockPos findOverworldLanding(ServerLevel level, BlockPos sourcePortalPos) {
        int targetX = sourcePortalPos.getX();
        int targetZ = sourcePortalPos.getZ();

        int topY = Math.min(level.getMaxY(),
                level.getHeight(Heightmap.Types.MOTION_BLOCKING, targetX, targetZ));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = topY; y >= level.getMinY(); y--) {
            cursor.set(targetX, y, targetZ);
            if (level.getBlockState(cursor).is(Blocks.AMETHYST_BLOCK)) {
                return new BlockPos(targetX - 1, y + 1, targetZ);
            }
        }

        int safeY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        return new BlockPos(targetX, safeY, targetZ);
    }
}
