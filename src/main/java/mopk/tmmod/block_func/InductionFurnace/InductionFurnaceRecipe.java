package mopk.tmmod.block_func.InductionFurnace;

import mopk.tmmod.registration.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record InductionFurnaceRecipe(Ingredient input1, int count1, Ingredient input2, int count2, ItemStack output1, ItemStack output2, int heatPerSecond, int time, int euPerTick) implements Recipe<RecipeInput> {
    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (input.size() < 2) return false;
        ItemStack s0 = input.getItem(0);
        ItemStack s1 = input.getItem(1);
        
        if (input2 == null || input2.isEmpty()) {
            return (input1.test(s0) && s0.getCount() >= count1) || (input1.test(s1) && s1.getCount() >= count1);
        }
        
        boolean direct = input1.test(s0) && s0.getCount() >= count1 && input2.test(s1) && s1.getCount() >= count2;
        boolean swapped = input1.test(s1) && s1.getCount() >= count1 && input2.test(s0) && s0.getCount() >= count2;
        return direct || swapped;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return output1.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.INDUCTION_FURNACE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.INDUCTION_FURNACE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<InductionFurnaceRecipe> {
        public static final MapCodec<InductionFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("input1").forGetter(InductionFurnaceRecipe::input1),
                Codec.INT.optionalFieldOf("count1", 1).forGetter(InductionFurnaceRecipe::count1),
                Ingredient.CODEC.optionalFieldOf("input2", Ingredient.EMPTY).forGetter(InductionFurnaceRecipe::input2),
                Codec.INT.optionalFieldOf("count2", 1).forGetter(InductionFurnaceRecipe::count2),
                ItemStack.STRICT_CODEC.fieldOf("output1").forGetter(InductionFurnaceRecipe::output1),
                ItemStack.STRICT_CODEC.optionalFieldOf("output2", ItemStack.EMPTY).forGetter(InductionFurnaceRecipe::output2),
                Codec.INT.fieldOf("heatPerSecond").forGetter(InductionFurnaceRecipe::heatPerSecond),
                Codec.INT.fieldOf("time").forGetter(InductionFurnaceRecipe::time),
                Codec.INT.optionalFieldOf("euPerTick", 20).forGetter(InductionFurnaceRecipe::euPerTick)
        ).apply(inst, InductionFurnaceRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, InductionFurnaceRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input1);
                    buf.writeInt(recipe.count1);
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input2);
                    buf.writeInt(recipe.count2);
                    ItemStack.STREAM_CODEC.encode(buf, recipe.output1);
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.output2);
                    buf.writeInt(recipe.heatPerSecond);
                    buf.writeInt(recipe.time);
                    buf.writeInt(recipe.euPerTick);
                },
                buf -> new InductionFurnaceRecipe(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        buf.readInt(),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        buf.readInt(),
                        ItemStack.STREAM_CODEC.decode(buf),
                        ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt()
                )
        );

        @Override public MapCodec<InductionFurnaceRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, InductionFurnaceRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
