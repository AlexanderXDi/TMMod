package mopk.tmmod.events_and_else.Generator;

import mopk.tmmod.events_and_else.ModBlockEntities;

import mopk.tmmod.events_and_else.ModDataComponents;
import mopk.tmmod.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties; // <-- Добавлен импорт для LIT
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class GeneratorBE extends BlockEntity implements MenuProvider {
    public GeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR_BE.get(), pos, state);
    }

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getBurnTime(null) > 0;
                case 1 -> true;
                default -> false;
            };
        }
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    private int energy = 0;
    private int maxEnergy = 20000;

    private int burningTimeRemaining = 0;
    private int energyPerTick = 4;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> GeneratorBE.this.energy;
                case 1 -> GeneratorBE.this.maxEnergy;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> GeneratorBE.this.energy = value;
                case 1 -> GeneratorBE.this.maxEnergy = value;
            }
        }
        @Override
        public int getCount() {
            return 2;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.generator");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        this.energy = tag.getInt("generator.energy");
        this.maxEnergy = tag.getInt("generator.maxEnergy");
        this.burningTimeRemaining = tag.getInt("generator.burnTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("generator.energy", this.energy);
        tag.putInt("generator.maxEnergy", this.maxEnergy);
        tag.putInt("generator.burnTime", this.burningTimeRemaining);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu(id, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean isDirty = false;

        boolean wasBurning = state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);

        if (this.burningTimeRemaining > 0) {
            this.burningTimeRemaining--;
            if (this.energy < this.maxEnergy) {
                this.energy = Math.min(this.energy + energyPerTick, this.maxEnergy);
            }
            isDirty = true;
        }

        if (this.burningTimeRemaining <= 0 && this.energy < this.maxEnergy) {
            ItemStack fuelStack = inventory.getStackInSlot(0);
            int burnTime = fuelStack.getBurnTime(null);

            if (burnTime > 0) {
                this.burningTimeRemaining = burnTime;

                if (fuelStack.hasCraftingRemainingItem()) {
                    inventory.setStackInSlot(0, fuelStack.getCraftingRemainingItem());
                } else {
                    fuelStack.shrink(1);
                }
                isDirty = true;
            }
        }

        ItemStack chargeStack = inventory.getStackInSlot(1);
        if (!chargeStack.isEmpty() && chargeStack.is(ModItems.BATTERY.get())) {
            boolean isCharged = chargeStack.getOrDefault(ModDataComponents.IS_CHARGED.get(), false);
            if (!isCharged && this.energy > 1000) {
                chargeStack.set(ModDataComponents.IS_CHARGED.get(), true);
                this.energy -= 1000;
            }
        }

        boolean isBurning = this.burningTimeRemaining > 0;

        if (wasBurning != isBurning && state.hasProperty(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isBurning), 3);
            isDirty = true;
        }

        if (isDirty) {
            setChanged();
        }
    }
}
