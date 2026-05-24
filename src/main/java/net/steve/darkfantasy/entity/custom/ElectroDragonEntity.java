package net.steve.darkfantasy.entity.custom;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.steve.darkfantasy.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Scaled-down dragon-shaped flying enemy with Phantom-style hunter AI. Renders with the
 * vanilla ender-dragon model (see {@link net.steve.darkfantasy.client.renderer.ElectroDragonRenderer});
 * the model needs {@link DragonFlightHistory} + {@code flapTime} to animate, so we
 * populate those fields each tick the same way vanilla
 * {@link net.minecraft.world.entity.boss.enderdragon.EnderDragon} does.
 *
 * <h2>AI (adapted from {@link net.minecraft.world.entity.monster.Phantom})</h2>
 * <p>Two-phase cycle: {@link AttackPhase#CIRCLE} → {@link AttackPhase#SWOOP} → back to CIRCLE.
 * <ul>
 *   <li><b>Circling</b>: orbits an anchor point above the target's head at a random radius
 *       (5-15 blocks) and altitude (±4 blocks), updating its waypoint as it goes. Looks
 *       like a vulture sizing up prey.</li>
 *   <li><b>Swooping</b>: peels off the orbit and dives directly at the target. At the
 *       moment the swoop starts, fires a {@link LightningBoltProjectile} from its mouth
 *       — the dive is the visual telegraph, the lightning is the actual attack. No melee
 *       damage on contact; the dragon pulls up and resumes circling once it gets close or
 *       collides with terrain.</li>
 *   <li><b>Target acquisition</b>: scans every 60 ticks for players within 64 blocks,
 *       picks the highest-Y candidate (i.e. easier to reach from the air).</li>
 * </ul>
 *
 * <p>All flight orientation is driven by {@link DragonMoveControl}, which lerps yRot at
 * ~4°/tick — slow enough that the EnderDragonModel's flight-history-driven neck and tail
 * track the body without splaying. The look-control is a no-op (head follows body).
 */
public class ElectroDragonEntity extends Mob implements Enemy {
    private static final float SCALE = 0.5F;

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(ElectroDragonEntity.class, EntityDataSerializers.BOOLEAN);

    /** Replicated from vanilla EnderDragon — drives the wing-flap animation. */
    public final DragonFlightHistory flightHistory = new DragonFlightHistory();
    public float flapTime;
    public float oFlapTime;

    /** Current orbit waypoint or swoop target. Updated by goals; read by {@link DragonMoveControl}. */
    private Vec3 moveTargetPoint = Vec3.ZERO;
    /** Centre of the orbit. Set above the target's head when circling. */
    private @Nullable BlockPos anchorPoint;
    /** CIRCLE = orbiting; SWOOP = diving at target. */
    private AttackPhase attackPhase = AttackPhase.CIRCLE;

    public ElectroDragonEntity(EntityType<? extends ElectroDragonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.moveControl = new DragonMoveControl(this);
        this.lookControl = new NoopLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.FOLLOW_RANGE, 96.0)
                .add(Attributes.FLYING_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                // 1.21+ scales rendering + bounding box via this attribute.
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AttackStrategyGoal());
        this.goalSelector.addGoal(2, new SwoopAttackGoal());
        this.goalSelector.addGoal(3, new CircleAroundAnchorGoal());
        // Idle patrol — runs only when there is no target. Without this the dragon
        // would tight-circle its spawn point. Lower priority than combat goals.
        this.goalSelector.addGoal(4, new PatrolGoal());
        this.targetSelector.addGoal(1, new AttackPlayerTargetGoal());
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        // Body always faces movement direction; head follows body. Avoids the visual
        // "head pointing one way, body another" that default BodyRotationControl can do.
        return new DragonBodyRotationControl(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGING, false);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    // ---- Flight, no fall damage --------------------------------------------

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.2F);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    // ---- Sounds ------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    // ---- Damage immunity ---------------------------------------------------

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_LIGHTNING)) return false;
        return super.hurtServer(level, source, damage);
    }

    // ---- Per-tick animation + telegraph particles --------------------------

    @Override
    public void tick() {
        super.tick();

        // Vanilla-dragon-style animation update — slowed from 0.2 to 0.06 base.
        this.oFlapTime = this.flapTime;
        Vec3 movement = this.getDeltaMovement();
        float flapSpeed = 0.06F / ((float) movement.horizontalDistance() * 10.0F + 1.0F);
        // Clamp the vertical-movement contribution so jittery deltaY doesn't pump the
        // flap rate up and down each tick.
        flapSpeed *= (float) Math.pow(2.0, Mth.clamp(movement.y, -0.3, 0.3));
        // Don't wrap flapTime — vanilla EnderDragon lets it grow unbounded. Wrapping it
        // creates a one-frame discontinuity in the oFlapTime→flapTime lerp every cycle,
        // which the model amplifies into visible wing/body flicker. The model only uses
        // flapTime inside sin/cos(flapTime * 2π), which is naturally periodic.
        this.flapTime += flapSpeed;

        // Smooth the flightHistory entries instead of recording the raw position. The
        // server periodically sends absolute position-correction packets to the client
        // (when client interpolation drifts from server-authoritative position). Without
        // smoothing, those snaps land in flightHistory and the EnderDragonModel — which
        // bends the neck/tail based on a 7-tick-delayed sample — visibly contorts for
        // ~7 ticks every time. Lerping at 0.6 absorbs the snap into a gradual transition
        // while still tracking real movement closely.
        DragonFlightHistory.Sample prev = this.flightHistory.get(0);
        double smoothedY = Mth.lerp(0.6, prev.y(), this.getY());
        float smoothedYRot = Mth.rotLerp(0.6F, prev.yRot(), this.getYRot());
        this.flightHistory.record(smoothedY, smoothedYRot);

        // Spark cloud around the mouth while charging a shot (client-only).
        if (this.level().isClientSide() && this.isCharging()) {
            Vec3 mouth = this.mouthPosition();
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        mouth.x + (this.random.nextDouble() - 0.5) * 0.5,
                        mouth.y + (this.random.nextDouble() - 0.5) * 0.3,
                        mouth.z + (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        0.05,
                        (this.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    /**
     * Approximate world-space position of the dragon's snout. Bounding-box dimensions
     * already account for {@link Attributes#SCALE}, so the offset shrinks with the
     * entity. The renderer is patched so the model's visual front aligns with
     * {@link #getLookAngle()}.
     */
    Vec3 mouthPosition() {
        return this.position()
                .add(0, this.getBbHeight() * 0.65, 0)
                .add(this.getLookAngle().scale(this.getBbWidth() * 0.5));
    }

    // ---- Death sequence ----------------------------------------------------

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel serverLevel) {
            final int boltCount = 6;
            final double radius = 4.0;
            for (int i = 0; i < boltCount; i++) {
                double angle = (i / (double) boltCount) * (Math.PI * 2);
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                LightningBolt strike = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
                if (strike != null) {
                    strike.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, this.getY(), z)));
                    serverLevel.addFreshEntity(strike);
                }
            }
        }
        super.die(source);
    }

    // ---- Lightning attack helper -------------------------------------------

    /** Fires a {@link LightningBoltProjectile} from the mouth toward the given target. */
    private void fireLightningAt(LivingEntity target) {
        Level level = this.level();
        Vec3 from = this.mouthPosition();
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 direction = to.subtract(from);

        LightningBoltProjectile bolt = new LightningBoltProjectile(
                ModEntities.LIGHTNING_PROJECTILE.get(), level);
        bolt.setOwner(this);
        bolt.setPos(from.x, from.y, from.z);
        bolt.shoot(direction.x, direction.y, direction.z, 1.6F, 1.5F);
        level.addFreshEntity(bolt);

        this.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 2.0F, 1.0F);
    }

    // ---- AI: phase enum + goals (adapted from Phantom) ---------------------

    private enum AttackPhase {
        CIRCLE,
        SWOOP
    }

    /**
     * Scans every ~60 ticks for the highest-Y player within 64 blocks and sets it as
     * target. Same pattern Phantom uses — it preferentially targets players who are
     * higher up because they're easier to dive on.
     */
    private class AttackPlayerTargetGoal extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(96.0);
        private int nextScanTick = reducedTickDelay(20);

        @Override
        public boolean canUse() {
            if (this.nextScanTick > 0) {
                this.nextScanTick--;
                return false;
            }
            this.nextScanTick = reducedTickDelay(60);
            ServerLevel level = (ServerLevel) ElectroDragonEntity.this.level();
            List<Player> players = level.getNearbyPlayers(
                    this.attackTargeting,
                    ElectroDragonEntity.this,
                    ElectroDragonEntity.this.getBoundingBox().inflate(16.0, 64.0, 16.0));
            if (players.isEmpty()) return false;

            players.sort(Comparator.<Player, Double>comparing(Entity::getY).reversed());
            for (Player p : players) {
                if (this.attackTargeting.test(level, ElectroDragonEntity.this, p)) {
                    ElectroDragonEntity.this.setTarget(p);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = ElectroDragonEntity.this.getTarget();
            return target != null
                    && this.attackTargeting.test(
                            (ServerLevel) ElectroDragonEntity.this.level(), ElectroDragonEntity.this, target);
        }
    }

    /**
     * Phase manager. Sits in CIRCLE for {@link #nextSweepTick} ticks, then flips to SWOOP
     * and tells the dragon to fire its lightning. When the SWOOP goal completes, this
     * goal goes back to counting down for the next dive.
     */
    private class AttackStrategyGoal extends Goal {
        private int nextSweepTick;

        @Override
        public boolean canUse() {
            return ElectroDragonEntity.this.getTarget() != null
                    && ElectroDragonEntity.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            ElectroDragonEntity.this.attackPhase = AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        @Override
        public void stop() {
            if (ElectroDragonEntity.this.anchorPoint != null) {
                // Drift the anchor up so the dragon doesn't camp the player's last position.
                ElectroDragonEntity.this.anchorPoint = ElectroDragonEntity.this.level()
                        .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, ElectroDragonEntity.this.anchorPoint)
                        .above(10 + ElectroDragonEntity.this.random.nextInt(20));
            }
        }

        @Override
        public void tick() {
            if (ElectroDragonEntity.this.attackPhase == AttackPhase.CIRCLE) {
                this.nextSweepTick--;
                if (this.nextSweepTick <= 0) {
                    // Flip into SWOOP. Re-anchor over the target, set a long timer until
                    // the next swoop (160-240 ticks ≈ 8-12 s of circling).
                    ElectroDragonEntity.this.attackPhase = AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    // 4-6 seconds between dives — faster than Phantom's 8-12 since the
                    // dragon's lightning attack happens on swoop start.
                    this.nextSweepTick = this.adjustedTickDelay(
                            (4 + ElectroDragonEntity.this.random.nextInt(3)) * 20);
                    ElectroDragonEntity.this.playSound(
                            SoundEvents.ENDER_DRAGON_FLAP, 4.0F,
                            0.95F + ElectroDragonEntity.this.random.nextFloat() * 0.1F);

                    // Fire the lightning bolt at the moment the dive begins.
                    LivingEntity target = ElectroDragonEntity.this.getTarget();
                    if (target != null && ElectroDragonEntity.this.hasLineOfSight(target)) {
                        ElectroDragonEntity.this.fireLightningAt(target);
                    }
                }
            }
        }

        private void setAnchorAboveTarget() {
            LivingEntity target = ElectroDragonEntity.this.getTarget();
            if (target == null) return;
            ElectroDragonEntity.this.anchorPoint = target.blockPosition()
                    .above(20 + ElectroDragonEntity.this.random.nextInt(20));
            int seaLevel = ElectroDragonEntity.this.level().getSeaLevel();
            if (ElectroDragonEntity.this.anchorPoint.getY() < seaLevel) {
                ElectroDragonEntity.this.anchorPoint = new BlockPos(
                        ElectroDragonEntity.this.anchorPoint.getX(), seaLevel + 1,
                        ElectroDragonEntity.this.anchorPoint.getZ());
            }
        }
    }

    /** Base class for goals that own the dragon's {@code moveTargetPoint}. */
    private abstract class MoveTargetGoal extends Goal {
        MoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        boolean touchingTarget() {
            return ElectroDragonEntity.this.moveTargetPoint.distanceToSqr(
                    ElectroDragonEntity.this.getX(),
                    ElectroDragonEntity.this.getY(),
                    ElectroDragonEntity.this.getZ()) < 4.0;
        }
    }

    /**
     * Orbits {@link #anchorPoint} at a random distance/altitude. Only runs in CIRCLE
     * phase (or when there's no target — keeps the dragon flying idly instead of stuck
     * in place). The orbit angle is incremented each tick the dragon reaches its
     * waypoint, producing a smooth circle.
     */
    private class CircleAroundAnchorGoal extends MoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        @Override
        public boolean canUse() {
            // Only orbit while we have a target. Idle wandering is handled by PatrolGoal
            // so the dragon doesn't tight-circle its spawn point.
            return ElectroDragonEntity.this.getTarget() != null
                    && ElectroDragonEntity.this.attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            this.distance = 5.0F + ElectroDragonEntity.this.random.nextFloat() * 10.0F;
            this.height = -4.0F + ElectroDragonEntity.this.random.nextFloat() * 9.0F;
            this.clockwise = ElectroDragonEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
        }

        @Override
        public void tick() {
            // Occasionally shake up the orbit so it doesn't look too mechanical.
            if (ElectroDragonEntity.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + ElectroDragonEntity.this.random.nextFloat() * 9.0F;
            }
            if (ElectroDragonEntity.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                this.distance++;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }
            if (ElectroDragonEntity.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = ElectroDragonEntity.this.random.nextFloat() * 2.0F * (float) Math.PI;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            // Avoid running into the ground/ceiling during the orbit.
            if (ElectroDragonEntity.this.moveTargetPoint.y < ElectroDragonEntity.this.getY()
                    && !ElectroDragonEntity.this.level().isEmptyBlock(
                            ElectroDragonEntity.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }
            if (ElectroDragonEntity.this.moveTargetPoint.y > ElectroDragonEntity.this.getY()
                    && !ElectroDragonEntity.this.level().isEmptyBlock(
                            ElectroDragonEntity.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }
        }

        private void selectNext() {
            if (ElectroDragonEntity.this.anchorPoint == null) {
                ElectroDragonEntity.this.anchorPoint = ElectroDragonEntity.this.blockPosition();
            }
            this.angle += this.clockwise * 15.0F * (float) (Math.PI / 180.0);
            ElectroDragonEntity.this.moveTargetPoint = Vec3.atLowerCornerOf(ElectroDragonEntity.this.anchorPoint)
                    .add(this.distance * Mth.cos(this.angle), -4.0F + this.height,
                            this.distance * Mth.sin(this.angle));
        }
    }

    /**
     * Dive at the target. Unlike Phantom (which deals melee damage on contact), our
     * dragon's lightning is already in the air from when the SWOOP started — this goal
     * just delivers the visual follow-through and pulls the dragon up after passing
     * near the target.
     */
    private class SwoopAttackGoal extends MoveTargetGoal {
        @Override
        public boolean canUse() {
            return ElectroDragonEntity.this.getTarget() != null
                    && ElectroDragonEntity.this.attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = ElectroDragonEntity.this.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (target instanceof Player p && (p.isSpectator() || p.isCreative())) return false;
            return this.canUse();
        }

        @Override
        public void stop() {
            ElectroDragonEntity.this.attackPhase = AttackPhase.CIRCLE;
        }

        @Override
        public void tick() {
            LivingEntity target = ElectroDragonEntity.this.getTarget();
            if (target == null) return;

            ElectroDragonEntity.this.moveTargetPoint = new Vec3(
                    target.getX(), target.getY(0.5), target.getZ());

            // Contact damage on the dive — uses ATTACK_DAMAGE attribute (6 by default).
            // The lightning bolt was already fired at swoop start, so a successful dive
            // deals both: ~lightning splash + melee swipe.
            if (ElectroDragonEntity.this.getBoundingBox().inflate(0.2F).intersects(target.getBoundingBox())) {
                if (ElectroDragonEntity.this.level() instanceof ServerLevel serverLevel) {
                    ElectroDragonEntity.this.doHurtTarget(serverLevel, target);
                }
                ElectroDragonEntity.this.attackPhase = AttackPhase.CIRCLE;
                if (!ElectroDragonEntity.this.isSilent()) {
                    ElectroDragonEntity.this.level()
                            .levelEvent(1039, ElectroDragonEntity.this.blockPosition(), 0);
                }
            } else if (ElectroDragonEntity.this.horizontalCollision
                    || ElectroDragonEntity.this.hurtTime > 0) {
                ElectroDragonEntity.this.attackPhase = AttackPhase.CIRCLE;
            }
        }
    }

    /**
     * Ghast-style idle wandering — picks a random point within ±24 blocks and flies to
     * it, repeating once arrived. Unlike Ghast, our move control reads from
     * {@code moveTargetPoint} (a Vec3 field) rather than vanilla's wanted-position
     * triple, so we set that directly. Wider radius than Ghast (24 vs 16) because the
     * dragon is faster and looks awkward repeatedly turning over short legs.
     */
    private class PatrolGoal extends MoveTargetGoal {
        @Override
        public boolean canUse() {
            if (ElectroDragonEntity.this.getTarget() != null) return false;
            // Pick a new patrol point if we don't have one yet or we've reached the
            // current one. Mirrors Ghast.RandomFloatAroundGoal's "hasWanted / distance"
            // check but expressed against our moveTargetPoint.
            return ElectroDragonEntity.this.moveTargetPoint.equals(Vec3.ZERO)
                    || this.touchingTarget()
                    || ElectroDragonEntity.this.moveTargetPoint.distanceToSqr(
                            ElectroDragonEntity.this.position()) > 3600.0; // >60 blocks
        }

        @Override
        public boolean canContinueToUse() {
            return false; // re-pick every time canUse() approves
        }

        @Override
        public void start() {
            ElectroDragonEntity.this.moveTargetPoint = pickPatrolPoint();
        }

        private Vec3 pickPatrolPoint() {
            // ±24 horizontal from current position. Altitude is GROUND-RELATIVE rather
            // than current-Y-relative — without this, "+dy each patrol" monotonically
            // drifts the dragon into the sky until it leaves render distance and despawns.
            double dx = (ElectroDragonEntity.this.random.nextDouble() * 2.0 - 1.0) * 24.0;
            double dz = (ElectroDragonEntity.this.random.nextDouble() * 2.0 - 1.0) * 24.0;
            double targetX = ElectroDragonEntity.this.getX() + dx;
            double targetZ = ElectroDragonEntity.this.getZ() + dz;

            int groundY = ElectroDragonEntity.this.level().getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(targetX),
                    (int) Math.floor(targetZ));
            // Cruise 18–32 blocks above the treetops — high enough to see over the
            // forest canopy, low enough to stay visible to the player.
            double targetY = groundY + 18.0 + ElectroDragonEntity.this.random.nextDouble() * 14.0;
            return new Vec3(targetX, targetY, targetZ);
        }
    }

    // ---- Controls (copied from Phantom, adapted) ---------------------------

    /** Body always follows yRot, head always follows body. No independent head turn. */
    private static class DragonBodyRotationControl extends BodyRotationControl {
        private final ElectroDragonEntity dragon;

        DragonBodyRotationControl(ElectroDragonEntity dragon) {
            super(dragon);
            this.dragon = dragon;
        }

        @Override
        public void clientTick() {
            this.dragon.yHeadRot = this.dragon.yBodyRot;
            this.dragon.yBodyRot = this.dragon.getYRot();
        }
    }

    /** Disabled — body rotation is driven entirely by {@link DragonMoveControl}. */
    private static class NoopLookControl extends LookControl {
        NoopLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }

    /**
     * Phantom's move control, copied verbatim. Lerps yRot at ~4°/tick toward the
     * direction of {@code moveTargetPoint}; accelerates speed when moving straight
     * (up to 1.8) and decelerates when turning. Also pitches the body (xRot) to point
     * up/down along the flight path, which the EnderDragonModel translates into the
     * dragon's body angle.
     */
    private static class DragonMoveControl extends MoveControl {
        private final ElectroDragonEntity dragon;
        private float speed = 0.1F;

        DragonMoveControl(ElectroDragonEntity dragon) {
            super(dragon);
            this.dragon = dragon;
        }

        @Override
        public void tick() {
            // On obstacle contact, veer ~10°/tick to escape instead of snap-flipping
            // 180° (which violently desyncs flightHistory and makes the neck/tail spaz).
            if (this.dragon.horizontalCollision) {
                this.dragon.setYRot(this.dragon.getYRot() + 10.0F);
                this.speed = 0.1F;
            }

            double tdx = this.dragon.moveTargetPoint.x - this.dragon.getX();
            double tdy = this.dragon.moveTargetPoint.y - this.dragon.getY();
            double tdz = this.dragon.moveTargetPoint.z - this.dragon.getZ();
            double sd = Math.sqrt(tdx * tdx + tdz * tdz);
            if (Math.abs(sd) > 1.0E-5F) {
                double yRelScale = 1.0 - Math.abs(tdy * 0.7F) / sd;
                tdx *= yRelScale;
                tdz *= yRelScale;
                sd = Math.sqrt(tdx * tdx + tdz * tdz);
                double sd2 = Math.sqrt(tdx * tdx + tdz * tdz + tdy * tdy);
                float prevYRot = this.dragon.getYRot();
                float targetAngle = (float) Mth.atan2(tdz, tdx);
                float currentDir = Mth.wrapDegrees(this.dragon.getYRot() + 90.0F);
                float desiredDir = Mth.wrapDegrees(targetAngle * (180.0F / (float) Math.PI));
                this.dragon.setYRot(Mth.approachDegrees(currentDir, desiredDir, 4.0F) - 90.0F);
                this.dragon.yBodyRot = this.dragon.getYRot();

                if (Mth.degreesDifferenceAbs(prevYRot, this.dragon.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 1.8F, 0.005F * (1.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }

                // Clamp pitch to ±35° — atan2 explodes as horizontal distance shrinks
                // during a dive (sd → 0 while tdy is still significant), which would
                // otherwise nose-vertical the dragon and snap back when pulling up.
                // The bobPitch term in the renderer amplifies that jump into a visible
                // flicker every few seconds.
                float xRotD = Mth.clamp(
                        (float) (-(Mth.atan2(-tdy, sd) * 180.0F / (float) Math.PI)),
                        -35.0F, 35.0F);
                this.dragon.setXRot(xRotD);
                float moveAngle = this.dragon.getYRot() + 90.0F;
                double txd = this.speed * Mth.cos(moveAngle * (float) (Math.PI / 180.0))
                        * Math.abs(tdx / sd2);
                double tzd = this.speed * Mth.sin(moveAngle * (float) (Math.PI / 180.0))
                        * Math.abs(tdz / sd2);
                double tyd = this.speed * Mth.sin(xRotD * (float) (Math.PI / 180.0))
                        * Math.abs(tdy / sd2);
                Vec3 movement = this.dragon.getDeltaMovement();
                this.dragon.setDeltaMovement(
                        movement.add(new Vec3(txd, tyd, tzd).subtract(movement).scale(0.2)));
            }
        }
    }
}
