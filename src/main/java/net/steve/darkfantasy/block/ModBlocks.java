package net.steve.darkfantasy.block;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.custom.CursedBlock;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(DarkFantasy.MOD_ID);

    public static final DeferredBlock<Block> SHADOWSTEEL_BLOCK = registerBlock("shadowsteel_block",
            properties -> new Block(properties.strength(4f)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> RAW_SHADOWSTEEL_BLOCK = registerBlock("raw_shadowsteel_block",
            properties -> new Block(properties.strength(4f)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> SHADOWSTEEL_ORE = registerBlock("shadowsteel_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> SHADOWSTEEL_DEEPSLATE_ORE = registerBlock("shadowsteel_deepslate_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 5), properties.strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));


    public static final DeferredBlock<Block> CURSED_BLOCK = registerBlock("cursed_block",
            properties -> new CursedBlock(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.DECORATED_POT)), Component.translatable("tooltip.darkfantasy.cursed_block.tooltip"));

    public static final DeferredBlock<Block> SHADOWSTEEL_STAIRS = registerBlock("shadowsteel_stairs",
            properties -> new StairBlock(ModBlocks.SHADOWSTEEL_BLOCK.get().defaultBlockState(),
                    properties.strength(3f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> SHADOWSTEEL_SLAB = registerBlock("shadowsteel_slab",
            properties -> new SlabBlock(properties.strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> SHADOWSTEEL_PRESSURE_PLATE = registerBlock("shadowsteel_pressure_plate",
            properties -> new PressurePlateBlock(BlockSetType.IRON, properties
                    .mapColor(MapColor.COLOR_BLACK).forceSolidOn().instrument(NoteBlockInstrument.BASS)
                    .requiresCorrectToolForDrops().noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> SHADOWSTEEL_BUTTON = registerBlock("shadowsteel_button",
            properties -> new ButtonBlock(BlockSetType.IRON, 20, properties
                    .noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> SHADOWSTEEL_FENCE = registerBlock("shadowsteel_fence",
            properties -> new FenceBlock(properties.strength(2F)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> SHADOWSTEEL_FENCE_GATE = registerBlock("shadowsteel_fence_gate",
            properties -> new FenceGateBlock(WoodType.ACACIA, properties.strength(2F)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> SHADOWSTEEL_WALL = registerBlock("shadowsteel_wall",
            properties -> new WallBlock(properties.strength(2F)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> SHADOWSTEEL_DOOR = registerBlock("shadowsteel_door",
            properties -> new DoorBlock(BlockSetType.IRON, properties.strength(2F)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST).noOcclusion()));
    public static final DeferredBlock<Block> SHADOWSTEEL_TRAPDOOR = registerBlock("shadowsteel_trapdoor",
            properties -> new TrapDoorBlock(BlockSetType.IRON, properties.strength(2F)
                    .requiresCorrectToolForDrops().sound(SoundType.AMETHYST).noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function, Component... components) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn, components);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block, Component... components) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()) {
            @Override
            public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
                for (var component : components) {
                    builder.accept(component);
                }
                super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
            }
        });
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
