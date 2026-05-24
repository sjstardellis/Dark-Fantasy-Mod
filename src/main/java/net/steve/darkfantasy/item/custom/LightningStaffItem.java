package net.steve.darkfantasy.item.custom;

import net.steve.darkfantasy.entity.custom.LightningBoltProjectile;
import net.steve.darkfantasy.init.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Right-click fires an invisible {@link LightningBoltProjectile} from the player's eye in
 * their look direction. The projectile streams an electric-spark trail, and on impact a
 * real lightning bolt strikes at the hit point.
 */
public class LightningStaffItem extends Item {
    /** Ticks between shots — prevents the player chaining bolts. */
    private static final int COOLDOWN_TICKS = 40;
    /** Initial projectile velocity. */
    private static final float PROJECTILE_VELOCITY = 2.5F;
    /** Durability cost per shot. */
    private static final int DURABILITY_COST = 1;

    public LightningStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Sound is played on both sides so the shooter hears it immediately without lag.
        level.playSound(player, player.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4F, 1.8F);

        if (!level.isClientSide()) {
            LightningBoltProjectile bolt = new LightningBoltProjectile(
                    ModEntities.LIGHTNING_PROJECTILE.get(), level);
            bolt.setOwner(player);
            bolt.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            bolt.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, PROJECTILE_VELOCITY, 0.0F);
            level.addFreshEntity(bolt);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(DURABILITY_COST, player, hand);

        return InteractionResult.SUCCESS;
    }
}
