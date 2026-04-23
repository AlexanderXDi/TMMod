package mopk.tmmod.block_func.InductionFurnace;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
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

    public InductionFurnaceMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, (InductionFurnaceBE) playerInv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public InductionFurnaceMenu(int id, Inventory playerInv, InductionFurnaceBE entity, ContainerData data) {
        super(ModMenuTypes.INDUCTION_FURNACE_MENU.get(), id);
        this.blockEntity = entity;
        this.level = playerInv.player.level();
        this.data = data;

        // Входные слоты (рядом)
        this.addSlot(new SlotItemHandler(entity.inventory, 0, 44, 35));
        this.addSlot(new SlotItemHandler(entity.inventory, 1, 62, 35));

        // Выходные слоты (рядом)
        this.addSlot(new SlotItemHandler(entity.inventory, 2, 116, 35));
        this.addSlot(new SlotItemHandler(entity.inventory, 3, 134, 35));

        // Слот для зарядки/редстоуна
        this.addSlot(new SlotItemHandler(entity.inventory, 4, 8, 53));

        // Улучшения
        for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotItemHandler(entity.inventory, 5 + i, 152, 8 + i * 18));
        }

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
        addDataSlots(data);
    }

    public int getData(int index) {
        return this.data.get(index);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.INDUCTION_FURNACE.get());
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
