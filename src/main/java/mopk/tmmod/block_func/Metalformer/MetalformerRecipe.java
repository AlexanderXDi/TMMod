package mopk.tmmod.block_func.Metalformer;

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
public record MetalformerRecipe(MetalformerMode mode, Ingredient input, ItemStack output, int energyPerTick, int time) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    // 2. Теперь рецепт сам понимает, какой сериализатор вернуть, исходя из своего режима
    @Override
    public RecipeSerializer<?> getSerializer() {
        return switch (mode) {
            case FORGING -> ModRecipes.FORGING_SERIALIZER.get();
            case CUTTING -> ModRecipes.CUTTING_SERIALIZER.get();
            case SQUEEZING -> ModRecipes.SQUEEZING_SERIALIZER.get();
        };
    }

    // 3. То же самое с типом рецепта
    @Override
    public RecipeType<?> getType() {
        return switch (mode) {
            case FORGING -> ModRecipes.FORGING_TYPE.get();
            case CUTTING -> ModRecipes.CUTTING_TYPE.get();
            case SQUEEZING -> ModRecipes.SQUEEZING_TYPE.get();
        };
    }

    public static class Serializer implements RecipeSerializer<MetalformerRecipe> {
        private final MetalformerMode mode;
        private final MapCodec<MetalformerRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, MetalformerRecipe> streamCodec;

        // 4. Сериализатор теперь принимает режим в конструктор!
        public Serializer(MetalformerMode mode) {
            this.mode = mode;

            this.codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(MetalformerRecipe::input),
                    ItemStack.STRICT_CODEC.fieldOf("output").forGetter(MetalformerRecipe::output),
                    Codec.INT.fieldOf("energyPerTick").forGetter(MetalformerRecipe::energyPerTick),
                    Codec.INT.fieldOf("time").forGetter(MetalformerRecipe::time)
            ).apply(inst, (input, output, energyPerTick, time) ->
                    // Автоматически подставляем режим при чтении JSON
                    new MetalformerRecipe(this.mode, input, output, energyPerTick, time)
            ));

            this.streamCodec = StreamCodec.of(
                    (buf, recipe) -> {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
                        ItemStack.STREAM_CODEC.encode(buf, recipe.output);
                        buf.writeInt(recipe.energyPerTick);
                        buf.writeInt(recipe.time);
                    },
                    buf -> new MetalformerRecipe(
                            this.mode, // Автоматически подставляем режим при получении с сервера
                            Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                            ItemStack.STREAM_CODEC.decode(buf),
                            buf.readInt(),
                            buf.readInt()
                    )
            );
        }

        @Override public MapCodec<MetalformerRecipe> codec() { return codec; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, MetalformerRecipe> streamCodec() { return streamCodec; }
    }
}
