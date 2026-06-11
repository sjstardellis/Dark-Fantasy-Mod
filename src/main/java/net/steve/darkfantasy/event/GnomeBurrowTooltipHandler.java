package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.block.entity.GnomeBurrowBlockEntity;
import net.steve.darkfantasy.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Adds a "Gnomes inside: N / max" line to the gnome-burrow item tooltip, read from the
 * {@link ModDataComponents#GNOME_COUNT} component the burrow carries when picked up/mined.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public final class GnomeBurrowTooltipHandler {
    private GnomeBurrowTooltipHandler() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModBlocks.GNOME_BURROW.get().asItem())) return;

        int count = stack.getOrDefault(ModDataComponents.GNOME_COUNT.get(), 0);
        event.getToolTip().add(Component.translatable(
                        "tooltip.darkfantasy.gnome_burrow.count", count, GnomeBurrowBlockEntity.CAPACITY)
                .withStyle(ChatFormatting.GRAY));
    }
}
