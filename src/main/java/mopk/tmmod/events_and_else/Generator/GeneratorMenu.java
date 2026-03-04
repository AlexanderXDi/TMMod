package mopk.tmmod.events_and_else.Generator;

import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.events_and_else.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GeneratorMenu extends AbstractContainerMenu {
    private final GeneratorBE blockEntity;
    private final ContainerData data;

    //слоты и создание меню
    public GeneratorMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(2));
    }

    //создание
    public GeneratorMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.GENERATOR_MENU.get(), containerId);

        if (entity instanceof GeneratorBE generatorBE) {
            this.blockEntity = generatorBE;
        } else {
            throw new IllegalStateException("BlockEntity is not a GeneratorBE! " + entity);
        }

        this.data = data;

        addDataSlots(data);

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 80, 35));
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 80, 55));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    // для прогрессбаров
    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.GENERATOR.get());
    }


    private static final int CONF_SLOT_COUNT = 2;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < CONF_SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, CONF_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, CONF_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
