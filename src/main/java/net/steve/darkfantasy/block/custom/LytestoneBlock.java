package net.steve.darkfantasy.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystallized lytebug essence. Emits full light (15) and acts as a lytebug magnet —
 * lytebugs within ~16 blocks drift toward placed Lytestone via LytebugEntity's
 * MoveToLytestoneGoal. This class only adds ambient GLOW particle drifts above the
 * block; light emission comes from Block.Properties#lightLevel in ModBlocks.
 */
public class LytestoneBlock extends Block {
    public LytestoneBlock(Properties properties) {
        super(properties);
    }

    /**
     * Client-side per-tick visual: occasional GLOW particle drifting up from the top.
     * Throttled to ~1-in-8 ticks so a wall of Lytestone doesn't drown the screen in
     * particles.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0) return;
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 1.0 + random.nextDouble() * 0.3;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0, 0.02, 0.0);
    }
}
