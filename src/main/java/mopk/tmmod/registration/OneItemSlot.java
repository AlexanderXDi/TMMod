package mopk.tmmod.registration;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OneItemSlot extends SlotItemHandler {
    public OneItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return 64; // Allow larger stacks if the specific item check allows it
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        if (stack.is(Items.REDSTONE)) {
            return stack.getMaxStackSize();
        }
        return 1;
    }
}
