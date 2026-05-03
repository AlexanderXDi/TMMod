package mopk.tmmod.block_func.RadioisotopeHeatGenerator;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
import mopk.tmmod.registration.ModItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RadioisotopeHeatGeneratorMenu extends AbstractContainerMenu {
    private final RadioisotopeHeatGeneratorBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public RadioisotopeHeatGeneratorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (RadioisotopeHeatGeneratorBE) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    public RadioisotopeHeatGeneratorMenu(int containerId, Inventory inv, RadioisotopeHeatGeneratorBE entity, ContainerData data) {
        super(ModMenuTypes.RADIOISOTOPE_HEAT_GENERATOR_MENU.get(), containerId);

        this.blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        // Слот 0: РИТЭГ-топливо
        this.addSlot(new SlotItemHandler(entity.getInventory(), 0, 80, 35) {
            @Override
            public int getMaxStackSize() {
                return 6;
            }
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addDataSlots(data);
    }

    public int getHeatOutput() {
        return this.data.get(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int invSize = 1;
            if (index < invSize) { // Из машины в инвентарь
                if (!this.moveItemStackTo(itemstack1, invSize, invSize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Из инвентаря в машину
                if (itemstack1.is(ModItems.RTG_FUEL.get())) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (index < invSize + 27) {
                        if (!this.moveItemStackTo(itemstack1, invSize + 27, invSize + 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, invSize, invSize + 27, false)) {
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.RADIOISOTOPE_HEAT_GENERATOR.get());
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
