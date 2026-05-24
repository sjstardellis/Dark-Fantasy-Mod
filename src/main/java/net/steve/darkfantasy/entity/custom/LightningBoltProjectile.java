package net.steve.darkfantasy.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Invisible projectile fired by the {@link net.steve.darkfantasy.item.custom.LightningStaffItem}.
 * Leaves an electric-spark trail behind it as it travels, and on impact (block or entity)
 * summons a real vanilla {@link LightningBolt} at the strike point.
 *
 * <p>Has a hard {@link #MAX_LIFETIME_TICKS} timeout so a shot fired into the sky doesn't
 * accumulate forever.
 */
public class LightningBoltProjectile extends ThrowableProjectile {
    private static final int MAX_LIFETIME_TICKS = 80; // 4 seconds

    public LightningBoltProjectile(EntityType<? extends LightningBoltProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        // No synched fields — the projectile carries no extra state beyond what Projectile gives us.
    }

    @Override
    protected double getDefaultGravity() {
        // Flies straight — no arc.
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();

        // Electric spark trail. Spawned on both sides so the shooter sees their own bolt
        // immediately and other clients see it once they receive the entity update.
        Vec3 pos = this.position();
        Vec3 motion = this.getDeltaMovement();
        for (int i = 0; i < 5; i++) {
            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    pos.x - motion.x * (i * 0.15),
                    pos.y - motion.y * (i * 0.15),
                    pos.z - motion.z * (i * 0.15),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
        }
        // A single end-rod sparkle at the head for visibility.
        this.level().addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, 0, 0);

        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level().isClientSide() || this.isRemoved()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // Strike at the hit location (works for both block and entity hits).
        Vec3 hitLocation = hitResult.getLocation();
        BlockPos strikePos = BlockPos.containing(hitLocation);

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(Vec3.atBottomCenterOf(strikePos));
            if (this.getOwner() instanceof net.minecraft.server.level.ServerPlayer shooter) {
                bolt.setCause(shooter);
            }
            serverLevel.addFreshEntity(bolt);
        }
        this.discard();
    }
}
