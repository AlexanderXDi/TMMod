package mopk.tmmod.block_func.Transformers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static mopk.tmmod.registration.ModMenuTypes.TRANSFORMER_MENU;

public class TransformerMenu extends AbstractContainerMenu {
    private final TransformerBE blockEntity;
    private final ContainerData data;

    public TransformerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (TransformerBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    public TransformerMenu(int id, Inventory inv, TransformerBE entity, ContainerData data) {
        super(TRANSFORMER_MENU.get(), id);
        this.blockEntity = entity;
        this.data = data;

        addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public TransformerMode getMode() {
        return TransformerMode.values()[data.get(0)];
    }

    public int getEuIn() {
        return data.get(1);
    }

    public int getEuOut() {
        return data.get(2);
    }

    public TransformerBE getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 0) { // No slots in transformer menu
                return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 0, false)) { // No slots
                    // return ItemStack.EMPTY;
                }
            }
            return ItemStack.EMPTY;
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }
}
