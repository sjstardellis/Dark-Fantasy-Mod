package net.steve.darkfantasy.item;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.food.ModFoods;
import net.steve.darkfantasy.item.custom.SoulCompassItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DarkFantasy.MOD_ID);

    public static final DeferredItem<Item> SHADOWSTEEL = ITEMS.registerSimpleItem("shadowsteel");
    public static final DeferredItem<Item> RAW_SHADOWSTEEL = ITEMS.registerSimpleItem("raw_shadowsteel");


    public static final DeferredItem<Item> MOONSILVER = ITEMS.registerSimpleItem("moonsilver");
    public static final DeferredItem<Item> RAW_MOONSILVER = ITEMS.registerSimpleItem("raw_moonsilver");

    public static final DeferredItem<Item> DAWNMETAL = ITEMS.registerSimpleItem("dawnmetal");
    public static final DeferredItem<Item> RAW_DAWNMETAL = ITEMS.registerSimpleItem("raw_dawnmetal");

    public static final DeferredItem<Item> SOUL_COMPASS = ITEMS.registerItem("soul_compass",
            properties -> new SoulCompassItem(properties.durability(64)));

    public static final DeferredItem<Item> NIGHTSHADE = ITEMS.registerItem("nightshade",
            properties -> new Item(properties.food(ModFoods.NIGHTSHADE, ModFoods.NIGHTSHADE_CONSUMABLE)) {
                @Override
                public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
                    builder.accept(Component.translatable("tooltip.darkfantasy.nightshade.tooltip"));
                    super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> HELLFIRE_KINDLING = ITEMS.registerItem("hellfire_kindling",
            properties -> new Item(properties.stacksTo(32)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
