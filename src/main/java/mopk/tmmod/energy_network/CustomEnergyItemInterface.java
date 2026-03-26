package mopk.tmmod.energy_network;

import net.minecraft.world.item.ItemStack;

public interface CustomEnergyItemInterface {
    int getEnergyStored(ItemStack stack);

    int getMaxEnergyStored(ItemStack stack);

    int receiveEnergy(ItemStack stack, int amount, boolean simulate);

    int extractEnergy(ItemStack stack, int amount, boolean simulate);
}
