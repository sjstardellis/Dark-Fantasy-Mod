package net.steve.darkfantasy.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ModFoods {
    // Beer — drinks like a potion; "alwaysEdible" so the player can drink even at full
    // hunger (it's a buff item more than a meal). Resistance gives the tank buff,
    // Confusion (nausea) is the classic drunk visual.
    public static final FoodProperties BEER = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.2f).alwaysEdible().build();

    public static final Consumable BEER_CONSUMABLE = Consumables.defaultDrink()
            .consumeSeconds(1.6f)
            // 100% chance to apply both effects every drink — fixed combo, no roll.
            // 6000 ticks = 5 minutes at 20 tps.
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.RESISTANCE, 6000, 0), 1.0f))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.NAUSEA, 6000, 0), 1.0f))
            .build();

    // ── Alchemy elixirs ──────────────────────────────────────────────────────
    // All share one FoodProperties (no nutrition, drinkable anytime); each has its
    // own fixed effect combo. Brewed at the alchemy stand from biome gems + reagents.
    public static final FoodProperties ELIXIR = new FoodProperties.Builder()
            .nutrition(0).saturationModifier(0f).alwaysEdible().build();

    /** Grimshard + fairy dust — see in the dark, fade from sight. */
    public static final Consumable MOONLIGHT_ELIXIR = Consumables.defaultDrink()
            .consumeSeconds(1.6f)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0), 1.0f))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0), 1.0f))
            .build();

    /** Storm scale + arcane ash — dragon-hide toughness. */
    public static final Consumable STONESKIN_ELIXIR = Consumables.defaultDrink()
            .consumeSeconds(1.6f)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.RESISTANCE, 2400, 1), 1.0f))
            .build();

    /** Emberstone + arcane ash — fire in the veins. */
    public static final Consumable EMBERBLOOD_ELIXIR = Consumables.defaultDrink()
            .consumeSeconds(1.6f)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0), 1.0f))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.STRENGTH, 2400, 0), 1.0f))
            .build();

    /** Wisp pearl + lytebug dust — light-footed as a marsh wisp. */
    public static final Consumable WISPSTEP_ELIXIR = Consumables.defaultDrink()
            .consumeSeconds(1.6f)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.SPEED, 2400, 1), 1.0f))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 2400, 1), 1.0f))
            .build();
}
