package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge {@link FluidType} registry. The FluidType holds the "behavioral &
 * sensory" properties of a fluid (motion scale, drowning, light, density,
 * viscosity, rarity, sounds) and is shared between the source and flowing
 * {@link net.minecraft.world.level.material.Fluid} instances in {@link ModFluids}.
 *
 * <p>The visual side (still/flow/overlay textures, tint) lives in
 * {@link net.steve.darkfantasy.client.ElixirClientExtensions}, registered against
 * the FluidType at client setup.
 */
public final class ModFluidTypes {
    private ModFluidTypes() {}

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, DarkFantasy.MOD_ID);

    /**
     * Elixir — lava-like flow, gives Levitation on contact. NOT extinguishing (it's
     * not water), NOT convertible to infinite source (matches user spec: bucket
     * pickup OK, but two adjacent sources won't fill the gap between them).
     *
     * <ul>
     *   <li>{@code motionScale 0.007}: lava's value — entities drift slowly within.</li>
     *   <li>{@code canDrown false}: thematically inappropriate; this is an elixir.</li>
     *   <li>{@code fallDistanceModifier 0F}: total fall absorption, like landing in a pool.</li>
     *   <li>{@code viscosity 6000} + {@code density 3000}: thick + heavy. Affects
     *       fog distance, pathing weight, and item entity buoyancy.</li>
     *   <li>{@code rarity RARE}: shows the bucket name in cyan in tooltips.</li>
     * </ul>
     */
    public static final DeferredHolder<FluidType, FluidType> ELIXIR_TYPE =
            FLUID_TYPES.register("elixir", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type." + DarkFantasy.MOD_ID + ".elixir")
                    .motionScale(0.007)
                    .canSwim(true)
                    .canDrown(false)
                    .fallDistanceModifier(0.0F)
                    .canExtinguish(false)
                    .canConvertToSource(false)
                    .supportsBoating(false)
                    .density(3000)
                    .viscosity(6000)
                    .temperature(300)
                    .lightLevel(4)
                    .rarity(Rarity.RARE)));

    public static ResourceKey<FluidType> elixirKey() {
        return ELIXIR_TYPE.getKey();
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
