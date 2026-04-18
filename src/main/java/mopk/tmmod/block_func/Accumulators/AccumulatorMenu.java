package mopk.tmmod.block_func.Accumulators;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import static mopk.tmmod.registration.ModMenuTypes.ACCUMULATOR_MENU;

public class AccumulatorMenu extends AbstractContainerMenu {
    private final AccumulatorBE blockEntity;
    private final ContainerData data;

    public AccumulatorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (AccumulatorBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public AccumulatorMenu(int id, Inventory inv, AccumulatorBE entity, ContainerData data) {
        super(ACCUMULATOR_MENU.get(), id);
        this.blockEntity = entity;
        this.data = data;

        // Main charging slot
        this.addSlot(new SlotItemHandler(entity.getInventory(), 0, 80, 50));
        
        // Armor slots (only for ordinary accumulators)
        if (!entity.isChargePad()) {
            this.addSlot(new SlotItemHandler(entity.getInventory(), 1, 8, 10)); // Helmet
            this.addSlot(new SlotItemHandler(entity.getInventory(), 2, 8, 28)); // Chestplate
            this.addSlot(new SlotItemHandler(entity.getInventory(), 3, 8, 46)); // Leggings
            this.addSlot(new SlotItemHandler(entity.getInventory(), 4, 8, 64)); // Boots
        }

        // Redstone charging slot
        this.addSlot(new SlotItemHandler(entity.getInventory(), 5, 80, 10));

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

    public AccumulatorBE getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        int machineSlots = blockEntity.isChargePad() ? 2 : 6;

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < machineSlots) {
                if (!this.moveItemStackTo(itemstack1, machineSlots, 36 + machineSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, machineSlots, false)) {
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
