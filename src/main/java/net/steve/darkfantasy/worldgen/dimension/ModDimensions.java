package net.steve.darkfantasy.worldgen.dimension;

import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Resource keys identifying the Skylands dimension. The actual dimension is registered
 * via data JSONs at {@code data/darkfantasy/dimension/skylands.json} and
 * {@code data/darkfantasy/dimension_type/skylands.json}.
 */
public class ModDimensions {
    public static final ResourceKey<Level> SKYLANDS = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "skylands"));

    public static final ResourceKey<DimensionType> SKYLANDS_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "skylands"));

    public static final ResourceKey<Level> TWILIGHT_FOREST = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "twilight_forest"));

    public static final ResourceKey<DimensionType> TWILIGHT_FOREST_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "twilight_forest"));
}
