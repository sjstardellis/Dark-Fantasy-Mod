package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Holder;
import net.minecraft.data.BlockFamily;
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
        itemModels.generateFlatItem(ModItems.ECLIPSIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FAIRY_DUST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LIGHTNING_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.FIREBALL_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);


        itemModels.generateFlatItem(ModItems.MOONSILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_MOONSILVER.get(), ModelTemplates.FLAT_ITEM);


        itemModels.generateFlatItem(ModItems.DAWNMETAL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_DAWNMETAL.get(), ModelTemplates.FLAT_ITEM);

        // LYTEBUG_DUST / ELIXIR_BUCKET / BEER / STEIN_GLASS / HOPS have hand-written
        // item models under src/main/resources/assets/darkfantasy/items/. Skipping
        // datagen for them (see #getKnownItems below) avoids duplicates.

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_ORE.get());
        blockModels.createTrivialCube(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get());

        blockModels.createTrivialCube(ModBlocks.DAWNMETAL_ORE.get());
        blockModels.createTrivialCube(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get());

        blockModels.createTrivialCube(ModBlocks.MOONSILVER_ORE.get());
        blockModels.createTrivialCube(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get());

        // LYTESTONE has hand-written blockstate + model under src/main/resources;
        // skipping datagen for it (see #getKnownBlocks below) avoids a duplicate.

        // Skylands portal + alchemy stand: blockstate + model + item model are all
        // hand-written under src/main/resources (custom Blockbench / starfield models).
        // See #getKnownBlocks / #getKnownItems below for the validator skip.

        /* WOOD SETS — ghostwillow, gravewood, cinderbark */
        generateWoodSet(blockModels,
                ModBlocks.GHOSTWILLOW_LOG.get(), ModBlocks.STRIPPED_GHOSTWILLOW_LOG.get(),
                ModBlocks.GHOSTWILLOW_WOOD.get(), ModBlocks.STRIPPED_GHOSTWILLOW_WOOD.get(),
                ModBlocks.GHOSTWILLOW_PLANKS.get(), ModBlocks.GHOSTWILLOW_STAIRS.get(),
                ModBlocks.GHOSTWILLOW_SLAB.get(), ModBlocks.GHOSTWILLOW_FENCE.get(),
                ModBlocks.GHOSTWILLOW_FENCE_GATE.get(), ModBlocks.GHOSTWILLOW_DOOR.get(),
                ModBlocks.GHOSTWILLOW_TRAPDOOR.get(), ModBlocks.GHOSTWILLOW_BUTTON.get(),
                ModBlocks.GHOSTWILLOW_PRESSURE_PLATE.get(), /* horizontalLog = */ true);
        blockModels.createTrivialBlock(ModBlocks.GHOSTWILLOW_LEAVES.get(), TexturedModel.LEAVES);
        blockModels.createCrossBlockWithDefaultItem(ModBlocks.GHOSTWILLOW_SAPLING.get(),
                BlockModelGenerators.PlantType.NOT_TINTED);

        generateWoodSet(blockModels,
                ModBlocks.GRAVEWOOD_LOG.get(), ModBlocks.STRIPPED_GRAVEWOOD_LOG.get(),
                ModBlocks.GRAVEWOOD_WOOD.get(), ModBlocks.STRIPPED_GRAVEWOOD_WOOD.get(),
                ModBlocks.GRAVEWOOD_PLANKS.get(), ModBlocks.GRAVEWOOD_STAIRS.get(),
                ModBlocks.GRAVEWOOD_SLAB.get(), ModBlocks.GRAVEWOOD_FENCE.get(),
                ModBlocks.GRAVEWOOD_FENCE_GATE.get(), ModBlocks.GRAVEWOOD_DOOR.get(),
                ModBlocks.GRAVEWOOD_TRAPDOOR.get(), ModBlocks.GRAVEWOOD_BUTTON.get(),
                ModBlocks.GRAVEWOOD_PRESSURE_PLATE.get(), /* horizontalLog = */ true);
        blockModels.createTrivialBlock(ModBlocks.GRAVEWOOD_LEAVES.get(), TexturedModel.LEAVES);
        blockModels.createCrossBlockWithDefaultItem(ModBlocks.GRAVEWOOD_SAPLING.get(),
                BlockModelGenerators.PlantType.NOT_TINTED);

        // Cinderbark — nether-style: vertical-only stem (no horizontal variant), no
        // leaves/sapling. Crimson/warped use .log(); we follow the same pattern.
        generateWoodSet(blockModels,
                ModBlocks.CINDERBARK_STEM.get(), ModBlocks.STRIPPED_CINDERBARK_STEM.get(),
                ModBlocks.CINDERBARK_HYPHAE.get(), ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get(),
                ModBlocks.CINDERBARK_PLANKS.get(), ModBlocks.CINDERBARK_STAIRS.get(),
                ModBlocks.CINDERBARK_SLAB.get(), ModBlocks.CINDERBARK_FENCE.get(),
                ModBlocks.CINDERBARK_FENCE_GATE.get(), ModBlocks.CINDERBARK_DOOR.get(),
                ModBlocks.CINDERBARK_TRAPDOOR.get(), ModBlocks.CINDERBARK_BUTTON.get(),
                ModBlocks.CINDERBARK_PRESSURE_PLATE.get(), /* horizontalLog = */ false);
    }

    /**
     * Generates blockstate + block model + item model JSONs for a full wood set.
     * Mirrors the helpers Mojang uses for vanilla wood (BlockModelGenerators).
     *
     * @param horizontalLog true for overworld logs (axis x/z gets a sideways model);
     *                      false for nether stems which only rotate vertically.
     */
    private static void generateWoodSet(BlockModelGenerators blockModels,
                                        Block log, Block strippedLog,
                                        Block wood, Block strippedWood,
                                        Block planks, Block stairs, Block slab,
                                        Block fence, Block fenceGate,
                                        Block door, Block trapdoor,
                                        Block button, Block pressurePlate,
                                        boolean horizontalLog) {
        if (horizontalLog) {
            blockModels.woodProvider(log).logWithHorizontal(log).wood(wood);
            blockModels.woodProvider(strippedLog).logWithHorizontal(strippedLog).wood(strippedWood);
        } else {
            blockModels.woodProvider(log).log(log).wood(wood);
            blockModels.woodProvider(strippedLog).log(strippedLog).wood(strippedWood);
        }

        BlockFamily family = new BlockFamily.Builder(planks)
                .stairs(stairs)
                .slab(slab)
                .fence(fence)
                .fenceGate(fenceGate)
                .door(door)
                .trapdoor(trapdoor)
                .button(button)
                .pressurePlate(pressurePlate)
                .getFamily();
        blockModels.family(planks).generateFor(family);
    }

    /**
     * Exclude blocks that have manually-written blockstate/model JSONs from validation,
     * so datagen doesn't error out demanding we generate them here.
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks()
                .filter(holder -> holder.value() != ModBlocks.SKYLANDS_PORTAL.get()
                               && holder.value() != ModBlocks.TWILIGHT_PORTAL.get()
                               && holder.value() != ModBlocks.ALCHEMY_STAND.get()
                               && holder.value() != ModBlocks.ENCHANTED_BOOKSHELF.get()
                               && holder.value() != ModBlocks.BREWING_KEG.get()
                               && holder.value() != ModBlocks.HOPS_CROP.get()
                               && holder.value() != ModBlocks.ELIXIR.get()
                               && holder.value() != ModBlocks.LYTESTONE.get());
    }

    /** Exclude items whose models are hand-written under src/main/resources. */
    @Override
    protected Stream<? extends Holder<net.minecraft.world.item.Item>> getKnownItems() {
        return super.getKnownItems()
                .filter(holder -> holder.value() != ModBlocks.ALCHEMY_STAND.get().asItem()
                               && holder.value() != ModBlocks.ENCHANTED_BOOKSHELF.get().asItem()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.FAIRY_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.WIZARD_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.ELECTRO_DRAGON_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.GOBLIN_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.GNOME_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.LYTEBUG_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.LYTEBUG_DUST.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.ELIXIR_BUCKET.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.BEER.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.STEIN_GLASS.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.HOPS.get()
                               && holder.value() != ModBlocks.BREWING_KEG.get().asItem()
                               && holder.value() != ModBlocks.LYTESTONE.get().asItem());
    }
}
