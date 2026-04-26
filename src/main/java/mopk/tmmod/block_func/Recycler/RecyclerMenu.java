package mopk.tmmod.block_func.Recycler;

import mopk.tmmod.registration.ModBlocks;
import mopk.tmmod.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RecyclerMenu extends AbstractContainerMenu {
    public final RecyclerBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public RecyclerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public RecyclerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.RECYCLER_MENU.get(), id);
        checkContainerSize(inv, 7);
        blockEntity = (RecyclerBE) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        IItemHandler inventory = blockEntity.getInventory();

        // Слот для входного предмета (0)
        this.addSlot(new SlotItemHandler(inventory, 0, 56, 17));
        // Слот для результата (1)
        this.addSlot(new SlotItemHandler(inventory, 1, 116, 35));
        // Слот для батарейки (2)
        this.addSlot(new SlotItemHandler(inventory, 2, 56, 53));
        // Слоты для улучшений (3, 4, 5, 6)
        this.addSlot(new SlotItemHandler(inventory, 3, 152, 8));
        this.addSlot(new SlotItemHandler(inventory, 4, 152, 26));
        this.addSlot(new SlotItemHandler(inventory, 5, 152, 44));
        this.addSlot(new SlotItemHandler(inventory, 6, 152, 62));

        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getMaxProgress() {
        return data.get(3);
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getScaledProgress() {
        int progress = getProgress();
        int maxProgress = getMaxProgress();
        int progressArrowSize = 24; // Ширина стрелки

        return (maxProgress != 0 && progress != 0) ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledEnergy() {
        int energy = getEnergy();
        int maxEnergy = getMaxEnergy();
        int energyBarSize = 14; // Высота молнии

        return (maxEnergy != 0 && energy != 0) ? energy * energyBarSize / maxEnergy : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        if (index < 36) { // Из инвентаря игрока в машину
            if (!moveItemStackTo(sourceStack, 36, 43, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 43) { // Из машины в инвентарь игрока
            if (!moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.RECYCLER.get());
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
