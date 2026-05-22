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
                .add(ModBlocks.SHADOWSTEEL_ORE.get())
                .add(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get())
                .add(ModBlocks.CURSED_BLOCK.get())
                .add(ModBlocks.ALCHEMY_STAND.get())
                .add(ModBlocks.DAWNMETAL_ORE.get())
                .add(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get())
                .add(ModBlocks.MOONSILVER_ORE.get())
                .add(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.ENCHANTED_BOOKSHELF.get());

        tag(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                .add(ModBlocks.ENCHANTED_BOOKSHELF.get());

        tag(ModTags.Blocks.TWILIGHT_PORTAL_FRAME)
                .add(ModBlocks.ENCHANTED_BOOKSHELF.get());


        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.MOONSILVER_ORE.get());
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get());
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.DAWNMETAL_ORE.get());
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get());


        tag(ModTags.Blocks.SOUL_DETECTABLES)
                .addTag(Tags.Blocks.ORES);

    }
}
