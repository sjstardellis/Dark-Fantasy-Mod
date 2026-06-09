package net.steve.darkfantasy.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Strippable;
import net.steve.darkfantasy.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        var strippables = builder(NeoForgeDataMaps.STRIPPABLES);
        strippables.add(ModBlocks.GHOSTWILLOW_LOG, new Strippable(ModBlocks.STRIPPED_GHOSTWILLOW_LOG.get()), false);
        strippables.add(ModBlocks.GHOSTWILLOW_WOOD, new Strippable(ModBlocks.STRIPPED_GHOSTWILLOW_WOOD.get()), false);
        strippables.add(ModBlocks.GRAVEWOOD_LOG, new Strippable(ModBlocks.STRIPPED_GRAVEWOOD_LOG.get()), false);
        strippables.add(ModBlocks.GRAVEWOOD_WOOD, new Strippable(ModBlocks.STRIPPED_GRAVEWOOD_WOOD.get()), false);
        strippables.add(ModBlocks.CINDERBARK_STEM, new Strippable(ModBlocks.STRIPPED_CINDERBARK_STEM.get()), false);
        strippables.add(ModBlocks.CINDERBARK_HYPHAE, new Strippable(ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get()), false);
    }
}
