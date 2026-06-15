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
                .add(ModBlocks.ALCHEMY_STAND.get())
                .add(ModBlocks.DAWNMETAL_ORE.get())
                .add(ModBlocks.DAWNMETAL_DEEPSLATE_ORE.get())
                .add(ModBlocks.MOONSILVER_ORE.get())
                .add(ModBlocks.MOONSILVER_DEEPSLATE_ORE.get())
                .add(ModBlocks.MERCURYGLASS_ORE.get())
                .add(ModBlocks.EMBERSTONE_ORE.get())
                .add(ModBlocks.LARIMAR_PEARL_ORE.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.ENCHANTED_BOOKSHELF.get())
                .add(ModBlocks.GNOME_BURROW.get());

        tag(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                .add(ModBlocks.ENCHANTED_BOOKSHELF.get());

        tag(ModTags.Blocks.TWILIGHT_PORTAL_FRAME)
                .add(ModBlocks.ENCHANTED_BOOKSHELF.get());


        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE.get())
                .add(ModBlocks.MERCURYGLASS_ORE.get())
                .add(ModBlocks.EMBERSTONE_ORE.get())
                .add(ModBlocks.LARIMAR_PEARL_ORE.get());

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

        // ──────────────────────────────────────────────────────────────────
        // Wood-set tags. LOGS_THAT_BURN excludes cinderbark (nether wood);
        // cinderbark goes into LOGS only, matching crimson/warped behavior.
        // ──────────────────────────────────────────────────────────────────
        tag(BlockTags.LOGS_THAT_BURN)
                .add(ModBlocks.GHOSTWILLOW_LOG.get())
                .add(ModBlocks.STRIPPED_GHOSTWILLOW_LOG.get())
                .add(ModBlocks.GHOSTWILLOW_WOOD.get())
                .add(ModBlocks.STRIPPED_GHOSTWILLOW_WOOD.get())
                .add(ModBlocks.GRAVEWOOD_LOG.get())
                .add(ModBlocks.STRIPPED_GRAVEWOOD_LOG.get())
                .add(ModBlocks.GRAVEWOOD_WOOD.get())
                .add(ModBlocks.STRIPPED_GRAVEWOOD_WOOD.get());

        tag(BlockTags.LOGS)
                .add(ModBlocks.CINDERBARK_STEM.get())
                .add(ModBlocks.STRIPPED_CINDERBARK_STEM.get())
                .add(ModBlocks.CINDERBARK_HYPHAE.get())
                .add(ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get());

        tag(BlockTags.PLANKS)
                .add(ModBlocks.GHOSTWILLOW_PLANKS.get())
                .add(ModBlocks.GRAVEWOOD_PLANKS.get())
                .add(ModBlocks.CINDERBARK_PLANKS.get());

        tag(BlockTags.WOODEN_STAIRS)
                .add(ModBlocks.GHOSTWILLOW_STAIRS.get())
                .add(ModBlocks.GRAVEWOOD_STAIRS.get())
                .add(ModBlocks.CINDERBARK_STAIRS.get());

        tag(BlockTags.WOODEN_SLABS)
                .add(ModBlocks.GHOSTWILLOW_SLAB.get())
                .add(ModBlocks.GRAVEWOOD_SLAB.get())
                .add(ModBlocks.CINDERBARK_SLAB.get());

        tag(BlockTags.WOODEN_FENCES)
                .add(ModBlocks.GHOSTWILLOW_FENCE.get())
                .add(ModBlocks.GRAVEWOOD_FENCE.get())
                .add(ModBlocks.CINDERBARK_FENCE.get());

        tag(BlockTags.FENCE_GATES)
                .add(ModBlocks.GHOSTWILLOW_FENCE_GATE.get())
                .add(ModBlocks.GRAVEWOOD_FENCE_GATE.get())
                .add(ModBlocks.CINDERBARK_FENCE_GATE.get());

        tag(BlockTags.WOODEN_DOORS)
                .add(ModBlocks.GHOSTWILLOW_DOOR.get())
                .add(ModBlocks.GRAVEWOOD_DOOR.get())
                .add(ModBlocks.CINDERBARK_DOOR.get());

        tag(BlockTags.WOODEN_TRAPDOORS)
                .add(ModBlocks.GHOSTWILLOW_TRAPDOOR.get())
                .add(ModBlocks.GRAVEWOOD_TRAPDOOR.get())
                .add(ModBlocks.CINDERBARK_TRAPDOOR.get());

        tag(BlockTags.WOODEN_BUTTONS)
                .add(ModBlocks.GHOSTWILLOW_BUTTON.get())
                .add(ModBlocks.GRAVEWOOD_BUTTON.get())
                .add(ModBlocks.CINDERBARK_BUTTON.get());

        tag(BlockTags.WOODEN_PRESSURE_PLATES)
                .add(ModBlocks.GHOSTWILLOW_PRESSURE_PLATE.get())
                .add(ModBlocks.GRAVEWOOD_PRESSURE_PLATE.get())
                .add(ModBlocks.CINDERBARK_PRESSURE_PLATE.get());

        tag(BlockTags.LEAVES)
                .add(ModBlocks.GHOSTWILLOW_LEAVES.get())
                .add(ModBlocks.GRAVEWOOD_LEAVES.get());

        tag(BlockTags.SAPLINGS)
                .add(ModBlocks.GHOSTWILLOW_SAPLING.get())
                .add(ModBlocks.GRAVEWOOD_SAPLING.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.GHOSTWILLOW_LOG.get())
                .add(ModBlocks.STRIPPED_GHOSTWILLOW_LOG.get())
                .add(ModBlocks.GHOSTWILLOW_WOOD.get())
                .add(ModBlocks.STRIPPED_GHOSTWILLOW_WOOD.get())
                .add(ModBlocks.GHOSTWILLOW_PLANKS.get())
                .add(ModBlocks.GHOSTWILLOW_STAIRS.get())
                .add(ModBlocks.GHOSTWILLOW_SLAB.get())
                .add(ModBlocks.GHOSTWILLOW_FENCE.get())
                .add(ModBlocks.GHOSTWILLOW_FENCE_GATE.get())
                .add(ModBlocks.GHOSTWILLOW_DOOR.get())
                .add(ModBlocks.GHOSTWILLOW_TRAPDOOR.get())
                .add(ModBlocks.GHOSTWILLOW_BUTTON.get())
                .add(ModBlocks.GHOSTWILLOW_PRESSURE_PLATE.get())
                .add(ModBlocks.GRAVEWOOD_LOG.get())
                .add(ModBlocks.STRIPPED_GRAVEWOOD_LOG.get())
                .add(ModBlocks.GRAVEWOOD_WOOD.get())
                .add(ModBlocks.STRIPPED_GRAVEWOOD_WOOD.get())
                .add(ModBlocks.GRAVEWOOD_PLANKS.get())
                .add(ModBlocks.GRAVEWOOD_STAIRS.get())
                .add(ModBlocks.GRAVEWOOD_SLAB.get())
                .add(ModBlocks.GRAVEWOOD_FENCE.get())
                .add(ModBlocks.GRAVEWOOD_FENCE_GATE.get())
                .add(ModBlocks.GRAVEWOOD_DOOR.get())
                .add(ModBlocks.GRAVEWOOD_TRAPDOOR.get())
                .add(ModBlocks.GRAVEWOOD_BUTTON.get())
                .add(ModBlocks.GRAVEWOOD_PRESSURE_PLATE.get())
                .add(ModBlocks.CINDERBARK_STEM.get())
                .add(ModBlocks.STRIPPED_CINDERBARK_STEM.get())
                .add(ModBlocks.CINDERBARK_HYPHAE.get())
                .add(ModBlocks.STRIPPED_CINDERBARK_HYPHAE.get())
                .add(ModBlocks.CINDERBARK_PLANKS.get())
                .add(ModBlocks.CINDERBARK_STAIRS.get())
                .add(ModBlocks.CINDERBARK_SLAB.get())
                .add(ModBlocks.CINDERBARK_FENCE.get())
                .add(ModBlocks.CINDERBARK_FENCE_GATE.get())
                .add(ModBlocks.CINDERBARK_DOOR.get())
                .add(ModBlocks.CINDERBARK_TRAPDOOR.get())
                .add(ModBlocks.CINDERBARK_BUTTON.get())
                .add(ModBlocks.CINDERBARK_PRESSURE_PLATE.get());

        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.GHOSTWILLOW_LEAVES.get())
                .add(ModBlocks.GRAVEWOOD_LEAVES.get());
    }
}
