package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.EclipseKingEntity;
import net.steve.darkfantasy.entity.custom.ElectroDragonEntity;
import net.steve.darkfantasy.entity.custom.FairyEntity;
import net.steve.darkfantasy.entity.custom.FrostBoltProjectile;
import net.steve.darkfantasy.entity.custom.GnomeEntity;
import net.steve.darkfantasy.entity.custom.GoblinEntity;
import net.steve.darkfantasy.entity.custom.GoblinRockProjectile;
import net.steve.darkfantasy.entity.custom.LightningBoltProjectile;
import net.steve.darkfantasy.entity.custom.LytebugEntity;
import net.steve.darkfantasy.entity.custom.WizardEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, DarkFantasy.MOD_ID);

    private static final ResourceKey<EntityType<?>> FAIRY_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "fairy"));
    private static final ResourceKey<EntityType<?>> WIZARD_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "wizard"));
    private static final ResourceKey<EntityType<?>> LIGHTNING_PROJECTILE_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "lightning_projectile"));
    private static final ResourceKey<EntityType<?>> ELECTRO_DRAGON_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "electro_dragon"));
    private static final ResourceKey<EntityType<?>> GOBLIN_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "goblin"));
    private static final ResourceKey<EntityType<?>> GOBLIN_ROCK_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "goblin_rock"));
    private static final ResourceKey<EntityType<?>> FROST_BOLT_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "frost_bolt"));
    private static final ResourceKey<EntityType<?>> GNOME_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "gnome"));
    private static final ResourceKey<EntityType<?>> LYTEBUG_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "lytebug"));
    private static final ResourceKey<EntityType<?>> ECLIPSE_KING_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "eclipse_king"));

    public static final DeferredHolder<EntityType<?>, EntityType<FairyEntity>> FAIRY =
            ENTITY_TYPES.register("fairy",
                    () -> EntityType.Builder.<FairyEntity>of(FairyEntity::new, MobCategory.MONSTER)
                            .sized(0.35F, 0.6F)
                            .eyeHeight(0.36F)
                            .clientTrackingRange(8)
                            .build(FAIRY_KEY));

    public static final DeferredHolder<EntityType<?>, EntityType<WizardEntity>> WIZARD =
            ENTITY_TYPES.register("wizard",
                    () -> EntityType.Builder.<WizardEntity>of(WizardEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build(WIZARD_KEY));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningBoltProjectile>> LIGHTNING_PROJECTILE =
            ENTITY_TYPES.register("lightning_projectile",
                    () -> EntityType.Builder.<LightningBoltProjectile>of(LightningBoltProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(LIGHTNING_PROJECTILE_KEY));

    /**
     * Base dimensions wrap the dragon's body (not wings/neck/tail) at vanilla scale —
     * the vanilla 16x8 box generously includes the full wingspan. Final hitbox is
     * base × Attributes.SCALE; with SCALE = 0.5 that's a 2x1 bounding box on the body.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<ElectroDragonEntity>> ELECTRO_DRAGON =
            ENTITY_TYPES.register("electro_dragon",
                    () -> EntityType.Builder.<ElectroDragonEntity>of(ElectroDragonEntity::new, MobCategory.MONSTER)
                            .sized(4.0F, 2.0F)
                            .clientTrackingRange(10)
                            .build(ELECTRO_DRAGON_KEY));

    /**
     * Goblin — small humanoid melee+ranged mob; reuses the copper-golem visual via
     * {@link net.steve.darkfantasy.client.renderer.GoblinRenderer}. Bbox is roughly
     * adult-villager-shaped pre-scale; the SCALE attribute (0.7) shrinks it in-world.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<GoblinEntity>> GOBLIN =
            ENTITY_TYPES.register("goblin",
                    () -> EntityType.Builder.<GoblinEntity>of(GoblinEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.2F)
                            .clientTrackingRange(8)
                            .build(GOBLIN_KEY));

    /**
     * Eclipse King — the celestial capstone boss. Rendered on the player model
     * ({@link net.steve.darkfantasy.client.renderer.EclipseKingRenderer}); the SCALE
     * attribute (1.2) makes him larger than a player. Tracked at a generous range so the
     * boss bar appears from afar.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<EclipseKingEntity>> ECLIPSE_KING =
            ENTITY_TYPES.register("eclipse_king",
                    () -> EntityType.Builder.<EclipseKingEntity>of(EclipseKingEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(16)
                            .fireImmune()
                            .build(ECLIPSE_KING_KEY));

    /**
     * Gnome — tiny splash-potion-throwing menace that uses the witch model at 5%
     * scale. Neutral toward players wearing any leather armor; hostile to everyone
     * else. Render: {@link net.steve.darkfantasy.client.renderer.GnomeRenderer}.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<GnomeEntity>> GNOME =
            ENTITY_TYPES.register("gnome",
                    () -> EntityType.Builder.<GnomeEntity>of(GnomeEntity::new, MobCategory.MONSTER)
                            // Tightened hitbox: was (0.6, 1.95) × 0.5 SCALE = 0.3 × 0.975.
                            // Now (0.5, 0.65) × 0.5 SCALE = 0.25 × 0.325 — XZ slightly
                            // narrower (0.3 → 0.25), Y is ~1/3 of its old value
                            // (0.975 → 0.325). Note: the witch model still renders at
                            // its full half-height, so the visible mesh extends well
                            // above the collision/hit box — deliberate.
                            .sized(0.5F, 0.65F)
                            .clientTrackingRange(8)
                            .build(GNOME_KEY));

    /**
     * Lytebug — passive ambient firefly that drifts around the Twilight Forest with
     * a cosmetic emissive glow. Uses the vanilla bee model retextured via
     * {@link net.steve.darkfantasy.client.renderer.LytebugRenderer}; SCALE attribute
     * (0.6) shrinks it well below bee size. {@link net.minecraft.world.entity.MobCategory#CREATURE}
     * so it shares the passive spawn cap, not the monster cap.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<LytebugEntity>> LYTEBUG =
            ENTITY_TYPES.register("lytebug",
                    () -> EntityType.Builder.<LytebugEntity>of(LytebugEntity::new, MobCategory.CREATURE)
                            .sized(0.4F, 0.3F)
                            .clientTrackingRange(8)
                            .build(LYTEBUG_KEY));

    /** Thrown stone projectile fired by the goblin's ranged attack. */
    public static final DeferredHolder<EntityType<?>, EntityType<GoblinRockProjectile>> GOBLIN_ROCK =
            ENTITY_TYPES.register("goblin_rock",
                    () -> EntityType.Builder.<GoblinRockProjectile>of(GoblinRockProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(GOBLIN_ROCK_KEY));

    /** Frost bolt fired by the Frost Staff — slows + freezes on hit. */
    public static final DeferredHolder<EntityType<?>, EntityType<FrostBoltProjectile>> FROST_BOLT =
            ENTITY_TYPES.register("frost_bolt",
                    () -> EntityType.Builder.<FrostBoltProjectile>of(FrostBoltProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(FROST_BOLT_KEY));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
