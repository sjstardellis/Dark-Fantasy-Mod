package net.steve.darkfantasy.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Thrown stone projectile fired by {@link GoblinEntity} when its target is too far for
 * melee. Behaves like a snowball physically (light, small arc) but deals real damage
 * on entity impact. Doesn't break blocks; doesn't spawn anything on terrain hits — just
 * a few crit particles to read as a thud.
 *
 * <p>Damage value is intentionally low ({@link #IMPACT_DAMAGE}) so a single rock isn't
 * scary on its own — the threat comes from a pack of goblins pelting the player at
 * once while one or two close to melee.
 */
public class GoblinRockProjectile extends ThrowableProjectile implements ItemSupplier {
    /** Per-hit damage. Tuned to feel like a thrown rock — annoying, not devastating. */
    private static final float IMPACT_DAMAGE = 2.0F;
    private static final int MAX_LIFETIME_TICKS = 100; // 5s — keeps stray rocks from haunting the world

    public GoblinRockProjectile(EntityType<? extends GoblinRockProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No extra synched state — owner + position come from Projectile/Entity.
    }

    /**
     * Tells {@link net.minecraft.client.renderer.entity.ThrownItemRenderer} which item
     * model to spin in the air for this projectile. Cobblestone gives a small grey
     * pebble that reads as a thrown rock without authoring a custom model.
     */
    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.COBBLESTONE);
    }

    @Override
    protected double getDefaultGravity() {
        // Slight gravity so the rock arcs visibly over a few-block distance.
        // 0.03 is between snowball (0.03) and arrow (0.05) — visible arc without
        // plummeting like an arrow at long range.
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide() || this.isRemoved()) return;
        Entity hit = result.getEntity();
        Entity owner = this.getOwner();
        // Don't let a goblin rock its own thrower.
        if (owner != null && hit == owner) return;

        DamageSource source = this.damageSources().thrown(this, owner);
        hit.hurt(source, IMPACT_DAMAGE);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT,
                    hit.getX(), hit.getY() + hit.getBbHeight() * 0.5, hit.getZ(),
                    6, 0.15, 0.15, 0.15, 0.05);
        }
        this.playSound(SoundEvents.STONE_HIT, 0.7F, 0.9F + this.random.nextFloat() * 0.2F);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        // Terrain hits are handled by super (just removes the projectile after the
        // standard onHitBlock logic). Add a tiny audio cue so the rock isn't silent
        // when it skips off a wall.
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide()) {
            this.playSound(SoundEvents.STONE_HIT, 0.4F, 0.7F + this.random.nextFloat() * 0.3F);
            this.discard();
        }
    }
}
