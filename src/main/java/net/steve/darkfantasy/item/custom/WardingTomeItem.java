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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Spell book — the defensive incantation the staff line lacks. A self-cast ward that stacks
 * Resistance II + Absorption III + Fire Resistance for {@link #DURATION} ticks. The
 * {@link #COOLDOWN_TICKS cooldown} is deliberately longer than the buff so there's an
 * exposed gap between casts — it's an "oh no" panic button, not a permanent shell.
 */
public class WardingTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 900;  // 45 s — outlasts the buff, leaving a gap
    private static final int DURATION = 600;        // 30 s of protection

    public WardingTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.5F);

        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURATION, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION, 2));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0));
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        40, 0.5, 1.0, 0.5, 0.6);
            }
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.warding_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
