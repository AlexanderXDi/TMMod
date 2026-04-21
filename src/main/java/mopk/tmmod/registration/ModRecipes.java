package mopk.tmmod.registration;


import mopk.tmmod.block_func.Compressor.CompressorRecipe;
import mopk.tmmod.block_func.Crusher.CrusherRecipe;
import mopk.tmmod.block_func.Extractor.ExtractorRecipe;
import mopk.tmmod.block_func.Metalformer.MetalformerMode;
import mopk.tmmod.block_func.Metalformer.MetalformerRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "tmmod");
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "tmmod");

    public static final Supplier<RecipeSerializer<CrusherRecipe>> CRUSHER_SERIALIZER = SERIALIZERS.register("crushing", CrusherRecipe.Serializer::new);
    public static final Supplier<RecipeType<CrusherRecipe>> CRUSHER_TYPE = RECIPE_TYPES.register("crushing", () -> new RecipeType<>() {});

    public static final Supplier<RecipeSerializer<ExtractorRecipe>> EXTRACTOR_SERIALIZER = SERIALIZERS.register("extracting", ExtractorRecipe.Serializer::new);
    public static final Supplier<RecipeType<ExtractorRecipe>> EXTRACTOR_TYPE = RECIPE_TYPES.register("extracting", () -> new RecipeType<>() {});

    public static final Supplier<RecipeSerializer<CompressorRecipe>> COMPRESSOR_SERIALIZER = SERIALIZERS.register("compressing", CompressorRecipe.Serializer::new);
    public static final Supplier<RecipeType<CompressorRecipe>> COMPRESSOR_TYPE = RECIPE_TYPES.register("compressing", () -> new RecipeType<>() {});

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> FORGING_SERIALIZER = SERIALIZERS.register("forging", () -> new MetalformerRecipe.Serializer(MetalformerMode.FORGING));    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CUTTING_SERIALIZER = SERIALIZERS.register("cutting", () -> new MetalformerRecipe.Serializer(MetalformerMode.CUTTING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SQUEEZING_SERIALIZER = SERIALIZERS.register("squeezing", () -> new MetalformerRecipe.Serializer(MetalformerMode.SQUEEZING));

    public static final DeferredHolder<RecipeType<?>, RecipeType<MetalformerRecipe>> FORGING_TYPE = RECIPE_TYPES.register("forging", () -> new RecipeType<>() { @Override public String toString() { return "forging"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<MetalformerRecipe>> CUTTING_TYPE = RECIPE_TYPES.register("cutting", () -> new RecipeType<>() { @Override public String toString() { return "cutting"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<MetalformerRecipe>> SQUEEZING_TYPE = RECIPE_TYPES.register("squeezing", () -> new RecipeType<>() { @Override public String toString() { return "squeezing"; } });
}



