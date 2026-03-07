package mopk.tmmod.events_and_else.Cables;

import mopk.tmmod.events_and_else.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;


public class CableBE extends BlockEntity {
    private final CableTier tier;
    private EnergyNetwork network;

    public CableBE(BlockPos pos, BlockState state, CableTier tier) {
        super(ModBlockEntities.CABLE_BE.get(), pos, state);
        this.tier = tier;
        this.energyStorage = new EnergyStorage(tier.getCapacity(), tier.getTransfer(), tier.getTransfer());
    }

    private final EnergyStorage energyStorage;

    public IEnergyStorage getEnergyStorage() { return this.energyStorage; }

    public int getTierCapacity() {
        return tier.getCapacity();
    }

    public int getTierTransfer() {
        return tier.getTransfer();
    }

    public void setNetwork(EnergyNetwork network) {
        this.network = network;
    }

    public EnergyNetwork getNetwork() {
        return network;
    }
}

