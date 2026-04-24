package mopk.tmmod.block_func.ElectricHeatGenerator;

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


public class ElectricHeatGeneratorMenu extends AbstractContainerMenu {
    private final ElectricHeatGeneratorBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public ElectricHeatGeneratorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ElectricHeatGeneratorBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public ElectricHeatGeneratorMenu(int containerId, Inventory inv, ElectricHeatGeneratorBE entity, ContainerData data) {
        super(ModMenuTypes.ELECTRIC_HEAT_GENERATOR_MENU.get(), containerId);

        this.blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        // Слот 0: Зарядка (слева)
        this.addSlot(new OneItemSlot(entity.getInventory(), 0, 8, 53));

        // Слоты 1-10: Катушки (центр, 5 колонок х 2 строки)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                this.addSlot(new OneItemSlot(entity.getInventory(), 1 + col + (row * 5), 44 + (col * 18), 26 + (row * 18)));
            }
        }

        // Слоты 11-12: Улучшения (справа в боковой панели)
        this.addSlot(new SlotItemHandler(entity.getInventory(), 11, 180, 26));
        this.addSlot(new SlotItemHandler(entity.getInventory(), 12, 180, 44));

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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int invSize = 13;
            if (index < invSize) { // Из слотов машины в инвентарь игрока
                if (!this.moveItemStackTo(itemstack1, invSize, invSize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Из инвентаря игрока в машину
                if (itemstack1.is(mopk.tmmod.registration.ModItems.COIL.get())) {
                    if (!this.moveItemStackTo(itemstack1, 1, 11, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.has(mopk.tmmod.registration.ModDataComponents.CHARGE.get()) || itemstack1.is(net.minecraft.world.item.Items.REDSTONE)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 11, 13, false)) {
                    if (!this.moveItemStackTo(itemstack1, invSize, invSize + 27, false)) {
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ELECTRIC_HEAT_GENERATOR.get());
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
