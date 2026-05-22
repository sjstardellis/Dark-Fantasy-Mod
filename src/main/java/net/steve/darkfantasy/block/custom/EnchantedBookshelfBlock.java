package net.steve.darkfantasy.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Drop-in bookshelf upgrade — counts as 2 vanilla bookshelves for the enchanting table
 * (so 8 of these around an enchanting table give max enchantment levels instead of 15).
 * Also tagged for the twilight portal frame so it can substitute for vanilla bookshelves
 * there. Emits a subtle enchanting glow.
 */
public class EnchantedBookshelfBlock extends Block {
    /** Each block counts as this many vanilla bookshelves for enchantment-power purposes. */
    private static final float ENCHANT_POWER = 2.0F;

    public EnchantedBookshelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, BlockGetter level, BlockPos pos) {

        return ENCHANT_POWER;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Sparse enchant-style glints drifting upward from the shelves.
        if (random.nextInt(4) == 0) {
            level.addParticle(
                    ParticleTypes.GLOW,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 1.0 + random.nextDouble() * 0.2,
                    pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.2,
                    random.nextDouble() * 0.3,
                    (random.nextDouble() - 0.5) * 0.2);
        }
    }
}
