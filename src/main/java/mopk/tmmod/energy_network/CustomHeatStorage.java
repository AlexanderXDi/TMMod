package mopk.tmmod.energy_network;

import net.minecraft.core.Direction;

public interface CustomHeatStorage {
    int getHeatStored();
    int getMaxHeatStored();

    int receiveHeat(int maxReceive, boolean simulate);

    int extractHeat(int maxExtract, boolean simulate);

    boolean canConnectHeat(Direction side);
}
