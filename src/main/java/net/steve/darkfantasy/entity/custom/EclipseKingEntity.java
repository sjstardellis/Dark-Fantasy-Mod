package net.steve.darkfantasy.entity.custom;

import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumSet;
import java.util.UUID;

/**
 * The Eclipse King — the mod's celestial capstone boss. A humanoid revenant monarch
 * rendered on the player model ({@link net.steve.darkfantasy.client.renderer.EclipseKingRenderer})
 * and wearing the {@link ModItems#ECLIPSE_CROWN} (which he drops on death).
 *
 * <h2>Fight</h2>
 * Two phases, gated at half health. Each cast is chosen at random from the pool the current
 * phase unlocks, never repeating the same move twice in a row:
 * <ul>
 *   <li><b>Phase 1</b> — melee plus four ranged casts: a {@link WitherSkull} volley, an
 *       {@link EvokerFangs} line that marches at the target, a telegraphed <em>starfall</em> that
 *       detonates on a marked patch of ground, and an {@link EvokerFangs} ring that boxes the
 *       target in.</li>
 *   <li><b>Phase 2 (enraged, &lt; 50% HP)</b> — permanent speed boost, faster casts, and four
 *       more moves unlock: summoning wither-skeleton knights, an <em>eclipse</em> aura that
 *       blinds and darkens nearby players, an <em>umbral step</em> blink onto a kiting target,
 *       and a <em>soul siphon</em> that drains their life to heal himself.</li>
 * </ul>
 * Fire-immune, heavily knockback-resistant, and never despawns. A purple {@link ServerBossEvent}
 * boss bar tracks his health for every nearby player.
 */
public class EclipseKingEntity extends Monster {
    private static final int CAST_INTERVAL_P1 = 70;
    private static final int CAST_INTERVAL_P2 = 45;
    private static final double CAST_RANGE = 28.0;
    private static final UUID ENRAGE_SPEED_ID = UUID.nameUUIDFromBytes("darkfantasy:eclipse_king_enrage".getBytes());
    private static final net.minecraft.resources.Identifier ENRAGE_SPEED_MOD =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("darkfantasy", "eclipse_king_enrage");

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(), this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    /** How long the king holds the casting pose, and when in that window the spell erupts. */
    private static final int CAST_WINDUP = 32;
    private static final int CAST_FIRE_AT = 16;
    /** Synced so the client knows to raise/wave his arms (see EclipseKingRenderer + EclipseKingModel). */
    private static final EntityDataAccessor<Boolean> DATA_CASTING =
            SynchedEntityData.defineId(EclipseKingEntity.class, EntityDataSerializers.BOOLEAN);

    /** Index of Starfall in the spell switch — telegraphed, so the goal special-cases it. */
    private static final int SPELL_STARFALL = 2;

    private int castCooldown = CAST_INTERVAL_P1;
    private int chosenSpell = -1;
    /** Ground spot Starfall will detonate on, locked in when the cast begins so the player can dodge. */
    private Vec3 starfallMark = null;
    private boolean enraged = false;
    private boolean crownEquipped = false;

