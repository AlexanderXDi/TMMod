package mopk.tmmod.block_func.LiquidHeatGenerator;

import mopk.tmmod.custom_interfaces.CustomHeatStorage;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModFluids;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class LiquidHeatGeneratorBE extends BlockEntity implements CustomHeatStorage, MenuProvider {
    
    // Внутренний бак на 10 ведер (10000 mB)
    private final FluidTank fluidTank = new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return getFuelValue(stack.getFluid()) > 0;
        }
    };

    private int burnTime = 0;
    private int maxBurnTime = 0;

    public LiquidHeatGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIQUID_HEAT_GENERATOR_BE.get(), pos, state);
    }

    /**
     * Возвращает количество тиков горения за 10 mB жидкости.
     * При 20 HU/t:
     * Биогаз: 32 HU/mB -> 10 mB = 320 HU -> 16 тиков.
     * Биомасса: 8 HU/mB -> 10 mB = 80 HU -> 4 тика.
     */
    private int getFuelValue(Fluid fluid) {
        if (fluid == ModFluids.BIOGAS.source.get()) return 16;
        if (fluid == ModFluids.BIOMASS.source.get()) return 4;
        return 0;
    }

    public CustomHeatStorage getHeatStorage(Direction side) {
        return this;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public int getHeatStored() {
        return 0;
    }

    @Override
    public int getMaxHeatStored() {
        return 0;
    }

    @Override
    public int receiveHeat(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractHeat(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LiquidHeatGeneratorBE.this.burnTime;
                case 1 -> LiquidHeatGeneratorBE.this.maxBurnTime;
                case 2 -> LiquidHeatGeneratorBE.this.fluidTank.getFluidAmount();
                case 3 -> LiquidHeatGeneratorBE.this.fluidTank.getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LiquidHeatGeneratorBE.this.burnTime = value;
                case 1 -> LiquidHeatGeneratorBE.this.maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tmmod.liquid_heat_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new LiquidHeatGeneratorMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        handleFluidInteraction();

        boolean isLit = false;
        int heatToGenerate = 20; 
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockEntity targetBE = level.getBlockEntity(targetPos);
        CustomHeatStorage targetStorage = null;

        if (targetBE != null) {
            targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
        }

        // Логика горения
        if (this.burnTime > 0) {
            isLit = true;
            if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                int accepted = targetStorage.receiveHeat(heatToGenerate, false);
                if (accepted > 0) {
                    this.burnTime--;
                    setChanged();
                }
            }
        }

        // Если горение закончилось, пробуем забрать новую порцию из БАКА
        if (this.burnTime <= 0) {
            if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                FluidStack fluidInTank = fluidTank.getFluid();
                int ticks = getFuelValue(fluidInTank.getFluid());
                
                if (ticks > 0 && fluidInTank.getAmount() >= 10) {
                    fluidTank.drain(10, IFluidHandler.FluidAction.EXECUTE);
                    this.maxBurnTime = ticks;
                    this.burnTime = ticks;
                    isLit = true;
                    setChanged();
                }
            }
        }

        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }

        if (isLit) {
            setChanged();
        }
    }

    private void handleFluidInteraction() {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty()) return;

        IFluidHandlerItem handler = input.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null) {
            FluidStack drainable = handler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (!drainable.isEmpty() && fluidTank.fill(drainable, IFluidHandler.FluidAction.SIMULATE) > 0) {
                int filled = fluidTank.fill(drainable, IFluidHandler.FluidAction.EXECUTE);
                handler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                
                ItemStack result = handler.getContainer();
                ItemStack outputSlot = inventory.getStackInSlot(1);
                
                if (outputSlot.isEmpty()) {
                    inventory.setStackInSlot(1, result);
                    inventory.getStackInSlot(0).shrink(1);
                } else if (ItemStack.isSameItem(outputSlot, result) && outputSlot.getCount() < outputSlot.getMaxStackSize()) {
                    outputSlot.grow(1);
                    inventory.getStackInSlot(0).shrink(1);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
        fluidTank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
        fluidTank.readFromNBT(registries, tag);
    }
}
