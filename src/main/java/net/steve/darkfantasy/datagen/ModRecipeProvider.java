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
        // Shadowsteel block <-> 9 ingots
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHADOWSTEEL_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.SHADOWSTEEL.get())
                .unlockedBy(getHasName(ModItems.SHADOWSTEEL.get()), has(ModItems.SHADOWSTEEL))
                .group("shadowsteel")
                .save(output);

        shapeless(RecipeCategory.MISC, ModItems.SHADOWSTEEL.get(), 9)
                .requires(ModBlocks.SHADOWSTEEL_BLOCK)
                .unlockedBy(getHasName(ModBlocks.SHADOWSTEEL_BLOCK.get()), has(ModBlocks.SHADOWSTEEL_BLOCK))
                .group("shadowsteel")
                .save(output);

        // Smelt raw_shadowsteel or ores -> shadowsteel
        List<ItemLike> SHADOWSTEEL_SMELTABLES = List.of(
                ModItems.RAW_SHADOWSTEEL,
                ModBlocks.SHADOWSTEEL_ORE,
                ModBlocks.SHADOWSTEEL_DEEPSLATE_ORE);

        oreSmelting(SHADOWSTEEL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.SHADOWSTEEL.get(), 0.25f, 200, "shadowsteel");
        oreBlasting(SHADOWSTEEL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC,
                ModItems.SHADOWSTEEL.get(), 0.25f, 100, "shadowsteel");
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
