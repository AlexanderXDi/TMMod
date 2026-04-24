package mopk.tmmod.custom_interfaces;

import net.minecraft.world.item.ItemStack;

public interface CustomEnergyItemInterface {
    int getEnergyStored(ItemStack stack);

    int getMaxEnergyStored(ItemStack stack);

    int getTier(ItemStack stack);

    int getTransferRate(ItemStack stack);

    int receiveEnergy(ItemStack stack, int amount, boolean simulate);

    int extractEnergy(ItemStack stack, int amount, boolean simulate);
}
