package net.steve.darkfantasy.creativemodetab;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DarkFantasy.MOD_ID);

    public static final Supplier<CreativeModeTab> DARKFANTASY_TAB = CREATIVE_MODE_TABS.register("darkfantasy_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SHADOWSTEEL.get()))
                    .title(Component.translatable("creativetab.darkfantasy.darkfantasy_tab"))
                    .withTabsBefore(CreativeModeTabs.INGREDIENTS)
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SHADOWSTEEL);
                        output.accept(ModItems.RAW_SHADOWSTEEL);
                        output.accept(ModItems.SHADOWSTEEL_SWORD);
                        output.accept(ModItems.SHADOWSTEEL_PICKAXE);
                        output.accept(ModItems.SHADOWSTEEL_AXE);
                        output.accept(ModItems.SHADOWSTEEL_SHOVEL);
                        output.accept(ModItems.SHADOWSTEEL_HOE);
                        output.accept(ModItems.SHADOWSTEEL_HELMET);
                        output.accept(ModItems.SHADOWSTEEL_CHESTPLATE);
                        output.accept(ModItems.SHADOWSTEEL_LEGGINGS);
                        output.accept(ModItems.SHADOWSTEEL_BOOTS);
                        output.accept(ModItems.SHADOWSTEEL_DAGGERS);

                        output.accept(ModItems.ECLIPSIUM);
                        output.accept(ModItems.ECLIPSIUM_SWORD);
                        output.accept(ModItems.ECLIPSIUM_PICKAXE);
                        output.accept(ModItems.ECLIPSIUM_AXE);
                        output.accept(ModItems.ECLIPSIUM_SHOVEL);
                        output.accept(ModItems.ECLIPSIUM_HOE);
                        output.accept(ModItems.ECLIPSIUM_HELMET);
                        output.accept(ModItems.ECLIPSIUM_CHESTPLATE);
                        output.accept(ModItems.ECLIPSIUM_LEGGINGS);
                        output.accept(ModItems.ECLIPSIUM_BOOTS);
                        output.accept(ModItems.ECLIPSE_CROWN);
                        output.accept(ModItems.ECLIPSE_GREATSWORD);
                        output.accept(ModBlocks.OBSIDIAN_BRICK);
                        output.accept(ModBlocks.OBSIDIAN_GLASS);

                        output.accept(ModItems.MOONSILVER);
                        output.accept(ModItems.RAW_MOONSILVER);

                        output.accept(ModItems.MOONSILVER_SWORD);
                        output.accept(ModItems.MOONSILVER_PICKAXE);
                        output.accept(ModItems.MOONSILVER_AXE);
                        output.accept(ModItems.MOONSILVER_SHOVEL);
                        output.accept(ModItems.MOONSILVER_HOE);
                        output.accept(ModItems.MOONSILVER_HELMET);
                        output.accept(ModItems.MOONSILVER_CHESTPLATE);
                        output.accept(ModItems.MOONSILVER_LEGGINGS);
                        output.accept(ModItems.MOONSILVER_BOOTS);
                        output.accept(ModItems.MOONSILVER_SCYTHE);

                        output.accept(ModItems.DAWNMETAL);
                        output.accept(ModItems.RAW_DAWNMETAL);
                        output.accept(ModItems.DAWNMETAL_SWORD);
                        output.accept(ModItems.DAWNMETAL_PICKAXE);
                        output.accept(ModItems.DAWNMETAL_AXE);
                        output.accept(ModItems.DAWNMETAL_SHOVEL);
                        output.accept(ModItems.DAWNMETAL_HOE);
                        output.accept(ModItems.DAWNMETAL_HELMET);
                        output.accept(ModItems.DAWNMETAL_CHESTPLATE);
                        output.accept(ModItems.DAWNMETAL_LEGGINGS);
                        output.accept(ModItems.DAWNMETAL_BOOTS);
                        output.accept(ModItems.DAWNMETAL_SUNLANCE);

                        output.accept(ModItems.CRESCENT_BOW);
                        output.accept(ModItems.THROWING_DAGGER);
                        output.accept(ModItems.DAWNMETAL_ARBALEST);

                        output.accept(ModBlocks.SHADOWSTEEL_ORE);
                        output.accept(ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE);

                        output.accept(ModBlocks.DAWNMETAL_ORE);
                        output.accept(ModBlocks.DAWNMETAL_DEEPSLATE_ORE);
                        output.accept(ModBlocks.MOONSILVER_ORE);
                        output.accept(ModBlocks.MOONSILVER_DEEPSLATE_ORE);

                        output.accept(ModItems.MERCURYGLASS);
                        output.accept(ModItems.EMBERSTONE);
                        output.accept(ModItems.LARIMAR_PEARL);
                        output.accept(ModItems.ARCANE_ASH);
                        output.accept(ModItems.STORM_SCALE);
                        output.accept(ModItems.FALLEN_STAR);
                        output.accept(ModItems.UMBRA_ESSENCE);
                        output.accept(ModItems.CINDER_FANG);
                        output.accept(ModItems.HAG_ICHOR);
                        output.accept(ModItems.MOONLIGHT_ELIXIR);
                        output.accept(ModItems.STONESKIN_ELIXIR);
                        output.accept(ModItems.EMBERBLOOD_ELIXIR);
                        output.accept(ModItems.WISPSTEP_ELIXIR);
                        output.accept(ModItems.STARLIGHT_ELIXIR);
                        output.accept(ModItems.UMBRAL_ELIXIR);
                        output.accept(ModItems.HOUNDSBLOOD_ELIXIR);
                        output.accept(ModItems.WITCHBANE_ELIXIR);
                        output.accept(ModBlocks.MERCURYGLASS_ORE);
                        output.accept(ModBlocks.EMBERSTONE_ORE);
                        output.accept(ModBlocks.LARIMAR_PEARL_ORE);

                        output.accept(ModBlocks.ALCHEMY_STAND);
                        output.accept(ModBlocks.ENCHANTED_BOOKSHELF);

                        output.accept(ModItems.FAIRY_DUST);
                        output.accept(ModItems.LYTEBUG_DUST);
                        output.accept(ModBlocks.LYTESTONE);
                        output.accept(ModBlocks.GNOME_BURROW);
                        output.accept(ModItems.ELIXIR_BUCKET);
                        output.accept(ModItems.HOPS);
                        output.accept(ModItems.BEER);
                        output.accept(ModItems.DARK_ALE);
                        output.accept(ModItems.HONEY_MEAD);
                        output.accept(ModItems.GLOWBREW);
                        output.accept(ModItems.MUSHROOM_STOUT);
                        output.accept(ModItems.WITHER_STOUT);
                        output.accept(ModItems.BATTLE_BREW);
                        output.accept(ModItems.STEIN_GLASS);
                        output.accept(ModBlocks.BREWING_KEG);
                        output.accept(ModItems.LIGHTNING_STAFF);
                        output.accept(ModItems.FIREBALL_STAFF);
                        output.accept(ModItems.FROST_STAFF);
                        output.accept(ModItems.BLINK_STAFF);
                        output.accept(ModItems.CINDER_STAFF);
                        output.accept(ModItems.WARDING_TOME);
                        output.accept(ModItems.MAELSTROM_TOME);
                        output.accept(ModItems.WAYFARER_TOME);
                        output.accept(ModItems.WITHER_SKULL_TOME);
                        output.accept(ModItems.PROSPECTOR_TOME);
                        output.accept(ModItems.STASIS_TOME);
                        output.accept(ModItems.EVOKER_CLAW_TOME);
                        output.accept(ModItems.ECLIPSE_TOME);
                        output.accept(ModItems.FAIRY_SPAWN_EGG);
                        output.accept(ModItems.WIZARD_SPAWN_EGG);
                        output.accept(ModItems.ELECTRO_DRAGON_SPAWN_EGG);
                        output.accept(ModItems.GOBLIN_SPAWN_EGG);
                        output.accept(ModItems.GNOME_SPAWN_EGG);
                        output.accept(ModItems.LYTEBUG_SPAWN_EGG);
                        output.accept(ModItems.ECLIPSE_KING_SPAWN_EGG);
                        output.accept(ModItems.CINDER_HOUND_SPAWN_EGG);
                        output.accept(ModItems.UMBRAL_WRAITH_SPAWN_EGG);
                        output.accept(ModItems.BOG_HAG_SPAWN_EGG);

                        // Ghostwillow wood set
                        output.accept(ModBlocks.GHOSTWILLOW_LOG);
                        output.accept(ModBlocks.STRIPPED_GHOSTWILLOW_LOG);
                        output.accept(ModBlocks.GHOSTWILLOW_WOOD);
                        output.accept(ModBlocks.STRIPPED_GHOSTWILLOW_WOOD);
                        output.accept(ModBlocks.GHOSTWILLOW_PLANKS);
                        output.accept(ModBlocks.GHOSTWILLOW_STAIRS);
                        output.accept(ModBlocks.GHOSTWILLOW_SLAB);
                        output.accept(ModBlocks.GHOSTWILLOW_FENCE);
                        output.accept(ModBlocks.GHOSTWILLOW_FENCE_GATE);
                        output.accept(ModBlocks.GHOSTWILLOW_DOOR);
                        output.accept(ModBlocks.GHOSTWILLOW_TRAPDOOR);
                        output.accept(ModBlocks.GHOSTWILLOW_BUTTON);
                        output.accept(ModBlocks.GHOSTWILLOW_PRESSURE_PLATE);
                        output.accept(ModBlocks.GHOSTWILLOW_LEAVES);
                        output.accept(ModBlocks.GHOSTWILLOW_SAPLING);

                        // Gravewood wood set
                        output.accept(ModBlocks.GRAVEWOOD_LOG);
                        output.accept(ModBlocks.STRIPPED_GRAVEWOOD_LOG);
                        output.accept(ModBlocks.GRAVEWOOD_WOOD);
                        output.accept(ModBlocks.STRIPPED_GRAVEWOOD_WOOD);
                        output.accept(ModBlocks.GRAVEWOOD_PLANKS);
                        output.accept(ModBlocks.GRAVEWOOD_STAIRS);
                        output.accept(ModBlocks.GRAVEWOOD_SLAB);
                        output.accept(ModBlocks.GRAVEWOOD_FENCE);
                        output.accept(ModBlocks.GRAVEWOOD_FENCE_GATE);
                        output.accept(ModBlocks.GRAVEWOOD_DOOR);
                        output.accept(ModBlocks.GRAVEWOOD_TRAPDOOR);
                        output.accept(ModBlocks.GRAVEWOOD_BUTTON);
                        output.accept(ModBlocks.GRAVEWOOD_PRESSURE_PLATE);
                        output.accept(ModBlocks.GRAVEWOOD_LEAVES);
                        output.accept(ModBlocks.GRAVEWOOD_SAPLING);

                        // Cinderbark wood set (fire-immune, nether-style stem)
                        output.accept(ModBlocks.CINDERBARK_STEM);
                        output.accept(ModBlocks.STRIPPED_CINDERBARK_STEM);
                        output.accept(ModBlocks.CINDERBARK_HYPHAE);
                        output.accept(ModBlocks.STRIPPED_CINDERBARK_HYPHAE);
                        output.accept(ModBlocks.CINDERBARK_PLANKS);
                        output.accept(ModBlocks.CINDERBARK_STAIRS);
                        output.accept(ModBlocks.CINDERBARK_SLAB);
                        output.accept(ModBlocks.CINDERBARK_FENCE);
                        output.accept(ModBlocks.CINDERBARK_FENCE_GATE);
                        output.accept(ModBlocks.CINDERBARK_DOOR);
                        output.accept(ModBlocks.CINDERBARK_TRAPDOOR);
                        output.accept(ModBlocks.CINDERBARK_BUTTON);
                        output.accept(ModBlocks.CINDERBARK_PRESSURE_PLATE);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
