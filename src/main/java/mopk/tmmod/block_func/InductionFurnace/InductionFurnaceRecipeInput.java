package mopk.tmmod.block_func.InductionFurnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record InductionFurnaceRecipeInput(ItemStack input1, ItemStack input2) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> input1;
            case 1 -> input2;
            default -> throw new IllegalArgumentException("Unexpected index: " + index);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
