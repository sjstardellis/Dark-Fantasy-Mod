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
 * A custom recipe for the {@link net.steve.darkfantasy.block.entity.BrewingKegBlockEntity}.
 * Takes the two solid ingredient slots (shapeless — any order) plus an implicit water bucket
 * and a heat source under the keg, and after {@link #brewTime} effective ticks fills the tank
 * with one batch of the result drink (dispensed later into a Stein).
 *
 * <p>Default brew time is the keg's full {@code BREW_DURATION_TICKS}; override per recipe with
 * the optional {@code brew_time} field.
 */
public record BrewingRecipe(
        List<Ingredient> ingredients,
        ItemStackTemplate result,
        int brewTime
) implements Recipe<BrewingRecipeInput> {

    public static final MapCodec<BrewingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(BrewingRecipe::ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(BrewingRecipe::result),
            Codec.INT.optionalFieldOf("brew_time", 6000).forGetter(BrewingRecipe::brewTime)
    ).apply(inst, BrewingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), BrewingRecipe::ingredients,
            ItemStackTemplate.STREAM_CODEC, BrewingRecipe::result,
            ByteBufCodecs.INT, BrewingRecipe::brewTime,
            BrewingRecipe::new
    );

    @Override
    public boolean matches(BrewingRecipeInput input, Level level) {
        if (ingredients.size() != input.size()) {
            return false;
        }
        // Shapeless matching across the two solid slots (greedy bipartite).
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
    public ItemStack assemble(BrewingRecipeInput input) {
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
    public RecipeSerializer<? extends Recipe<BrewingRecipeInput>> getSerializer() {
        return ModRecipes.BREWING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<BrewingRecipeInput>> getType() {
        return ModRecipes.BREWING_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
