package net.steve.darkfantasy.worldgen.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.steve.darkfantasy.DarkFantasy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Splices darkfantasy:cinderbark_forest, gravewood_grove, and ghostwillow_marsh into
 * the overworld's MultiNoise climate map at level load. NeoForge has no first-class
 * data-driven hook for adding *new* biomes to the overworld preset (the JSON biome
 * modifiers only mutate existing biomes), so we mutate the source's parameter list
 * via reflection and invalidate the memoized possibleBiomes cache so /locate biome,
 * structure placement, and spawn target collection see the new entries.
 */
public final class OverworldBiomeInjector {

    public static final ResourceKey<Biome> CINDERBARK_FOREST = key("cinderbark_forest");
    public static final ResourceKey<Biome> GRAVEWOOD_GROVE = key("gravewood_grove");
    public static final ResourceKey<Biome> GHOSTWILLOW_MARSH = key("ghostwillow_marsh");

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME,
                Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, name));
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BiomeSource biomeSource = generator.getBiomeSource();
        if (!(biomeSource instanceof MultiNoiseBiomeSource source)) {
            DarkFantasy.LOGGER.info("Overworld biome source is not MultiNoise (preset={}); skipping injection",
                    biomeSource.getClass().getSimpleName());
            return;
        }

        Registry<Biome> biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        try {
            inject(source, biomes);
            DarkFantasy.LOGGER.info("Injected 3 Dark Fantasy biomes into the overworld parameter list");
        } catch (ReflectiveOperationException e) {
            DarkFantasy.LOGGER.error("Failed to inject Dark Fantasy biomes into overworld", e);
        }
    }

    private static void inject(MultiNoiseBiomeSource source, Registry<Biome> biomes) throws ReflectiveOperationException {
        // Resolve existing parameter list (works whether the source was built from a
        // preset Holder or a direct list).
        Field paramField = MultiNoiseBiomeSource.class.getDeclaredField("parameters");
        paramField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> existing =
                (Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>>) paramField.get(source);
        Climate.ParameterList<Holder<Biome>> current =
                existing.map(direct -> direct, preset -> preset.value().parameters());

        List<Pair<Climate.ParameterPoint, Holder<Biome>>> merged = new ArrayList<>(current.values());

        // Each biome is locked to its temp/humidity climate identity, but the other
        // axes (continentalness/erosion/weirdness) are wide so the biome appears
        // wherever its temp/humidity is hit on inland terrain — comparable to how
        // a plains or forest biome generates.

        // Cinderbark Forest — hot and dry. Any inland erosion band.
        merged.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(0.55F, 1.0F),       // temp: hot
                Climate.Parameter.span(-1.0F, -0.35F),     // humidity: driest
                Climate.Parameter.span(0.03F, 1.0F),       // continentalness: mid-to-far inland
                Climate.Parameter.span(-1.0F, 0.45F),      // erosion: most bands
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-1.0F, 1.0F),       // weirdness: any
                0.0F),
                biomes.getOrThrow(CINDERBARK_FOREST)));

        // Gravewood Grove — cool, slightly dry. Inland, any erosion.
        merged.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(-0.45F, -0.15F),    // temp: cool
                Climate.Parameter.span(-0.35F, 0.1F),      // humidity: dry-to-neutral
                Climate.Parameter.span(-0.11F, 1.0F),      // continentalness: near-to-far inland
                Climate.Parameter.span(-1.0F, 0.45F),      // erosion: most bands
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                0.0F),
                biomes.getOrThrow(GRAVEWOOD_GROVE)));

        // Ghostwillow Marsh — temperate, wet. Near-coast and inland low-elevation.
        merged.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(0.2F, 0.55F),       // temp: temperate
                Climate.Parameter.span(0.3F, 1.0F),        // humidity: wet
                Climate.Parameter.span(-0.19F, 0.55F),     // continentalness: coast-to-mid inland
                Climate.Parameter.span(-1.0F, 0.45F),      // erosion: most bands
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                0.0F),
                biomes.getOrThrow(GHOSTWILLOW_MARSH)));

        Climate.ParameterList<Holder<Biome>> rebuilt = new Climate.ParameterList<>(merged);
        paramField.set(source, Either.left(rebuilt));

        // BiomeSource.possibleBiomes is a Suppliers.memoize() — if anything queried
        // it before us the cache is now stale. Replace it with a fresh memoized
        // supplier built from the rebuilt list so /locate biome, structure starts,
        // and spawn targets all see the new biomes.
        Field possField = BiomeSource.class.getDeclaredField("possibleBiomes");
        possField.setAccessible(true);
        Supplier<Set<Holder<Biome>>> fresh = Suppliers.memoize(
                () -> rebuilt.values().stream().map(Pair::getSecond).distinct()
                        .collect(ImmutableSet.toImmutableSet()));
        possField.set(source, fresh);
    }

    private OverworldBiomeInjector() {}
}
