package net.steve.darkfantasy.item.custom;

import net.steve.darkfantasy.entity.custom.FrostBoltProjectile;
import net.steve.darkfantasy.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Mercuryglass-core staff: fires a {@link FrostBoltProjectile} that slows its target and
 * builds freezing ticks. Mirrors the existing staff pattern (right-click, cooldown,
 * durability).
 */
public class FrostStaffItem extends Item {
    private static final int COOLDOWN_TICKS = 30;
    private static final float VELOCITY = 1.8F;
    private static final float INACCURACY = 0.5F;
    private static final double SPAWN_OFFSET = 1.0;

    public FrostStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.4F);

        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(look.scale(SPAWN_OFFSET));

            FrostBoltProjectile bolt = new FrostBoltProjectile(ModEntities.FROST_BOLT.get(), level);
            bolt.setOwner(player);
            bolt.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            bolt.shoot(look.x, look.y, look.z, VELOCITY, INACCURACY);
            level.addFreshEntity(bolt);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.frost_staff").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
