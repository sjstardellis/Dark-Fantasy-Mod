package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.Optional;

/**
 * Pins the Twilight Forest's clock at perpetual midnight (tick 18000) so the moon is
 * always overhead and the dim purple sky/fog from the dimension type render correctly.
 * Uses a dimension-specific {@link WorldClock} so pausing only affects the Twilight Forest
 * — the overworld and any other dimensions keep their normal day/night cycle.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public final class TwilightAtmosphereHandler {
    private TwilightAtmosphereHandler() {}

    public static final ResourceKey<WorldClock> TWILIGHT_FOREST_CLOCK = ResourceKey.create(
            Registries.WORLD_CLOCK,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "twilight_forest"));

    /** Tick value to freeze at — 18000 is midnight in vanilla time terms. */
    private static final long FROZEN_TICK = 18000L;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        HolderLookup.RegistryLookup<WorldClock> clocks =
                server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK);
        Optional<Holder.Reference<WorldClock>> holder = clocks.get(TWILIGHT_FOREST_CLOCK);
        if (holder.isEmpty()) {
            DarkFantasy.LOGGER.warn("Twilight Forest clock not found; perpetual-midnight setup skipped.");
            return;
        }

        ServerClockManager mgr = server.clockManager();
        mgr.setTotalTicks(holder.get(), FROZEN_TICK);
        mgr.setPaused(holder.get(), true);
    }
}
