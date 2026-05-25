package net.steve.darkfantasy.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Liquid block placed in-world for the Elixir fluid. Inherits all the standard
 * fluid block behavior (BucketPickup, source-block bucket fill, etc.) from
 * {@link LiquidBlock}; the only addition is applying {@link MobEffects#LEVITATION}
 * to any LivingEntity touching the fluid.
 *
 * <h2>Levitation application</h2>
 * {@link #entityInside} fires every tick a colliding entity's hitbox overlaps the
 * block. We re-apply a fresh 40-tick Levitation level-0 effect, which:
 * <ul>
 *   <li>Refreshes faster than it expires (40t apply, ticked every game tick) so
 *       there's no "popping" out the moment the entity briefly leaves the column.</li>
 *   <li>Uses {@code particles=false, icon=false} so the player's HUD doesn't get a
 *       persistent effect badge during what should feel like passive movement.</li>
 *   <li>Filters to {@link LivingEntity} — items/projectiles don't get pushed up.</li>
 * </ul>
 *
 * <p>Note: Levitation overrides regular gravity entirely. Sit a player in a pool
 * and they'll drift upward at ~0.9 blocks/sec (vanilla level-0 rate). Combined
 * with the fluid's lava-like slow flow, an elixir pool functions as a "lift."
 */
public class ElixirLiquidBlock extends LiquidBlock {
    /** Re-applied every tick the entity is inside. 40 ticks = 2 s of overlap buffer. */
    private static final int LEVITATION_DURATION_TICKS = 40;

    public ElixirLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos,
                                Entity entity, InsideBlockEffectApplier effectApplier,
                                boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        if (!(entity instanceof LivingEntity living)) return;
        // Server-authoritative effect application — adding from client would desync.
        if (level.isClientSide()) return;
        living.addEffect(new MobEffectInstance(MobEffects.LEVITATION,
                LEVITATION_DURATION_TICKS, 0, false, false));
    }
}
