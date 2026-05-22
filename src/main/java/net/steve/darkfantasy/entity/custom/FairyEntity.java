package net.steve.darkfantasy.entity.custom;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Hostile flying pixie that spawns in groups in the Twilight Forest. Light melee charge
 * attacker — fragile (6 HP) but irritating in numbers. Behaviour is modelled on
 * {@link net.minecraft.world.entity.monster.Vex}: free-flying with no physics, charge at
 * the target, drift randomly when nothing's in range. The Vex's raid/owner/limited-life
 * machinery is dropped since fairies aren't summoned.
 */
public class FairyEntity extends Monster {
    /** Same flap cadence as the Vex/Allay (~5 ticks per wing flap). */
    private static final int TICKS_PER_FLAP = Mth.ceil((float) (Math.PI * 5.0 / 4.0));

    public FairyEntity(EntityType<? extends FairyEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FairyMoveControl(this);
        this.xpReward = 2;
    }

    @Override
    public boolean isFlapping() {
        return this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    protected boolean isAffectedByBlocks() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new FairyChargeAttackGoal());
        this.goalSelector.addGoal(8, new FairyRandomMoveGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    // ---- AI goals ----------------------------------------------------------

    /** Charge at the current target, melee on contact. Adapted from VexChargeAttackGoal. */
    private class FairyChargeAttackGoal extends Goal {
        FairyChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = FairyEntity.this.getTarget();
            return target != null && target.isAlive() && !FairyEntity.this.getMoveControl().hasWanted()
                    && FairyEntity.this.random.nextInt(reducedTickDelay(7)) == 0
                    && FairyEntity.this.distanceToSqr(target) > 4.0;
        }

        @Override
        public boolean canContinueToUse() {
            return FairyEntity.this.getMoveControl().hasWanted()
                    && FairyEntity.this.getTarget() != null
                    && FairyEntity.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity target = FairyEntity.this.getTarget();
            if (target != null) {
                Vec3 eyePos = target.getEyePosition();
                FairyEntity.this.moveControl.setWantedPosition(eyePos.x, eyePos.y, eyePos.z, 1.0);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = FairyEntity.this.getTarget();
            if (target == null) return;
            if (FairyEntity.this.getBoundingBox().intersects(target.getBoundingBox())) {
                FairyEntity.this.doHurtTarget(getServerLevel(FairyEntity.this.level()), target);
            } else if (FairyEntity.this.distanceToSqr(target) < 9.0) {
                Vec3 eyePos = target.getEyePosition();
                FairyEntity.this.moveControl.setWantedPosition(eyePos.x, eyePos.y, eyePos.z, 1.0);
            }
        }
    }

    /** Free-flight move control with no pathfinding — direct steering toward wanted pos. */
    private class FairyMoveControl extends MoveControl {
        FairyMoveControl(FairyEntity fairy) {
            super(fairy);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) return;
            Vec3 delta = new Vec3(this.wantedX - FairyEntity.this.getX(),
                    this.wantedY - FairyEntity.this.getY(),
                    this.wantedZ - FairyEntity.this.getZ());
            double len = delta.length();
            if (len < FairyEntity.this.getBoundingBox().getSize()) {
                this.operation = MoveControl.Operation.WAIT;
                FairyEntity.this.setDeltaMovement(FairyEntity.this.getDeltaMovement().scale(0.5));
                return;
            }
            FairyEntity.this.setDeltaMovement(
                    FairyEntity.this.getDeltaMovement().add(delta.scale(this.speedModifier * 0.05 / len)));
            if (FairyEntity.this.getTarget() == null) {
                Vec3 movement = FairyEntity.this.getDeltaMovement();
                FairyEntity.this.setYRot(-((float) Mth.atan2(movement.x, movement.z)) * (180.0F / (float) Math.PI));
            } else {
                double tx = FairyEntity.this.getTarget().getX() - FairyEntity.this.getX();
                double tz = FairyEntity.this.getTarget().getZ() - FairyEntity.this.getZ();
                FairyEntity.this.setYRot(-((float) Mth.atan2(tx, tz)) * (180.0F / (float) Math.PI));
            }
            FairyEntity.this.yBodyRot = FairyEntity.this.getYRot();
        }
    }

    /** Sub-target random drift around the fairy's current position. */
    private class FairyRandomMoveGoal extends Goal {
        FairyRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !FairyEntity.this.getMoveControl().hasWanted()
                    && FairyEntity.this.random.nextInt(reducedTickDelay(7)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos origin = FairyEntity.this.blockPosition();
            for (int i = 0; i < 3; i++) {
                BlockPos test = origin.offset(
                        FairyEntity.this.random.nextInt(15) - 7,
                        FairyEntity.this.random.nextInt(11) - 5,
                        FairyEntity.this.random.nextInt(15) - 7);
                if (FairyEntity.this.level().isEmptyBlock(test)) {
                    FairyEntity.this.moveControl.setWantedPosition(
                            test.getX() + 0.5, test.getY() + 0.5, test.getZ() + 0.5, 0.25);
                    if (FairyEntity.this.getTarget() == null) {
                        FairyEntity.this.getLookControl().setLookAt(
                                test.getX() + 0.5, test.getY() + 0.5, test.getZ() + 0.5, 180.0F, 20.0F);
                    }
                    return;
                }
            }
        }
    }
}
