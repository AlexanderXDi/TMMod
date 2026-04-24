package mopk.tmmod.registration;

import mopk.tmmod.custom_interfaces.CustomEnergyStorage;
import mopk.tmmod.custom_interfaces.CustomHeatStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class CustomCapabilities {
    public static final BlockCapability<CustomEnergyStorage, Direction> ENERGY =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath("tmmod", "energy"),
                    CustomEnergyStorage.class
            );

    public static final BlockCapability<CustomHeatStorage, Direction> HEAT =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath("tmmod", "heat"),
                    CustomHeatStorage.class
            );
}

