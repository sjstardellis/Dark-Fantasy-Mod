package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/**
 * A star knocked loose from the veiled sky (spawned by
 * {@link net.steve.darkfantasy.event.StarfallHandler}). It streaks down trailing light;
 * on impact it scorches a small crater ring and scatters {@link ModItems#FALLEN_STAR}
 * reagents (with extended item lifetime, so the prize survives until someone reaches the
 * crash site). The crater is deliberately tiny — a scar, not a bomb.
 */
public class FallenStarEntity extends ThrowableProjectile implements ItemSupplier {
    private static final int MAX_LIFETIME_TICKS = 1200;   // failsafe: never streak forever

    public FallenStarEntity(EntityType<? extends FallenStarEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.FALLEN_STAR.get());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02;                              // shallow, comet-like arc
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                        0.0, 0.0, 0.0);
            }
            this.level().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY(), this.getZ(), 0.0, -0.05, 0.0);
        } else if (this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide() || this.isRemoved()) return;
        ServerLevel server = (ServerLevel) this.level();
        BlockPos center = this.blockPosition();

        // scorch a small ring where solid ground took the blow
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (this.random.nextFloat() > 0.6F) continue;
                BlockPos ground = findGround(server, center.offset(dx, 1, dz));
                if (ground == null) continue;
                BlockState scorch = (dx == 0 && dz == 0)
                        ? Blocks.MAGMA_BLOCK.defaultBlockState()
                        : (this.random.nextBoolean() ? Blocks.BASALT.defaultBlockState()
                                                     : Blocks.BLACKSTONE.defaultBlockState());
                server.setBlockAndUpdate(ground, scorch);
            }
        }
        // the prize — extended lifetime so it survives until someone arrives
        int shards = 2 + this.random.nextInt(3);
        for (int i = 0; i < shards; i++) {
            ItemEntity drop = new ItemEntity(server,
                    this.getX() + (this.random.nextDouble() - 0.5),
                    this.getY() + 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5),
                    new ItemStack(ModItems.FALLEN_STAR.get()));
            drop.setExtendedLifetime();
            server.addFreshEntity(drop);
        }
        // a beacon of light so the crash site reads from a distance
        server.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + 1.0, this.getZ(), 120, 0.5, 7.0, 0.5, 0.02);
        server.sendParticles(ParticleTypes.FIREWORK,
                this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.8, 0.8, 0.8, 0.1);
        server.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.AMBIENT, 2.0F, 1.4F);
        this.discard();
    }

    /** Nearest solid block at-or-below `from` (a few blocks down at most). */
    private static BlockPos findGround(ServerLevel level, BlockPos from) {
        BlockPos p = from;
        for (int i = 0; i < 4; i++) {
            if (!level.getBlockState(p).isAir()) {
                return level.getBlockState(p).canBeReplaced() ? null : p;
            }
            p = p.below();
        }
        return null;
    }
}
