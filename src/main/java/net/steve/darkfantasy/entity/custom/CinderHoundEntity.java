package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Cinder Hound — the Cinderbark Forest's native pack predator: an ember-eyed wolf whose
 * bite sets its prey alight. Fire-immune (registered on the entity type). Wild hounds
 * behave like untamed wolves; they can't be won over with bones — only a raw
 * {@link ModItems#EMBERSTONE emberstone} placates the furnace in their chest (same 1-in-3
 * taming odds as vanilla wolves). Tamed hounds breed and heal with emberstone too.
 * Drops {@link ModItems#CINDER_FANG} for the alchemy stand.
 */
public class CinderHoundEntity extends Wolf {

    public CinderHoundEntity(EntityType<? extends CinderHoundEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        // emberstone breeds/heals; ordinary wolf food means nothing to a furnace
        return stack.is(ModItems.EMBERSTONE.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame()) {
            if (stack.is(ModItems.EMBERSTONE.get())) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    this.tryToTame(player);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(Items.BONE)) {
                return InteractionResult.PASS;   // bones don't impress a fire
            }
        }
        return super.mobInteract(player, hand);
    }

    /** Vanilla wolf taming odds (1 in 3), with ember feedback instead of hearts-only. */
    private void tryToTame(Player player) {
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.level().broadcastEntityEvent(this, (byte) 7);   // hearts
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6);   // smoke
        }
        this.playSound(SoundEvents.BLAZE_SHOOT, 0.4F, 1.6F);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            target.igniteForSeconds(4.0F);       // the bite carries the furnace with it
            level.sendParticles(ParticleTypes.SMALL_FLAME,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    8, 0.2, 0.2, 0.2, 0.02);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(6) == 0) {
            this.level().addParticle(ParticleTypes.SMALL_FLAME,
                    this.getRandomX(0.6), this.getY() + 0.4 + this.random.nextDouble() * 0.4,
                    this.getRandomZ(0.6), 0.0, 0.02, 0.0);
        }
    }

    @Override
    public CinderHoundEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return net.steve.darkfantasy.init.ModEntities.CINDER_HOUND.get().create(level,
                net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }
}
