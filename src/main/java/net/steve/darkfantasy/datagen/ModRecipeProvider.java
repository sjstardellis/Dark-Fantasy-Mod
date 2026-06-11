package net.steve.darkfantasy.datagen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.block.ModBlocks;
import net.steve.darkfantasy.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "DarkFantasy Recipes";
        }
    }

    @Override
    protected void buildRecipes() {
        // Smelt raw_shadowsteel or ores -> shadowsteel
        List<ItemLike> SHADOWSTEEL_SMELTABLES = List.of(
                ModItems.RAW_SHADOWSTEEL,
                ModBlocks.SHADOWSTEEL_ORE,
                ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE);

        oreSmelting(SHADOWSTEEL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.SHADOWSTEEL.get(), 0.25f, 200, "shadowsteel");
        oreBlasting(SHADOWSTEEL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.SHADOWSTEEL.get(), 0.25f, 100, "shadowsteel");

        // Smelt raw_moonsilver or ores -> moonsilver (chain was previously missing,
        // leaving the ingot unobtainable; required for moonsilver gear).
        List<ItemLike> MOONSILVER_SMELTABLES = List.of(
                ModItems.RAW_MOONSILVER,
                ModBlocks.MOONSILVER_ORE,
                ModBlocks.MOONSILVER_DEEPSLATE_ORE);
        oreSmelting(MOONSILVER_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.MOONSILVER.get(), 0.7f, 200, "moonsilver");
        oreBlasting(MOONSILVER_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.MOONSILVER.get(), 0.7f, 100, "moonsilver");

        // Smelt raw_dawnmetal or ores -> dawnmetal (chain was previously missing).
        List<ItemLike> DAWNMETAL_SMELTABLES = List.of(
                ModItems.RAW_DAWNMETAL,
                ModBlocks.DAWNMETAL_ORE,
                ModBlocks.DAWNMETAL_DEEPSLATE_ORE);
        oreSmelting(DAWNMETAL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.DAWNMETAL.get(), 1.0f, 200, "dawnmetal");
        oreBlasting(DAWNMETAL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.DAWNMETAL.get(), 1.0f, 100, "dawnmetal");

        // ── Metal tools + armor (eclipsium ingots come from the alchemy stand) ──
        gearRecipes(ModItems.MOONSILVER.get(),
                ModItems.MOONSILVER_SWORD.get(), ModItems.MOONSILVER_PICKAXE.get(), ModItems.MOONSILVER_AXE.get(),
                ModItems.MOONSILVER_SHOVEL.get(), ModItems.MOONSILVER_HOE.get(), ModItems.MOONSILVER_HELMET.get(),
                ModItems.MOONSILVER_CHESTPLATE.get(), ModItems.MOONSILVER_LEGGINGS.get(), ModItems.MOONSILVER_BOOTS.get());
        gearRecipes(ModItems.SHADOWSTEEL.get(),
                ModItems.SHADOWSTEEL_SWORD.get(), ModItems.SHADOWSTEEL_PICKAXE.get(), ModItems.SHADOWSTEEL_AXE.get(),
                ModItems.SHADOWSTEEL_SHOVEL.get(), ModItems.SHADOWSTEEL_HOE.get(), ModItems.SHADOWSTEEL_HELMET.get(),
                ModItems.SHADOWSTEEL_CHESTPLATE.get(), ModItems.SHADOWSTEEL_LEGGINGS.get(), ModItems.SHADOWSTEEL_BOOTS.get());
        gearRecipes(ModItems.DAWNMETAL.get(),
                ModItems.DAWNMETAL_SWORD.get(), ModItems.DAWNMETAL_PICKAXE.get(), ModItems.DAWNMETAL_AXE.get(),
                ModItems.DAWNMETAL_SHOVEL.get(), ModItems.DAWNMETAL_HOE.get(), ModItems.DAWNMETAL_HELMET.get(),
                ModItems.DAWNMETAL_CHESTPLATE.get(), ModItems.DAWNMETAL_LEGGINGS.get(), ModItems.DAWNMETAL_BOOTS.get());
        gearRecipes(ModItems.ECLIPSIUM.get(),
                ModItems.ECLIPSIUM_SWORD.get(), ModItems.ECLIPSIUM_PICKAXE.get(), ModItems.ECLIPSIUM_AXE.get(),
                ModItems.ECLIPSIUM_SHOVEL.get(), ModItems.ECLIPSIUM_HOE.get(), ModItems.ECLIPSIUM_HELMET.get(),
                ModItems.ECLIPSIUM_CHESTPLATE.get(), ModItems.ECLIPSIUM_LEGGINGS.get(), ModItems.ECLIPSIUM_BOOTS.get());

        // ── Signature weapons ────────────────────────────────────────────
        this.shaped(RecipeCategory.COMBAT, ModItems.MOONSILVER_SCYTHE.get())
                .define('M', ModItems.MOONSILVER.get()).define('S', Items.STICK)
                .pattern("MMM").pattern("  M").pattern("  S")
                .unlockedBy("has_moonsilver", has(ModItems.MOONSILVER.get())).save(output);
        this.shaped(RecipeCategory.COMBAT, ModItems.SHADOWSTEEL_DAGGERS.get())
                .define('M', ModItems.SHADOWSTEEL.get()).define('S', Items.STICK)
                .pattern("M").pattern("S")
                .unlockedBy("has_shadowsteel", has(ModItems.SHADOWSTEEL.get())).save(output);
        this.shaped(RecipeCategory.COMBAT, ModItems.DAWNMETAL_SUNLANCE.get())
                .define('M', ModItems.DAWNMETAL.get()).define('S', Items.STICK)
                .pattern("  M").pattern(" S ").pattern("S  ")
                .unlockedBy("has_dawnmetal", has(ModItems.DAWNMETAL.get())).save(output);

        // Staves — previously creative-only (no recipe). Tie them to fire/mod materials.
        this.shaped(RecipeCategory.TOOLS, ModItems.FIREBALL_STAFF.get())
                .define('D', ModItems.DAWNMETAL.get()).define('B', Items.BLAZE_ROD)
                .pattern(" D ").pattern(" B ").pattern(" B ")
                .unlockedBy("has_dawnmetal", has(ModItems.DAWNMETAL.get())).save(output);
        this.shaped(RecipeCategory.TOOLS, ModItems.LIGHTNING_STAFF.get())
                .define('R', Items.LIGHTNING_ROD).define('B', Items.BLAZE_ROD)
                .pattern(" R ").pattern(" B ").pattern(" B ")
                .unlockedBy("has_lightning_rod", has(Items.LIGHTNING_ROD)).save(output);

        // Gnome burrow — ghostwillow planks ringing a mushroom (a gnome's toadstool home).
        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.GNOME_BURROW.get())
                .define('P', ModBlocks.GHOSTWILLOW_PLANKS.get()).define('M', Items.BROWN_MUSHROOM)
                .pattern("PPP").pattern("PMP").pattern("PPP")
                .unlockedBy("has_brown_mushroom", has(Items.BROWN_MUSHROOM)).save(output);

        // Enchanted bookshelf: oak planks on top and bottom rows, bottle of enchanting
        // flanking a regular bookshelf in the middle.
        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.ENCHANTED_BOOKSHELF.get())
                .define('P', Items.OAK_PLANKS)
                .define('E', Items.EXPERIENCE_BOTTLE)
                .define('B', Blocks.BOOKSHELF)
                .pattern("PPP")
                .pattern("EBE")
                .pattern("PPP")
                .unlockedBy("has_bookshelf", has(Blocks.BOOKSHELF))
                .unlockedBy("has_experience_bottle", has(Items.EXPERIENCE_BOTTLE))
                .save(output);
    }

    /** Standard sword/pickaxe/axe/shovel/hoe + 4 armor recipes for one metal ({@code mat} = the ingot). */
    private void gearRecipes(ItemLike mat, ItemLike sword, ItemLike pickaxe, ItemLike axe,
                             ItemLike shovel, ItemLike hoe, ItemLike helmet, ItemLike chestplate,
                             ItemLike leggings, ItemLike boots) {
        String crit = "has_" + getItemName(mat);
        this.shaped(RecipeCategory.COMBAT, sword).define('M', mat).define('S', Items.STICK)
                .pattern("M").pattern("M").pattern("S").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.TOOLS, pickaxe).define('M', mat).define('S', Items.STICK)
                .pattern("MMM").pattern(" S ").pattern(" S ").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.TOOLS, axe).define('M', mat).define('S', Items.STICK)
                .pattern("MM").pattern("MS").pattern(" S").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.TOOLS, shovel).define('M', mat).define('S', Items.STICK)
                .pattern("M").pattern("S").pattern("S").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.TOOLS, hoe).define('M', mat).define('S', Items.STICK)
                .pattern("MM").pattern(" S").pattern(" S").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.COMBAT, helmet).define('M', mat)
                .pattern("MMM").pattern("M M").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.COMBAT, chestplate).define('M', mat)
                .pattern("M M").pattern("MMM").pattern("MMM").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.COMBAT, leggings).define('M', mat)
                .pattern("MMM").pattern("M M").pattern("M M").unlockedBy(crit, has(mat)).save(output);
        this.shaped(RecipeCategory.COMBAT, boots).define('M', mat)
                .pattern("M M").pattern("M M").unlockedBy(crit, has(mat)).save(output);
    }

    @Override
    protected <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables,
                                                                RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result,
                                                                float experience, int cookingTime, String group, String fromDesc) {
        for (ItemLike itemlike : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), craftingCategory, cookingCategory, result, experience, cookingTime, factory)
                    .group(group).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(output, DarkFantasy.MOD_ID + ":" + getItemName(result) + fromDesc + "_" + getItemName(itemlike));
        }
    }
}
