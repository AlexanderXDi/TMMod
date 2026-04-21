package mopk.tmmod.block_func.Compressor;

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

public record CompressorRecipe(Ingredient input, ItemStack output, int energyPerTick, int time) implements Recipe<RecipeInput> {
    @Override public boolean matches(RecipeInput input, Level level) { return this.input.test(input.getItem(0)); }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return output.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return output; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.COMPRESSOR_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.COMPRESSOR_TYPE.get(); }

    public static class Serializer implements RecipeSerializer<CompressorRecipe> {
        public static final MapCodec<CompressorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("input").forGetter(CompressorRecipe::input),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(CompressorRecipe::output),
                Codec.INT.fieldOf("energyPerTick").forGetter(CompressorRecipe::energyPerTick),
                Codec.INT.fieldOf("time").forGetter(CompressorRecipe::time)
        ).apply(inst, CompressorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
                    ItemStack.STREAM_CODEC.encode(buf, recipe.output);
                    buf.writeInt(recipe.energyPerTick);
                    buf.writeInt(recipe.time);
                },
                buf -> new CompressorRecipe(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        ItemStack.STREAM_CODEC.decode(buf),
                        buf.readInt(),
                        buf.readInt()
                )
        );

        @Override public MapCodec<CompressorRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
