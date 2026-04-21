package mopk.tmmod.block_func.Compressor;

import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class CompressorBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private int energyStored;
    private int currentMaxEnergyStored;
    private int currentEnergyTier;
    private int currentMaxReceiveAmount;
    private double currentSpeedMultiplier;
    private boolean[] canReceiveSides = new boolean[6];
    private boolean[] canExtractSides = new boolean[6];
    private int progress = 0;
    private int maxProgress = 0;

    public CompressorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSOR_BE.get(), pos, state);
        this.energyStored = 0;
        this.currentMaxEnergyStored = 10000;
        this.currentEnergyTier = 1;
        this.currentMaxReceiveAmount = 32;
        for (int i = 0; i < 6; i++) {
            canReceiveSides[i] = true;
            canExtractSides[i] = false;
        }
        recalculateBonuses();
    }

    public CustomEnergyStorage getEnergyStorage(Direction side) { return this; }
    @Override public int getEnergyStored() { return this.energyStored; }
    @Override public int getMaxEnergyStored() { return this.currentMaxEnergyStored; }
    @Override public int getEnergyTier() { return this.currentEnergyTier; }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > this.currentEnergyTier) {
            if (!simulate) { triggerExplosion(); this.energyStored = 0; this.setChanged(); }
            return 0;
        }
        int actualMaxReceive = Math.min(maxReceive, this.currentMaxReceiveAmount);
        int energyReceived = Math.min(actualMaxReceive, this.currentMaxEnergyStored - this.energyStored);
        if (!simulate) { this.energyStored += energyReceived; this.setChanged(); }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(maxExtract, this.energyStored);
        if (!simulate) { this.energyStored -= energyExtracted; this.setChanged(); }
        return energyExtracted;
    }

    @Override public boolean canReceive(Direction side) { return canReceiveSides[side.ordinal()]; }
    @Override public boolean canExtract(Direction side) { return canExtractSides[side.ordinal()]; }

    private void recalculateBonuses() {
        int totalAccumulatorModules = 0;
        int totalTransformerModules = 0;
        int totalSpeedModules = 0;

        for (int i = 3; i <= 6; i++) {
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
        if (this.energyStored > this.currentMaxEnergyStored) this.energyStored = this.currentMaxEnergyStored;
        this.currentSpeedMultiplier = Math.pow(1.5, totalSpeedModules);
        this.setChanged();
    }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, 4.0F, Level.ExplosionInteraction.TNT);
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> CompressorBE.this.energyStored;
                case 1 -> CompressorBE.this.currentMaxEnergyStored;
                case 2 -> CompressorBE.this.progress;
                case 3 -> CompressorBE.this.maxProgress;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 4; }
    };

    @Override public Component getDisplayName() { return Component.translatable("container.compressor"); }

    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CompressorMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(7) {
        @Override protected void onContentsChanged(int slot) {
            if (slot >= 3 && slot <= 6) CompressorBE.this.recalculateBonuses();
            setChanged();
        }
    };

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        boolean isLit = false;
        Optional<RecipeHolder<CompressorRecipe>> recipeHolder = level.getRecipeManager().getRecipeFor(ModRecipes.COMPRESSOR_TYPE.get(), new SingleRecipeInput(inventory.getStackInSlot(0)), level);

        // Зарядка из слота №2
        ItemStack chargeStack = inventory.getStackInSlot(2);
        if (!chargeStack.isEmpty() && energyStored < currentMaxEnergyStored) {
            if (chargeStack.is(Items.REDSTONE)) {
                int energyGain = 400;
                if (energyStored + energyGain <= currentMaxEnergyStored) { energyStored += energyGain; chargeStack.shrink(1); setChanged(); }
            } else if (chargeStack.getItem() instanceof mopk.tmmod.energy_network.CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= this.currentEnergyTier) {
                    int transferRate = Math.min(this.currentMaxReceiveAmount, energyItem.getTransferRate(chargeStack));
                    int toExtract = Math.min(currentMaxEnergyStored - energyStored, transferRate);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) { energyStored += extracted; setChanged(); }
                }
            }
        }

        if (recipeHolder.isPresent()) {
            CompressorRecipe recipe = recipeHolder.get().value();
            this.maxProgress = recipe.time();
            int energyPerTick = 10;
            int actualMaxProgress = (int) (maxProgress / currentSpeedMultiplier);
            int actualEnergyPerTick = (int) (energyPerTick * (currentSpeedMultiplier / 2));

            if (canCraft(recipe)) {
                if (energyStored >= actualEnergyPerTick) {
                    extractEnergy(actualEnergyPerTick, false);
                    progress++;
                    isLit = true;
                    if (progress >= actualMaxProgress) { craft(recipe); progress = 0; }
                }
            } else { progress = 0; }
        } else { progress = 0; }

        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) { level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3); }
        if (isLit) setChanged();
    }

    private boolean canCraft(CompressorRecipe recipe) {
        ItemStack outputSlot = inventory.getStackInSlot(1);
        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.isSameItem(outputSlot, recipe.output())) return false;
        return outputSlot.getCount() + recipe.output().getCount() <= outputSlot.getMaxStackSize();
    }

    private void craft(CompressorRecipe recipe) {
        inventory.getStackInSlot(0).shrink(1);
        inventory.insertItem(1, recipe.output().copy(), false);
    }

    @Override public void onLoad() { super.onLoad(); if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, worldPosition); }
    @Override public void setRemoved() { super.setRemoved(); if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(worldPosition); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energyStored);
        tag.putInt("progress", progress);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStored = tag.getInt("energy");
        progress = tag.getInt("progress");
        recalculateBonuses();
    }
}
