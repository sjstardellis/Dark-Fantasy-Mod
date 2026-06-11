package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Frost bolt fired by the {@link net.steve.darkfantasy.item.custom.FrostStaffItem}.
 * Snowball physics; on entity impact it deals light damage, applies heavy slowness,
 * and builds freezing ticks (the powder-snow frost overlay + freeze damage). Renders
 * via {@link net.minecraft.client.renderer.entity.ThrownItemRenderer} as a grimshard.
 */
public class FrostBoltProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final float IMPACT_DAMAGE = 3.0F;
    private static final int SLOWNESS_TICKS = 100;   // 5s of slowness III
    private static final int FREEZE_TICKS = 240;     // frost overlay + freeze damage-over-time
    private static final int MAX_LIFETIME_TICKS = 60;

    public FrostBoltProjectile(EntityType<? extends FrostBoltProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.GRIMSHARD.get());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01; // near-flat flight; it's magic, not a lobbed rock
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide() || this.isRemoved()) return;
        Entity hit = result.getEntity();
        Entity owner = this.getOwner();
        if (owner != null && hit == owner) return;

        DamageSource source = this.damageSources().thrown(this, owner);
        hit.hurt(source, IMPACT_DAMAGE);
        if (hit instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 2));
            if (living.canFreeze()) {
                living.setTicksFrozen(Math.max(living.getTicksFrozen(), FREEZE_TICKS));
            }
        }
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    hit.getX(), hit.getY() + hit.getBbHeight() * 0.5, hit.getZ(),
                    12, 0.2, 0.2, 0.2, 0.05);
        }
        this.playSound(SoundEvents.GLASS_BREAK, 0.7F, 1.4F);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SNOWFLAKE,
                        this.getX(), this.getY(), this.getZ(), 8, 0.15, 0.15, 0.15, 0.03);
            }
            this.playSound(SoundEvents.GLASS_BREAK, 0.5F, 1.5F);
            this.discard();
        }
    }
}
