package net.steve.darkfantasy.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Spell book — a personal recall point, the one bit of utility the staffs don't cover (the
 * Blink Staff is line-of-sight only). <b>Sneak-use</b> binds the caster's current block as a
 * waypoint, stored in the stack's {@link CustomData}; a plain <b>use</b> recalls to it from
 * any distance. The waypoint is dimension-locked: recall refuses if you've crossed into
 * another world, so it can't be used as a free cross-dimension shortcut.
 */
public class WayfarerTomeItem extends Item {
    private static final int RECALL_COOLDOWN = 200;
    private static final int BIND_COOLDOWN = 10;

    private static final String TAG_X = "wp_x";
    private static final String TAG_Y = "wp_y";
    private static final String TAG_Z = "wp_z";
    private static final String TAG_DIM = "wp_dim";

    public WayfarerTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // All logic is server-authoritative; report SUCCESS on the client so the hand swings.
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        String dimNow = server.dimension().identifier().toString();

        if (player.isShiftKeyDown()) {
            bindWaypoint(stack, player, server, dimNow);
            return InteractionResult.SUCCESS;
        }
        return recall(stack, player, server, dimNow, hand);
    }

    /** Stamp the caster's current position + dimension into the book's CustomData. */
    private void bindWaypoint(ItemStack stack, Player player, ServerLevel server, String dimNow) {
        BlockPos pos = player.blockPosition();
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putString(TAG_DIM, dimNow);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        player.sendSystemMessage(
                Component.translatable("message.darkfantasy.wayfarer_tome.bound").withStyle(ChatFormatting.AQUA));
        server.playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.2F);
        server.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(), 16, 0.3, 0.6, 0.3, 0.04);
        player.getCooldowns().addCooldown(stack, BIND_COOLDOWN);
    }

    /** Teleport the caster back to the bound waypoint, if one exists in this dimension. */
    private InteractionResult recall(ItemStack stack, Player player, ServerLevel server,
                                     String dimNow, InteractionHand hand) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        String boundDim = tag.getStringOr(TAG_DIM, "");

        if (boundDim.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("message.darkfantasy.wayfarer_tome.unbound").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }
        if (!boundDim.equals(dimNow)) {
            player.sendSystemMessage(
                    Component.translatable("message.darkfantasy.wayfarer_tome.wrong_dimension").withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS;
        }

        double x = tag.getIntOr(TAG_X, 0) + 0.5;
        double y = tag.getIntOr(TAG_Y, 0);
        double z = tag.getIntOr(TAG_Z, 0) + 0.5;
        Vec3 from = player.position();

        // randomTeleport validates a safe landing (won't drop you inside a wall); mirrors BlinkStaff.
        if (player.randomTeleport(x, y, z, true)) {
            server.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z, 24, 0.3, 0.6, 0.3, 0.05);
            server.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.3, 0.6, 0.3, 0.05);
            server.playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
            player.sendSystemMessage(
                    Component.translatable("message.darkfantasy.wayfarer_tome.recalled").withStyle(ChatFormatting.AQUA));
            player.getCooldowns().addCooldown(stack, RECALL_COOLDOWN);
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.wayfarer_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
