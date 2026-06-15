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
 * Spell book — time-stop. Every non-player living thing within {@link #RADIUS} blocks is
 * frozen for {@link #FREEZE_TICKS} ticks: motion zeroed, then Slowness at a crushing
 * amplifier (no walking) and Weakness (no bite). It does no damage — it's a hard-CC reset
 * that buys you a few seconds to reposition, heal, or line up a follow-up.
 */
public class StasisTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 160;
    private static final double RADIUS = 6.0;
    private static final int FREEZE_TICKS = 80;      // 4 s
    /** Slowness amplifier high enough that movement speed clamps to ~zero (a true root). */
    private static final int ROOT_AMPLIFIER = 250;

    public StasisTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9F, 0.6F);

        if (level instanceof ServerLevel server) {
            AABB area = player.getBoundingBox().inflate(RADIUS);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
                if (victim == player || victim instanceof Player || !victim.isAlive()) continue;

                victim.setDeltaMovement(Vec3.ZERO);
                victim.hurtMarked = true;
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FREEZE_TICKS, ROOT_AMPLIFIER));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, FREEZE_TICKS, 4));

                server.sendParticles(ParticleTypes.SNOWFLAKE,
                        victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                        14, 0.3, 0.5, 0.3, 0.0);
            }
            server.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.stasis_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
