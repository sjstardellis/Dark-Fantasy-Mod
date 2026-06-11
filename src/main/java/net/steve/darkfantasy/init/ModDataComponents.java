package net.steve.darkfantasy.init;

import com.mojang.serialization.Codec;
import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom item data components. {@link #GNOME_COUNT} rides on the gnome-burrow item so a
 * picked-up / mined burrow remembers how many gnomes were inside (mirrors how a beehive
 * keeps its bees via the {@code minecraft:bees} component).
 */
public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DarkFantasy.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GNOME_COUNT =
            COMPONENTS.registerComponentType("gnome_count",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT));

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
