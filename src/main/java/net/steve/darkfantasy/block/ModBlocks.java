package net.steve.darkfantasy.block;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.custom.AlchemyStandBlock;
import net.steve.darkfantasy.block.custom.BrewingKegBlock;
import net.steve.darkfantasy.block.custom.CursedBlock;
import net.steve.darkfantasy.block.custom.EnchantedBookshelfBlock;
import net.steve.darkfantasy.block.custom.HopsBlock;
import net.steve.darkfantasy.block.custom.SkylandsPortalBlock;
import net.steve.darkfantasy.block.custom.TwilightPortalBlock;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    public static final DeferredBlock<Block> CURSED_BLOCK = registerBlock("cursed_block",
            properties -> new CursedBlock(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.DECORATED_POT)), Component.translatable("tooltip.darkfantasy.cursed_block.tooltip"));

    public static final DeferredBlock<Block> ALCHEMY_STAND = registerBlock("alchemy_stand",
            properties -> new AlchemyStandBlock(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));

    public static final DeferredBlock<Block> ENCHANTED_BOOKSHELF = registerBlock("enchanted_bookshelf",
            properties -> new EnchantedBookshelfBlock(properties.strength(1.8f)
                    .sound(SoundType.WOOD).lightLevel(state -> 4).ignitedByLava()));

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
