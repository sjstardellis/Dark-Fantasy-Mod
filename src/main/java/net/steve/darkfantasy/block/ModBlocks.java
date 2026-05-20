package net.steve.darkfantasy.block;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.custom.AlchemyStandBlock;
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

    public static final DeferredBlock<Block> SHADOWSTEEL_ORE = registerBlock("shadowsteel_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.strength(3f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> SHADOWSTEEL_DEEPSLATE_ORE = registerBlock("shadowsteel_deepslate_ore",
            properties -> new DropExperienceBlock(UniformInt.of(3, 5), properties.strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));


    public static final DeferredBlock<Block> CURSED_BLOCK = registerBlock("cursed_block",
            properties -> new CursedBlock(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.DECORATED_POT)), Component.translatable("tooltip.darkfantasy.cursed_block.tooltip"));

    public static final DeferredBlock<Block> ALCHEMY_STAND = registerBlock("alchemy_stand",
            properties -> new AlchemyStandBlock(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));


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
