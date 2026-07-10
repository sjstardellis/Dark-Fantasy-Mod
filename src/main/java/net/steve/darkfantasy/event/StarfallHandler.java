package net.steve.darkfantasy.event;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.FallenStarEntity;
import net.steve.darkfantasy.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Starfall — on some Overworld nights a star is knocked loose from the veiled sky and
 * streaks down near a player. The {@link FallenStarEntity} scorches a small crater and
 * scatters Fallen Star reagents (alchemy-stand fuel for the Starlight Elixir).
 *
 * <p>Cadence: one roll every {@link #ROLL_INTERVAL_TICKS} while it's night, at
 * {@link #CHANCE_PER_ROLL} — roughly one fall every three or four nights per world.
 * The star spawns high and off to one side of a random player, angled to land
 * 60–140 blocks away: close enough to chase, far enough to be a journey.
 */
@EventBusSubscriber(modid = DarkFantasy.MOD_ID)
public final class StarfallHandler {
    private static final int ROLL_INTERVAL_TICKS = 600;      // every 30 s of night
    private static final float CHANCE_PER_ROLL = 1.0F / 70.0F;
    private static final double MIN_LAND_DISTANCE = 60.0;
    private static final double MAX_LAND_DISTANCE = 140.0;
    private static final double SPAWN_HEIGHT = 60.0;         // above the landing point
    private static final double NOTIFY_RANGE = 260.0;

    private StarfallHandler() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (server.dimension() != Level.OVERWORLD) return;
        if (server.getGameTime() % ROLL_INTERVAL_TICKS != 0) return;
        if (!server.isDarkOutside()) return;

        RandomSource random = server.getRandom();
        if (random.nextFloat() >= CHANCE_PER_ROLL) return;

        List<ServerPlayer> players = server.players();
        if (players.isEmpty()) return;
        ServerPlayer chosen = players.get(random.nextInt(players.size()));

        double angle = random.nextDouble() * Math.TAU;
        double dist = MIN_LAND_DISTANCE + random.nextDouble() * (MAX_LAND_DISTANCE - MIN_LAND_DISTANCE);
        double lx = chosen.getX() + Math.cos(angle) * dist;
        double lz = chosen.getZ() + Math.sin(angle) * dist;
        double ly = server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                (int) lx, (int) lz);

        // spawn offset laterally so the streak crosses the sky rather than dropping straight
        double ox = lx - Math.cos(angle) * 40.0;
        double oz = lz - Math.sin(angle) * 40.0;
        Vec3 spawn = new Vec3(ox, ly + SPAWN_HEIGHT, oz);
        Vec3 toward = new Vec3(lx - ox, ly - spawn.y, lz - oz).normalize();

        FallenStarEntity star = new FallenStarEntity(ModEntities.FALLEN_STAR.get(), server);
        star.setPos(spawn.x, spawn.y, spawn.z);
        star.setDeltaMovement(toward.scale(1.6));
        server.addFreshEntity(star);

        Component message = Component.translatable("message.darkfantasy.starfall")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
        for (ServerPlayer player : players) {
            if (player.distanceToSqr(lx, ly, lz) <= NOTIFY_RANGE * NOTIFY_RANGE) {
                player.sendOverlayMessage(message);
            }
        }
    }
}
