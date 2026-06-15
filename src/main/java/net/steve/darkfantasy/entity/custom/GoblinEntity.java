package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.init.ModEntities;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Small, scrappy melee+ranged mob designed as a Twilight Forest encounter. Uses the
 * vanilla copper-golem model (see {@link net.steve.darkfantasy.client.renderer.GoblinRenderer})
 * scaled down to ~0.7 via {@link Attributes#SCALE}, which gives a knee-height humanoid
 * that reads as a goblin without authoring a new model.
 *
 * <h2>Combat</h2>
 * Two attack ranges:
 * <ul>
 *   <li><b>Melee</b> (≤ ~2 blocks): vanilla {@link MeleeAttackGoal} — deals
 *       {@link Attributes#ATTACK_DAMAGE} on contact.</li>
 *   <li><b>Ranged</b> ({@link #RANGED_MIN}–{@link #RANGED_MAX} blocks, line of sight):
 *       {@link GoblinRockThrowGoal} throws a {@link GoblinRockProjectile} every
 *       {@link #RANGED_COOLDOWN_TICKS} ticks. Each rock is light damage on its own —
 *       the pressure comes from a pack.</li>
 * </ul>
 *
 * <h2>Berserker</h2>
 * When HP drops below {@link #BERSERK_HP_FRACTION} of max, the goblin enters berserk
 * mode <em>permanently for the rest of its life</em>: attribute modifiers boost speed
 * and attack damage, and {@link #spawnBerserkParticles} adds red angry-villager
 * wisps so the player can read the state visually. The transition fires once via
 * {@link #berserkApplied} — no per-tick attribute churn.
 *
 * <h2>Trading economy</h2>
 * Neutral goblins barter two currencies: fairy dust → an everyday pool of modest vanilla
 * resources (no mod metals), and <b>beer</b> → a prized pool with only a small chance at a
 * mod metal. Beer is the goblins' weakness — uniquely it pacifies a hostile goblin and
 * leaves it {@linkplain #makeTipsy tipsy}, a 60 s window where it won't fight. See
 * {@link #mobInteract}.
 */
public class GoblinEntity extends Monster {
    private static final float SCALE = 0.7F;

    // Combat tuning ------------------------------------------------------------
    /** Min distance (blocks) at which the rock-throw goal becomes valid. Closer than this, the goblin should melee instead. */
    private static final double RANGED_MIN = 4.0;
    /** Max distance (blocks). Beyond this, the goblin closes in to melee range first. */
    private static final double RANGED_MAX = 12.0;
    /** Ticks between thrown rocks. ~1.5 s — quick enough to feel threatening, slow enough to dodge. */
    private static final int RANGED_COOLDOWN_TICKS = 30;
    /** Initial throw speed; gravity is set on the projectile (0.03), so this controls range. */
    private static final float ROCK_LAUNCH_VELOCITY = 1.4F;
    /** Random inaccuracy added to throws. 4° is "primitive aim" — not pixel-perfect like a skeleton bow. */
    private static final float ROCK_INACCURACY = 4.0F;

    // Berserker tuning ---------------------------------------------------------
    /** Health fraction below which berserker mode triggers. 0.3 = below 30% HP. */
    private static final float BERSERK_HP_FRACTION = 0.3F;
    /** Movement-speed multiplier when berserking (+50%). */
    private static final double BERSERK_SPEED_MULT = 0.5;
    /** Attack-damage flat add when berserking (+2 damage). */
    private static final double BERSERK_DAMAGE_ADD = 2.0;
    /** Stable resource locations for the attribute modifiers so they don't stack on world reload. */
    private static final Identifier BERSERK_SPEED_MODIFIER =
            Identifier.fromNamespaceAndPath("darkfantasy", "goblin_berserk_speed");
    private static final Identifier BERSERK_DAMAGE_MODIFIER =
            Identifier.fromNamespaceAndPath("darkfantasy", "goblin_berserk_damage");

    /** True once the goblin has entered berserk mode this lifetime. Never resets. */
    private boolean berserkApplied = false;

    // Tipsy (beer) tuning ------------------------------------------------------
    /** How long a goblin stays tipsy after a beer. 1200 ticks = 60 s. */
    private static final int TIPSY_TICKS = 1200;
    /**
     * Ticks of tipsiness remaining. While &gt; 0 the goblin won't fight — its target is
     * cleared every tick (see {@link #tick}) — which is what lets a beer pacify a hostile
     * goblin. Set by {@link #makeTipsy}; decays in {@link #tick}. Transient: a save/reload
     * sobers the goblin up, which is fine for a 60 s buzz.
     */
    private int tipsyTicks = 0;

    // Animation event IDs — broadcast via Level.broadcastEntityEvent so client viewers
    // start their AnimationStates in sync with server-side actions. Values are in the
    // mod-safe 60+ range to avoid collisions with vanilla LivingEntity event constants.
    private static final byte EVENT_MELEE_SWING = 60;
    private static final byte EVENT_ROCK_THROW = 61;
    private static final byte EVENT_TRADE_ACCEPT = 62;

    /**
     * Client-side animation states. The renderer copies these into the matching
     * CopperGolemRenderState interaction slots so {@link net.minecraft.client.model.animal.golem.CopperGolemModel#setupAnim}
     * plays them. Server only mutates them indirectly by broadcasting the matching
     * entity event below.
     */
    public final AnimationState meleeSwingState = new AnimationState();
    public final AnimationState rockThrowState = new AnimationState();
    public final AnimationState tradeAcceptState = new AnimationState();

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EVENT_MELEE_SWING) {
            this.meleeSwingState.start(this.tickCount);
        } else if (event == EVENT_ROCK_THROW) {
            this.rockThrowState.start(this.tickCount);
        } else if (event == EVENT_TRADE_ACCEPT) {
            this.tradeAcceptState.start(this.tickCount);
        } else {
            super.handleEntityEvent(event);
        }
    }

    /**
     * Override the swing hook so every melee attempt (hit OR miss) plays the animation.
     * MeleeAttackGoal calls {@code swing(MAIN_HAND)} just before {@code doHurtTarget},
     * so this fires on the visual moment of the strike rather than only on successful
     * damage application.
     */
    @Override
    public void swing(InteractionHand hand) {
        super.swing(hand);
        if (this.level() instanceof ServerLevel server) {
            server.broadcastEntityEvent(this, EVENT_MELEE_SWING);
        }
    }

    public GoblinEntity(EntityType<? extends GoblinEntity> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Rock-throwing takes priority over melee when at range — otherwise the goblin
        // would always close to melee and never throw.
        this.goalSelector.addGoal(2, new GoblinRockThrowGoal());
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Default-neutral: no unconditional NearestAttackableTargetGoal on players.
        // The HurtByTargetGoal makes the goblin retaliate when struck (and alerts
        // pack mates). Aggro from other triggers — chest-opening (handled by
        // GoblinChestAggroHandler) — calls aggroOn(...) directly.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(GoblinEntity.class));
    }

    /**
     * Public hook for external triggers (chest-open handler, future scripted events)
     * to make this goblin attack a specific target. Idempotent — calling twice with the
     * same target has no extra effect; calling while already targeting someone else
     * leaves the existing target alone so the goblin doesn't constantly retarget mid-fight.
     */
    public void aggroOn(LivingEntity target) {
        if (this.getTarget() != null && this.getTarget().isAlive()) return;
        this.setTarget(target);
        this.setLastHurtByMob(target); // makes HurtByTargetGoal's alert path fire so packmates join
    }

    // ---- Sounds (zombie villager fits the "small humanoid muttering" vibe) ---

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    // ---- Per-tick berserker + visual cue -----------------------------------

    @Override
    public void tick() {
        super.tick();
        if (!this.berserkApplied && !this.level().isClientSide()
                && this.getHealth() < this.getMaxHealth() * BERSERK_HP_FRACTION
                && this.isAlive()) {
            this.enterBerserkMode();
        }
        if (this.berserkApplied && this.level().isClientSide()) {
            this.spawnBerserkParticles();
        }

        if (this.tipsyTicks > 0) {
            this.tipsyTicks--;
            if (this.level().isClientSide()) {
                this.spawnTipsyParticles();
            } else if (this.getTarget() != null) {
                // Too drunk to brawl: drop any target it picks up (e.g. retaliation)
                // for the duration of the buzz. The beer wears off and it can fight again.
                this.setTarget(null);
            }
        }
    }

    /**
     * Calm the goblin and put it in a tipsy buzz. Clears any current target and the
     * last-attacker memory so a pacified goblin actually disengages, then plays a burp
     * and a puff of happy particles. Server-authoritative; called from {@link #mobInteract}.
     */
    private void makeTipsy() {
        this.tipsyTicks = TIPSY_TICKS;
        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.playSound(SoundEvents.PLAYER_BURP, 0.7F, 0.8F + this.random.nextFloat() * 0.2F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(),
                    8, 0.25, 0.2, 0.25, 0.0);
            server.broadcastEntityEvent(this, EVENT_TRADE_ACCEPT);
        }
    }

    /** Lazy happy-villager wisps while buzzed (client-side cosmetic), echoing the berserk cue. */
    private void spawnTipsyParticles() {
        if (this.random.nextInt(10) != 0) return;
        double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
        double y = this.getY() + this.getBbHeight() * 0.85 + this.random.nextDouble() * 0.2;
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
        this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0, 0.02, 0.0);
    }

    /**
     * Apply the berserker stat boost once. Uses {@link AttributeModifier} with stable
     * resource locations so a save+reload doesn't double-apply. The modifiers persist
     * for the rest of the goblin's life — no reset on healing.
     */
    private void enterBerserkMode() {
        this.berserkApplied = true;

        var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && !speedAttr.hasModifier(BERSERK_SPEED_MODIFIER)) {
            speedAttr.addPermanentModifier(new AttributeModifier(
                    BERSERK_SPEED_MODIFIER, BERSERK_SPEED_MULT,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        var damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null && !damageAttr.hasModifier(BERSERK_DAMAGE_MODIFIER)) {
            damageAttr.addPermanentModifier(new AttributeModifier(
                    BERSERK_DAMAGE_MODIFIER, BERSERK_DAMAGE_ADD,
                    AttributeModifier.Operation.ADD_VALUE));
        }

        // Dramatic burst at the moment of activation — angry-villager particles for
        // a clear "uh oh, it's pissed now" cue. Server-side broadcast so all viewers see it.
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(),
                    8, 0.25, 0.2, 0.25, 0.0);
            this.playSound(SoundEvents.RAVAGER_ROAR, 0.6F, 1.6F);
        }
    }

    /** Ongoing red wisp particles around the head while berserking (client-side cosmetic). */
    private void spawnBerserkParticles() {
        if (this.random.nextInt(4) != 0) return;
        double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
        double y = this.getY() + this.getBbHeight() * 0.8 + this.random.nextDouble() * 0.2;
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
        this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, x, y, z, 0.0, 0.02, 0.0);
    }

    // ---- Ranged attack goal -------------------------------------------------

    /**
     * Throws a rock at the current target every {@link #RANGED_COOLDOWN_TICKS} ticks
     * as long as the target is in the {@link #RANGED_MIN}..{@link #RANGED_MAX} window
     * and visible. Outside that window the goal yields so MeleeAttackGoal can take
     * over (close in or chase, depending on distance).
     */
    private class GoblinRockThrowGoal extends Goal {
        private int throwCooldown = RANGED_COOLDOWN_TICKS;
        private int seeTime = 0;

        GoblinRockThrowGoal() {
            // Movement-flag means MeleeAttackGoal can't run at the same priority.
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = GoblinEntity.this.getTarget();
            if (target == null || !target.isAlive()) return false;
            double d2 = GoblinEntity.this.distanceToSqr(target);
            return d2 >= RANGED_MIN * RANGED_MIN && d2 <= RANGED_MAX * RANGED_MAX;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void stop() {
            this.seeTime = 0;
            this.throwCooldown = RANGED_COOLDOWN_TICKS;
        }

        @Override
        public void tick() {
            LivingEntity target = GoblinEntity.this.getTarget();
            if (target == null) return;
            boolean canSee = GoblinEntity.this.getSensing().hasLineOfSight(target);
            if (canSee) {
                this.seeTime++;
            } else {
                this.seeTime = 0;
            }
            GoblinEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (--this.throwCooldown <= 0) {
                // Require at least 5 ticks of LOS before throwing — keeps the goblin
                // from blind-firing the instant a target appears behind a tree.
                if (canSee && this.seeTime >= 5) {
                    throwRockAt(target);
                    this.throwCooldown = RANGED_COOLDOWN_TICKS;
                } else {
                    this.throwCooldown = 10; // retry soon
                }
            }
        }
    }

    /** Spawn and fire a {@link GoblinRockProjectile} aimed at the given target. */
    private void throwRockAt(LivingEntity target) {
        Level level = this.level();
        Vec3 from = this.position().add(0, this.getBbHeight() * 0.85, 0);
        // Aim slightly above the target so the gravity arc lands the rock on the body
        // rather than at the feet.
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.6, 0);
        Vec3 dir = to.subtract(from);

        GoblinRockProjectile rock = new GoblinRockProjectile(
                ModEntities.GOBLIN_ROCK.get(), level);
        rock.setOwner(this);
        rock.setPos(from.x, from.y, from.z);
        rock.shoot(dir.x, dir.y, dir.z, ROCK_LAUNCH_VELOCITY, ROCK_INACCURACY);
        level.addFreshEntity(rock);

        this.playSound(SoundEvents.SNOWBALL_THROW, 0.9F,
                0.85F + this.random.nextFloat() * 0.3F);
        if (level instanceof ServerLevel server) {
            server.broadcastEntityEvent(this, EVENT_ROCK_THROW);
        }
    }

    /** Stable identifier used in error messages / debug. */
    public static Identifier id() {
        return Identifier.fromNamespaceAndPath("darkfantasy", "goblin");
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return super.getTarget();
    }

    // ---- Trading -----------------------------------------------------------

    /**
     * One possible trade outcome. {@code weight} is relative within a pool; counts are
     * inclusive uniform random in [{@code minCount}, {@code maxCount}].
     */
    private record TradeOption(Item item, int weight, int minCount, int maxCount) {}

    /**
     * Fairy-dust pool — the everyday currency. Modest vanilla resources in small amounts;
     * deliberately NO mod metals and no jackpots, so dust trading is a steady trickle of
     * basics rather than a shortcut to gear. Nuggets dominate; a whole iron ingot or an
     * emerald is the rare upside.
     */
    private static final List<TradeOption> FAIRY_DUST_TRADES = List.of(
            new TradeOption(Items.IRON_NUGGET, 12, 2, 5),
            new TradeOption(Items.GOLD_NUGGET, 11, 2, 5),
            new TradeOption(Items.LAPIS_LAZULI, 9, 1, 3),
            new TradeOption(Items.REDSTONE, 8, 1, 3),
            new TradeOption(Items.STRING, 7, 1, 3),
            new TradeOption(Items.ARROW, 7, 2, 5),
            new TradeOption(Items.GLASS_BOTTLE, 6, 1, 2),
            new TradeOption(Items.IRON_INGOT, 5, 1, 1),
            new TradeOption(Items.EXPERIENCE_BOTTLE, 4, 1, 1),
            new TradeOption(Items.EMERALD, 3, 1, 1));

    /**
     * Beer pool — the prized currency, and the goblins' weakness. A clear step up from dust
     * (real ingots, the occasional diamond or golden apple), but kept in check: the three
     * mod metals sit at weight 1 each in a ~62-weight pool, so a given beer trade only yields
     * one with very low odds (~5% combined). No enchanted-apple jackpot.
     */
    private static final List<TradeOption> BEER_TRADES = List.of(
            new TradeOption(Items.IRON_INGOT, 12, 1, 3),
            new TradeOption(Items.GOLD_INGOT, 10, 1, 3),
            new TradeOption(Items.EMERALD, 8, 1, 2),
            new TradeOption(Items.EXPERIENCE_BOTTLE, 7, 1, 2),
            new TradeOption(Items.ENDER_PEARL, 6, 1, 2),
            new TradeOption(ModItems.LYTEBUG_DUST.get(), 6, 1, 2),
            new TradeOption(ModItems.ARCANE_ASH.get(), 5, 1, 2),
            new TradeOption(Items.DIAMOND, 3, 1, 1),
            new TradeOption(Items.GOLDEN_APPLE, 2, 1, 1),
            // Mod metals — intentionally rare (weight 1 apiece).
            new TradeOption(ModItems.MOONSILVER.get(), 1, 1, 1),
            new TradeOption(ModItems.DAWNMETAL.get(), 1, 1, 1),
            new TradeOption(ModItems.ECLIPSIUM.get(), 1, 1, 1));

    /**
     * Right-click interaction — the goblin trading economy. Two currencies:
     * <ul>
     *   <li><b>Fairy dust</b> → {@link #FAIRY_DUST_TRADES}: the everyday pool — modest
     *       vanilla resources, no mod metals.</li>
     *   <li><b>Beer</b> → {@link #BEER_TRADES}: the prized pool, and the goblins' weakness.
     *       Better goods, with only a small chance at a mod metal.</li>
     * </ul>
     *
     * <p><b>Beer is special.</b> It's the only currency that pacifies a <em>hostile</em>
     * goblin: tossing one a beer mid-fight clears its aggro and leaves it {@linkplain
     * #makeTipsy tipsy} for {@value #TIPSY_TICKS} ticks (no trade — you spent it to escape).
     * Given to a <em>neutral</em> goblin it buys from the prized pool and still gets it tipsy.
     *
     * <p>Fairy-dust trades are refused while the goblin is hostile — only beer calms one.
     *
     * <p>Made {@code public} to match the vanilla pattern ({@link net.minecraft.world.entity.animal.pig.Pig#mobInteract})
     * — protected works too but some interaction code paths (e.g. NeoForge events) are
     * defensively coded around the public form. Returns {@link InteractionResult#SUCCESS_SERVER}
     * so the inventory change is server-authoritative rather than client-predicted.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Check ingredient first — if the player isn't even offering a currency, fall
        // through to vanilla immediately so we don't accidentally swallow unrelated
        // interactions (lead, name tag, spawn egg) above.
        ItemStack offered = player.getItemInHand(hand);
        boolean offeredBeer = offered.is(ModItems.BEER.get());
        boolean hostile = this.getTarget() != null;

        List<TradeOption> pool;
        if (offeredBeer) {
            pool = BEER_TRADES;
        } else if (offered.is(ModItems.FAIRY_DUST.get())) {
            pool = FAIRY_DUST_TRADES;
        } else {
            return super.mobInteract(player, hand);
        }

        // Fairy dust can't bribe a charging goblin — only beer can.
        if (hostile && !offeredBeer) {
            // SUCCESS_SERVER so we still consume the click (no stray place-block/attack),
            // but no inventory change happens.
            if (!this.level().isClientSide()) {
                this.playSound(SoundEvents.VILLAGER_NO, 0.8F, 0.9F);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        if (this.level().isClientSide()) {
            // Report success so the player's hand swings immediately and no other
            // interaction handler runs. Actual result arrives via the next server sync.
            return InteractionResult.SUCCESS;
        }

        // ----- Server-side -----
        if (!player.getAbilities().instabuild) offered.shrink(1);

        // Beer always buys a round of calm: clear aggro and start the buzz.
        if (offeredBeer) {
            this.makeTipsy();
            if (hostile) {
                // Spent to de-aggro a fight — the goblin staggers off, no loot this time.
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        ItemStack reward = rollTrade(pool);
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
        this.playSound(SoundEvents.PIGLIN_AMBIENT, 0.8F, 1.3F);
        this.playSound(SoundEvents.ITEM_PICKUP, 0.6F, 1.1F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(),
                    6, 0.25, 0.2, 0.25, 0.02);
            server.broadcastEntityEvent(this, EVENT_TRADE_ACCEPT);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Weighted random pick from a trade pool. Count is uniform within the option's range. */
    private ItemStack rollTrade(List<TradeOption> pool) {
        int totalWeight = 0;
        for (TradeOption opt : pool) totalWeight += opt.weight();
        int pick = this.random.nextInt(totalWeight);
        int cursor = 0;
        for (TradeOption opt : pool) {
            cursor += opt.weight();
            if (pick < cursor) {
                int count = opt.minCount() + this.random.nextInt(opt.maxCount() - opt.minCount() + 1);
                return new ItemStack(opt.item(), count);
            }
        }
        // Unreachable in practice — pick < totalWeight is guaranteed by nextInt(totalWeight).
        return ItemStack.EMPTY;
    }
}
