package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DarkFantasy.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Cinderbark is fire-immune nether wood. Tagging the items as
        // NON_FLAMMABLE_WOOD prevents the item form from burning when dropped
        // into fire/lava, matching crimson/warped item behavior.
        tag(ItemTags.NON_FLAMMABLE_WOOD)
                .add(ModBlocks.CINDERBARK_STEM.get().asItem())
                .add(ModBlocks.STRIPPED_CINDERBARK_STEM.get().asItem())
                .add(ModBlocks.CINDERBARK_HYPHAE.get().asItem())
                .add(ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get().asItem())
                .add(ModBlocks.CINDERBARK_PLANKS.get().asItem())
                .add(ModBlocks.CINDERBARK_STAIRS.get().asItem())
                .add(ModBlocks.CINDERBARK_SLAB.get().asItem())
                .add(ModBlocks.CINDERBARK_FENCE.get().asItem())
                .add(ModBlocks.CINDERBARK_FENCE_GATE.get().asItem())
                .add(ModBlocks.CINDERBARK_DOOR.get().asItem())
                .add(ModBlocks.CINDERBARK_TRAPDOOR.get().asItem())
                .add(ModBlocks.CINDERBARK_BUTTON.get().asItem())
                .add(ModBlocks.CINDERBARK_PRESSURE_PLATE.get().asItem());
    }
}
