package mopk.tmmod.block_func.ElectricFurnace;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ElectricFurnaceMenu extends AbstractContainerMenu {
    private final ElectricFurnaceBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public ElectricFurnaceMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ElectricFurnaceBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public ElectricFurnaceMenu(int containerId, Inventory inv, ElectricFurnaceBE entity, ContainerData data) {
        super(ModMenuTypes.ELECTRIC_FURNACE_MENU.get(), containerId);

        this.blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        this.addSlot(new SlotItemHandler(entity.getInventory(), 0, 56, 24));
        this.addSlot(new SlotItemHandler(entity.getInventory(), 1, 110, 35));
        this.addSlot(new SlotItemHandler(entity.getInventory(), 2, 56, 46));

        this.addSlot(new SlotItemHandler(entity.getInventory(), 3, 180, 4));
        this.addSlot(new SlotItemHandler(entity.getInventory(), 4, 180, 22));
        this.addSlot(new SlotItemHandler(entity.getInventory(), 5, 180, 40));
        this.addSlot(new SlotItemHandler(entity.getInventory(), 6, 180, 58));

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

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ELECTRIC_FURNACE.get());
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

