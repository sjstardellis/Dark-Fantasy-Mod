package net.steve.darkfantasy.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;

/**
 * Bog Hag — the crone of the Ghostwillow Marsh, haunting the reeds and the drowned
 * church. Rides the full vanilla witch brain (splash potions offense, drinking cures
 * and invisibility under pressure) with a marsh-rotten look, tougher constitution, and
 * a drifting cloud of spores. Drops {@link net.steve.darkfantasy.item.ModItems#HAG_ICHOR}
 * — the reagent that makes the Witchbane Elixir possible.
 */
public class BogHagEntity extends Witch {

    public BogHagEntity(EntityType<? extends BogHagEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Witch.createAttributes()
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(5) == 0) {
            this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                    this.getRandomX(0.8), this.getY() + this.random.nextDouble() * 1.8,
                    this.getRandomZ(0.8), 0.0, 0.0, 0.0);
        }
    }
}
