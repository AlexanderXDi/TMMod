package mopk.tmmod.custom_interfaces;

import net.minecraft.core.Direction;

public interface CustomEnergyStorage {
    int getEnergyStored();
    int getMaxEnergyStored();

    int receiveEnergy(int maxReceive, int tier, boolean simulate);

    int extractEnergy(int maxExtract, boolean simulate);

    int getEnergyTier();

    boolean canReceive(Direction side);
    boolean canExtract(Direction side);
}
