package net.steve.darkfantasy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.steve.darkfantasy.init.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A custom recipe for the Alchemy Stand.
 * Takes exactly 3 ingredients (shapeless — any order) plus elixir fuel
 * (in mB; one elixir bucket = 1000) and produces a single output stack after
 * a cook time.
 */
public record AlchemyRecipe(
        List<Ingredient> ingredients,
        ItemStackTemplate result,
        int elixir,
        int cookTime
) implements Recipe<AlchemyRecipeInput> {

    public static final MapCodec<AlchemyRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(AlchemyRecipe::ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(AlchemyRecipe::result),
            Codec.INT.optionalFieldOf("elixir", 100).forGetter(AlchemyRecipe::elixir),
            Codec.INT.optionalFieldOf("cook_time", 200).forGetter(AlchemyRecipe::cookTime)
    ).apply(inst, AlchemyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyRecipe::ingredients,
            ItemStackTemplate.STREAM_CODEC, AlchemyRecipe::result,
            ByteBufCodecs.INT, AlchemyRecipe::elixir,
            ByteBufCodecs.INT, AlchemyRecipe::cookTime,
            AlchemyRecipe::new
    );

    @Override
    public boolean matches(AlchemyRecipeInput input, Level level) {
        if (ingredients.size() != input.size()) {
            return false;
        }
        // Shapeless matching: greedy bipartite. Works for 3 ingredients.
        boolean[] used = new boolean[input.size()];
        outer:
        for (Ingredient ing : ingredients) {
            for (int i = 0; i < input.size(); i++) {
                if (!used[i] && ing.test(input.getItem(i))) {
                    used[i] = true;
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(AlchemyRecipeInput input) {
        return result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<AlchemyRecipeInput>> getSerializer() {
        return ModRecipes.ALCHEMY_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<AlchemyRecipeInput>> getType() {
        return ModRecipes.ALCHEMY_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        // We don't actually show in the recipe book — just satisfies the interface.
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
