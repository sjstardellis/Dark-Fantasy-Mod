package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

/**
 * Heavy, slow scythe that reaps a wide arc. The primary strike is normal weapon damage
 * (from the sword properties applied at registration); on a successful hit it follows up
 * with a flat {@link #SWEEP_DAMAGE} sweep to every other living entity within
 * {@link #SWEEP_RADIUS} of the struck target — a crowd-clearer that fits the gravewood /
 * reaper aesthetic. Server-side only; the visual sweep + sound are broadcast from there.
 */
public class ScytheItem extends Item {
    private static final double SWEEP_RADIUS = 2.5;
    private static final float SWEEP_DAMAGE = 4.0F;

    public ScytheItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.moonsilver_scythe").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker instanceof Player player)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;

        DamageSource src = level.damageSources().playerAttack(player);
        AABB area = target.getBoundingBox().inflate(SWEEP_RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (victim == player || victim == target || !victim.isAlive()) continue;
            // Fresh targets have no active i-frames, but reset defensively so the sweep
            // always lands even if something hit them this tick.
            victim.invulnerableTime = 0;
            victim.hurt(src, SWEEP_DAMAGE);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.0F, 0.7F);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
    }
}
