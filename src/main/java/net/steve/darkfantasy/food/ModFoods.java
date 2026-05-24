package net.steve.darkfantasy.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ModFoods {
    public static final FoodProperties NIGHTSHADE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build();

    public static final Consumable NIGHTSHADE_CONSUMABLE = Consumables.defaultFood()
            .consumeSeconds(2.1f).onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.GLOWING, 200), 0.10f)).build();

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
}
