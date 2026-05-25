package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.worldgen.dimension.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;

/**
 * Pins the Twilight Forest's clock at perpetual midnight (tick 18000) so the moon is
 * always overhead and the dim purple sky from the dimension type renders correctly.
 * Uses a dimension-specific {@link WorldClock} so pausing only affects the Twilight
 * Forest — the overworld and any other dimensions keep their normal day/night cycle.
 *
 * <p><b>Why not just {@code has_fixed_time: true} in the dimension type JSON?</b>
 * That field only short-circuits {@link Level#isBrightOutside()}/{@code isDarkOutside}
 * for mob-spawn light predicates. It does <em>not</em> stop the WorldClock from
 * advancing — the sun/moon still moves visually unless the clock itself is paused.
 *
 * <p><b>Why {@link ServerStartedEvent}, not {@code ServerStartingEvent}?</b>
 * The {@link ServerClockManager} initializes during server start; calling
 * {@code setPaused} before that init has completed throws
 * {@code IllegalStateException("No clock initialized")} and silently breaks the
 * perpetual-midnight setup. {@code ServerStartedEvent} fires after init is done.
 *
 * <p>{@link LevelEvent.Load} provides a belt-and-suspenders re-pin whenever the
 * Twilight Forest dimension first comes online, in case the startup pause was lost
 * (e.g., admin used {@code /neoforge day} or the clock was unpaused mid-game).
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
    public static void onServerStarted(ServerStartedEvent event) {
        pinClock(event.getServer());
    }

    /**
     * Re-pin whenever the Twilight Forest dimension itself loads. Cheap (one registry
     * lookup, two field writes) and guarantees the clock state is correct even if
     * something unpaused it earlier.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (server.dimension() != ModDimensions.TWILIGHT_FOREST) return;
        pinClock(server.getServer());
    }

    private static void pinClock(MinecraftServer server) {
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
        DarkFantasy.LOGGER.info("Pinned Twilight Forest clock to midnight (tick {}) and paused.", FROZEN_TICK);
    }
}
