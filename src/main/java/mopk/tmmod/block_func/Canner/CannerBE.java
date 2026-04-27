 
package mopk.tmmod.block_func.Canner;

import mopk.tmmod.custom_interfaces.CustomEnergyStorage;
import mopk.tmmod.custom_interfaces.EnergyNetworkManager;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModDataComponents;
import mopk.tmmod.registration.ModRecipes;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CannerBE extends BlockEntity implements MenuProvider, CustomEnergyStorage, IFluidHandler {
    private int energyStored;
    private int currentMaxEnergyStored = 10000;
    private int currentEnergyTier = 1;
    private int currentMaxReceiveAmount = 32;
    private double currentSpeedMultiplier = 1.0;
    private boolean[] canReceiveSides = new boolean[6];
    private boolean[] canExtractSides = new boolean[6];
    private int progress = 0;
    private int maxProgress = 0;

    private CannerMode mode = CannerMode.ITEMS_TO_ITEM;

    public final FluidTank inputTank = new FluidTank(10000);
    public final FluidTank outputTank = new FluidTank(10000);

    public final ItemStackHandler inventory = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 4 && slot <= 7) {
                recalculateBonuses();
            }
            setChanged();
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CannerBE.this.energyStored;
                case 1 -> CannerBE.this.currentMaxEnergyStored;
                case 2 -> CannerBE.this.progress;
                case 3 -> CannerBE.this.maxProgress;
                case 4 -> CannerBE.this.mode.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 4) CannerBE.this.mode = CannerMode.values()[value];
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public CannerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CANNER_BE.get(), pos, state);
        for (int i = 0; i < 6; i++) {
            canReceiveSides[i] = true;
            canExtractSides[i] = false;
        }
    }

    private RecipeType<CannerRecipe> getCurrentRecipeType() {
        return switch (mode) {
            case ITEMS_TO_ITEM -> ModRecipes.CANNER_ITEMS_TO_ITEM_TYPE.get();
            case ITEM_TO_ITEM_FLUID -> ModRecipes.CANNER_ITEM_TO_ITEM_FLUID_TYPE.get();
            case ITEM_FLUID_TO_ITEM -> ModRecipes.CANNER_ITEM_FLUID_TO_ITEM_TYPE.get();
            case ITEMS_FLUID_TO_ITEM_FLUID -> ModRecipes.CANNER_ITEMS_FLUID_TO_ITEM_FLUID_TYPE.get();
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        handleEnergyCharging();

        Optional<RecipeHolder<CannerRecipe>> recipeHolder = level.getRecipeManager().getRecipeFor(
                getCurrentRecipeType(),
                new CannerRecipe.CannerRecipeInput(inventory.getStackInSlot(0), inventory.getStackInSlot(1), inputTank.getFluid()),
                level
        );

        boolean isLit = false;
        if (recipeHolder.isPresent()) {
            CannerRecipe recipe = recipeHolder.get().value();
            this.maxProgress = (int) (recipe.time() / currentSpeedMultiplier);
            int energyPerTick = (int) (recipe.energyPerTick() * (currentSpeedMultiplier / 2));
            if (energyPerTick < 1) energyPerTick = 1;

            if (canCraft(recipe)) {
                if (energyStored >= energyPerTick) {
                    energyStored -= energyPerTick;
                    progress++;
                    isLit = true;
                    if (progress >= maxProgress) {
                        craft(recipe);
                        progress = 0;
                    }
                }
            } else {
                progress = 0;
            }
        } else {
            progress = 0;
        }

        updateLitState(level, pos, state, isLit);
        setChanged();
    }

    private void handleEnergyCharging() {
        ItemStack chargeStack = inventory.getStackInSlot(3);
        if (!chargeStack.isEmpty() && energyStored < currentMaxEnergyStored) {
            if (chargeStack.is(Items.REDSTONE)) {
                if (energyStored + 400 <= currentMaxEnergyStored) {
                    energyStored += 400;
                    chargeStack.shrink(1);
                }
            } else if (chargeStack.getItem() instanceof mopk.tmmod.custom_interfaces.CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= this.currentEnergyTier) {
                    int space = currentMaxEnergyStored - energyStored;
                    int transferRate = Math.min(this.currentMaxReceiveAmount, energyItem.getTransferRate(chargeStack));
                    int toExtract = Math.min(space, transferRate);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) {
                        energyStored += extracted;
                    }
                }
            }
        }
    }

    private boolean canCraft(CannerRecipe recipe) {
        ItemStack outputSlot = inventory.getStackInSlot(2);
        if (!outputSlot.isEmpty()) {
            if (!ItemStack.isSameItem(outputSlot, recipe.output())) return false;
            if (outputSlot.getCount() + recipe.output().getCount() > outputSlot.getMaxStackSize()) return false;
        }

        if (recipe.fluidOutput().isPresent()) {
            FluidStack recipeOutputFluid = recipe.fluidOutput().get();
            if (!outputTank.getFluid().isEmpty() && !outputTank.getFluid().is(recipeOutputFluid.getFluid())) return false;
            if (outputTank.getFluidAmount() + recipeOutputFluid.getAmount() > outputTank.getCapacity()) return false;
        }

        return true;
    }

    private void craft(CannerRecipe recipe) {
        inventory.getStackInSlot(0).shrink(1);
        if (recipe.input2().isPresent()) inventory.getStackInSlot(1).shrink(1);
        if (recipe.fluidInput().isPresent()) inputTank.drain(recipe.fluidInput().get().getAmount(), IFluidHandler.FluidAction.EXECUTE);

        ItemStack result = recipe.output().copy();
        inventory.insertItem(2, result, false);

        if (recipe.fluidOutput().isPresent()) {
            outputTank.fill(recipe.fluidOutput().get().copy(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void updateLitState(Level level, BlockPos pos, BlockState state, boolean isLit) {
        boolean wasLit = state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit && state.hasProperty(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }
    }

    public void swapFluids() {
        FluidStack input = inputTank.getFluid().copy();
        FluidStack output = outputTank.getFluid().copy();
        inputTank.setFluid(output);
        outputTank.setFluid(input);
        setChanged();
    }

    private void recalculateBonuses() {
        int totalAccumulatorModules = 0;
        int totalTransformerModules = 0;
        int totalSpeedModules = 0;

        for (int i = 4; i <= 7; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.has(ModDataComponents.ACCUMULATORBONUS.get())) totalAccumulatorModules += stack.getCount();
                if (stack.has(ModDataComponents.TRANSFORMERBONUS.get())) totalTransformerModules += stack.getCount();
                if (stack.has(ModDataComponents.SPEEDBONUS.get())) totalSpeedModules += stack.getCount();
            }
        }

        this.currentMaxEnergyStored = 10000 + (5000 * totalAccumulatorModules);
        this.currentEnergyTier = Math.min(1 + totalTransformerModules, 5);
        this.currentMaxReceiveAmount = (int) (32 * Math.pow(4, this.currentEnergyTier - 1));
        this.currentSpeedMultiplier = Math.pow(1.5, totalSpeedModules);

        if (this.energyStored > this.currentMaxEnergyStored) this.energyStored = this.currentMaxEnergyStored;
        setChanged();
    }

    public void toggleMode() {
        this.mode = this.mode.next();
        setChanged();
    }

    public CannerMode getMode() { return mode; }

    public CustomEnergyStorage getEnergyStorage(Direction direction) {
        return this;
    }

    @Override
    public Component getDisplayName() { return Component.translatable("container.tmmod.canner"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CannerMenu(id, inventory, this, this.data);
    }

    // CustomEnergyStorage implementation
    @Override public int getEnergyStored() { return energyStored; }
    @Override public int getMaxEnergyStored() { return currentMaxEnergyStored; }
    @Override public int getEnergyTier() { return currentEnergyTier; }
    @Override public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > this.currentEnergyTier) { triggerExplosion(); return 0; }
        int energyReceived = Math.min(Math.min(maxReceive, this.currentMaxReceiveAmount), this.currentMaxEnergyStored - this.energyStored);
        if (!simulate) { this.energyStored += energyReceived; setChanged(); }
        return energyReceived;
    }
    @Override public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(maxExtract, this.energyStored);
        if (!simulate) { this.energyStored -= energyExtracted; setChanged(); }
        return energyExtracted;
    }
    @Override public boolean canReceive(Direction side) { return true; }
    @Override public boolean canExtract(Direction side) { return false; }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
        }
    }

    // IFluidHandler implementation
    @Override public int getTanks() { return 2; }
    @Nonnull @Override public FluidStack getFluidInTank(int tank) { return tank == 0 ? inputTank.getFluid() : outputTank.getFluid(); }
    @Override public int getTankCapacity(int tank) { return 10000; }
    @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return tank == 0; }
    @Override public int fill(FluidStack resource, FluidAction action) { return inputTank.fill(resource, action); }
    @Nonnull @Override public FluidStack drain(FluidStack resource, FluidAction action) { return inputTank.drain(resource, action); }
    @Nonnull @Override public FluidStack drain(int maxDrain, FluidAction action) { return outputTank.drain(maxDrain, action); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energyStored);
        tag.putInt("progress", progress);
        tag.putInt("mode", mode.ordinal());
        tag.put("inputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("outputTank", outputTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStored = tag.getInt("energy");
        progress = tag.getInt("progress");
        mode = CannerMode.values()[tag.getInt("mode")];
        inputTank.readFromNBT(registries, tag.getCompound("inputTank"));
        outputTank.readFromNBT(registries, tag.getCompound("outputTank"));
        recalculateBonuses();
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
}

