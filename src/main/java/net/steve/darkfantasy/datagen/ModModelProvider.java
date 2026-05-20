package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, DarkFantasy.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        /* ITEMS */
        itemModels.generateFlatItem(ModItems.SHADOWSTEEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_SHADOWSTEEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SOUL_COMPASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.NIGHTSHADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.HELLFIRE_KINDLING.get(), ModelTemplates.FLAT_ITEM);

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_ORE.get());
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get());
        blockModels.createTrivialCube(ModBlocks.CURSED_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.ALCHEMY_STAND.get());
    }
}
