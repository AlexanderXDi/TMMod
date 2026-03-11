package mopk.tmmod.etc.Crusher;

import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.etc.ModMenuTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CrusherMenu extends AbstractContainerMenu {
    private final CrusherBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public CrusherMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (CrusherBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public CrusherMenu(int containerId, Inventory inv, CrusherBE entity, ContainerData data) {
        super(ModMenuTypes.CRUSHER_MENU.get(), containerId);

        this.blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        this.addSlot(new SlotItemHandler(entity.getInventory(), 0, 100, 50));
        this.addSlot(new mopk.tmmod.etc.Crusher.CrusherMenu.SingleItemSlot(entity.getInventory(), 1, 120, 50));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addDataSlots(data);
    }

    private static class SingleItemSlot extends SlotItemHandler {
        public SingleItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
        @Override
        public int getMaxStackSize() {
            return 1;
        }
        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.CRUSHER.get());
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

