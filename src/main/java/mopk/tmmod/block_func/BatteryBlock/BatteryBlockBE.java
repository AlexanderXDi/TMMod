package mopk.tmmod.block_func.BatteryBlock;

import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import mopk.tmmod.registration.CustomCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

public class BatteryBlockBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private int energyStored = 0;
    private final int maxEnergyStored = 1000000;
    private final int transferRate = 32;
    private BatteryBlockMode mode = BatteryBlockMode.BOTH;

    public BatteryBlockBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BLOCK_BE.get(), pos, state);
    }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (mode == BatteryBlockMode.OUTPUT) return 0;
        int space = maxEnergyStored - energyStored;
        int toReceive = Math.min(maxReceive, Math.min(space, transferRate));
        if (!simulate) {
            energyStored += toReceive;
            setChanged();
        }
        return toReceive;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (mode == BatteryBlockMode.INPUT) return 0;
        int toExtract = Math.min(maxExtract, Math.min(energyStored, transferRate));
        if (!simulate) {
            energyStored -= toExtract;
            setChanged();
        }
        return toExtract;
    }

    @Override
    public int getEnergyTier() {
        return 1;
    }

    @Override
    public boolean canReceive(Direction side) {
        if (mode == BatteryBlockMode.OUTPUT) return false;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing;
    }

    @Override
    public boolean canExtract(Direction side) {
        if (mode == BatteryBlockMode.INPUT) return false;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    @Override
    public int getEnergyStored() { return energyStored; }

    @Override
    public int getMaxEnergyStored() { return maxEnergyStored; }

    public CustomEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
        }
    }

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStored & 0xFFFF;
                case 1 -> (energyStored >> 16) & 0xFFFF;
                case 2 -> maxEnergyStored & 0xFFFF;
                case 3 -> (maxEnergyStored >> 16) & 0xFFFF;
                case 4 -> mode.ordinal();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { if (index == 4) mode = BatteryBlockMode.values()[value]; }
        @Override public int getCount() { return 5; }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tmmod.battery_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BatteryBlockMenu(id, inv, this, this.data);
    }

    public void toggleMode() {
        this.mode = this.mode.next();
        setChanged();
    }

    public BatteryBlockMode getMode() { return mode; }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || mode == BatteryBlockMode.INPUT || energyStored <= 0) return;

        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockPos targetPos = pos.relative(facing);

        CustomEnergyStorage target = level.getCapability(CustomCapabilities.ENERGY, targetPos, facing.getOpposite());
        if (target != null && target.canReceive(facing.getOpposite())) {
            int toExtract = Math.min(energyStored, transferRate);
            int accepted = target.receiveEnergy(toExtract, getEnergyTier(), false);
            if (accepted > 0) {
                energyStored -= accepted;
                setChanged();
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStored);
        tag.putInt("Mode", mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getInt("Energy");
        if (tag.contains("Mode")) mode = BatteryBlockMode.values()[tag.getInt("Mode")];
    }
}
