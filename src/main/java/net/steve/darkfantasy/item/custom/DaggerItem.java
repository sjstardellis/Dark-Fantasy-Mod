package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Fast, low-base-damage paired daggers built for assassins. They're quick to swing
 * (set in the sword properties at registration), and land a {@link #BACKSTAB_BONUS}
 * follow-up when the attacker is either sneaking or striking from the target's rear arc
 * — rewarding positioning over raw stats. The bonus uses a second damage instance with
 * the target's i-frames cleared so it always applies.
 */
public class DaggerItem extends Item {
    private static final float BACKSTAB_BONUS = 5.0F;
    /** dot(targetFacing, dirToAttacker) below this ⇒ attacker is behind the target (~rear 100°). */
    private static final double REAR_ARC_DOT = -0.2;

    public DaggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.shadowsteel_daggers").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker instanceof Player player)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;

        boolean qualifies = player.isCrouching() || isBehind(target, player);
        if (!qualifies) return;

        target.invulnerableTime = 0;
        target.hurt(level.damageSources().playerAttack(player), BACKSTAB_BONUS);
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS, 1.0F, 1.3F);
        level.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                8, 0.2, 0.2, 0.2, 0.1);
    }

    /** True when {@code attacker} is in {@code target}'s rear arc (horizontal plane). */
    private static boolean isBehind(LivingEntity target, LivingEntity attacker) {
        Vec3 facing = flatten(target.getLookAngle());
        Vec3 toAttacker = flatten(attacker.position().subtract(target.position()));
        if (facing.lengthSqr() < 1.0E-4 || toAttacker.lengthSqr() < 1.0E-4) return false;
        return facing.normalize().dot(toAttacker.normalize()) < REAR_ARC_DOT;
    }

    private static Vec3 flatten(Vec3 v) {
        return new Vec3(v.x, 0.0, v.z);
    }
}
