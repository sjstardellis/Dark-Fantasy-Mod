package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Umbral Wraith — a shred of the eclipse given hunger. Haunts the mod's dark biomes at
 * night and the Umbral Obelisks always. Its arsenal:
 *
 * <ul>
 *   <li><b>Umbral bolt</b> — spits a {@link UmbralBoltProjectile} that blinds and
 *       darkens whatever it strikes;</li>
 *   <li><b>Shadow-step</b> — when its prey keeps its distance, the wraith collapses
 *       into ink and reappears at arm's reach behind them (with a heartbeat of
 *       invisibility as it re-forms);</li>
 *   <li><b>Chilling touch</b> — its melee saps speed for a few seconds.</li>
 * </ul>
 *
 * Sunlight sets it smouldering (it is a thing of the dark), and it's tagged undead so
 * Smite and the Sunlance treat it accordingly. Drops Umbra Essence for the alchemy stand.
 */
public class UmbralWraithEntity extends Monster implements RangedAttackMob {
    private static final int BLINK_COOLDOWN_TICKS = 120;
    private static final double BLINK_MIN_RANGE = 5.0;
    private static final int VANISH_TICKS = 20;

    private int blinkCooldown;

    public UmbralWraithEntity(EntityType<? extends UmbralWraithEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 50, 12.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        UmbralBoltProjectile bolt = new UmbralBoltProjectile(ModEntities.UMBRAL_BOLT.get(), this.level());
        bolt.setOwner(this);
        bolt.setPos(this.getX(), this.getEyeY() - 0.2, this.getZ());
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5) - bolt.getY();
        double dz = target.getZ() - this.getZ();
        bolt.shoot(dx, dy, dz, 1.1F, 4.0F);
        this.playSound(SoundEvents.PHANTOM_SWOOP, 0.8F, 0.5F);
        this.level().addFreshEntity(bolt);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getRandomX(0.6), this.getY() + this.random.nextDouble() * 1.6,
                        this.getRandomZ(0.6), 0.0, 0.015, 0.0);
            }
            return;
        }
        // sunlight is anathema (Mob's own sun-burn check is private, so approximate:
        // daylight + open sky overhead sets the wraith smouldering)
        if (this.level().isBrightOutside() && this.level().canSeeSky(this.blockPosition())
                && this.tickCount % 20 == 0) {
            this.igniteForSeconds(4.0F);
        }
        // shadow-step: collapse into ink, re-form behind distant prey
        if (this.blinkCooldown > 0) this.blinkCooldown--;
        LivingEntity target = this.getTarget();
        if (target != null && this.blinkCooldown <= 0
                && this.distanceToSqr(target) > BLINK_MIN_RANGE * BLINK_MIN_RANGE) {
            Vec3 behind = target.position().subtract(target.getLookAngle().scale(1.8));
            if (this.randomTeleport(behind.x, behind.y, behind.z, false)) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.SQUID_INK,
                        this.getX(), this.getY() + 1.0, this.getZ(), 16, 0.3, 0.6, 0.3, 0.02);
                this.level().playSound(null, this.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.4F);
                this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, VANISH_TICKS));
                this.blinkCooldown = BLINK_COOLDOWN_TICKS;
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));   // chilling touch
        }
        return hurt;
    }

    // ---- Sounds: hollow and breathy -----------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }
}
