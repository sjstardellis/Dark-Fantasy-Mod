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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Spell book — a singularity. Yanks every non-player living thing within {@link #RADIUS}
 * blocks toward the caster and mires them with Slowness, bunching a scattered pack into one
 * spot. Deals no damage on its own: it's pure crowd control, designed to set up an AoE
 * follow-up (e.g. the {@link CinderStaffItem} nova). Players are never pulled, so it's safe
 * to cast next to allies.
 */
public class MaelstromTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 120;
    private static final double RADIUS = 8.0;
    /** Horizontal pull impulse per cast; tuned so a mob at the edge is dragged ~half the radius. */
    private static final double PULL = 0.9;
    private static final int SLOW_TICKS = 60;       // 3 s of Slowness II
    private static final double MIN_DIST = 0.6;     // mobs already on top of the caster aren't yanked

    public MaelstromTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.7F);

        if (level instanceof ServerLevel server) {
            Vec3 center = player.position();
            AABB area = player.getBoundingBox().inflate(RADIUS);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
                if (victim == player || victim instanceof Player || !victim.isAlive()) continue;

                Vec3 toCenter = center.subtract(victim.position());
                if (toCenter.length() < MIN_DIST) continue;

                Vec3 pull = toCenter.normalize().scale(PULL);
                // Damp existing momentum, then add the inward pull with a small upward kick so
                // mobs unstick from the ground instead of grinding into terrain.
                victim.setDeltaMovement(victim.getDeltaMovement().scale(0.2)
                        .add(pull.x, Math.min(pull.y + 0.25, 0.45), pull.z));
                victim.hurtMarked = true;  // force the server to broadcast the new velocity
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOW_TICKS, 1));
            }

            // Inward-spiralling dust ring so the pull radius is readable.
            for (int i = 0; i < 32; i++) {
                double angle = (Math.PI * 2 * i) / 32;
                double px = center.x + Math.cos(angle) * (RADIUS * 0.6);
                double pz = center.z + Math.sin(angle) * (RADIUS * 0.6);
                server.sendParticles(ParticleTypes.PORTAL, px, center.y + 0.5, pz,
                        2, 0.0, 0.1, 0.0, 0.0);
            }
            server.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    center.x, center.y + 1.0, center.z, 30, 0.3, 0.6, 0.3, 0.4);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.maelstrom_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
