/* 
package mopk.tmmod.block_func.Canner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mopk.tmmod.registration.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public record CannerRecipe(
        CannerMode mode,
        Ingredient input1,
        Optional<Ingredient> input2,
        Optional<FluidStack> fluidInput,
        ItemStack output,
        Optional<FluidStack> fluidOutput,
        int energyPerTick,
        int time
) implements Recipe<CannerRecipe.CannerRecipeInput> {

    @Override
    public boolean matches(CannerRecipeInput input, Level level) {
        if (!input1.test(input.getItem(0))) return false;
        
        if (input2.isPresent()) {
            if (!input2.get().test(input.getItem(1))) return false;
        }

        if (fluidInput.isPresent()) {
            if (input.fluid().isEmpty() || !input.fluid().containsFluid(fluidInput.get())) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CannerRecipeInput input, HolderLookup.Provider registries) {
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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return switch (mode) {
            case ITEMS_TO_ITEM -> ModRecipes.CANNER_ITEMS_TO_ITEM_SERIALIZER.get();
            case ITEM_TO_ITEM_FLUID -> ModRecipes.CANNER_ITEM_TO_ITEM_FLUID_SERIALIZER.get();
            case ITEM_FLUID_TO_ITEM -> ModRecipes.CANNER_ITEM_FLUID_TO_ITEM_TYPE.get(); // Fixed mismatch
            case ITEMS_FLUID_TO_ITEM_FLUID -> ModRecipes.CANNER_ITEMS_FLUID_TO_ITEM_FLUID_SERIALIZER.get();
        };
    }

    @Override
    public RecipeType<?> getType() {
        return switch (mode) {
            case ITEMS_TO_ITEM -> ModRecipes.CANNER_ITEMS_TO_ITEM_TYPE.get();
            case ITEM_TO_ITEM_FLUID -> ModRecipes.CANNER_ITEM_TO_ITEM_FLUID_TYPE.get();
            case ITEM_FLUID_TO_ITEM -> ModRecipes.CANNER_ITEM_FLUID_TO_ITEM_TYPE.get();
            case ITEMS_FLUID_TO_ITEM_FLUID -> ModRecipes.CANNER_ITEMS_FLUID_TO_ITEM_FLUID_TYPE.get();
        };
    }

    public record CannerRecipeInput(ItemStack item1, ItemStack item2, FluidStack fluid) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> item1;
                case 1 -> item2;
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int size() {
            return 2;
        }
    }

    public static class Serializer implements RecipeSerializer<CannerRecipe> {
        private final CannerMode mode;
        private final MapCodec<CannerRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, CannerRecipe> streamCodec;

        public Serializer(CannerMode mode) {
            this.mode = mode;
            this.codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("input1").forGetter(CannerRecipe::input1),
                    Ingredient.CODEC.optionalFieldOf("input2").forGetter(CannerRecipe::input2),
                    FluidStack.CODEC.optionalFieldOf("fluidInput").forGetter(CannerRecipe::fluidInput),
                    ItemStack.STRICT_CODEC.fieldOf("output").forGetter(CannerRecipe::output),
                    FluidStack.CODEC.optionalFieldOf("fluidOutput").forGetter(CannerRecipe::fluidOutput),
                    Codec.INT.fieldOf("energyPerTick").forGetter(CannerRecipe::energyPerTick),
                    Codec.INT.fieldOf("time").forGetter(CannerRecipe::time)
            ).apply(inst, (i1, i2, fi, o, fo, e, t) -> new CannerRecipe(this.mode, i1, i2, fi, o, fo, e, t)));

            this.streamCodec = StreamCodec.of(
                    (buf, recipe) -> {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input1);
                        buf.writeOptional(recipe.input2, (b, i) -> Ingredient.CONTENTS_STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, i));
                        buf.writeOptional(recipe.fluidInput, (b, f) -> FluidStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, f));
                        ItemStack.STREAM_CODEC.encode(buf, recipe.output);
                        buf.writeOptional(recipe.fluidOutput, (b, f) -> FluidStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, f));
                        buf.writeInt(recipe.energyPerTick);
                        buf.writeInt(recipe.time);
                    },
                    buf -> new CannerRecipe(
                            this.mode,
                            Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                            buf.readOptional(b -> Ingredient.CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) b)),
                            buf.readOptional(b -> FluidStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) b)),
                            ItemStack.STREAM_CODEC.decode(buf),
                            buf.readOptional(b -> FluidStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) b)),
                            buf.readInt(),
                            buf.readInt()
                    )
            );
        }

        @Override
        public MapCodec<CannerRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CannerRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
*/
