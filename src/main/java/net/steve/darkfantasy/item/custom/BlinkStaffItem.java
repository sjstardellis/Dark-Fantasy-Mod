package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Wisp-pearl-core staff: short-range blink. Raytraces up to {@link #RANGE} blocks along
 * the look vector and teleports the player there via {@link Player#randomTeleport}
 * (which validates a safe landing spot, so you can't blink into a wall). No fall-damage
 * shenanigans, no hunger cost — the price is durability + cooldown.
 */
public class BlinkStaffItem extends Item {
    private static final int COOLDOWN_TICKS = 60;
    private static final double RANGE = 12.0;

    public BlinkStaffItem(Properties properties) {
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

            // Land slightly short of the impact point (or at full range on a miss).
            Vec3 target = hit.getType() == HitResult.Type.MISS
                    ? reach
                    : hit.getLocation().subtract(look.scale(0.5));

            Vec3 from = player.position();
            if (player.randomTeleport(target.x, target.y, target.z, true)) {
                server.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z,
                        24, 0.3, 0.6, 0.3, 0.05);
                server.sendParticles(ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        24, 0.3, 0.6, 0.3, 0.05);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
                player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
                stack.hurtAndBreak(1, player, hand);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.blink_staff").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
