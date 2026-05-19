package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DarkFantasy.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.SHADOWSTEEL_BLOCK.get())
                .add(ModBlocks.RAW_SHADOWSTEEL_BLOCK.get())
                .add(ModBlocks.SHADOWSTEEL_ORE.get())
                .add(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get())
                .add(ModBlocks.CURSED_BLOCK.get())
                .add(ModBlocks.SHADOWSTEEL_STAIRS.get())
                .add(ModBlocks.SHADOWSTEEL_SLAB.get())
                .add(ModBlocks.SHADOWSTEEL_PRESSURE_PLATE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get());


        tag(ModTags.Blocks.SOUL_DETECTABLES)
                .addTag(Tags.Blocks.ORES);

        tag(BlockTags.STAIRS)
                .add(ModBlocks.SHADOWSTEEL_STAIRS.get());
        tag(BlockTags.SLABS)
                .add(ModBlocks.SHADOWSTEEL_SLAB.get());
        tag(BlockTags.PRESSURE_PLATES)
                .add(ModBlocks.SHADOWSTEEL_PRESSURE_PLATE.get());
        tag(BlockTags.BUTTONS)
                .add(ModBlocks.SHADOWSTEEL_BUTTON.get());

        tag(BlockTags.FENCES).add(ModBlocks.SHADOWSTEEL_FENCE.get());
        tag(BlockTags.FENCE_GATES).add(ModBlocks.SHADOWSTEEL_FENCE_GATE.get());
        tag(BlockTags.WALLS).add(ModBlocks.SHADOWSTEEL_WALL.get());

        tag(BlockTags.DOORS).add(ModBlocks.SHADOWSTEEL_DOOR.get());
        tag(BlockTags.TRAPDOORS).add(ModBlocks.SHADOWSTEEL_TRAPDOOR.get());
    }
}
