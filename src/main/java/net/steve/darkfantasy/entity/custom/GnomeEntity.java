package net.steve.darkfantasy.entity.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Tiny splash-potion-throwing menace that uses the witch's visual but at 5% scale —
 * about half a block tall. Designed as a "fairy-folk" Twilight Forest mob that mostly
 * ignores well-equipped players but harasses unprepared ones.
 *
 * <h2>Hostility</h2>
 * Gnomes are default-neutral. They aggro on a player <em>only when that player has
 * no leather armor equipped anywhere</em> (head, chest, legs, or feet). Wearing even
 * one piece of leather is enough to be ignored — the gnome reads it as "fairy folk
 * disguise" and lets the player pass. Once aggro'd, they stay angry until the target
 * dies or leaves range (vanilla {@link NearestAttackableTargetGoal} behavior); putting
 * on leather mid-fight does <em>not</em> calm them.
 *
 * <h2>Combat</h2>
 * Splash-potion thrower via {@link RangedAttackGoal} + {@link RangedAttackMob}. Picks
 * a random harmful potion per throw (poison/slowness/weakness/harming/blindness). The
 * lead-prediction math is copied from vanilla {@link net.minecraft.world.entity.monster.Witch#performRangedAttack}
 * so throws land on a moving target.
 *
 * <h2>Immunity</h2>
 * {@link #canBeAffected} returns false for all effects, so other potion-throwers (or
 * a gnome's own friendly fire) can't debuff them.
 */
public class GnomeEntity extends Monster implements RangedAttackMob {
    /**
     * 50% of the witch model's size. Witch base bbox is 0.6×1.95, so SCALE=0.5
     * gives an in-world hitbox of 0.3×0.975 — exactly the same as a baby zombie.
     * Model renders at ~0.975 blocks tall (half-witch), matching the hitbox so
     * model and collision box align visually.
     */
    private static final float SCALE = 0.27F;

    /**
     * Harmful potion pool — drawn from uniformly per throw. All entries are the
     * <em>strong</em> or <em>long</em> vanilla variants:
     * <ul>
     *   <li>{@code STRONG_HARMING}  — instant damage II (~12 hearts on a no-armor hit).</li>
     *   <li>{@code STRONG_POISON}   — poison II, ticks 1 hp every ~1.25 s.</li>
     *   <li>{@code LONG_POISON}     — regular poison for 90 s — long-burn pressure.</li>
     *   <li>{@code STRONG_SLOWNESS} — slowness IV, near-stop movement for ~20 s.</li>
     *   <li>{@code LONG_WEAKNESS}   — 4-minute weakness (-0.5 attack damage).</li>
     * </ul>
     * Healing/regeneration variants are deliberately excluded — they'd heal the
     * target. Gnomes themselves are potion-immune so their splash radius can't hurt
     * each other.
     */
    private static final Holder<Potion>[] HARMFUL_POTIONS = harmful();

    @SuppressWarnings("unchecked")
    private static Holder<Potion>[] harmful() {
        return new Holder[] {
                Potions.STRONG_HARMING,
                Potions.STRONG_POISON,
                Potions.LONG_POISON,
                Potions.STRONG_SLOWNESS,
                Potions.LONG_WEAKNESS,
        };
    }

    public GnomeEntity(EntityType<? extends GnomeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                // Very fast — well above the vanilla "fast monster" baseline (zombie 0.23,
                // skeleton 0.25). At 0.5 the gnome easily outpaces a walking player and
                // matches a sprint; combined with potion debuffs this makes them slippery.
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                // No melee — gnomes only throw potions. ATTACK_DAMAGE here is the
                // contact damage if a player walks into them, kept low (1).
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Garden-gnome stillness: priority 0 + claims MOVE/LOOK/JUMP flags, so it
        // preempts every other goal when a player is watching. Combat resumes the
        // tick the player looks away.
        this.goalSelector.addGoal(0, new StillWhenObservedGoal());
        // RangedAttackGoal: speedModifier=1.0, attackInterval=80 ticks (4 s),
        // attackRange=10 blocks. Slower throw cadence than vanilla witch (60) because
        // gnomes pelt in groups and 60 made packs feel oppressive.
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, 80, 10.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(GnomeEntity.class));
        // Initial aggro: only target players who lack any leather armor. The predicate
        // is re-checked when the goal is considering a new target; existing targets
        // are not dropped by the goal even if armor is donned mid-combat (the
        // NearestAttackableTargetGoal only re-evaluates when picking new targets).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false,
                (entity, level) -> entity instanceof Player p && !playerHasAnyLeatherArmor(p)));
    }

    // ---- Leather-armor hostility gate ---------------------------------------

    /**
     * True iff the player has at least one leather armor piece in the matching slot
     * (leather helmet on head, leather chestplate on chest, etc.). Iron leggings +
     * leather chestplate counts as "has leather". Leather boots in the chest slot
     * (via creative inventory shenanigans) does NOT count — the item must be in its
     * proper armor slot.
     */
    private static boolean playerHasAnyLeatherArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(Items.LEATHER_HELMET)
                || player.getItemBySlot(EquipmentSlot.CHEST).is(Items.LEATHER_CHESTPLATE)
                || player.getItemBySlot(EquipmentSlot.LEGS).is(Items.LEATHER_LEGGINGS)
                || player.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS);
    }

    // ---- Potion immunity ----------------------------------------------------

    /**
     * Universal potion-effect immunity. Returns false for every effect so neither
     * other gnomes' friendly fire nor a player splash-potion can debuff this gnome.
     */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    // ---- Ranged attack: splash potion throw ---------------------------------

    /**
     * Picks a random harmful potion and throws it at the target. Lead-prediction
     * math (using the target's current delta) is borrowed from vanilla Witch so
     * a sprinting player can still be hit. Throw arc strength scales with horizontal
     * distance the same way the witch's does.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel server)) return;

        Vec3 targetMovement = target.getDeltaMovement();
        double xd = target.getX() + targetMovement.x - this.getX();
        double yd = target.getEyeY() - 1.1F - this.getY();
        double zd = target.getZ() + targetMovement.z - this.getZ();
        double horizontalDist = Math.sqrt(xd * xd + zd * zd);

        Holder<Potion> chosen = HARMFUL_POTIONS[this.random.nextInt(HARMFUL_POTIONS.length)];
        ItemStack potionStack = PotionContents.createItemStack(Items.SPLASH_POTION, chosen);

        Projectile.spawnProjectileUsingShoot(
                ThrownSplashPotion::new, server, potionStack, this,
                xd, yd + horizontalDist * 0.2, zd,
                horizontalDist <= 2.0 ? 0.45F : 0.75F, // arc strength
                8.0F);                                  // inaccuracy

        if (!this.isSilent()) {
            this.playSound(SoundEvents.WITCH_THROW, 1.0F,
                    0.8F + this.random.nextFloat() * 0.4F);
        }
    }

    // ---- Sounds (witch sounds, pitched up to fit the small size) ------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    @Override
    public float getVoicePitch() {
        // Pitched up substantially so the tiny gnome sounds tiny.
        return super.getVoicePitch() * 1.6F;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    // ---- Garden-gnome stillness --------------------------------------------

    /** Half-FOV cosine. dot ≥ 0.93 corresponds to a roughly ±22° cone — wide enough that
     *  glancing at a gnome counts, narrow enough that peripheral vision doesn't.  */
    private static final double OBSERVATION_DOT_THRESHOLD = 0.93;
    /** Max range (blocks) at which a player's gaze can pin a gnome. */
    private static final double OBSERVATION_RANGE = 24.0;
    private static final double OBSERVATION_RANGE_SQR = OBSERVATION_RANGE * OBSERVATION_RANGE;

    /**
     * True iff any non-spectator player is currently looking at this gnome — within
     * {@link #OBSERVATION_RANGE}, with view direction within {@link #OBSERVATION_DOT_THRESHOLD}
     * of the line to the gnome, and with clear line of sight. Used by
     * {@link StillWhenObservedGoal} to freeze AI.
     */
    private boolean isBeingObservedByPlayer() {
        Level level = this.level();
        Vec3 myPos = this.position().add(0, this.getBbHeight() * 0.5, 0);
        for (Player player : level.players()) {
            if (player.isSpectator()) continue;
            if (this.distanceToSqr(player) > OBSERVATION_RANGE_SQR) continue;

            Vec3 fromPlayerToGnome = myPos.subtract(player.getEyePosition()).normalize();
            Vec3 playerView = player.getViewVector(1.0F).normalize();
            if (playerView.dot(fromPlayerToGnome) < OBSERVATION_DOT_THRESHOLD) continue;

            // hasLineOfSight does an obstruction raytrace — walls/blocks break the gaze.
            if (player.hasLineOfSight(this)) return true;
        }
        return false;
    }

    /**
     * Freezes all movement, look-tracking, jumping, and potion-throwing while any
     * player is staring at the gnome. Combat resumes the tick the player looks away.
     *
     * <p>The goal claims {@link Goal.Flag#MOVE} and {@link Goal.Flag#LOOK} (and
     * {@link Goal.Flag#JUMP} for defensive measure), which preempts {@link RangedAttackGoal}
     * (also MOVE+LOOK) and {@link WaterAvoidingRandomStrollGoal} (MOVE). Lower-
     * priority goals don't even tick while this one is active.
     *
     * <p>The HurtByTargetGoal entry in {@code targetSelector} is unaffected — a
     * frozen gnome can still receive a target from being hit; it just won't act
     * on that target until the player looks away.
     */
    private class StillWhenObservedGoal extends Goal {
        StillWhenObservedGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return GnomeEntity.this.isBeingObservedByPlayer();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            // Halt path + cancel any residual inertia so the freeze reads as instant.
            GnomeEntity.this.getNavigation().stop();
            GnomeEntity.this.setDeltaMovement(Vec3.ZERO);
        }

        @Override
        public void tick() {
            // Any nav requests that snuck in (e.g., from physics) get re-cancelled.
            GnomeEntity.this.getNavigation().stop();
            GnomeEntity.this.setDeltaMovement(
                    0.0, GnomeEntity.this.getDeltaMovement().y, 0.0); // keep gravity
        }
    }
}
