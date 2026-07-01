package net.steve.darkfantasy.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.steve.darkfantasy.init.ModStructureTypes;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Optional;

/**
 * A jigsaw structure that refuses to start over water or on steep terrain. Vanilla's
 * jigsaw JSON has no dry-land flag — with {@code project_start_to_heightmap:
 * WORLD_SURFACE_WG} a start point over a pond/river simply floats on the water
 * surface, and one on a mountainside half-buries the build. This wrapper delegates
 * everything to {@link JigsawStructure} (same JSON fields, via the same codec) but
 * first probes a cross of columns around the chunk center and skips the chunk if any
 * probed column has fluid as its top block or the surface varies too much.
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

    /**
     * Max height difference allowed across the probe cross. Anything steeper is a
     * hillside/cliff where a heightmap-projected start would half-bury the build.
     * Kept generous because the structure's {@code beard_thin} terrain adaptation
     * carves a foundation — too tight a value makes it near-unspawnable in the
     * mountain biomes this structure is restricted to.
     */
    private static final int MAX_SURFACE_SPREAD = 10;

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int cx = context.chunkPos().getMiddleBlockX();
        int cz = context.chunkPos().getMiddleBlockZ();
        // 5-point cross roughly covering the footprint: every column must be dry,
        // and the surface must be close to level across the cross.
        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        for (int[] p : new int[][]{{cx, cz}, {cx + PROBE_RADIUS, cz}, {cx - PROBE_RADIUS, cz},
                                   {cx, cz + PROBE_RADIUS}, {cx, cz - PROBE_RADIUS}}) {
            int surface = context.chunkGenerator().getFirstFreeHeight(
                    p[0], p[1], Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            if (surface <= context.heightAccessor().getMinY()) {
                return Optional.empty(); // degenerate column
            }
            // Read the predicted block directly under the surface from the noise column:
            // comparing WORLD_SURFACE_WG to OCEAN_FLOOR_WG heightmaps misreports on 26.1,
            // but the column itself reliably says whether the top block is a fluid.
            NoiseColumn column = context.chunkGenerator().getBaseColumn(
                    p[0], p[1], context.heightAccessor(), context.randomState());
            if (!column.getBlock(surface - 1).getFluidState().isEmpty()) {
                return Optional.empty(); // standing water (pond/lake/river/ocean)
            }
            minSurface = Math.min(minSurface, surface);
            maxSurface = Math.max(maxSurface, surface);
        }
        if (maxSurface - minSurface > MAX_SURFACE_SPREAD) {
            return Optional.empty(); // too steep — let this region go without
        }
        return this.jigsaw.findGenerationPoint(context);
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.WATER_AVOIDING_JIGSAW.get();
    }
}
