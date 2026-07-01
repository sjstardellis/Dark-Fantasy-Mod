package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Full-set armor bonuses. Each metal's four pieces, worn together, grant a themed perk
 * package; the bonuses are re-applied as short hidden status effects every tick while the
 * set is equipped (and lapse shortly after a piece is removed). A matching one-line
 * description is appended to each armor piece's tooltip so the bonus is discoverable.
 *
 * <ul>
 *   <li><b>Moonsilver (lunar)</b> — Night Vision always; Strength after dark.</li>
 *   <li><b>Shadowsteel (stealth)</b> — Invisibility + Speed while sneaking.</li>
 *   <li><b>Dawnmetal (solar)</b> — Fire Resistance always; Absorption in daylight.</li>
 *   <li><b>Eclipsium (eclipse)</b> — the fused package: Night Vision, Fire Resistance,
 *       and Strength, with no day/night gating.</li>
 * </ul>
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public final class ArmorSetBonusHandler {
    private ArmorSetBonusHandler() {}

    /** Long enough that re-applying each tick never trips Night Vision's end-of-effect flicker (<10s). */
    private static final int NIGHT_VISION_TICKS = 300;
    /** Short, so combat/utility buffs fade ~3s after a set piece is removed. */
    private static final int SHORT_TICKS = 60;

    // ---- Tick: apply the bonuses --------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (!(p.level() instanceof ServerLevel currentLevel)) return;

        // Day/night is judged by the OVERWORLD clock and applies in whatever dimension the
        // player is in. This keeps the lunar/solar timing consistent everywhere — including
        // fixed-time dimensions (Twilight Forest, Nether, End) whose own isDark/isBright
        // always report false. The overworld isn't fixed-time, so its checks work.
        ServerLevel overworld = currentLevel.getServer().overworld();
        boolean night = overworld.isDarkOutside();
        boolean day = overworld.isBrightOutside();

        if (wears(p, ModItems.MOONSILVER_HELMET.get(), ModItems.MOONSILVER_CHESTPLATE.get(),
                ModItems.MOONSILVER_LEGGINGS.get(), ModItems.MOONSILVER_BOOTS.get())) {
            apply(p, MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0);
            if (night) apply(p, MobEffects.STRENGTH, SHORT_TICKS, 0);

        } else if (wears(p, ModItems.SHADOWSTEEL_HELMET.get(), ModItems.SHADOWSTEEL_CHESTPLATE.get(),
                ModItems.SHADOWSTEEL_LEGGINGS.get(), ModItems.SHADOWSTEEL_BOOTS.get())) {
            if (p.isCrouching()) {
                apply(p, MobEffects.INVISIBILITY, SHORT_TICKS, 0);
                apply(p, MobEffects.SPEED, SHORT_TICKS, 0);
            }

        } else if (wears(p, ModItems.DAWNMETAL_HELMET.get(), ModItems.DAWNMETAL_CHESTPLATE.get(),
                ModItems.DAWNMETAL_LEGGINGS.get(), ModItems.DAWNMETAL_BOOTS.get())) {
            apply(p, MobEffects.FIRE_RESISTANCE, SHORT_TICKS, 0);
            if (day) apply(p, MobEffects.ABSORPTION, SHORT_TICKS, 0);

        } else if (wears(p, ModItems.ECLIPSIUM_HELMET.get(), ModItems.ECLIPSIUM_CHESTPLATE.get(),
                ModItems.ECLIPSIUM_LEGGINGS.get(), ModItems.ECLIPSIUM_BOOTS.get())) {
            apply(p, MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0);
            apply(p, MobEffects.FIRE_RESISTANCE, SHORT_TICKS, 0);
            apply(p, MobEffects.STRENGTH, SHORT_TICKS, 0);
        }

        // Standalone relic — the Eclipse King's crown is a guaranteed boss drop and works in any
        // head slot, independent of (and stacking with) the four-piece sets above. It carries the
        // late monarch's gifts: sight in the dark, his might, and his unnatural vitality.
        if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ECLIPSE_CROWN.get())) {
            apply(p, MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0);
            apply(p, MobEffects.STRENGTH, SHORT_TICKS, 0);
            apply(p, MobEffects.REGENERATION, SHORT_TICKS, 0);
        }
    }

    private static boolean wears(Player p, Item helmet, Item chest, Item legs, Item boots) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(helmet)
                && p.getItemBySlot(EquipmentSlot.CHEST).is(chest)
                && p.getItemBySlot(EquipmentSlot.LEGS).is(legs)
                && p.getItemBySlot(EquipmentSlot.FEET).is(boots);
    }

    /** Re-apply a hidden (no particles, icon shown) effect, refreshing its duration. */
    private static void apply(Player p, Holder<MobEffect> effect, int duration, int amplifier) {
        p.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, true));
    }

    // ---- Tooltip: describe the bonus on each piece --------------------------

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (isPiece(item, ModItems.MOONSILVER_HELMET.get(), ModItems.MOONSILVER_CHESTPLATE.get(),
                ModItems.MOONSILVER_LEGGINGS.get(), ModItems.MOONSILVER_BOOTS.get())) {
            event.getToolTip().add(line("tooltip.darkfantasy.set.moonsilver", ChatFormatting.AQUA));
        } else if (isPiece(item, ModItems.SHADOWSTEEL_HELMET.get(), ModItems.SHADOWSTEEL_CHESTPLATE.get(),
                ModItems.SHADOWSTEEL_LEGGINGS.get(), ModItems.SHADOWSTEEL_BOOTS.get())) {
            event.getToolTip().add(line("tooltip.darkfantasy.set.shadowsteel", ChatFormatting.DARK_PURPLE));
        } else if (isPiece(item, ModItems.DAWNMETAL_HELMET.get(), ModItems.DAWNMETAL_CHESTPLATE.get(),
                ModItems.DAWNMETAL_LEGGINGS.get(), ModItems.DAWNMETAL_BOOTS.get())) {
            event.getToolTip().add(line("tooltip.darkfantasy.set.dawnmetal", ChatFormatting.GOLD));
        } else if (isPiece(item, ModItems.ECLIPSIUM_HELMET.get(), ModItems.ECLIPSIUM_CHESTPLATE.get(),
                ModItems.ECLIPSIUM_LEGGINGS.get(), ModItems.ECLIPSIUM_BOOTS.get())) {
            event.getToolTip().add(line("tooltip.darkfantasy.set.eclipsium", ChatFormatting.LIGHT_PURPLE));
        } else if (item == ModItems.ECLIPSE_CROWN.get()) {
            event.getToolTip().add(line("tooltip.darkfantasy.eclipse_crown", ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static boolean isPiece(Item item, Item helmet, Item chest, Item legs, Item boots) {
        return item == helmet || item == chest || item == legs || item == boots;
    }

    private static Component line(String key, ChatFormatting color) {
        return Component.translatable(key).withStyle(color);
    }
}
