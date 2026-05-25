package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Source + flowing fluid pair for Elixir, plus the shared {@link BaseFlowingFluid.Properties}
 * builder that wires them to the {@link ModFluidTypes#ELIXIR_TYPE} fluid type,
 * the {@link ModItems#ELIXIR_BUCKET} bucket, and the {@link ModBlocks#ELIXIR}
 * placed-in-world liquid block.
 *
 * <p>Tuning notes (lava-like, slow & thick):
 * <ul>
 *   <li>{@code slopeFindDistance 2} — short. Lava uses 2; water uses 4. Limits how
 *       far the fluid "searches" downhill when picking flow direction.</li>
 *   <li>{@code levelDecreasePerBlock 2} — drops 2 fluid units per block, so the
 *       flowing edge thins out after ~4 blocks (lava behavior). Water uses 1 for
 *       its 8-block reach.</li>
 *   <li>{@code tickRate 30} — slow updates. Lava is 30, water is 5.</li>
 *   <li>{@code explosionResistance 100F} — virtually indestructible by explosions.</li>
 * </ul>
 *
 * <p>The shared {@code Properties} instance is critical: source and flowing MUST
 * reference each other via {@code still}/{@code flowing} suppliers so that
 * {@link FlowingFluid#isSame} works (otherwise the engine treats flowing edges as
 * a different fluid and they instantly evaporate).
 */
public final class ModFluids {
    private ModFluids() {}

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, DarkFantasy.MOD_ID);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> ELIXIR_SOURCE =
            FLUIDS.register("elixir", () -> new BaseFlowingFluid.Source(elixirProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> ELIXIR_FLOWING =
            FLUIDS.register("flowing_elixir", () -> new BaseFlowingFluid.Flowing(elixirProperties()));

    private static BaseFlowingFluid.Properties elixirProperties() {
        return new BaseFlowingFluid.Properties(
                ModFluidTypes.ELIXIR_TYPE,
                ELIXIR_SOURCE,
                ELIXIR_FLOWING)
                .bucket(ModItems.ELIXIR_BUCKET)
                .block(ModBlocks.ELIXIR)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(100.0F);
    }

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