    public EclipseKingEntity(EntityType<? extends EclipseKingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 60;
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CASTING, false);
    }

    /** True while the king is channelling a spell — the client raises and waves his arms. */
    public boolean isCasting() {
        return this.entityData.get(DATA_CASTING);
    }

    private void setCasting(boolean casting) {
        this.entityData.set(DATA_CASTING, casting);
    }

    /**
     * Equip the crown on the first server tick. Done here rather than in the constructor
     * because equipment set during construction doesn't survive the {@code /summon} (or
     * structure-NBT) spawn path — the entity's data is applied afterwards. The loot table
     * grants the guaranteed drop, so the worn copy itself never drops (chance 0).
     */
    private void ensureCrown() {
        if (this.crownEquipped) return;
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ECLIPSE_CROWN.get()));
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ECLIPSE_GREATSWORD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);   // loot table grants the guaranteed drop
        this.crownEquipped = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0)
                .add(Attributes.ARMOR, 12.0)
                // Low base — the wielded Eclipse Greatsword adds +8, landing his melee near 10.
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.SCALE, 1.13);   // an imposing, larger-than-life monarch
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EclipseCastGoal());   // preempts melee: he stops to cast
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** Block self-wither so a point-blank skull can't curse the king. */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.WITHER && super.canBeAffected(effect);
    }

    // ---- Boss bar visibility ------------------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    // ---- Per-tick boss logic ------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.ensureCrown();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (!this.enraged && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.enrage();
        }
        // Casting (with its wind-up animation) is driven by EclipseCastGoal below.
    }

    /**
     * Stops the king to channel a spell: he holds the casting pose for {@value #CAST_WINDUP}
     * ticks (arms raised + waving, synced via {@link #DATA_CASTING}) and the spell erupts at
     * the apex ({@value #CAST_FIRE_AT} ticks in). Claims MOVE+LOOK so it preempts the melee
     * goal — no more charging while spells leak out of his chest.
     */
    private class EclipseCastGoal extends Goal {
        private int windup;

        EclipseCastGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (castCooldown > 0) {
                castCooldown--;
                return false;
            }
            LivingEntity t = getTarget();
            return t != null && t.isAlive()
                    && distanceToSqr(t) <= CAST_RANGE * CAST_RANGE
                    && getSensing().hasLineOfSight(t);
        }

        @Override
        public boolean canContinueToUse() {
            return this.windup > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.windup = CAST_WINDUP;
            // Pick at random from the pool the current phase unlocks — 4 moves normally, all 8
            // once enraged. Reroll an immediate repeat so the same spell never fires back-to-back.
            int pool = enraged ? 8 : 4;
            int pick = EclipseKingEntity.this.random.nextInt(pool);
            if (pick == chosenSpell) pick = (pick + 1) % pool;
            chosenSpell = pick;
            setCasting(true);
            getNavigation().stop();
            playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 2.0F, 0.7F);
            // Starfall is telegraphed: lock the impact spot on the target's feet right now so the
            // player has the whole wind-up to step off it before it detonates at the apex.
            LivingEntity t = getTarget();
            starfallMark = (chosenSpell == SPELL_STARFALL && t != null) ? t.position() : null;
        }

        @Override
        public void stop() {
            setCasting(false);
            starfallMark = null;
            castCooldown = enraged ? CAST_INTERVAL_P2 : CAST_INTERVAL_P1;
        }

        @Override
        public void tick() {
            getNavigation().stop();
            LivingEntity t = getTarget();
            if (t != null) {
                getLookControl().setLookAt(t, 30.0F, 30.0F);
            }
            // Keep the Starfall warning ring lit on the marked spot for the whole wind-up.
            if (chosenSpell == SPELL_STARFALL && starfallMark != null && this.windup > CAST_FIRE_AT
                    && this.windup % 2 == 0 && level() instanceof ServerLevel server) {
                markStarfall(server, starfallMark);
            }
            if (this.windup == CAST_FIRE_AT && t != null && level() instanceof ServerLevel server) {
                fireSpell(server, t, chosenSpell);
            }
            this.windup--;
        }
    }

    private void enrage() {
        this.enraged = true;
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(ENRAGE_SPEED_MOD)) {
            speed.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    ENRAGE_SPEED_MOD, 0.35,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 0.7F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 1.5, this.getZ(), 40, 0.5, 1.0, 0.5, 0.1);
        }
    }

    /** Fire the chosen spell at the apex of the cast animation. */
    private void fireSpell(ServerLevel server, LivingEntity target, int index) {
        switch (index) {
            case 0 -> this.witherSkullVolley(server, target);
            case 1 -> this.fangLine(server, target);
            case SPELL_STARFALL -> this.starfall(server, target);
            case 3 -> this.fangRing(server, target);
            case 4 -> this.summonKnights(server, target);
            case 5 -> this.eclipseAura(server);
            case 6 -> this.teleportStrike(server, target);
            default -> this.soulSiphon(server, target);
        }
    }

    // ---- Spells -------------------------------------------------------------

    private void witherSkullVolley(ServerLevel server, LivingEntity target) {
        this.playSound(SoundEvents.WITHER_SHOOT, 2.0F, 0.9F);
        Vec3 from = this.position().add(0, this.getBbHeight() * 0.8, 0);
        for (int i = -1; i <= 1; i++) {
            Vec3 to = target.position().add(i * 1.5, target.getBbHeight() * 0.5, 0);
            WitherSkull skull = new WitherSkull(server, this, to.subtract(from).normalize());
            skull.setOwner(this);
            skull.setPos(from.x, from.y, from.z);
            server.addFreshEntity(skull);
        }
    }

    private void fangLine(ServerLevel server, LivingEntity target) {
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 2.0F, 0.8F);
        Vec3 dir = target.position().subtract(this.position());
        double len = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        if (len < 1.0E-4) return;
        double dx = dir.x / len, dz = dir.z / len;
        float angle = (float) Mth.atan2(dz, dx);
        for (int i = 0; i < 14; i++) {
            double reach = 1.4 * (i + 1);
            spawnFang(server, this.getX() + dx * reach, this.getZ() + dz * reach,
                    this.getY() - 3.0, this.getY() + 2.0, angle, i);
        }
    }

    private void spawnFang(ServerLevel level, double x, double z, double minY, double maxY, float angle, int warmup) {
        BlockPos pos = BlockPos.containing(x, maxY, z);
        boolean found = false;
        double top = 0.0;
        do {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.isFaceSturdy(level, below, Direction.UP)) {
                if (!level.isEmptyBlock(pos)) {
                    VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
                    if (!shape.isEmpty()) top = shape.max(Direction.Axis.Y);
                }
                found = true;
                break;
            }
            pos = pos.below();
        } while (pos.getY() >= Mth.floor(minY) - 1);
        if (found) {
            level.addFreshEntity(new EvokerFangs(level, x, pos.getY() + top, z, angle, warmup, this));
        }
    }

    private void summonKnights(ServerLevel server, LivingEntity target) {
        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.8F);
        for (int i = 0; i < 3; i++) {
            BlockPos pos = this.blockPosition().offset(
                    this.random.nextInt(5) - 2, 0, this.random.nextInt(5) - 2);
            Mob knight = EntityType.WITHER_SKELETON.spawn(server, pos, EntitySpawnReason.MOB_SUMMONED);
            if (knight != null) {
                knight.setTarget(target);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                        knight.getX(), knight.getY() + 1.0, knight.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }

    private void eclipseAura(ServerLevel server) {
        this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0F, 0.6F);
        for (Player player : server.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(14.0))) {
            if (player.isCreative() || player.isSpectator()) continue;
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SQUID_INK,
                this.getX(), this.getY() + 2.0, this.getZ(), 60, 4.0, 2.0, 4.0, 0.0);
    }

    /** Radius of the Starfall impact and its warning ring. */
    private static final double STARFALL_RADIUS = 2.6;

    /**
     * Paints the Starfall warning ring on the ground — a circle of soul-fire the player must step
     * out of. Re-emitted every couple of ticks through the wind-up by {@link EclipseCastGoal}.
     */
    private void markStarfall(ServerLevel server, Vec3 c) {
        int n = 14;
        for (int i = 0; i < n; i++) {
            double a = (Math.PI * 2.0 * i) / n;
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    c.x + Math.cos(a) * STARFALL_RADIUS, c.y + 0.15, c.z + Math.sin(a) * STARFALL_RADIUS,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                c.x, c.y + 0.15, c.z, 3, 0.3, 0.0, 0.3, 0.0);
    }

    /**
     * Starfall — a column of dark fire crashes down on the spot marked when the cast began
     * (see {@link EclipseKingEntity#starfallMark}). Anyone still inside the warning ring is struck
     * and set ablaze; step off the soul-fire to avoid it entirely. Reliable in any arena — a
     * direct ground detonation, no falling projectile to snag on a ceiling.
     */
    private void starfall(ServerLevel server, LivingEntity target) {
        Vec3 c = (this.starfallMark != null) ? this.starfallMark : target.position();
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.7F);
        this.playSound(SoundEvents.FIRECHARGE_USE, 2.0F, 0.6F);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                c.x, c.y + 0.3, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                c.x, c.y + 0.2, c.z, 24, STARFALL_RADIUS * 0.5, 0.1, STARFALL_RADIUS * 0.5, 0.0);
        for (int y = 0; y < 8; y++) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    c.x, c.y + y, c.z, 8, 0.45, 0.25, 0.45, 0.02);
        }

        AABB area = new AABB(c.x - STARFALL_RADIUS, c.y - 2.0, c.z - STARFALL_RADIUS,
                c.x + STARFALL_RADIUS, c.y + 3.0, c.z + STARFALL_RADIUS);
        DamageSource src = server.damageSources().mobAttack(this);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e.isAlive())) {
            double dx = victim.getX() - c.x, dz = victim.getZ() - c.z;
            if (dx * dx + dz * dz <= STARFALL_RADIUS * STARFALL_RADIUS) {
                victim.hurt(src, 8.0F);
                victim.igniteForSeconds(4.0F);
            }
        }
    }

    /**
     * Umbral Ring — evoker fangs erupt in a full circle around the target, punishing whichever
     * way they bolt. The companion to {@link #fangLine}: that one marches at you, this one boxes
     * you in.
     */
    private void fangRing(ServerLevel server, LivingEntity target) {
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 2.0F, 0.9F);
        Vec3 c = target.position();
        int n = 10;
        for (int i = 0; i < n; i++) {
            double a = (Math.PI * 2.0 * i) / n;
            spawnFang(server, c.x + Math.cos(a) * 3.0, c.z + Math.sin(a) * 3.0,
                    c.y - 3.0, c.y + 2.0, (float) a, i % 3);
        }
    }

    /**
     * Umbral Step — the king dissolves and reforms right beside the target, trailing soul ash at
     * both ends and cursing them with Darkness. Breaks the "stand and cast" rhythm and keeps the
     * pressure on a kiting player. Phase 2 only.
     */
    private void teleportStrike(ServerLevel server, LivingEntity target) {
        Vec3 from = this.position();
        Vec3 t = target.position();
        double ang = this.random.nextDouble() * Math.PI * 2.0;
        double tx = t.x + Math.cos(ang) * 2.5;
        double tz = t.z + Math.sin(ang) * 2.5;
        if (this.randomTeleport(tx, t.y, tz, true)) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
                    from.x, from.y + 1.0, from.z, 24, 0.3, 0.6, 0.3, 0.05);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 1.0, this.getZ(), 24, 0.3, 0.6, 0.3, 0.05);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.2F, 0.7F);
            this.getLookControl().setLookAt(target, 60.0F, 60.0F);
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
        }
    }

    /**
     * Soul Siphon — a tether of souls tears life from the target and pours it into the king,
     * healing him and leaving a Wither curse behind. Turns a drawn-out fight into a DPS race.
     * Phase 2 only.
     */
    private void soulSiphon(ServerLevel server, LivingEntity target) {
        this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.6F, 0.9F);
        target.hurt(server.damageSources().mobAttack(this), 6.0F);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
        this.heal(8.0F);
        Vec3 a = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 b = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
        for (int i = 0; i <= 12; i++) {
            double f = i / 12.0;
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                    a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f, a.z + (b.z - a.z) * f,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ---- Sounds -------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}
