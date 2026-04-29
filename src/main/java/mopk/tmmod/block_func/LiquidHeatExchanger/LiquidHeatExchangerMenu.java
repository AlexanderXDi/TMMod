package mopk.tmmod.block_func.LiquidHeatExchanger;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LiquidHeatExchangerMenu extends AbstractContainerMenu {
    public final LiquidHeatExchangerBE blockEntity;
    private final ContainerData data;

    public LiquidHeatExchangerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public LiquidHeatExchangerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.LIQUID_HEAT_EXCHANGER_MENU.get(), id);
        this.blockEntity = (LiquidHeatExchangerBE) entity;
        this.data = data;

        IItemHandler inventory = blockEntity.inventory;

        // 1 - Горячая жидкость (слева снизу 2 шт)
        this.addSlot(new SlotItemHandler(inventory, 0, 8, 54));
        this.addSlot(new SlotItemHandler(inventory, 1, 26, 54));

        // 2 - Холодная жидкость (справа снизу 2 шт)
        this.addSlot(new SlotItemHandler(inventory, 2, 134, 54));
        this.addSlot(new SlotItemHandler(inventory, 3, 152, 54));

        // 6 - Теплопроводы (5x2 в центре)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                this.addSlot(new SlotItemHandler(inventory, 4 + col + (row * 5), 44 + col * 18, 36 + row * 18));
            }
        }

        // 3 - Улучшения (3 шт справа отдельно)
        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotItemHandler(inventory, 14 + i, 152, 10 + i * 18));
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public int getCurrentHeat() { return data.get(0); }
    public int getMaxHeat() { return data.get(1); }
    public int getHotAmount() { return data.get(2); }
    public int getHotCapacity() { return data.get(3); }
    public int getColdAmount() { return data.get(4); }
    public int getColdCapacity() { return data.get(5); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 17) {
                if (!this.moveItemStackTo(itemstack1, 17, 53, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 17, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.LIQUID_HEAT_EXCHANGER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
