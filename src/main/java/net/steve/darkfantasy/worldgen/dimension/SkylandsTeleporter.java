package net.steve.darkfantasy.worldgen.dimension;

import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.event.PortalIgnitionHandler;
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
 * Builds the {@link TeleportTransition} used by {@link net.steve.darkfantasy.block.custom.SkylandsPortalBlock}
 * when an entity steps into a portal. On first arrival at a Skylands landing site we stamp a
 * 7×7 stone platform with a fully-ignited return portal centered on it, so the player is
 * never dropped into the void even if the noise generator gave us no terrain at that X/Z.
 */
public final class SkylandsTeleporter {
    private SkylandsTeleporter() {}

    /** Stone platform block layer; player feet land one block above this. */
    private static final int SKYLANDS_PLATFORM_Y = 99;

    /** Half-extent in blocks — gives a 7×7 stone pad. */
    private static final int PLATFORM_RADIUS = 3;

    public static @Nullable TeleportTransition createTransition(Entity entity, ServerLevel destination, BlockPos sourcePortalPos) {
        BlockPos landingBlock;
        if (destination.dimension() == ModDimensions.SKYLANDS) {
            BlockPos platformCenter = new BlockPos(sourcePortalPos.getX(), SKYLANDS_PLATFORM_Y, sourcePortalPos.getZ());
            landingBlock = ensureSkylandsLandingPad(destination, platformCenter);
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
     * Idempotently builds the 7×7 platform + ignited return portal. The portal column sits
     * directly above the platform's center block; the player lands one block west of that
     * column, atop the western perimeter brick. Repeat visits reuse the existing pad.
     */
    private static BlockPos ensureSkylandsLandingPad(ServerLevel level, BlockPos platformCenter) {
        BlockPos soulSoilPos = platformCenter.above();
        BlockPos landingPos = soulSoilPos.offset(-1, 1, 0);

        if (level.getBlockState(soulSoilPos).is(Blocks.SOUL_SOIL)) {
            return landingPos;
        }

        // 1. Stamp the 7×7 platform and clear 5 blocks of headroom above it.
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                level.setBlock(platformCenter.offset(dx, 0, dz), stone, 3);
                for (int dy = 1; dy <= 5; dy++) {
                    level.setBlock(platformCenter.offset(dx, dy, dz), air, 3);
                }
            }
        }

        // 2. Lay the frame.
        BlockState netherBrick = Blocks.NETHER_BRICKS.defaultBlockState();
        level.setBlock(soulSoilPos, Blocks.SOUL_SOIL.defaultBlockState(), 3);
        for (int[] offset : PortalIgnitionHandler.REQUIRED_NETHER_BRICKS) {
            level.setBlock(soulSoilPos.offset(offset[0], offset[1], offset[2]), netherBrick, 3);
        }
        BlockPos goldPos = soulSoilPos.offset(
                PortalIgnitionHandler.GOLD_BLOCK_POS[0],
                PortalIgnitionHandler.GOLD_BLOCK_POS[1],
                PortalIgnitionHandler.GOLD_BLOCK_POS[2]);
        level.setBlock(goldPos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);

        // 3. Portal blocks last so updateShape sees a completed frame.
        BlockState portal = ModBlocks.SKYLANDS_PORTAL.get().defaultBlockState();
        for (int[] offset : PortalIgnitionHandler.PORTAL_POSITIONS) {
            level.setBlock(soulSoilPos.offset(offset[0], offset[1], offset[2]), portal, 3);
        }

        return landingPos;
    }

    /**
     * Scan down at the source X/Z for the overworld portal's soul-soil anchor and land one
     * cardinal step west of it. Falls back to the heightmap so we never drop into open air.
     */
    private static BlockPos findOverworldLanding(ServerLevel level, BlockPos sourcePortalPos) {
        int targetX = sourcePortalPos.getX();
        int targetZ = sourcePortalPos.getZ();

        int topY = Math.min(level.getMaxY(),
                level.getHeight(Heightmap.Types.MOTION_BLOCKING, targetX, targetZ));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = topY; y >= level.getMinY(); y--) {
            cursor.set(targetX, y, targetZ);
            if (level.getBlockState(cursor).is(Blocks.SOUL_SOIL)) {
                return new BlockPos(targetX - 1, y + 1, targetZ);
            }
        }

        int safeY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        return new BlockPos(targetX, safeY, targetZ);
    }
}
