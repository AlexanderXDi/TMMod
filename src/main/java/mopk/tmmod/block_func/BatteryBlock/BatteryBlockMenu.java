package mopk.tmmod.block_func.BatteryBlock;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import static mopk.tmmod.registration.ModMenuTypes.BATTERY_BLOCK_MENU;

public class BatteryBlockMenu extends AbstractContainerMenu {
    private final BatteryBlockBE blockEntity;
    private final ContainerData data;

    public BatteryBlockMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (BatteryBlockBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public BatteryBlockMenu(int id, Inventory inv, BatteryBlockBE entity, ContainerData data) {
        super(BATTERY_BLOCK_MENU.get(), id);
        this.blockEntity = entity;
        this.data = data;

        this.addSlot(new SlotItemHandler(entity.getInventory(), 0, 80, 50));

        addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public int getEnergy() {
        return (data.get(1) << 16) | (data.get(0) & 0xFFFF);
    }

    public int getMaxEnergy() {
        return (data.get(3) << 16) | (data.get(2) & 0xFFFF);
    }

    public BatteryBlockMode getMode() {
        return BatteryBlockMode.values()[data.get(4)];
    }

    public BatteryBlockBE getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
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
