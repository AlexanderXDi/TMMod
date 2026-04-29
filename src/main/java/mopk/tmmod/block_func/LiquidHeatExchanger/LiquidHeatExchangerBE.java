package mopk.tmmod.block_func.LiquidHeatExchanger;

import mopk.tmmod.custom_interfaces.CustomHeatStorage;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModFluids;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LiquidHeatExchangerBE extends BlockEntity implements CustomHeatStorage, MenuProvider, IFluidHandler {

    // Резервуары по 2 ведра
    private final FluidTank hotTank = new FluidTank(2000) {
        @Override
        protected void onContentsChanged() { setChanged(); }
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isHotFluid(stack.getFluid());
        }
    };

    private final FluidTank coldTank = new FluidTank(2000) {
        @Override
        protected void onContentsChanged() { setChanged(); }
        @Override
        public boolean isFluidValid(FluidStack stack) { return false; } // Только выход
    };

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? hotTank.getFluid() : coldTank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return 2000;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 && isHotFluid(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!isHotFluid(resource.getFluid())) return 0;
        return hotTank.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        if (coldTank.getFluid().isFluidEqual(resource)) {
            return coldTank.drain(resource.getAmount(), action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return coldTank.drain(maxDrain, action);
    }

    // Слоты: 
    // 0, 1 - Горячая жидкость (вход, пустая тара)
    // 2, 3 - Холодная жидкость (пустая тара, выход)
    // 4-13 - Теплопроводы (10 шт)
    // 14-16 - Улучшения (3 шт)
    public final ItemStackHandler inventory = new ItemStackHandler(17) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int currentHeatProduction = 0;
    private int maxHeatProduction = 0;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> currentHeatProduction;
                case 1 -> maxHeatProduction;
                case 2 -> hotTank.getFluidAmount();
                case 3 -> hotTank.getCapacity();
                case 4 -> coldTank.getFluidAmount();
                case 5 -> coldTank.getCapacity();
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            if (index == 0) currentHeatProduction = value;
            if (index == 1) maxHeatProduction = value;
        }
        @Override
        public int getCount() { return 6; }
    };

    public LiquidHeatExchangerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIQUID_HEAT_EXCHANGER_BE.get(), pos, state);
    }

    private boolean isHotFluid(Fluid fluid) {
        return fluid == Fluids.LAVA || fluid == ModFluids.HOT_COOLANT.source.get();
    }

    private Fluid getCooledFluid(Fluid hotFluid) {
        if (hotFluid == Fluids.LAVA) return ModFluids.PAHOEHOE_LAVA.source.get(); // Лава -> Базальтовая лава
        if (hotFluid == ModFluids.HOT_COOLANT.source.get()) return ModFluids.COOLANT.source.get(); // Гор. хладагент -> Хладагент
        return Fluids.EMPTY;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        handleFluidInteraction();
        
        // Считаем теплопроводы
        int conductorCount = 0;
        for (int i = 4; i <= 13; i++) {
            if (inventory.getStackInSlot(i).is(ModItems.HEAT_CONDUCTOR.get())) {
                conductorCount++;
            }
        }

        // Логика расчета скорости: каждые 2 дают 20 HU/t. Макс 100.
        this.maxHeatProduction = (conductorCount / 2) * 20;
        this.currentHeatProduction = 0;

        if (this.maxHeatProduction > 0 && !hotTank.isEmpty()) {
            Fluid hotFluid = hotTank.getFluid().getFluid();
            Fluid coldResult = getCooledFluid(hotFluid);
            
            if (coldResult != Fluids.EMPTY) {
                // Пытаемся передать тепло
                Direction facing = state.getValue(BlockStateProperties.FACING);
                BlockPos targetPos = pos.relative(facing);
                BlockEntity targetBE = level.getBlockEntity(targetPos);
                CustomHeatStorage targetStorage = null;
                if (targetBE != null) {
                    targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
                }

                if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                    // Сколько мВ нужно охладить для maxHeatProduction?
                    // 1 мВ = 20 HU. Если maxHeatProduction = 20 HU/t, то 1 мВ/t.
                    int mbToProcess = maxHeatProduction / 20;
                    
                    // Проверяем наличие жидкости и место в выходном баке
                    int canDrain = hotTank.drain(mbToProcess, IFluidHandler.FluidAction.SIMULATE).getAmount();
                    int canFillCold = coldTank.fill(new FluidStack(coldResult, canDrain), IFluidHandler.FluidAction.SIMULATE);
                    
                    int actualToProcess = Math.min(canDrain, canFillCold);
                    if (actualToProcess > 0) {
                        int heatToTransfer = actualToProcess * 20;
                        int acceptedHeat = targetStorage.receiveHeat(heatToTransfer, false);
                        
                        if (acceptedHeat > 0) {
                            // Округляем до целых мВ, которые реально превратились в тепло
                            int actuallyProcessedMb = acceptedHeat / 20;
                            if (actuallyProcessedMb > 0) {
                                hotTank.drain(actuallyProcessedMb, IFluidHandler.FluidAction.EXECUTE);
                                coldTank.fill(new FluidStack(coldResult, actuallyProcessedMb), IFluidHandler.FluidAction.EXECUTE);
                                this.currentHeatProduction = acceptedHeat;
                            }
                        }
                    }
                }
            }
        }

        boolean isLit = currentHeatProduction > 0;
        if (state.getValue(BlockStateProperties.LIT) != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }
        setChanged();
    }

    private void handleFluidInteraction() {
        // Вход (слоты 0 и 1)
        handleTankFilling(0, 1, hotTank);
        // Выход (слоты 2 и 3)
        handleTankEmptying(2, 3, coldTank);
    }

    private void handleTankFilling(int inputSlot, int outputSlot, FluidTank tank) {
        ItemStack input = inventory.getStackInSlot(inputSlot);
        if (input.isEmpty()) return;

        IFluidHandlerItem handler = input.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null) {
            FluidStack drainable = handler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (!drainable.isEmpty() && isHotFluid(drainable.getFluid()) && tank.fill(drainable, IFluidHandler.FluidAction.SIMULATE) > 0) {
                int filled = tank.fill(drainable, IFluidHandler.FluidAction.EXECUTE);
                handler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                moveContainerToOutput(inputSlot, outputSlot, handler.getContainer());
            }
        }
    }

    private void handleTankEmptying(int inputSlot, int outputSlot, FluidTank tank) {
        ItemStack input = inventory.getStackInSlot(inputSlot);
        if (input.isEmpty()) return;

        IFluidHandlerItem handler = input.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null) {
            FluidStack tankFluid = tank.getFluid();
            if (!tankFluid.isEmpty()) {
                int filled = handler.fill(tankFluid, IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                    moveContainerToOutput(inputSlot, outputSlot, handler.getContainer());
                }
            }
        }
    }

    private void moveContainerToOutput(int inputSlot, int outputSlot, ItemStack result) {
        ItemStack outStack = inventory.getStackInSlot(outputSlot);
        if (outStack.isEmpty()) {
            inventory.setStackInSlot(outputSlot, result);
            inventory.getStackInSlot(inputSlot).shrink(1);
        } else if (ItemStack.isSameItem(outStack, result) && outStack.getCount() < outStack.getMaxStackSize()) {
            outStack.grow(1);
            inventory.getStackInSlot(inputSlot).shrink(1);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tmmod.liquid_heat_exchanger");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new LiquidHeatExchangerMenu(id, inventory, this, this.data);
    }

    @Override
    public int getHeatStored() { return 0; }
    @Override
    public int getMaxHeatStored() { return 0; }
    @Override
    public int receiveHeat(int maxReceive, boolean simulate) { return 0; }
    @Override
    public int extractHeat(int maxExtract, boolean simulate) { return 0; }
    @Override
    public boolean canConnectHeat(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("hotTank", hotTank.writeToNBT(registries, new CompoundTag()));
        tag.put("coldTank", coldTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        hotTank.readFromNBT(registries, tag.getCompound("hotTank"));
        coldTank.readFromNBT(registries, tag.getCompound("coldTank"));
    }
}
