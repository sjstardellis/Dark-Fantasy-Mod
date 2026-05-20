package net.steve.darkfantasy.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe input for the Alchemy Stand — wraps the three input slots.
 * The output slot is intentionally not included.
 */
public record AlchemyRecipeInput(ItemStack slot0, ItemStack slot1, ItemStack slot2) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> slot0;
            case 1 -> slot1;
            case 2 -> slot2;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 3;
    }
}
