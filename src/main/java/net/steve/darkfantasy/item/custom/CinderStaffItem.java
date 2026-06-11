package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

/**
 * Emberstone-core staff: a fire nova centered on the caster. Damages and ignites every
 * living thing within {@link #RADIUS} blocks (except the caster), with a flame ring for
 * readability. Complement to the Fireball Staff: that one is single-target at range,
 * this one is get-off-me AoE.
 */
public class CinderStaffItem extends Item {
    private static final int COOLDOWN_TICKS = 80;
    private static final double RADIUS = 4.0;
    private static final float DAMAGE = 5.0F;
    private static final int IGNITE_TICKS = 120;

    public CinderStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);

        if (level instanceof ServerLevel server) {
            AABB area = player.getBoundingBox().inflate(RADIUS);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
                if (victim == player || !victim.isAlive()) continue;
                victim.hurt(server.damageSources().playerAttack(player), DAMAGE);
                victim.igniteForTicks(IGNITE_TICKS);
            }
            // Flame ring at waist height for a readable blast radius.
            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2 * i) / 24;
                double px = player.getX() + Math.cos(angle) * (RADIUS * 0.7);
                double pz = player.getZ() + Math.sin(angle) * (RADIUS * 0.7);
                server.sendParticles(ParticleTypes.FLAME, px, player.getY() + 1.0, pz,
                        3, 0.1, 0.2, 0.1, 0.02);
            }
            server.sendParticles(ParticleTypes.LAVA,
                    player.getX(), player.getY() + 0.5, player.getZ(), 8, 0.5, 0.3, 0.5, 0.0);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.cinder_staff").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
