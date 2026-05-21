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

        dropSelf(ModBlocks.CURSED_BLOCK.get());
        dropSelf(ModBlocks.ALCHEMY_STAND.get());

        // Skylands portal — never drops anything
        add(ModBlocks.SKYLANDS_PORTAL.get(), noDrop());
    }

    protected LootTable.Builder createMultipleOreDrops(Block block, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, this.applyExplosionDecay(block,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                                .apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
