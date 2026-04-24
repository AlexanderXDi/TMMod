package mopk.tmmod.block_func.InductionFurnace;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
import mopk.tmmod.registration.OneItemSlot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;


public class InductionFurnaceMenu extends AbstractContainerMenu {
    private final InductionFurnaceBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public InductionFurnaceMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (InductionFurnaceBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public InductionFurnaceMenu(int containerId, Inventory inv, InductionFurnaceBE entity, ContainerData data) {
        super(ModMenuTypes.INDUCTION_FURNACE_MENU.get(), containerId);

        this.blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        this.addSlot(new SlotItemHandler(entity.inventory, 0, 51, 21));
        this.addSlot(new SlotItemHandler(entity.inventory, 1, 51, 48));
        this.addSlot(new SlotItemHandler(entity.inventory, 2, 109, 21));
        this.addSlot(new SlotItemHandler(entity.inventory, 3, 109, 48));
        this.addSlot(new OneItemSlot(entity.inventory, 4, 8, 53));

        this.addSlot(new SlotItemHandler(entity.inventory, 5, 180, 4));
        this.addSlot(new SlotItemHandler(entity.inventory, 6, 180, 22));
        this.addSlot(new SlotItemHandler(entity.inventory, 7, 180, 40));
        this.addSlot(new SlotItemHandler(entity.inventory, 8, 180, 58));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addDataSlots(data);
    }

    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    public int getProgress() {
        return this.data.get(2);
    }

    public int getMaxProgress() {
        return this.data.get(3);
    }

    public int getLastHeatFlow() {
        return this.data.get(4);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 9) { // Из слотов машины в инвентарь игрока
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Из инвентаря игрока в машину
                if (itemstack1.has(mopk.tmmod.registration.ModDataComponents.CHARGE.get())) {
                    if (!this.moveItemStackTo(itemstack1, 4, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                    if (!this.moveItemStackTo(itemstack1, 5, 9, false)) {
                        return ItemStack.EMPTY;
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.INDUCTION_FURNACE.get());
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
