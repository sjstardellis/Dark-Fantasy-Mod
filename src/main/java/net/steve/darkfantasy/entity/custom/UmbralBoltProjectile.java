package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Umbral bolt spat by the {@link UmbralWraithEntity} — a mote of living dark. Near-flat
 * flight with an ink trail; on impact it deals light magic damage and smothers the
 * target's sight: Blindness + Darkness for {@link #BLIND_TICKS} ticks. The wraith
 * follows up while its prey gropes in the black.
 */
public class UmbralBoltProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final float IMPACT_DAMAGE = 3.0F;
    private static final int BLIND_TICKS = 80;   // 4 s
    private static final int MAX_LIFETIME_TICKS = 60;

    public UmbralBoltProjectile(EntityType<? extends UmbralBoltProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.UMBRA_ESSENCE.get());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SQUID_INK,
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
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS));
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, BLIND_TICKS));
        }
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    hit.getX(), hit.getY() + hit.getBbHeight() * 0.5, hit.getZ(),
                    10, 0.25, 0.35, 0.25, 0.02);
        }
        this.level().playSound(null, this.blockPosition(),
                SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.7F, 0.6F);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide()) {
            this.discard();
        }
    }
}
