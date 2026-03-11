package mopk.tmmod.etc;


import mopk.tmmod.etc.Crusher.CrusherRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "tmmod");
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "tmmod");

    public static final Supplier<RecipeSerializer<CrusherRecipe>> CRUSHER_SERIALIZER = SERIALIZERS.register("crushing", CrusherRecipe.Serializer::new);
    public static final Supplier<RecipeType<CrusherRecipe>> CRUSHER_TYPE = RECIPE_TYPES.register("crushing", () -> new RecipeType<>() {});
}
