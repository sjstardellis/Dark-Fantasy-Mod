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
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Spell book — the dark capstone of the offensive line. Launches a vanilla
 * {@link WitherSkull} in the look direction: it flies straight, explodes on impact, and
 * leaves the Wither effect on whatever it catches. The caster is set as owner so the skull
 * never hits them.
 *
 * <p>It's a real wither skull, so its blast obeys the {@code mobGriefing} gamerule — with
 * griefing on it scorches terrain like the boss's. The cost is a hefty cooldown plus low
 * durability ({@value #COOLDOWN_TICKS}-tick recharge, ~24 casts), keeping it a finisher
 * rather than a spammable cannon.
 */
public class WitherSkullTomeItem extends Item {
    private static final int COOLDOWN_TICKS = 60;
    /** Spawn the skull a block ahead of the eyes so it clears the caster's hitbox. */
    private static final double SPAWN_OFFSET = 1.0;

    public WitherSkullTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);

        if (level instanceof ServerLevel server) {
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(look.scale(SPAWN_OFFSET));

            // WitherSkull(Level, owner, direction) seeds the skull's acceleration from the
            // (normalized) look vector — the same path the Wither boss uses to fire heads.
            WitherSkull skull = new WitherSkull(level, player, look.normalize());
            skull.setOwner(player);
            skull.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            level.addFreshEntity(skull);

            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    spawnPos.x, spawnPos.y, spawnPos.z, 8, 0.1, 0.1, 0.1, 0.01);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.darkfantasy.wither_skull_tome").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
