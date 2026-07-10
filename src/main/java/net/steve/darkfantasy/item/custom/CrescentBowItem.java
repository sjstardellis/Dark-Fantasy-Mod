package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Crescent — the moonsilver bow. By day it is merely a fine bow; under the night sky
 * every arrow leaves the string as a bolt of moonlight: {@link #NIGHT_DAMAGE_BONUS}
 * extra base damage, a glowing mark on whatever it strikes (loosed arrows glow, so you
 * can find both your quarry and your spent shafts in the dark), and a trail of pale
 * light. Pairs with the moonsilver set's after-dark identity.
 */
public class CrescentBowItem extends BowItem {
    private static final double NIGHT_DAMAGE_BONUS = 2.0;

    public CrescentBowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float power, float uncertainty, float angle,
                                   @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, index, power, uncertainty, angle, target);
        if (shooter.level().isDarkOutside() && projectile instanceof AbstractArrow arrow) {
            // vanilla arrows start at base 2.0 (enchantments layer on top of this)
            arrow.setBaseDamage(2.0 + NIGHT_DAMAGE_BONUS);
            arrow.setGlowingTag(true);
            if (shooter.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD,
                        arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.crescent_bow").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
