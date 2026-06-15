package net.steve.darkfantasy.worldgen.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.steve.darkfantasy.DarkFantasy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Splices darkfantasy:cinderbark_forest, gravewood_grove, and ghostwillow_marsh into
 * the overworld's MultiNoise climate map at level load. NeoForge has no first-class
 * data-driven hook for adding *new* biomes to the overworld preset (the JSON biome
 * modifiers only mutate existing biomes), so we mutate the source's parameter list
 * via reflection and invalidate the memoized possibleBiomes cache so /locate biome,
 * structure placement, and spawn target collection see the new entries.
 *
 * <p><b>Save-size note (critical):</b> a {@link MultiNoiseBiomeSource} backed by the
 * overworld preset stores {@code Either.right(Holder<…ParameterList>)} and serializes
 * to just {@code {"preset":"minecraft:overworld"}}. If we instead overwrite its field
 * with {@code Either.left(expandedList)}, the codec writes the whole ~120-entry climate
 * list inline; persisted into {@code world_gen_settings.dat} that balloons past the
 * 2 MiB {@code NbtAccounter} read quota and the world can never be reopened. So in the
 * preset case we mutate the preset's parameter list <em>in place</em> and leave the
 * source's {@code Either.right} untouched — runtime sees the new biomes, but the save
 * still writes only the preset key.
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
            refreshStructureState(level, source);
            DarkFantasy.LOGGER.info("Injected 3 Dark Fantasy biomes + refreshed structure placement state");
        } catch (ReflectiveOperationException e) {
            DarkFantasy.LOGGER.error("Failed to inject Dark Fantasy biomes into overworld", e);
        }
    }

    /**
     * {@link ChunkGeneratorStructureState} is built during ServerLevel construction — BEFORE
     * this Load handler runs — and at that moment it permanently filters out every structure
     * set whose biomes aren't in {@code possibleBiomes()} (see {@code hasBiomesForStructureSet}).
     * Our injected biomes weren't known yet, so structures gated solely to them (ring_ruins,
     * crypt) were dropped and would never generate <em>anywhere</em>. Now that
     * {@link #inject} has added them to {@code possibleBiomes()}, re-run that same filter and
     * swap in the refreshed list, clearing the cached placements so they recompute.
     */
    private static void refreshStructureState(ServerLevel level, MultiNoiseBiomeSource source)
            throws ReflectiveOperationException {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        HolderLookup<StructureSet> structureSets = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        Set<Holder<Biome>> possible = source.possibleBiomes();

        List<Holder<StructureSet>> rebuilt = structureSets.listElements()
                .filter(set -> hasBiomesForStructureSet(set.value(), possible))
                .collect(Collectors.toUnmodifiableList());

        Field setsField = ChunkGeneratorStructureState.class.getDeclaredField("possibleStructureSets");
        setsField.setAccessible(true);
        setsField.set(state, rebuilt);

        // Discard any positions/placements computed from the pre-injection set so they
        // regenerate against the refreshed list on first use.
        Field generatedField = ChunkGeneratorStructureState.class.getDeclaredField("hasGeneratedPositions");
        generatedField.setAccessible(true);
        generatedField.setBoolean(state, false);
        Field placementsField = ChunkGeneratorStructureState.class.getDeclaredField("placementsForStructure");
        placementsField.setAccessible(true);
        ((Map<?, ?>) placementsField.get(state)).clear();

        DarkFantasy.LOGGER.info("Refreshed structure placement: {} structure sets now eligible", rebuilt.size());
    }

    /** Mirrors {@code ChunkGeneratorStructureState.hasBiomesForStructureSet} against a given biome set. */
    private static boolean hasBiomesForStructureSet(StructureSet structureSet, Set<Holder<Biome>> possibleBiomes) {
        return structureSet.structures().stream()
                .flatMap(entry -> entry.structure().value().biomes().stream())
                .anyMatch(possibleBiomes::contains);
    }

    private static void inject(MultiNoiseBiomeSource source, Registry<Biome> biomes) throws ReflectiveOperationException {
        Field paramField = MultiNoiseBiomeSource.class.getDeclaredField("parameters");
        paramField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> existing =
                (Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>>) paramField.get(source);

        // The parameter list we'll publish to possibleBiomes once (de)injection is done.
        Climate.ParameterList<Holder<Biome>> effective;

        Optional<Holder<MultiNoiseBiomeSourceParameterList>> presetHolder = existing.right();
        if (presetHolder.isPresent()) {
            // NORMAL CASE — mutate the preset's list in place, keep source's Either.right
            // so the save stays a one-line preset reference (see class javadoc).
            MultiNoiseBiomeSourceParameterList presetList = presetHolder.get().value();
            Field plField = MultiNoiseBiomeSourceParameterList.class.getDeclaredField("parameters");
            plField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Climate.ParameterList<Holder<Biome>> current =
                    (Climate.ParameterList<Holder<Biome>>) plField.get(presetList);
            // The preset object is a shared registry value; a previous overworld load in
            // this same JVM may have already mutated it. Stay idempotent.
            if (containsInjected(current)) {
                effective = current;
            } else {
                effective = withDarkFantasyBiomes(current, biomes);
                plField.set(presetList, effective);
            }
            // Deliberately NOT writing paramField here — that's what keeps the save small.
        } else {
            // EDGE CASE — source built from a direct (inline) list. It already serializes
            // inline in vanilla, so appending our entries introduces no *new* bloat.
            Climate.ParameterList<Holder<Biome>> current = existing.left().orElseThrow();
            effective = containsInjected(current) ? current : withDarkFantasyBiomes(current, biomes);
            paramField.set(source, Either.left(effective));
        }

        refreshPossibleBiomes(source, effective);
    }

    /** True once our biomes are present, so re-running injection on the shared preset is a no-op. */
    private static boolean containsInjected(Climate.ParameterList<Holder<Biome>> list) {
        return list.values().stream().anyMatch(pair -> pair.getSecond().is(GRAVEWOOD_GROVE));
    }

    /** Returns a new parameter list = {@code current} plus the three Dark Fantasy biomes. */
    private static Climate.ParameterList<Holder<Biome>> withDarkFantasyBiomes(
            Climate.ParameterList<Holder<Biome>> current, Registry<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> merged = new ArrayList<>(current.values());

        // Each biome is locked to its temp/humidity climate identity. Cinderbark keeps
        // wide continentalness/erosion/weirdness (common, plains-like); gravewood and
        // ghostwillow narrow those axes so they're rare (roughly badlands-frequency).

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

        // Gravewood Grove — cool, slightly dry. Deliberately RARE (badlands-like): the
        // temp/humidity identity is kept, but continentalness/erosion/weirdness are all
        // narrowed so it only wins a small slice of inland terrain.
        merged.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(-0.45F, -0.15F),    // temp: cool
                Climate.Parameter.span(-0.35F, 0.1F),      // humidity: dry-to-neutral
                Climate.Parameter.span(0.55F, 1.0F),       // continentalness: far inland only (rarer)
                Climate.Parameter.span(-0.375F, 0.2F),     // erosion: narrow band
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(0.15F, 0.55F),      // weirdness: narrow positive band
                0.0F),
                biomes.getOrThrow(GRAVEWOOD_GROVE)));

        // Ghostwillow Marsh — temperate, wet. Also deliberately RARE (badlands-like).
        // A distinct (negative) weirdness band from the gravewood grove so the two don't
        // compete for the same terrain.
        merged.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(0.2F, 0.55F),       // temp: temperate
                Climate.Parameter.span(0.3F, 1.0F),        // humidity: wet
                Climate.Parameter.span(-0.05F, 0.2F),      // continentalness: thin coastal band (rarer)
                Climate.Parameter.span(-0.375F, 0.2F),     // erosion: narrow band
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-0.55F, -0.15F),    // weirdness: narrow negative band
                0.0F),
                biomes.getOrThrow(GHOSTWILLOW_MARSH)));

        return new Climate.ParameterList<>(merged);
    }

    /**
     * BiomeSource.possibleBiomes is a Suppliers.memoize() — if anything queried it before
     * us (structure-state build does) the cache is stale. Replace it with a fresh memoized
     * supplier from {@code list} so /locate biome, structure starts, and spawn targets all
     * see the new biomes.
     */
    private static void refreshPossibleBiomes(MultiNoiseBiomeSource source,
            Climate.ParameterList<Holder<Biome>> list) throws ReflectiveOperationException {
        Field possField = BiomeSource.class.getDeclaredField("possibleBiomes");
        possField.setAccessible(true);
        Supplier<Set<Holder<Biome>>> fresh = Suppliers.memoize(
                () -> list.values().stream().map(Pair::getSecond).distinct()
                        .collect(ImmutableSet.toImmutableSet()));
        possField.set(source, fresh);
    }

    private OverworldBiomeInjector() {}
}
