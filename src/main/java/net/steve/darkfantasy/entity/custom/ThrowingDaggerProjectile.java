package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Thrown shadowsteel dagger. Flat, fast flight; on an entity hit it deals
 * {@link #IMPACT_DAMAGE} — or {@link #STEALTH_DAMAGE} if the thrower was sneaking when it
 * connects (an assassin's opener, mirroring the shadowsteel melee daggers' identity).
 * Daggers are retrievable: a terrain hit always drops the dagger back as an item; an
 * entity hit recovers it {@link #RECOVER_CHANCE} of the time (the rest snap on bone).
 */
public class ThrowingDaggerProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final float IMPACT_DAMAGE = 4.0F;
    private static final float STEALTH_DAMAGE = 8.0F;
    private static final float RECOVER_CHANCE = 0.65F;
    private static final int MAX_LIFETIME_TICKS = 200;

    public ThrowingDaggerProjectile(EntityType<? extends ThrowingDaggerProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.THROWING_DAGGER.get());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.025;                            // flatter than a rock — it's balanced steel
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.dropSelf();
            this.discard();
        }
    }

    private void dropSelf() {
        if (this.level() instanceof ServerLevel server) {
            server.addFreshEntity(new ItemEntity(server, this.getX(), this.getY(), this.getZ(),
                    new ItemStack(ModItems.THROWING_DAGGER.get())));
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide() || this.isRemoved()) return;
        Entity hit = result.getEntity();
        Entity owner = this.getOwner();
        if (owner != null && hit == owner) return;

        boolean stealth = owner != null && owner.isShiftKeyDown();
        DamageSource source = this.damageSources().thrown(this, owner);
        hit.hurt(source, stealth ? STEALTH_DAMAGE : IMPACT_DAMAGE);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(stealth ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT,
                    hit.getX(), hit.getY() + hit.getBbHeight() * 0.5, hit.getZ(),
                    stealth ? 12 : 6, 0.15, 0.15, 0.15, 0.05);
            if (this.random.nextFloat() < RECOVER_CHANCE) {
                this.dropSelf();
            }
        }
        this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 0.8F, 1.3F + this.random.nextFloat() * 0.2F);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide()) {
            this.playSound(SoundEvents.TRIDENT_HIT_GROUND, 0.6F, 1.4F);
            this.dropSelf();                     // stuck in the wall — go pull it out
            this.discard();
        }
    }
}
