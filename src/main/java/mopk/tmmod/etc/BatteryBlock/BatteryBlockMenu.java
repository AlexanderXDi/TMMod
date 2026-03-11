package mopk.tmmod.etc.BatteryBlock;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static mopk.tmmod.etc.ModMenuTypes.BATTERY_BLOCK_MENU;

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
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
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

