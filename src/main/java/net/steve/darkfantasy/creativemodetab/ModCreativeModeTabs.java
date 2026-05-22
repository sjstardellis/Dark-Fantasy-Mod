package net.steve.darkfantasy.creativemodetab;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DarkFantasy.MOD_ID);

    public static final Supplier<CreativeModeTab> DARKFANTASY_TAB = CREATIVE_MODE_TABS.register("darkfantasy_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SHADOWSTEEL.get()))
                    .title(Component.translatable("creativetab.darkfantasy.darkfantasy_tab"))
                    .withTabsBefore(CreativeModeTabs.INGREDIENTS)
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SHADOWSTEEL);
                        output.accept(ModItems.RAW_SHADOWSTEEL);
                        output.accept(ModItems.ECLIPSIUM);
                        output.accept(ModItems.MOONSILVER);
                        output.accept(ModItems.RAW_MOONSILVER);

                        output.accept(ModItems.DAWNMETAL);
                        output.accept(ModItems.RAW_DAWNMETAL);

                        output.accept(ModItems.SOUL_COMPASS);
                        output.accept(ModItems.NIGHTSHADE);
                        output.accept(ModItems.HELLFIRE_KINDLING);
                        output.accept(ModBlocks.SHADOWSTEEL_ORE);
                        output.accept(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE);

                        output.accept(ModBlocks.DAWNMETAL_ORE);
                        output.accept(ModBlocks.DAWNMETAL_DEEPSLATE_ORE);
                        output.accept(ModBlocks.MOONSILVER_ORE);
                        output.accept(ModBlocks.MOONSILVER_DEEPSLATE_ORE);

                        output.accept(ModBlocks.CURSED_BLOCK);
                        output.accept(ModBlocks.ALCHEMY_STAND);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
