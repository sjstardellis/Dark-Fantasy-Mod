package net.steve.darkfantasy.block;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.custom.AlchemyStandBlock;
import net.steve.darkfantasy.block.custom.BrewingKegBlock;
import net.steve.darkfantasy.block.custom.ElixirLiquidBlock;
import net.steve.darkfantasy.block.custom.EnchantedBookshelfBlock;
import net.steve.darkfantasy.block.custom.HopsBlock;
import net.steve.darkfantasy.block.custom.LytestoneBlock;
import net.steve.darkfantasy.block.custom.SkylandsPortalBlock;
import net.steve.darkfantasy.block.custom.TwilightPortalBlock;
import net.steve.darkfantasy.init.ModFluids;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(DarkFantasy.MOD_ID);



    public static final DeferredBlock<Block> SHADOWSTEEL_ORE = registerBlock("shadowsteel_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> SHADOWSTEEL_DEEPSLATE_ORE = registerBlock("shadowsteel_deepslate_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 5), properties.strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    

    public static final DeferredBlock<Block> MOONSILVER_ORE = registerBlock("moonsilver_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 4), properties.strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> MOONSILVER_DEEPSLATE_ORE = registerBlock("moonsilver_deepslate_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 6), properties.strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> DAWNMETAL_ORE = registerBlock("dawnmetal_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 5), properties.strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DAWNMETAL_DEEPSLATE_ORE = registerBlock("dawnmetal_deepslate_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 7), properties.strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> ALCHEMY_STAND = registerBlock("alchemy_stand",
            properties -> new AlchemyStandBlock(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));

    public static final DeferredBlock<Block> ENCHANTED_BOOKSHELF = registerBlock("enchanted_bookshelf",
            properties -> new EnchantedBookshelfBlock(properties.strength(1.8f)
                    .sound(SoundType.WOOD).lightLevel(state -> 4).ignitedByLava()));

    // Lytestone — crystallized lytebug essence. Glowstone-grade light (15) and acts as
    // a lytebug magnet (see LytebugEntity.MoveToLytestoneGoal). Drops 2-4 dust like
    // glowstone unless mined with silk touch — loot table handles that.
    public static final DeferredBlock<Block> LYTESTONE = registerBlock("lytestone",
            properties -> new LytestoneBlock(properties.strength(0.3f)
                    .sound(SoundType.GLASS).lightLevel(state -> 15)));

    // Skylands portal — no BlockItem (not obtainable in survival).
    public static final DeferredBlock<SkylandsPortalBlock> SKYLANDS_PORTAL = BLOCKS.registerBlock("skylands_portal",
            SkylandsPortalBlock::new,
            properties -> properties
                    .noCollision()
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .noOcclusion());

    // Brewing keg — beer fermentation. State + ingredient flags live in
    // BrewingKegBlockEntity; brewing only progresses while a heat source sits below.
    // noOcclusion() is critical: the custom model is smaller than 16x16x16, and
    // without this flag the engine assumes a full cube and culls adjacent faces
    // (causing the floor under the keg to look transparent / see-through).
    public static final DeferredBlock<Block> BREWING_KEG = registerBlock("brewing_keg",
            properties -> new BrewingKegBlock(properties.strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava().noOcclusion()));

    // Hops — 4-stage farmland crop. Planting is via the Hops item (registered as
    // BlockItem(HOPS_CROP) in ModItems), matching the vanilla wheat-seeds → wheat-block
    // pattern.
    public static final DeferredBlock<HopsBlock> HOPS_CROP = BLOCKS.registerBlock("hops_crop",
            HopsBlock::new,
            properties -> properties
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));

    // Elixir liquid block — placed in world by the bucket or by the elixir_lake
    // worldgen feature. No BlockItem (the bucket IS the item form, registered in
    // ModItems). Standard vanilla-liquid properties: replaceable, no collision,
    // no loot, very high blast resistance, push-destroyed by pistons. The light
    // value (4) mirrors the FluidType's getLightLevel — the block-light side has
    // to be set here separately for chunk lighting to pick it up.
    public static final DeferredBlock<LiquidBlock> ELIXIR = BLOCKS.registerBlock("elixir",
            properties -> new ElixirLiquidBlock(ModFluids.ELIXIR_SOURCE.get(), properties),
            properties -> properties
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .noLootTable()
                    .liquid()
                    .lightLevel(state -> 4));

    // Twilight Forest portal — also no BlockItem.
    public static final DeferredBlock<TwilightPortalBlock> TWILIGHT_PORTAL = BLOCKS.registerBlock("twilight_portal",
            TwilightPortalBlock::new,
            properties -> properties
                    .noCollision()
                    .strength(-1.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 11)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .noOcclusion());

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOM WOOD TYPES
    //
    // Three new wood sets:
    //   • Ghostwillow — pale overworld tree (oak-like sounds, flammable from lava).
    //   • Gravewood   — dark overworld tree (oak-like sounds, flammable from lava).
    //   • Cinderbark  — nether-style stem (nether-wood sounds, FIRE IMMUNE — no
    //                   ignitedByLava() anywhere in its chain, mirroring crimson/warped).
    //
    // BlockSetType + WoodType are registered eagerly via their static register()
    // hooks so that DoorBlock/TrapDoorBlock/FenceGateBlock/etc. can resolve them
    // when the block fields below initialize.
    // ─────────────────────────────────────────────────────────────────────────

    public static final BlockSetType GHOSTWILLOW_SET_TYPE = BlockSetType.register(new BlockSetType("ghostwillow"));
    public static final WoodType GHOSTWILLOW_WOOD_TYPE = WoodType.register(new WoodType("ghostwillow", GHOSTWILLOW_SET_TYPE));

    public static final BlockSetType GRAVEWOOD_SET_TYPE = BlockSetType.register(new BlockSetType("gravewood"));
    public static final WoodType GRAVEWOOD_WOOD_TYPE = WoodType.register(new WoodType("gravewood", GRAVEWOOD_SET_TYPE));

    public static final BlockSetType CINDERBARK_SET_TYPE = BlockSetType.register(new BlockSetType(
            "cinderbark",
            true, true, true,
            BlockSetType.PressurePlateSensitivity.EVERYTHING,
            SoundType.NETHER_WOOD,
            SoundEvents.NETHER_WOOD_DOOR_CLOSE,
            SoundEvents.NETHER_WOOD_DOOR_OPEN,
            SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE,
            SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN,
            SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF,
            SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON
    ));
    public static final WoodType CINDERBARK_WOOD_TYPE = WoodType.register(new WoodType(
            "cinderbark",
            CINDERBARK_SET_TYPE,
            SoundType.NETHER_WOOD,
            SoundType.NETHER_WOOD_HANGING_SIGN,
            SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE,
            SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN
    ));

    // ResourceKeys pointing at the configured-feature JSONs under
    // src/main/resources/data/darkfantasy/worldgen/configured_feature/. Saplings
    // resolve these keys from the level's CONFIGURED_FEATURE registry at growth time.
    public static final ResourceKey<ConfiguredFeature<?, ?>> GHOSTWILLOW_TREE_KEY = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "ghostwillow"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRAVEWOOD_TREE_KEY = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "gravewood"));

    // Saplings invoke these growers on random tick (stage 1 → tree feature). Each
    // grower must have a unique name string — TreeGrower's constructor puts itself
    // into a static name->grower map for serialization.
    private static final TreeGrower GHOSTWILLOW_GROWER = new TreeGrower(
            "darkfantasy:ghostwillow",
            Optional.empty(), Optional.of(GHOSTWILLOW_TREE_KEY), Optional.empty());
    private static final TreeGrower GRAVEWOOD_GROWER = new TreeGrower(
            "darkfantasy:gravewood",
            Optional.empty(), Optional.of(GRAVEWOOD_TREE_KEY), Optional.empty());

    // ─── Ghostwillow ─────────────────────────────────────────────────────────
    public static final DeferredBlock<RotatedPillarBlock> GHOSTWILLOW_LOG = registerBlock("ghostwillow_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOL).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GHOSTWILLOW_LOG = registerBlock("stripped_ghostwillow_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOL).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<RotatedPillarBlock> GHOSTWILLOW_WOOD = registerBlock("ghostwillow_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOL).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GHOSTWILLOW_WOOD = registerBlock("stripped_ghostwillow_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOL).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> GHOSTWILLOW_PLANKS = registerBlock("ghostwillow_planks",
            properties -> new Block(properties.mapColor(MapColor.WOOL).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<StairBlock> GHOSTWILLOW_STAIRS = registerBlock("ghostwillow_stairs",
            properties -> new StairBlock(GHOSTWILLOW_PLANKS.get().defaultBlockState(),
                    properties.mapColor(MapColor.WOOL).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<SlabBlock> GHOSTWILLOW_SLAB = registerBlock("ghostwillow_slab",
            properties -> new SlabBlock(properties.mapColor(MapColor.WOOL).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<FenceBlock> GHOSTWILLOW_FENCE = registerBlock("ghostwillow_fence",
            properties -> new FenceBlock(properties.mapColor(MapColor.WOOL).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<FenceGateBlock> GHOSTWILLOW_FENCE_GATE = registerBlock("ghostwillow_fence_gate",
            properties -> new FenceGateBlock(GHOSTWILLOW_WOOD_TYPE,
                    properties.mapColor(MapColor.WOOL).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<DoorBlock> GHOSTWILLOW_DOOR = registerBlock("ghostwillow_door",
            properties -> new DoorBlock(GHOSTWILLOW_SET_TYPE,
                    properties.mapColor(MapColor.WOOL).strength(3.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<TrapDoorBlock> GHOSTWILLOW_TRAPDOOR = registerBlock("ghostwillow_trapdoor",
            properties -> new TrapDoorBlock(GHOSTWILLOW_SET_TYPE,
                    properties.mapColor(MapColor.WOOL).strength(3.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<ButtonBlock> GHOSTWILLOW_BUTTON = registerBlock("ghostwillow_button",
            properties -> new ButtonBlock(GHOSTWILLOW_SET_TYPE, 30,
                    properties.noCollision().strength(0.5f).sound(SoundType.WOOD)));
    public static final DeferredBlock<PressurePlateBlock> GHOSTWILLOW_PRESSURE_PLATE = registerBlock("ghostwillow_pressure_plate",
            properties -> new PressurePlateBlock(GHOSTWILLOW_SET_TYPE,
                    properties.mapColor(MapColor.WOOL).noCollision().strength(0.5f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<UntintedParticleLeavesBlock> GHOSTWILLOW_LEAVES = registerBlock("ghostwillow_leaves",
            properties -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.PALE_OAK_LEAVES,
                    properties.mapColor(MapColor.PLANT).strength(0.2F)
                            .randomTicks().sound(SoundType.GRASS).noOcclusion().ignitedByLava()
                            .pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<SaplingBlock> GHOSTWILLOW_SAPLING = registerBlock("ghostwillow_sapling",
            properties -> new SaplingBlock(GHOSTWILLOW_GROWER, properties.mapColor(MapColor.PLANT).noCollision()
                    .randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    // ─── Gravewood ───────────────────────────────────────────────────────────
    public static final DeferredBlock<RotatedPillarBlock> GRAVEWOOD_LOG = registerBlock("gravewood_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GRAVEWOOD_LOG = registerBlock("stripped_gravewood_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<RotatedPillarBlock> GRAVEWOOD_WOOD = registerBlock("gravewood_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GRAVEWOOD_WOOD = registerBlock("stripped_gravewood_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> GRAVEWOOD_PLANKS = registerBlock("gravewood_planks",
            properties -> new Block(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<StairBlock> GRAVEWOOD_STAIRS = registerBlock("gravewood_stairs",
            properties -> new StairBlock(GRAVEWOOD_PLANKS.get().defaultBlockState(),
                    properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<SlabBlock> GRAVEWOOD_SLAB = registerBlock("gravewood_slab",
            properties -> new SlabBlock(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<FenceBlock> GRAVEWOOD_FENCE = registerBlock("gravewood_fence",
            properties -> new FenceBlock(properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<FenceGateBlock> GRAVEWOOD_FENCE_GATE = registerBlock("gravewood_fence_gate",
            properties -> new FenceGateBlock(GRAVEWOOD_WOOD_TYPE,
                    properties.mapColor(MapColor.COLOR_BROWN).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<DoorBlock> GRAVEWOOD_DOOR = registerBlock("gravewood_door",
            properties -> new DoorBlock(GRAVEWOOD_SET_TYPE,
                    properties.mapColor(MapColor.COLOR_BROWN).strength(3.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<TrapDoorBlock> GRAVEWOOD_TRAPDOOR = registerBlock("gravewood_trapdoor",
            properties -> new TrapDoorBlock(GRAVEWOOD_SET_TYPE,
                    properties.mapColor(MapColor.COLOR_BROWN).strength(3.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<ButtonBlock> GRAVEWOOD_BUTTON = registerBlock("gravewood_button",
            properties -> new ButtonBlock(GRAVEWOOD_SET_TYPE, 30,
                    properties.noCollision().strength(0.5f).sound(SoundType.WOOD)));
    public static final DeferredBlock<PressurePlateBlock> GRAVEWOOD_PRESSURE_PLATE = registerBlock("gravewood_pressure_plate",
            properties -> new PressurePlateBlock(GRAVEWOOD_SET_TYPE,
                    properties.mapColor(MapColor.COLOR_BROWN).noCollision().strength(0.5f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<UntintedParticleLeavesBlock> GRAVEWOOD_LEAVES = registerBlock("gravewood_leaves",
            properties -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.PALE_OAK_LEAVES,
                    properties.mapColor(MapColor.PLANT).strength(0.2F)
                            .randomTicks().sound(SoundType.GRASS).noOcclusion().ignitedByLava()
                            .pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<SaplingBlock> GRAVEWOOD_SAPLING = registerBlock("gravewood_sapling",
            properties -> new SaplingBlock(GRAVEWOOD_GROWER, properties.mapColor(MapColor.PLANT).noCollision()
                    .randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    // ─── Cinderbark (FIRE IMMUNE) ────────────────────────────────────────────
    // No .ignitedByLava() on any block in this set — mirrors crimson/warped, which
    // are also nether wood and cannot catch fire. The animated stem texture
    // (cinderbark_stem.png + .mcmeta) is read automatically by the texture loader.
    public static final DeferredBlock<RotatedPillarBlock> CINDERBARK_STEM = registerBlock("cinderbark_stem",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.0f)
                    .sound(SoundType.STEM)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_CINDERBARK_STEM = registerBlock("stripped_cinderbark_stem",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.0f)
                    .sound(SoundType.STEM)));
    public static final DeferredBlock<RotatedPillarBlock> CINDERBARK_HYPHAE = registerBlock("cinderbark_hyphae",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.0f)
                    .sound(SoundType.STEM)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_CINDERBARK_HYPHAE = registerBlock("stripped_cinderbark_hyphae",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.0f)
                    .sound(SoundType.STEM)));
    public static final DeferredBlock<Block> CINDERBARK_PLANKS = registerBlock("cinderbark_planks",
            properties -> new Block(properties.mapColor(MapColor.COLOR_RED).strength(2.0f, 3.0f)
                    .sound(SoundType.NETHER_WOOD)));
    public static final DeferredBlock<StairBlock> CINDERBARK_STAIRS = registerBlock("cinderbark_stairs",
            properties -> new StairBlock(CINDERBARK_PLANKS.get().defaultBlockState(),
                    properties.mapColor(MapColor.COLOR_RED).strength(2.0f, 3.0f).sound(SoundType.NETHER_WOOD)));
    public static final DeferredBlock<SlabBlock> CINDERBARK_SLAB = registerBlock("cinderbark_slab",
            properties -> new SlabBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.0f, 3.0f)
                    .sound(SoundType.NETHER_WOOD)));
    public static final DeferredBlock<FenceBlock> CINDERBARK_FENCE = registerBlock("cinderbark_fence",
            properties -> new FenceBlock(properties.mapColor(MapColor.COLOR_RED).strength(2.0f, 3.0f)
                    .sound(SoundType.NETHER_WOOD)));
    public static final DeferredBlock<FenceGateBlock> CINDERBARK_FENCE_GATE = registerBlock("cinderbark_fence_gate",
            properties -> new FenceGateBlock(CINDERBARK_WOOD_TYPE,
                    properties.mapColor(MapColor.COLOR_RED).strength(2.0f, 3.0f).sound(SoundType.NETHER_WOOD)));
    public static final DeferredBlock<DoorBlock> CINDERBARK_DOOR = registerBlock("cinderbark_door",
            properties -> new DoorBlock(CINDERBARK_SET_TYPE,
                    properties.mapColor(MapColor.COLOR_RED).strength(3.0f).sound(SoundType.NETHER_WOOD).noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> CINDERBARK_TRAPDOOR = registerBlock("cinderbark_trapdoor",
            properties -> new TrapDoorBlock(CINDERBARK_SET_TYPE,
                    properties.mapColor(MapColor.COLOR_RED).strength(3.0f).sound(SoundType.NETHER_WOOD).noOcclusion()));
    public static final DeferredBlock<ButtonBlock> CINDERBARK_BUTTON = registerBlock("cinderbark_button",
            properties -> new ButtonBlock(CINDERBARK_SET_TYPE, 30,
                    properties.noCollision().strength(0.5f).sound(SoundType.NETHER_WOOD)));
    public static final DeferredBlock<PressurePlateBlock> CINDERBARK_PRESSURE_PLATE = registerBlock("cinderbark_pressure_plate",
            properties -> new PressurePlateBlock(CINDERBARK_SET_TYPE,
                    properties.mapColor(MapColor.COLOR_RED).noCollision().strength(0.5f).sound(SoundType.NETHER_WOOD)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function, Component... tooltipLines) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, function);
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()) {
            @Override
            public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
                for (Component line : tooltipLines) {
                    builder.accept(line);
                }
                super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
            }
        });
        return block;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
