package net.steve.darkfantasy.init;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.recipe.AlchemyRecipe;
import net.steve.darkfantasy.recipe.BrewingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, DarkFantasy.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, DarkFantasy.MOD_ID);

    public static final Supplier<RecipeSerializer<AlchemyRecipe>> ALCHEMY_SERIALIZER =
            SERIALIZERS.register("alchemy",
                    () -> new RecipeSerializer<>(AlchemyRecipe.MAP_CODEC, AlchemyRecipe.STREAM_CODEC));

    public static final Supplier<RecipeType<AlchemyRecipe>> ALCHEMY_TYPE =
            TYPES.register("alchemy", () -> RecipeType.simple(
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "alchemy")));

    public static final Supplier<RecipeSerializer<BrewingRecipe>> BREWING_SERIALIZER =
            SERIALIZERS.register("brewing",
                    () -> new RecipeSerializer<>(BrewingRecipe.MAP_CODEC, BrewingRecipe.STREAM_CODEC));

    public static final Supplier<RecipeType<BrewingRecipe>> BREWING_TYPE =
            TYPES.register("brewing", () -> RecipeType.simple(
                    Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "brewing")));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
