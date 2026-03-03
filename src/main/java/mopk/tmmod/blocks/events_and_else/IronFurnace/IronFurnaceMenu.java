package mopk.tmmod.blocks.events_and_else.IronFurnace;

import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.blocks.events_and_else.ModMenuTypes;
import static mopk.tmmod.blocks.events_and_else.ModMenuTypes.IRON_FURNACE_MENU;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;



public class IronFurnaceMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public IronFurnaceMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, new SimpleContainer(3), new SimpleContainerData(4));
    }

    public IronFurnaceMenu(int containerId, Inventory playerInv, Container container, ContainerData data) {
        super(IRON_FURNACE_MENU.get(), containerId);
        checkContainerSize(container, 3);
        this.container = container;
        this.data = data;

        this.addSlot(new Slot(container, 0, 56, 17));
        this.addSlot(new ModFuelSlot(container, 1, 56, 53));
        this.addSlot(new ModResultSlot(container, 2, 110, 35));

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);

        this.addDataSlots(data);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) i = 200;
        return this.data.get(0) * 13 / i;
    }

    public int getBurnProgress() {
        int i = this.data.get(2);
        int j = this.data.get(3);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                if (this.isFuel(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        if (index < 30) {
                            if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    private boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(null) > 0;
    }

    private static class ModFuelSlot extends Slot {
        public ModFuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getBurnTime(null) > 0 || stack.is(Items.BUCKET);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return stack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(stack);
        }
    }

    private static class ModResultSlot extends Slot {
        public ModResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
