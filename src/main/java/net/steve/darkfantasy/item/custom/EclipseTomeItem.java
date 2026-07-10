package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Spell book — the Eclipse King's parting gift, dropped only by him. A shadow-dash:
 * the caster becomes a streak of umbra, reappearing up to {@link #RANGE} blocks along the
 * look vector (same safe-landing raytrace as the blink staff). Everything living the
 * shadow passes through is left staring into the dark — Blindness and Darkness for
 * {@link #BLIND_TICKS} ticks — and the caster slips out of sight for a heartbeat
 * afterwards. No damage; it's an engage/escape tool that turns a melee scrum inside-out.
 */
public class EclipseTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 100;
    private static final double RANGE = 9.0;
    /** How far off the dash line a victim can be and still get eclipsed. */
    private static final double BLIND_REACH = 1.6;
    private static final int BLIND_TICKS = 100;      // 5 s
    private static final int VANISH_TICKS = 30;      // 1.5 s of afterimage invisibility

    public EclipseTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel server) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 reach = eye.add(look.scale(RANGE));
            BlockHitResult hit = level.clip(new ClipContext(
                    eye, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 target = hit.getType() == HitResult.Type.MISS
                    ? reach
                    : hit.getLocation().subtract(look.scale(0.5));

            Vec3 from = player.position();
            if (player.randomTeleport(target.x, target.y, target.z, true)) {
                Vec3 to = player.position();

                // eclipse everything the shadow streaked through
                AABB corridor = new AABB(from, to).inflate(BLIND_REACH);
                for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, corridor)) {
                    if (victim == player || victim instanceof Player || !victim.isAlive()) continue;
                    Vec3 mid = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
                    if (distToSegment(mid, from.add(0.0, 1.0, 0.0), to.add(0.0, 1.0, 0.0)) > BLIND_REACH) continue;

                    victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS));
                    victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, BLIND_TICKS));
                    server.sendParticles(ParticleTypes.SQUID_INK,
                            mid.x, mid.y, mid.z, 10, 0.25, 0.4, 0.25, 0.02);
                }

                // an ink-and-embers trail along the dash line
                double len = from.distanceTo(to);
                Vec3 step = len > 1.0E-4 ? to.subtract(from).scale(1.0 / Math.ceil(len * 2.0)) : Vec3.ZERO;
                Vec3 p = from;
                for (int i = 0; i <= Math.ceil(len * 2.0); i++, p = p.add(step)) {
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 1.0, p.z, 2, 0.15, 0.3, 0.15, 0.0);
                    if (i % 3 == 0) {
                        server.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 1.2, p.z, 1, 0.1, 0.2, 0.1, 0.0);
                    }
                }

                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, VANISH_TICKS));
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 0.55F);
                player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
                stack.hurtAndBreak(1, player, hand);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Distance from point {@code p} to the segment {@code a→b}. */
    private static double distToSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double abLenSq = ab.lengthSqr();
        if (abLenSq < 1.0E-8) return p.distanceTo(a);
        double t = Math.clamp(p.subtract(a).dot(ab) / abLenSq, 0.0, 1.0);
        return p.distanceTo(a.add(ab.scale(t)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.eclipse_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
