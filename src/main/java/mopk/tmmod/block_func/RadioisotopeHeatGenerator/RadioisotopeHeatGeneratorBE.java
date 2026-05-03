package mopk.tmmod.block_func.RadioisotopeHeatGenerator;

import mopk.tmmod.custom_interfaces.CustomHeatStorage;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class RadioisotopeHeatGeneratorBE extends BlockEntity implements CustomHeatStorage, MenuProvider {
    private int heatOutput = 0;

    public RadioisotopeHeatGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIOISOTOPE_HEAT_GENERATOR_BE.get(), pos, state);
    }

    @Override
    public int getHeatStored() {
        return 0;
    }

    @Override
    public int getMaxHeatStored() {
        return 0;
    }

    @Override
    public int receiveHeat(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractHeat(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> RadioisotopeHeatGeneratorBE.this.heatOutput;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> RadioisotopeHeatGeneratorBE.this.heatOutput = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tmmod.radioisotope_heat_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RadioisotopeHeatGeneratorMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.RTG_FUEL.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 6;
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        ItemStack fuel = inventory.getStackInSlot(0);
        int n = fuel.getCount();
        this.heatOutput = n > 0 ? (int) Math.pow(2, n) : 0;

        boolean isLit = this.heatOutput > 0;
        if (isLit) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos targetPos = pos.relative(facing);
            BlockEntity targetBE = level.getBlockEntity(targetPos);
            
            if (targetBE != null) {
                CustomHeatStorage targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
                if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                    targetStorage.receiveHeat(this.heatOutput, false);
                }
            }
        }

        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }

        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }
}
