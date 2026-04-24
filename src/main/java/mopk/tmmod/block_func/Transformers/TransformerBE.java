package mopk.tmmod.block_func.Transformers;

import mopk.tmmod.custom_interfaces.CustomEnergyStorage;
import mopk.tmmod.custom_interfaces.EnergyNetworkManager;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
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
import org.jetbrains.annotations.Nullable;

public class TransformerBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private final TransformerTier tier;
    private TransformerMode mode = TransformerMode.DOWN;
    
    private int energyBuffer = 0;
    private final int maxBuffer;
    
    private int euInLastTick = 0;
    private int euOutLastTick = 0;
    private int euInCurrent = 0;
    private int euOutCurrent = 0;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mode.ordinal();
                case 1 -> euInLastTick;
                case 2 -> euOutLastTick;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { mode = TransformerMode.values()[value]; }
        @Override public int getCount() { return 3; }
    };

    public TransformerBE(BlockPos pos, BlockState state, TransformerTier tier) {
        super(ModBlockEntities.TRANSFORMER_BE.get(), pos, state);
        this.tier = tier;
        this.maxBuffer = tier.getHighTransfer() * 2;
    }

    public void toggleMode() {
        this.mode = this.mode.next();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tmmod." + tier.getName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TransformerMenu(id, inv, this, this.data);
    }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        int allowedTier = (mode == TransformerMode.DOWN) ? this.tier.getHighTier() : this.tier.getLowTier();
        if (tier > allowedTier) {
            if (!simulate) triggerExplosion();
            return 0;
        }
        
        int space = maxBuffer - energyBuffer;
        int toReceive = Math.min(maxReceive, space);
        if (!simulate) {
            energyBuffer += toReceive;
            euInCurrent += toReceive;
            setChanged();
        }
        return toReceive;
    }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int toExtract = Math.min(maxExtract, energyBuffer);
        if (!simulate) {
            energyBuffer -= toExtract;
            euOutCurrent += toExtract;
            setChanged();
        }
        return toExtract;
    }

    @Override
    public int getEnergyStored() { return energyBuffer; }
    @Override
    public int getMaxEnergyStored() { return maxBuffer; }
    @Override
    public int getEnergyTier() {
        return (mode == TransformerMode.DOWN) ? tier.getLowTier() : tier.getHighTier();
    }

    @Override
    public boolean canReceive(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing;
    }

    @Override
    public boolean canExtract(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        euInLastTick = euInCurrent;
        euOutLastTick = euOutCurrent;
        euInCurrent = 0;
        euOutCurrent = 0;

        if (energyBuffer > 0) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos targetPos = pos.relative(facing);
            CustomEnergyStorage target = level.getCapability(CustomCapabilities.ENERGY, targetPos, facing.getOpposite());
            if (target != null && target.canReceive(facing.getOpposite())) {
                int outputTierValue = getEnergyTier();
                int transferLimit = (mode == TransformerMode.DOWN) ? tier.getLowTransfer() : tier.getHighTransfer();
                
                // In IC2, step down outputs multiple packets. Here we just output up to the transfer limit of the tier.
                int toExtract = Math.min(energyBuffer, transferLimit);
                int accepted = target.receiveEnergy(toExtract, outputTierValue, false);
                if (accepted > 0) {
                    energyBuffer -= accepted;
                    euOutLastTick += accepted; // Since we already reset it, add to last tick for better display or just wait for next tick
                    // Actually let's just use euOutCurrent for the remainder of this tick
                    euOutCurrent += accepted; 
                    setChanged();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyBuffer);
        tag.putInt("Mode", mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyBuffer = tag.getInt("Energy");
        if (tag.contains("Mode")) mode = TransformerMode.values()[tag.getInt("Mode")];
    }
}
