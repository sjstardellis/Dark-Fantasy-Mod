package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Dawnmetal Arbalest — the sun-forged crossbow. Every bolt leaves the rail burning
 * ({@link #IGNITE_SECONDS}s of fire) with a touch of extra punch — undead in particular
 * dread it, completing dawnmetal's smite identity alongside the Sunlance. Standard
 * crossbow handling otherwise (charging, enchants, durability).
 */
public class DawnmetalArbalestItem extends CrossbowItem {
    private static final float IGNITE_SECONDS = 5.0F;
    private static final double DAMAGE_BONUS = 1.0;

    public DawnmetalArbalestItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon,
                                          ItemStack ammo, boolean crit) {
        Projectile projectile = super.createProjectile(level, shooter, weapon, ammo, crit);
        if (projectile instanceof AbstractArrow arrow) {
            arrow.igniteForSeconds(IGNITE_SECONDS);
            // vanilla arrows start at base 2.0 (enchantments layer on top of this)
            arrow.setBaseDamage(2.0 + DAMAGE_BONUS);
        }
        return projectile;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.dawnmetal_arbalest").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
