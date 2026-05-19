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
        itemModels.generateFlatItem(ModItems.SHADOWSTEEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_SHADOWSTEEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SOUL_COMPASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.NIGHTSHADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.HELLFIRE_KINDLING.get(), ModelTemplates.FLAT_ITEM);


        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.RAW_SHADOWSTEEL_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_ORE.get());
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get());
        blockModels.createTrivialCube(ModBlocks.CURSED_BLOCK.get());

        blockModels.family(ModBlocks.SHADOWSTEEL_BLOCK.get())
                .stairs(ModBlocks.SHADOWSTEEL_STAIRS.get())
                .slab(ModBlocks.SHADOWSTEEL_SLAB.get())
                .pressurePlate(ModBlocks.SHADOWSTEEL_PRESSURE_PLATE.get())
                .button(ModBlocks.SHADOWSTEEL_BUTTON.get())
                .fence(ModBlocks.SHADOWSTEEL_FENCE.get())
                .fenceGate(ModBlocks.SHADOWSTEEL_FENCE_GATE.get())
                .wall(ModBlocks.SHADOWSTEEL_WALL.get())
                .door(ModBlocks.SHADOWSTEEL_DOOR.get())
                .trapdoor(ModBlocks.SHADOWSTEEL_TRAPDOOR.get());
    }
}
