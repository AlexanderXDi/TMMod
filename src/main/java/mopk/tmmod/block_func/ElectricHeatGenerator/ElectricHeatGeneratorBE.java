package mopk.tmmod.block_func.ElectricHeatGenerator;

import mopk.tmmod.energy_network.CustomEnergyItemInterface;
import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.CustomHeatStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModItems;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class ElectricHeatGeneratorBE extends BlockEntity implements CustomEnergyStorage, CustomHeatStorage, MenuProvider {
    private int energyStored = 0;
    private final int maxEnergyStored = 10000;
    private final int maxReceiveAmount = 32; // T1
    
    private int activeCoils = 0;
    private float energyFraction = 0.0f; 
    
    private int heatBuffer = 0; 

    public ElectricHeatGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_HEAT_GENERATOR_BE.get(), pos, state);
    }

    // --- HEAT STORAGE ---
    public CustomHeatStorage getHeatStorage(Direction side) { return this; }
    @Override public int getHeatStored() { return heatBuffer; }
    @Override public int getMaxHeatStored() { return 1000; }

    @Override public int receiveHeat(int maxReceive, boolean simulate) { return 0; } 

    @Override
    public int extractHeat(int maxExtract, boolean simulate) {
        int toExtract = Math.min(heatBuffer, maxExtract);
        if (!simulate) { heatBuffer -= toExtract; setChanged(); }
        return toExtract;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    // --- INVENTORY ---
    public final ItemStackHandler inventory = new ItemStackHandler(13) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 1 && slot <= 10) recalculateCoils();
            setChanged();
        }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= 1 && slot <= 10) return stack.is(ModItems.COIL.get());
            return super.isItemValid(slot, stack);
        }
    };

    private void recalculateCoils() {
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            if (inventory.getStackInSlot(i).is(ModItems.COIL.get())) count++;
        }
        this.activeCoils = count;
    }

    // --- GUI ---
    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energyStored;
                case 1 -> maxEnergyStored;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 2; }
    };

    @Override public Component getDisplayName() { return Component.translatable("container.tmmod.electric_heat_generator"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ElectricHeatGeneratorMenu(id, inventory, this, this.data);
    }

    // --- ENERGY STORAGE ---
    public CustomEnergyStorage getEnergyStorage(Direction side) { return this; }
    @Override public int getEnergyStored() { return energyStored; }
    @Override public int getMaxEnergyStored() { return maxEnergyStored; }
    @Override public int getEnergyTier() { return 1; }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > 1) { if (!simulate) triggerExplosion(); return 0; }
        int accepted = Math.min(maxReceive, maxReceiveAmount);
        int received = Math.min(accepted, maxEnergyStored - energyStored);
        if (!simulate) { energyStored += received; setChanged(); }
        return received;
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; } 
    @Override public boolean canReceive(Direction side) { return side != getBlockState().getValue(BlockStateProperties.FACING); }
    @Override public boolean canExtract(Direction side) { return false; }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, 4.0F, Level.ExplosionInteraction.TNT);
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    // --- LOGIC ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        boolean isLit = false;

        // 1. Зарядка
        ItemStack chargeStack = inventory.getStackInSlot(0);
        if (!chargeStack.isEmpty() && energyStored < maxEnergyStored) {
            if (chargeStack.is(Items.REDSTONE)) {
                if (energyStored + 400 <= maxEnergyStored) { energyStored += 400; chargeStack.shrink(1); setChanged(); }
            } else if (chargeStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= 1) {
                    int toExtract = Math.min(maxEnergyStored - energyStored, maxReceiveAmount);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) { energyStored += extracted; setChanged(); }
                }
            }
        }

        // 2. Генерация тепла в буфер
        if (activeCoils > 0) {
            float exactCost = activeCoils * 3.2f;
            int intCost = (int) exactCost;
            energyFraction += (exactCost - intCost);
            if (energyFraction >= 1.0f) { intCost += 1; energyFraction -= 1.0f; }

            int heatToGenerate = activeCoils * 10;
            if (energyStored >= intCost && heatBuffer + heatToGenerate <= getMaxHeatStored()) {
                energyStored -= intCost;
                heatBuffer += heatToGenerate;
                isLit = true;
                setChanged();
            }
        }

        // 3. Передача тепла (Потоковая логика)
        if (heatBuffer > 0) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos targetPos = pos.relative(facing);
            BlockEntity targetBE = level.getBlockEntity(targetPos);
            
            if (targetBE != null) {
                CustomHeatStorage targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
                if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                    int toSend = Math.min(heatBuffer, 100); 
                    int accepted = targetStorage.receiveHeat(toSend, false);
                    if (accepted > 0) {
                        heatBuffer -= accepted;
                        isLit = true;
                        setChanged();
                    }
                }
            }
        }

        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
    }

    @Override public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStored);
        tag.putInt("heatBuffer", heatBuffer);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getInt("energy");
        heatBuffer = tag.getInt("heatBuffer");
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        recalculateCoils();
    }
}
