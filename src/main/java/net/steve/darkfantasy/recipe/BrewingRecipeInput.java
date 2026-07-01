package net.steve.darkfantasy.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe input for the Brewing Keg — the two <em>solid</em> ingredient slots. The water
 * bucket and heat requirement are handled by the block entity, not the recipe.
 */
public record BrewingRecipeInput(ItemStack slot0, ItemStack slot1) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> slot0;
            case 1 -> slot1;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
