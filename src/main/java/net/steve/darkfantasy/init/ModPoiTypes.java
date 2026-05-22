package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

/**
 * Registers a POI type that tracks every twilight-portal block state. NeoForge wires the
 * block-state-to-POI map automatically for modded POI types, so once this is registered
 * we can call {@code level.getPoiManager().getInSquare(...)} to find existing twilight
 * portals — the same lookup vanilla uses for the nether portal.
 */
public class ModPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, DarkFantasy.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> TWILIGHT_PORTAL = POI_TYPES.register(
            "twilight_portal",
            () -> new PoiType(
                    Set.copyOf(ModBlocks.TWILIGHT_PORTAL.get().getStateDefinition().getPossibleStates()),
                    0,
                    1));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}
