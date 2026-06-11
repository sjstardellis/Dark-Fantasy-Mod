package net.steve.darkfantasy.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.steve.darkfantasy.init.ModStructureTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Optional;

/**
 * A jigsaw structure that refuses to start over water. Vanilla's jigsaw JSON has no
 * dry-land flag — with {@code project_start_to_heightmap: WORLD_SURFACE_WG} a start
 * point over a pond/river simply floats on the water surface. This wrapper delegates
 * everything to {@link JigsawStructure} (same JSON fields, via the same codec) but
 * first probes a cross of columns around the chunk center: wherever the WORLD_SURFACE
 * heightmap (which counts fluid) sits above the OCEAN_FLOOR heightmap (solid only),
 * there's standing water, and generation is skipped for that chunk.
 */
public class WaterAvoidingJigsawStructure extends Structure {
    public static final MapCodec<WaterAvoidingJigsawStructure> CODEC =
            JigsawStructure.CODEC.xmap(WaterAvoidingJigsawStructure::new, w -> w.jigsaw);

    /** Horizontal reach (blocks) of the dry-land probe around the chunk center. */
    private static final int PROBE_RADIUS = 8;

    private final JigsawStructure jigsaw;

    public WaterAvoidingJigsawStructure(JigsawStructure jigsaw) {
        super(new StructureSettings(jigsaw.biomes(), jigsaw.spawnOverrides(),
                jigsaw.step(), jigsaw.terrainAdaptation()));
        this.jigsaw = jigsaw;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();
        // 5-point cross roughly covering the footprint.
        if (isWaterColumn(context, cx, cz)
                || isWaterColumn(context, cx + PROBE_RADIUS, cz)
                || isWaterColumn(context, cx - PROBE_RADIUS, cz)
                || isWaterColumn(context, cx, cz + PROBE_RADIUS)
                || isWaterColumn(context, cx, cz - PROBE_RADIUS)) {
            return Optional.empty();
        }
        return this.jigsaw.findGenerationPoint(context);
    }

    /** True when fluid sits on top of the solid surface at this column (pond/lake/river/ocean). */
    private static boolean isWaterColumn(GenerationContext context, int x, int z) {
        int surface = context.chunkGenerator().getFirstFreeHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int floor = context.chunkGenerator().getFirstFreeHeight(
                x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        return surface != floor;
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.WATER_AVOIDING_JIGSAW.get();
    }
}
