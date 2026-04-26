package mopk.tmmod.registration;


import mopk.tmmod.block_func.Compressor.CompressorRecipe;
import mopk.tmmod.block_func.Crusher.CrusherRecipe;
import mopk.tmmod.block_func.Recycler.RecyclerRecipe;
import mopk.tmmod.block_func.Extractor.ExtractorRecipe;
import mopk.tmmod.block_func.InductionFurnace.InductionFurnaceRecipe;
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

    public static final Supplier<RecipeSerializer<RecyclerRecipe>> RECYCLER_SERIALIZER = SERIALIZERS.register("recycling", RecyclerRecipe.Serializer::new);
    public static final Supplier<RecipeType<RecyclerRecipe>> RECYCLER_TYPE = RECIPE_TYPES.register("recycling", () -> new RecipeType<>() {});

    public static final Supplier<RecipeSerializer<ExtractorRecipe>> EXTRACTOR_SERIALIZER = SERIALIZERS.register("extracting", ExtractorRecipe.Serializer::new);
    public static final Supplier<RecipeType<ExtractorRecipe>> EXTRACTOR_TYPE = RECIPE_TYPES.register("extracting", () -> new RecipeType<>() {});

    public static final Supplier<RecipeSerializer<CompressorRecipe>> COMPRESSOR_SERIALIZER = SERIALIZERS.register("compressing", CompressorRecipe.Serializer::new);
    public static final Supplier<RecipeType<CompressorRecipe>> COMPRESSOR_TYPE = RECIPE_TYPES.register("compressing", () -> new RecipeType<>() {});

    public static final Supplier<RecipeSerializer<InductionFurnaceRecipe>> INDUCTION_FURNACE_SERIALIZER = SERIALIZERS.register("induction_smelting", InductionFurnaceRecipe.Serializer::new);
    public static final Supplier<RecipeType<InductionFurnaceRecipe>> INDUCTION_FURNACE_TYPE = RECIPE_TYPES.register("induction_smelting", () -> new RecipeType<>() {});

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> FORGING_SERIALIZER = SERIALIZERS.register("forging", () -> new MetalformerRecipe.Serializer(MetalformerMode.FORGING));    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CUTTING_SERIALIZER = SERIALIZERS.register("cutting", () -> new MetalformerRecipe.Serializer(MetalformerMode.CUTTING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SQUEEZING_SERIALIZER = SERIALIZERS.register("squeezing", () -> new MetalformerRecipe.Serializer(MetalformerMode.SQUEEZING));

    public static final DeferredHolder<RecipeType<?>, RecipeType<MetalformerRecipe>> FORGING_TYPE = RECIPE_TYPES.register("forging", () -> new RecipeType<>() { @Override public String toString() { return "forging"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<MetalformerRecipe>> CUTTING_TYPE = RECIPE_TYPES.register("cutting", () -> new RecipeType<>() { @Override public String toString() { return "cutting"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<MetalformerRecipe>> SQUEEZING_TYPE = RECIPE_TYPES.register("squeezing", () -> new RecipeType<>() { @Override public String toString() { return "squeezing"; } });

    // Canner
    /* public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEMS_TO_ITEM_SERIALIZER = SERIALIZERS.register("canner_items_to_item", () -> new mopk.tmmod.block_func.Canner.CannerRecipe.Serializer(mopk.tmmod.block_func.Canner.CannerMode.ITEMS_TO_ITEM));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEM_TO_ITEM_FLUID_SERIALIZER = SERIALIZERS.register("canner_item_to_item_fluid", () -> new mopk.tmmod.block_func.Canner.CannerRecipe.Serializer(mopk.tmmod.block_func.Canner.CannerMode.ITEM_TO_ITEM_FLUID));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEM_FLUID_TO_ITEM_SERIALIZER = SERIALIZERS.register("canner_item_fluid_to_item", () -> new mopk.tmmod.block_func.Canner.CannerRecipe.Serializer(mopk.tmmod.block_func.Canner.CannerMode.ITEM_FLUID_TO_ITEM));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEMS_FLUID_TO_ITEM_FLUID_SERIALIZER = SERIALIZERS.register("canner_items_fluid_to_item_fluid", () -> new mopk.tmmod.block_func.Canner.CannerRecipe.Serializer(mopk.tmmod.block_func.Canner.CannerMode.ITEMS_FLUID_TO_ITEM_FLUID));

    public static final DeferredHolder<RecipeType<?>, RecipeType<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEMS_TO_ITEM_TYPE = RECIPE_TYPES.register("canner_items_to_item", () -> new RecipeType<>() { @Override public String toString() { return "canner_items_to_item"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEM_TO_ITEM_FLUID_TYPE = RECIPE_TYPES.register("canner_item_to_item_fluid", () -> new RecipeType<>() { @Override public String toString() { return "canner_item_to_item_fluid"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEM_FLUID_TO_ITEM_TYPE = RECIPE_TYPES.register("canner_item_fluid_to_item", () -> new RecipeType<>() { @Override public String toString() { return "canner_item_fluid_to_item"; } });
    public static final DeferredHolder<RecipeType<?>, RecipeType<mopk.tmmod.block_func.Canner.CannerRecipe>> CANNER_ITEMS_FLUID_TO_ITEM_FLUID_TYPE = RECIPE_TYPES.register("canner_items_fluid_to_item_fluid", () -> new RecipeType<>() { @Override public String toString() { return "canner_items_fluid_to_item_fluid"; } }); */
}



