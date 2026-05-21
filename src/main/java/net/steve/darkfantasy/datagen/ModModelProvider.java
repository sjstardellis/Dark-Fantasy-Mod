package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

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


        itemModels.generateFlatItem(ModItems.MOONSILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_MOONSILVER.get(), ModelTemplates.FLAT_ITEM);


        itemModels.generateFlatItem(ModItems.DAWNMETAL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_DAWNMETAL.get(), ModelTemplates.FLAT_ITEM);

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_ORE.get());
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get());

        blockModels.createTrivialCube(ModBlocks.DAWNMETAL_ORE.get());
        blockModels.createTrivialCube(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get());

        blockModels.createTrivialCube(ModBlocks.MOONSILVER_ORE.get());
        blockModels.createTrivialCube(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get());

        blockModels.createTrivialCube(ModBlocks.CURSED_BLOCK.get());

        // Skylands portal + alchemy stand: blockstate + model + item model are all
        // hand-written under src/main/resources (custom Blockbench / starfield models).
        // See #getKnownBlocks / #getKnownItems below for the validator skip.
    }

    /**
     * Exclude blocks that have manually-written blockstate/model JSONs from validation,
     * so datagen doesn't error out demanding we generate them here.
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks()
                .filter(holder -> holder.value() != ModBlocks.SKYLANDS_PORTAL.get()
                               && holder.value() != ModBlocks.ALCHEMY_STAND.get());
    }

    /** Exclude the alchemy stand's BlockItem too — its item model is hand-written. */
    @Override
    protected Stream<? extends Holder<net.minecraft.world.item.Item>> getKnownItems() {
        return super.getKnownItems()
                .filter(holder -> holder.value() != ModBlocks.ALCHEMY_STAND.get().asItem());
    }
}
