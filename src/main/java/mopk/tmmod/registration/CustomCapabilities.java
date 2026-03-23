package mopk.tmmod.registration;

import mopk.tmmod.energy_network.CustomEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class CustomCapabilities {
    public static final BlockCapability<CustomEnergyStorage, Direction> ENERGY =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath("tmmod", "energy"),
                    CustomEnergyStorage.class
            );
}

