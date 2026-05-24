package net.steve.darkfantasy.item.custom;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Right-click fires a {@link LargeFireball} in the player's look direction. Uses the same
 * projectile a Ghast (or our Wizard) shoots — explodes on impact with power 1 and obeys
 * the {@code mobGriefing} gamerule, so block damage can be turned off server-side without
 * changing this item.
 */
public class FireballStaffItem extends Item {
    private static final int COOLDOWN_TICKS = 60;
    private static final int EXPLOSION_POWER = 1;
    private static final int DURABILITY_COST = 1;
    /** How far in front of the player to spawn the fireball — keeps it from colliding with the shooter. */
    private static final double SPAWN_OFFSET = 1.0;

    public FireballStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!level.isClientSide()) {
            Vec3 lookVec = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(SPAWN_OFFSET));

            // Constructor: (level, owner, motionDirection, explosionPower).
            // The direction vector is normalized internally.
            LargeFireball fireball = new LargeFireball(level, player, lookVec, EXPLOSION_POWER);
            fireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            level.addFreshEntity(fireball);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(DURABILITY_COST, player, hand);

        return InteractionResult.SUCCESS;
    }
}
