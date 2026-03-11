package mopk.tmmod.etc.BatteryBlock;

import mopk.tmmod.etc.ModBlockEntities;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;


public class BatteryBlockBE extends BlockEntity implements MenuProvider {

    private final IEnergyStorage outputWrapper = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return energyStorage.extractEnergy(maxExtract, simulate); }
        @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    };

    private final IEnergyStorage inputWrapper = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return energyStorage.receiveEnergy(maxReceive, simulate); }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    };

    private BatteryBlockMode mode = BatteryBlockMode.BOTH;

    public BatteryBlockBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BLOCK_BE.get(), pos, state);
    }

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored() & 0xFFFF;
                case 1 -> (energyStorage.getEnergyStored() >> 16) & 0xFFFF;
                case 2 -> energyStorage.getMaxEnergyStored() & 0xFFFF;
                case 3 -> (energyStorage.getMaxEnergyStored() >> 16) & 0xFFFF;
                case 4 -> mode.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 4) mode = BatteryBlockMode.values()[value];
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if (side == null) return energyStorage;

        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);

        if (side == facing) {
            return outputWrapper;
        }

        return inputWrapper;
    }

    public final EnergyStorage energyStorage = new EnergyStorage(1000000, 1000, 1000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (mode == BatteryBlockMode.OUTPUT) return 0;
            return super.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (mode == BatteryBlockMode.INPUT) return 0;
            return super.extractEnergy(maxExtract, simulate);
        }
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

    public BatteryBlockMode getMode() {
        return mode;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || mode == BatteryBlockMode.INPUT || energyStorage.getEnergyStored() <= 0) return;

        for (Direction direction : Direction.values()) {
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null && target.canReceive()) {
                int extracted = energyStorage.extractEnergy(1000, true);
                int accepted = target.receiveEnergy(extracted, false);
                energyStorage.extractEnergy(accepted, false);
            }
        }
        if (level.isClientSide || energyStorage.getEnergyStored() <= 0) return;

        Direction facing = state.getValue(BlockStateProperties.FACING);

        IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(facing), facing.getOpposite());

        if (target != null && target.canReceive()) {
            int extracted = energyStorage.extractEnergy(1000, true);
            int accepted = target.receiveEnergy(extracted, false);
            if (accepted > 0) {
                energyStorage.extractEnergy(accepted, false);
                setChanged();
            }
        }
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("Mode", mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.receiveEnergy(tag.getInt("Energy"), false);
        if (tag.contains("Mode")) mode = BatteryBlockMode.values()[tag.getInt("Mode")];
    }
}

