/* 
package mopk.tmmod.block_func.Canner;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
import mopk.tmmod.registration.OneItemSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CannerMenu extends AbstractContainerMenu {
    private final CannerBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public CannerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (CannerBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public CannerMenu(int containerId, Inventory inv, CannerBE entity, ContainerData data) {
        super(ModMenuTypes.CANNER_MENU.get(), containerId);
        this.blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        this.addSlot(new SlotItemHandler(entity.inventory, 0, 38, 20)); // Input 1
        this.addSlot(new SlotItemHandler(entity.inventory, 1, 38, 45)); // Input 2
        this.addSlot(new SlotItemHandler(entity.inventory, 2, 110, 35)); // Output
        this.addSlot(new OneItemSlot(entity.inventory, 3, 56, 46)); // Energy slot (reuse OneItemSlot or just use SlotItemHandler)

        this.addSlot(new SlotItemHandler(entity.inventory, 4, 180, 4)); // Upgrade 1
        this.addSlot(new SlotItemHandler(entity.inventory, 5, 180, 22)); // Upgrade 2
        this.addSlot(new SlotItemHandler(entity.inventory, 6, 180, 40)); // Upgrade 3
        this.addSlot(new SlotItemHandler(entity.inventory, 7, 180, 58)); // Upgrade 4

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        this.addDataSlots(data);
    }

    public int getEnergy() { return this.data.get(0); }
    public int getMaxEnergy() { return this.data.get(1); }
    public int getProgress() { return this.data.get(2); }
    public int getMaxProgress() { return this.data.get(3); }
    public CannerMode getMode() { return CannerMode.values()[data.get(4)]; }
    public CannerBE getBlockEntity() { return blockEntity; }

    public FluidStack getFluidInTank(int tank) {
        return blockEntity != null ? blockEntity.getFluidInTank(tank) : FluidStack.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 8) {
                if (!this.moveItemStackTo(itemstack1, 8, 44, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 2, false)) { // Try inputs
                    if (!this.moveItemStackTo(itemstack1, 3, 4, false)) { // Try energy
                        if (!this.moveItemStackTo(itemstack1, 4, 8, false)) return ItemStack.EMPTY; // Try upgrades
                    }
                }
            }
            if (itemstack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.CANNER.get());
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
*/
