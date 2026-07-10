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
import net.minecraft.world.item.Item;
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
        itemModels.generateFlatItem(ModItems.MERCURYGLASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.EMBERSTONE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LARIMAR_PEARL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ARCANE_ASH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STORM_SCALE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FALLEN_STAR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.UMBRA_ESSENCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CINDER_FANG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.HAG_ICHOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STARLIGHT_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.UMBRAL_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.HOUNDSBLOOD_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.WITCHBANE_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.THROWING_DAGGER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.MOONLIGHT_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STONESKIN_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.EMBERBLOOD_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.WISPSTEP_ELIXIR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LIGHTNING_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.FIREBALL_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.FROST_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.BLINK_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.CINDER_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);


        itemModels.generateFlatItem(ModItems.MOONSILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_MOONSILVER.get(), ModelTemplates.FLAT_ITEM);

        // Metal gear — tools use the handheld template, armor uses flat inventory icons.
        gearModels(itemModels,
                ModItems.MOONSILVER_SWORD.get(), ModItems.MOONSILVER_PICKAXE.get(), ModItems.MOONSILVER_AXE.get(),
                ModItems.MOONSILVER_SHOVEL.get(), ModItems.MOONSILVER_HOE.get(), ModItems.MOONSILVER_HELMET.get(),
                ModItems.MOONSILVER_CHESTPLATE.get(), ModItems.MOONSILVER_LEGGINGS.get(), ModItems.MOONSILVER_BOOTS.get());
        gearModels(itemModels,
                ModItems.SHADOWSTEEL_SWORD.get(), ModItems.SHADOWSTEEL_PICKAXE.get(), ModItems.SHADOWSTEEL_AXE.get(),
                ModItems.SHADOWSTEEL_SHOVEL.get(), ModItems.SHADOWSTEEL_HOE.get(), ModItems.SHADOWSTEEL_HELMET.get(),
                ModItems.SHADOWSTEEL_CHESTPLATE.get(), ModItems.SHADOWSTEEL_LEGGINGS.get(), ModItems.SHADOWSTEEL_BOOTS.get());
        gearModels(itemModels,
                ModItems.DAWNMETAL_SWORD.get(), ModItems.DAWNMETAL_PICKAXE.get(), ModItems.DAWNMETAL_AXE.get(),
                ModItems.DAWNMETAL_SHOVEL.get(), ModItems.DAWNMETAL_HOE.get(), ModItems.DAWNMETAL_HELMET.get(),
                ModItems.DAWNMETAL_CHESTPLATE.get(), ModItems.DAWNMETAL_LEGGINGS.get(), ModItems.DAWNMETAL_BOOTS.get());
        gearModels(itemModels,
                ModItems.ECLIPSIUM_SWORD.get(), ModItems.ECLIPSIUM_PICKAXE.get(), ModItems.ECLIPSIUM_AXE.get(),
                ModItems.ECLIPSIUM_SHOVEL.get(), ModItems.ECLIPSIUM_HOE.get(), ModItems.ECLIPSIUM_HELMET.get(),
                ModItems.ECLIPSIUM_CHESTPLATE.get(), ModItems.ECLIPSIUM_LEGGINGS.get(), ModItems.ECLIPSIUM_BOOTS.get());

        // Signature weapons (all handheld).
        itemModels.generateFlatItem(ModItems.MOONSILVER_SCYTHE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHADOWSTEEL_DAGGERS.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.DAWNMETAL_SUNLANCE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);


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

        blockModels.createTrivialCube(ModBlocks.MERCURYGLASS_ORE.get());
        blockModels.createTrivialCube(ModBlocks.EMBERSTONE_ORE.get());
        blockModels.createTrivialCube(ModBlocks.LARIMAR_PEARL_ORE.get());

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

    /** Item models for a full metal gear set: tools handheld, armor flat. */
    private static void gearModels(ItemModelGenerators m, Item sword, Item pickaxe, Item axe,
                                   Item shovel, Item hoe, Item helmet, Item chestplate,
                                   Item leggings, Item boots) {
        m.generateFlatItem(sword, ModelTemplates.FLAT_HANDHELD_ITEM);
        m.generateFlatItem(pickaxe, ModelTemplates.FLAT_HANDHELD_ITEM);
        m.generateFlatItem(axe, ModelTemplates.FLAT_HANDHELD_ITEM);
        m.generateFlatItem(shovel, ModelTemplates.FLAT_HANDHELD_ITEM);
        m.generateFlatItem(hoe, ModelTemplates.FLAT_HANDHELD_ITEM);
        m.generateFlatItem(helmet, ModelTemplates.FLAT_ITEM);
        m.generateFlatItem(chestplate, ModelTemplates.FLAT_ITEM);
        m.generateFlatItem(leggings, ModelTemplates.FLAT_ITEM);
        m.generateFlatItem(boots, ModelTemplates.FLAT_ITEM);
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
                               && holder.value() != ModBlocks.GNOME_BURROW.get()
                               && holder.value() != ModBlocks.OBSIDIAN_BRICK.get()
                               && holder.value() != ModBlocks.OBSIDIAN_GLASS.get()
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
                               && holder.value() != net.steve.darkfantasy.item.ModItems.DARK_ALE.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.HONEY_MEAD.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.GLOWBREW.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.MUSHROOM_STOUT.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.WITHER_STOUT.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.BATTLE_BREW.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.STEIN_GLASS.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.HOPS.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.ECLIPSE_CROWN.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.ECLIPSE_GREATSWORD.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.ECLIPSE_KING_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.WARDING_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.MAELSTROM_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.WAYFARER_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.WITHER_SKULL_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.PROSPECTOR_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.STASIS_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.EVOKER_CLAW_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.ECLIPSE_TOME.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.CRESCENT_BOW.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.DAWNMETAL_ARBALEST.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.CINDER_HOUND_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.UMBRAL_WRAITH_SPAWN_EGG.get()
                               && holder.value() != net.steve.darkfantasy.item.ModItems.BOG_HAG_SPAWN_EGG.get()
                               && holder.value() != ModBlocks.BREWING_KEG.get().asItem()
                               && holder.value() != ModBlocks.GNOME_BURROW.get().asItem()
                               && holder.value() != ModBlocks.OBSIDIAN_BRICK.get().asItem()
                               && holder.value() != ModBlocks.OBSIDIAN_GLASS.get().asItem()
                               && holder.value() != ModBlocks.LYTESTONE.get().asItem());
    }
}
