package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(ModBlocks.SHADOWSTEEL_ORE.get(),
                createOreDrop(ModBlocks.SHADOWSTEEL_ORE.get(), ModItems.RAW_SHADOWSTEEL.get()));
        add(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get(),
                createOreDrop(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get(), ModItems.RAW_SHADOWSTEEL.get()));

        add(ModBlocks.MOONSILVER_ORE.get(),
                createOreDrop(ModBlocks.MOONSILVER_ORE.get(), ModItems.RAW_MOONSILVER.get()));
        add(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get(),
                createOreDrop(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get(), ModItems.RAW_MOONSILVER.get()));

        add(ModBlocks.DAWNMETAL_ORE.get(),
                createOreDrop(ModBlocks.DAWNMETAL_ORE.get(), ModItems.RAW_DAWNMETAL.get()));
        add(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get(),
                createOreDrop(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get(), ModItems.RAW_DAWNMETAL.get()));

        dropSelf(ModBlocks.ALCHEMY_STAND.get());
        dropSelf(ModBlocks.ENCHANTED_BOOKSHELF.get());

        // Portals - never drops anything
        add(ModBlocks.SKYLANDS_PORTAL.get(), noDrop());
        add(ModBlocks.TWILIGHT_PORTAL.get(), noDrop());

        // Wood sets — every block drops itself by default. Leaves use vanilla
        // leaves-drop logic (sapling chance + shears/silk-touch override).
        // Doors emit two drops naturally via the lower-half logic baked into
        // BlockLootSubProvider#createDoorTable — we use the helper to handle
        // that correctly instead of dropSelf.
        addWoodSetDrops(
                ModBlocks.GHOSTWILLOW_LOG.get(), ModBlocks.STRIPPED_GHOSTWILLOW_LOG.get(),
                ModBlocks.GHOSTWILLOW_WOOD.get(), ModBlocks.STRIPPED_GHOSTWILLOW_WOOD.get(),
                ModBlocks.GHOSTWILLOW_PLANKS.get(), ModBlocks.GHOSTWILLOW_STAIRS.get(),
                ModBlocks.GHOSTWILLOW_SLAB.get(), ModBlocks.GHOSTWILLOW_FENCE.get(),
                ModBlocks.GHOSTWILLOW_FENCE_GATE.get(), ModBlocks.GHOSTWILLOW_DOOR.get(),
                ModBlocks.GHOSTWILLOW_TRAPDOOR.get(), ModBlocks.GHOSTWILLOW_BUTTON.get(),
                ModBlocks.GHOSTWILLOW_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.GHOSTWILLOW_SAPLING.get());
        add(ModBlocks.GHOSTWILLOW_LEAVES.get(),
                createLeavesDrops(ModBlocks.GHOSTWILLOW_LEAVES.get(),
                        ModBlocks.GHOSTWILLOW_SAPLING.get(),
                        NORMAL_LEAVES_SAPLING_CHANCES));

        addWoodSetDrops(
                ModBlocks.GRAVEWOOD_LOG.get(), ModBlocks.STRIPPED_GRAVEWOOD_LOG.get(),
                ModBlocks.GRAVEWOOD_WOOD.get(), ModBlocks.STRIPPED_GRAVEWOOD_WOOD.get(),
                ModBlocks.GRAVEWOOD_PLANKS.get(), ModBlocks.GRAVEWOOD_STAIRS.get(),
                ModBlocks.GRAVEWOOD_SLAB.get(), ModBlocks.GRAVEWOOD_FENCE.get(),
                ModBlocks.GRAVEWOOD_FENCE_GATE.get(), ModBlocks.GRAVEWOOD_DOOR.get(),
                ModBlocks.GRAVEWOOD_TRAPDOOR.get(), ModBlocks.GRAVEWOOD_BUTTON.get(),
                ModBlocks.GRAVEWOOD_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.GRAVEWOOD_SAPLING.get());
        add(ModBlocks.GRAVEWOOD_LEAVES.get(),
                createLeavesDrops(ModBlocks.GRAVEWOOD_LEAVES.get(),
                        ModBlocks.GRAVEWOOD_SAPLING.get(),
                        NORMAL_LEAVES_SAPLING_CHANCES));

        addWoodSetDrops(
                ModBlocks.CINDERBARK_STEM.get(), ModBlocks.STRIPPED_CINDERBARK_STEM.get(),
                ModBlocks.CINDERBARK_HYPHAE.get(), ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get(),
                ModBlocks.CINDERBARK_PLANKS.get(), ModBlocks.CINDERBARK_STAIRS.get(),
                ModBlocks.CINDERBARK_SLAB.get(), ModBlocks.CINDERBARK_FENCE.get(),
                ModBlocks.CINDERBARK_FENCE_GATE.get(), ModBlocks.CINDERBARK_DOOR.get(),
                ModBlocks.CINDERBARK_TRAPDOOR.get(), ModBlocks.CINDERBARK_BUTTON.get(),
                ModBlocks.CINDERBARK_PRESSURE_PLATE.get());
    }

    private void addWoodSetDrops(Block log, Block strippedLog, Block wood, Block strippedWood,
                                 Block planks, Block stairs, Block slab, Block fence,
                                 Block fenceGate, Block door, Block trapdoor,
                                 Block button, Block pressurePlate) {
        dropSelf(log);
        dropSelf(strippedLog);
        dropSelf(wood);
        dropSelf(strippedWood);
        dropSelf(planks);
        dropSelf(stairs);
        // Slabs drop two when broken in their double form.
        add(slab, createSlabItemTable(slab));
        dropSelf(fence);
        dropSelf(fenceGate);
        // Doors are two-block; the helper makes only the lower half drop the item.
        add(door, createDoorTable(door));
        dropSelf(trapdoor);
        dropSelf(button);
        dropSelf(pressurePlate);
    }

    protected LootTable.Builder createMultipleOreDrops(Block block, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, this.applyExplosionDecay(block,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                                .apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }
}
