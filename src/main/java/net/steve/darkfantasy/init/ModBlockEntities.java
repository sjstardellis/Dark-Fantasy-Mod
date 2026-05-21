package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.block.entity.AlchemyStandBlockEntity;
import net.steve.darkfantasy.block.entity.SkylandsPortalBlockEntity;
import net.steve.darkfantasy.block.entity.TwilightPortalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DarkFantasy.MOD_ID);

    public static final Supplier<BlockEntityType<AlchemyStandBlockEntity>> ALCHEMY_STAND_BE =
            BLOCK_ENTITIES.register("alchemy_stand_be",
                    () -> new BlockEntityType<>(AlchemyStandBlockEntity::new,
                            ModBlocks.ALCHEMY_STAND.get()));

    public static final Supplier<BlockEntityType<SkylandsPortalBlockEntity>> SKYLANDS_PORTAL_BE =
            BLOCK_ENTITIES.register("skylands_portal_be",
                    () -> new BlockEntityType<>(SkylandsPortalBlockEntity::new,
                            ModBlocks.SKYLANDS_PORTAL.get()));

    public static final Supplier<BlockEntityType<TwilightPortalBlockEntity>> TWILIGHT_PORTAL_BE =
            BLOCK_ENTITIES.register("twilight_portal_be",
                    () -> new BlockEntityType<>(TwilightPortalBlockEntity::new,
                            ModBlocks.TWILIGHT_PORTAL.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
