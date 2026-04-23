package mopk.tmmod.block_func.ElectricHeatGenerator;

import mopk.tmmod.energy_network.CustomEnergyItemInterface;
import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.CustomHeatStorage;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModItems;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class ElectricHeatGeneratorBE extends BlockEntity implements CustomEnergyStorage, MenuProvider {
    private int energyStored = 0;
    private final int maxEnergyStored = 10000;
    private final int maxReceiveAmount = 32; // T1
    
    private int activeCoils = 0;
    private float energyFraction = 0.0f; // Для точного подсчета дробных 3.2 EU/t

    public ElectricHeatGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_HEAT_GENERATOR_BE.get(), pos, state);
    }

    // --- ИНВЕНТАРЬ ---
    public final ItemStackHandler inventory = new ItemStackHandler(13) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 1 && slot <= 10) {
                recalculateCoils();
            }
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
            if (inventory.getStackInSlot(i).is(ModItems.COIL.get())) {
                count++;
            }
        }
        this.activeCoils = count;
    }

    // --- GUI СИНХРОНИЗАЦИЯ ---
    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> ElectricHeatGeneratorBE.this.energyStored;
                case 1 -> ElectricHeatGeneratorBE.this.maxEnergyStored;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 2; }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.electric_heat_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ElectricHeatGeneratorMenu(id, inventory, this, this.data);
    }

    // --- ENERGY STORAGE (Вход) ---
    public CustomEnergyStorage getEnergyStorage(Direction side) { return this; }

    public int getEnergyStored() { return energyStored; }
    public int getMaxEnergyStored() { return maxEnergyStored; }
    public int getEnergyTier() { return 1; } // Понижено до T1
    public int getTransferRate(ItemStack stack) { return maxReceiveAmount; }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > 1) { // Взрыв от превышения напряжения T1
            if (!simulate) { this.energyStored = 0; triggerExplosion(); setChanged(); } 
            return 0;
        }
        int accepted = Math.min(maxReceive, maxReceiveAmount);
        int space = maxEnergyStored - energyStored;
        int received = Math.min(accepted, space);
        if (!simulate) { energyStored += received; setChanged(); }
        return received;
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; } 

    @Override
    public boolean canReceive(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing; 
    }
    @Override public boolean canExtract(Direction side) { return false; }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, 4.0F, Level.ExplosionInteraction.TNT);
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    // --- ЛОГИКА ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        boolean isLit = false;

        // 1. Зарядка из слота 0
        ItemStack chargeStack = inventory.getStackInSlot(0);
        if (!chargeStack.isEmpty() && energyStored < maxEnergyStored) {
            if (chargeStack.is(Items.REDSTONE)) {
                int energyGain = 400;
                if (energyStored + energyGain <= maxEnergyStored) { energyStored += energyGain; chargeStack.shrink(1); setChanged(); }
            } else if (chargeStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= 1) {
                    int transferRate = Math.min(maxReceiveAmount, energyItem.getTransferRate(chargeStack));
                    int toExtract = Math.min(maxEnergyStored - energyStored, transferRate);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) { energyStored += extracted; setChanged(); }
                }
            }
        }

        // 2. Генерация тепла
        if (activeCoils > 0) {
            float exactCost = activeCoils * 3.2f;
            int intCost = (int) exactCost;
            
            // Накапливаем дробную часть
            energyFraction += (exactCost - intCost);
            if (energyFraction >= 1.0f) {
                intCost += 1;
                energyFraction -= 1.0f;
            }

            int heatToGenerate = activeCoils * 5;

            if (energyStored >= intCost) {
                Direction facing = state.getValue(BlockStateProperties.FACING);
                BlockPos targetPos = pos.relative(facing);
                
                BlockEntity targetBE = level.getBlockEntity(targetPos);
                if (targetBE != null) {
                    CustomHeatStorage targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
                    if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                        
                        // Симуляция: может ли принять всё тепло?
                        int acceptedHeat = targetStorage.receiveHeat(heatToGenerate, true);
                        if (acceptedHeat == heatToGenerate) {
                            // Передаем реально
                            targetStorage.receiveHeat(heatToGenerate, false);
                            energyStored -= intCost;
                            isLit = true;
                            setChanged();
                        }
                    }
                }
            }
        }

        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStored);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getInt("energy");
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        recalculateCoils();
    }
}
