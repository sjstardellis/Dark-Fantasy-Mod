package net.steve.darkfantasy.item.custom;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.tags.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;

import java.util.function.Consumer;

/**
 * A solar reach weapon. Built with a manual attribute set (rather than the standard
 * sword builder) so it can carry an {@link Attributes#ENTITY_INTERACTION_RANGE} bonus on
 * top of attack damage/speed — letting it strike from a lance's distance. On hit it sets
 * the target alight; undead burn far longer and take a {@link #UNDEAD_BONUS} smite hit.
 */
public class SunlanceItem extends Item {
    private static final float UNDEAD_BONUS = 6.0F;
    private static final int IGNITE_TICKS = 3 * 20;
    private static final int UNDEAD_IGNITE_TICKS = 8 * 20;
    private static final Identifier REACH_ID =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "sunlance_reach");

    public SunlanceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.dawnmetal_sunlance").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }

    /**
     * Applies the sunlance's stats to a fresh {@link Item.Properties}: dawnmetal-grade
     * durability/enchantability, a {@link Weapon} component for on-hit durability cost,
     * and the attack-damage / attack-speed / reach attribute trio.
     */
    public static Properties applyProperties(Properties p) {
        return p
                .durability(900)
                .repairable(ModTags.Items.DAWNMETAL_REPAIR)
                .enchantable(12)
                .component(DataComponents.WEAPON, new Weapon(2))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 7.0,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(REACH_ID, 1.5,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build());
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker instanceof Player player)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;

        boolean undead = target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
        target.igniteForTicks(undead ? UNDEAD_IGNITE_TICKS : IGNITE_TICKS);
        if (undead) {
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().playerAttack(player), UNDEAD_BONUS);
        }
        level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 0.7F, 1.4F);
    }
}
