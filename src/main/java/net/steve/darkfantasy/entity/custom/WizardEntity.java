package net.steve.darkfantasy.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Dark-fantasy wizard — a re-themed spellcasting illager that doesn't join raids. Alternates
 * between two ranged spells:
 *
 * <ul>
 *   <li>A {@link LargeFireball} barrage (modelled on the Ghast attack) — small explosion, sets fires.</li>
 *   <li>A {@link WitherSkull} bolt (modelled on the Wither boss) — applies wither effect on hit.</li>
 * </ul>
 *
 * <p>Behaviour borrows from {@link net.minecraft.world.entity.monster.illager.Illusioner}: cross-armed
 * idle, hands-raised casting pose driven by {@link SpellcasterIllager#getArmPose()}, particle effects
 * around the hands while casting. Strafes back from the target so it favours staying at range.
 */
public class WizardEntity extends SpellcasterIllager {
    /** Cooldown (ticks) between any two spells. Both goals share roughly the same cadence. */
    private static final int SPELL_COOLDOWN_TICKS = 100;
    private static final int SPELL_CAST_TICKS = 30;

    public WizardEntity(EntityType<? extends WizardEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterCastingSpellGoal());
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.6, 1.0)); // keep distance
        this.goalSelector.addGoal(4, new WizardFireballSpellGoal());
        this.goalSelector.addGoal(5, new WizardWitherSkullSpellGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MAX_HEALTH, 24.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.EVOKER_HURT;
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.EVOKER_CELEBRATE;
    }

    /** No-op: the wizard isn't part of raid waves, so there are no buffs to apply. */
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean isCaptain) {
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        // Arms-up while casting, crossed otherwise. Skips Illusioner's bow-pose check
        // since wizards don't carry a weapon.
        if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
    }

    // ---- Spell goals -------------------------------------------------------

    /**
     * Shoots a {@link LargeFireball} (Ghast projectile) toward the current target. Reduced
     * explosion power so the wizard doesn't grief the world like a ghast would.
     */
    private class WizardFireballSpellGoal extends SpellcasterUseSpellGoal {
        @Override
        protected int getCastingTime() {
            return SPELL_CAST_TICKS;
        }

        @Override
        protected int getCastingInterval() {
            return SPELL_COOLDOWN_TICKS;
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = WizardEntity.this.getTarget();
            if (target == null) return;
            Level level = WizardEntity.this.level();
            Vec3 from = WizardEntity.this.position().add(0, WizardEntity.this.getBbHeight() * 0.5, 0);
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 direction = to.subtract(from);
            // Small explosion power (1) — same as a ghast fireball.
            LargeFireball fireball = new LargeFireball(level, WizardEntity.this, direction, 1);
            fireball.setPos(from.x, from.y, from.z);
            level.addFreshEntity(fireball);
        }

        @Override
        protected @Nullable SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_ATTACK;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            // WOLOLO has warm orange particle colour — reads as fire while casting.
            return SpellcasterIllager.IllagerSpell.WOLOLO;
        }
    }

    /**
     * Shoots a {@link WitherSkull} (Wither projectile) toward the current target. Non-dangerous
     * variant so it doesn't shatter blocks on impact.
     */
    private class WizardWitherSkullSpellGoal extends SpellcasterUseSpellGoal {
        @Override
        protected int getCastingTime() {
            return SPELL_CAST_TICKS;
        }

        @Override
        protected int getCastingInterval() {
            return SPELL_COOLDOWN_TICKS;
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = WizardEntity.this.getTarget();
            if (target == null) return;
            Level level = WizardEntity.this.level();
            Vec3 from = WizardEntity.this.position().add(0, WizardEntity.this.getBbHeight() * 0.5, 0);
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 direction = to.subtract(from);
            WitherSkull skull = new WitherSkull(level, WizardEntity.this, direction);
            skull.setOwner(WizardEntity.this);
            skull.setPos(from.x, from.y, from.z);
            level.addFreshEntity(skull);
        }

        @Override
        protected @Nullable SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            // BLINDNESS has dark indigo particle colour — reads as a wither curse mid-cast.
            return SpellcasterIllager.IllagerSpell.BLINDNESS;
        }
    }
}
