package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.ElectroDragonEntity;
import net.steve.darkfantasy.entity.custom.FairyEntity;
import net.steve.darkfantasy.entity.custom.GoblinEntity;
import net.steve.darkfantasy.entity.custom.GoblinRockProjectile;
import net.steve.darkfantasy.entity.custom.LightningBoltProjectile;
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

    /** Thrown stone projectile fired by the goblin's ranged attack. */
    public static final DeferredHolder<EntityType<?>, EntityType<GoblinRockProjectile>> GOBLIN_ROCK =
            ENTITY_TYPES.register("goblin_rock",
                    () -> EntityType.Builder.<GoblinRockProjectile>of(GoblinRockProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(GOBLIN_ROCK_KEY));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
