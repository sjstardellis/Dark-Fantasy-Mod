package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.FairyEntity;
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

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
