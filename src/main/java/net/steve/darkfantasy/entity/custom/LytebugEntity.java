package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

/**
 * Passive ambient firefly-like mob for the Twilight Forest biome. Reuses the vanilla
 * bee model (re-textured) via {@link net.steve.darkfantasy.client.renderer.LytebugRenderer}
 * plus a fullbright emissive overlay layer for the "glow." The glow is purely cosmetic
 * — the world stays dark; only the bug looks bright.
 *
 * <h2>Behavior</h2>
 * Fully passive: no melee, no targeting goals. Flees on hurt via {@link PanicGoal} and
 * follows sugar via {@link TemptGoal}. Not breedable — {@link #getBreedOffspring}
 * returns null and no {@code BreedGoal} is registered.
 *
 * <h2>Flight</h2>
 * Uses {@link FlyingMoveControl} + {@link FlyingPathNavigation} like vanilla {@code Bee},
 * minus the hive/flower pollination machinery.
 *
 * <h2>Spawning</h2>
 * Registered as {@link net.minecraft.world.entity.MobCategory#CREATURE} in
 * {@link net.steve.darkfantasy.init.ModEntities} so it's subject to the passive
 * (not monster) spawn cap. Biome spawn entry lives in the twilight_forest biome JSON.
 */
public class LytebugEntity extends Animal implements FlyingAnimal {
    /**
     * Final hitbox = base × SCALE. EntityType base is 0.4×0.3; SCALE 0.6 → 0.24×0.18.
     * Smaller than a bee (which is 0.7×0.6) — reads as "tiny glowing bug" rather than
     * "hornet." The bee model still renders at its full mesh size, so the visible
     * sprite is a bit larger than the collision box — fine for a fluttery thing.
     */
    private static final float SCALE = 0.6F;

    public LytebugEntity(EntityType<? extends LytebugEntity> type, Level level) {
        super(type, level);
        // 20 = max speed records used by FlyingMoveControl; true = always hover (no gravity).
        // Same numbers vanilla Bee uses.
        this.moveControl = new FlyingMoveControl(this, 20, true);
        // Bees mark water/fire as impassable for pathing; mirror that.
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, -1.0F);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Brief sprint away when struck. 1.5x speed for 8 ticks of panic, then drops.
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        // Tempted by sugar. Cocoa beans / honey blocks are bee defaults; sugar is the
        // simpler thematic fit for a sweet-loving glow bug.
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1,
                stack -> stack.is(Items.SUGAR), false));
        // Drift toward any Lytestone block within 16 blocks horizontally / 4 vertically.
        // Priority sits between tempt (2) and random wander (5) so a sugar-holding
        // player still wins, but otherwise the bug "homes" on Lytestone — the magnet
        // mechanic that closes the loop on the dust → block → more bugs economy.
        this.goalSelector.addGoal(4, new MoveToLytestoneGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        // No targetSelector entries — lytebugs never attack anything.
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        // Same shape as vanilla Bee's flying nav: refuses to land on air blocks so it
        // perches on real geometry when it does come down.
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(net.minecraft.core.BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        nav.setCanOpenDoors(false);
        nav.setCanFloat(false);
        return nav;
    }

    /** Lytebugs ignore gravity while alive — they hover. */
    @Override
    public boolean isFlapping() {
        return !this.onGround();
    }

    /** Required by {@link FlyingAnimal} — true means "currently airborne." */
    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    // ---- Breeding disabled --------------------------------------------------

    /** No breeding — return null so the (unused) breed path silently no-ops. */
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    /** Sugar tempts, but it isn't "food" for breeding purposes either. */
    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    // ---- Trail particles ----------------------------------------------------

    /**
     * Trail a couple of GLOW particles every ~10 ticks for atmosphere. Client-side
     * via the level's per-player particle path so we don't double-spawn on the server.
     * Cheap: at most one particle per tick spread across the whole entity lifecycle.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.isAlive() && this.tickCount % 10 == 0) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 0.3;
            double y = this.getY() + this.getBbHeight() * 0.5 + (this.random.nextDouble() - 0.5) * 0.2;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 0.3;
            this.level().addParticle(ParticleTypes.GLOW, x, y, z, 0.0, 0.01, 0.0);
        }
    }

    // ---- Lytestone magnet ---------------------------------------------------

    /**
     * Drifts toward the nearest {@link ModBlocks#LYTESTONE} within a 16-block
     * horizontal / 4-block vertical search range. Subclass of vanilla
     * {@link MoveToBlockGoal} so we inherit the cooldown + path-recalc logic; we
     * only customize the validity predicate.
     */
    private final class MoveToLytestoneGoal extends MoveToBlockGoal {
        MoveToLytestoneGoal() {
            super(LytebugEntity.this, 1.0, 16, 4);
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(ModBlocks.LYTESTONE.get());
        }
    }

    // ---- Sounds (very quiet bee loop) ---------------------------------------

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BEE_LOOP;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
    }
}
