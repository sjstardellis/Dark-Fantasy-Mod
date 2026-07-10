package net.steve.darkfantasy.item.custom;

import net.steve.darkfantasy.entity.custom.ThrowingDaggerProjectile;
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

import java.util.function.Consumer;

/**
 * Shadowsteel throwing dagger — a stackable sidearm for the stealth tier. Use to hurl a
 * {@link ThrowingDaggerProjectile}: fast, flat, and doubled-up when thrown from a sneak
 * (see the projectile for the numbers). Daggers stick into terrain and can usually be
 * recovered; flesh sometimes keeps them. A short cooldown stops machine-gunning a stack.
 */
public class ThrowingDaggerItem extends Item {
    private static final int COOLDOWN_TICKS = 12;
    private static final float THROW_POWER = 1.6F;

    public ThrowingDaggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.blockPosition(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.8F);
        if (!level.isClientSide()) {
            ThrowingDaggerProjectile dagger =
                    new ThrowingDaggerProjectile(ModEntities.THROWING_DAGGER.get(), level);
            dagger.setOwner(player);
            dagger.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());
            dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, THROW_POWER, 1.0F);
            level.addFreshEntity(dagger);
        }
        stack.consume(1, player);
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.throwing_dagger").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
