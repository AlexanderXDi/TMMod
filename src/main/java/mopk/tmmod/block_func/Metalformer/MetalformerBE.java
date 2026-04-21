package mopk.tmmod.block_func.Metalformer;

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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

import static mopk.tmmod.block_func.Metalformer.MetalformerMode.*;


public class MetalformerBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private int energyStored;
    private int currentMaxEnergyStored;
    private int currentEnergyTier;
    private int currentMaxReceiveAmount;
    private double currentSpeedMultiplier;
    private boolean[] canReceiveSides = new boolean[6];
    private boolean[] canExtractSides = new boolean[6];
    private int progress = 0;
    private int maxProgress = 0;

    private MetalformerMode mode = FORGING;

    private RecipeType<MetalformerRecipe> getCurrentRecipeType() {
        return (RecipeType<MetalformerRecipe>) (switch (mode) {
            case FORGING -> ModRecipes.FORGING_TYPE.get();
            case CUTTING -> ModRecipes.CUTTING_TYPE.get();
            case SQUEEZING -> ModRecipes.SQUEEZING_TYPE.get();
        });
    }

    public MetalformerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.METALFORMER_BE.get(), pos, state);
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

    public CustomEnergyStorage getEnergyStorage(Direction side) {
        return this;
    }

    public int getEnergyStored() {
        return this.energyStored;
    }

    public int getMaxEnergyStored() {
        return this.currentMaxEnergyStored;
    }

    public int getEnergyTier() {
        return this.currentEnergyTier;
    }

    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > this.currentEnergyTier) {
            if (!simulate) {
                triggerExplosion();
                this.energyStored = 0;
                this.setChanged();
            }
            return 0;
        }

        int actualMaxReceive = Math.min(maxReceive, this.currentMaxReceiveAmount);

        int energyReceived = Math.min(actualMaxReceive, this.currentMaxEnergyStored - this.energyStored);

        if (!simulate) {
            this.energyStored += energyReceived;
            this.setChanged();
        }
        return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(maxExtract, this.energyStored);

        if (!simulate) {
            this.energyStored -= energyExtracted;
            this.setChanged();
        }
        return energyExtracted;
    }

    public boolean canReceive(Direction side) {
        return canReceiveSides[side.ordinal()];
    }

    public boolean canExtract(Direction side) {
        return canExtractSides[side.ordinal()];
    }

    private void recalculateBonuses() {
        int totalAccumulatorModules = 0;
        int totalTransformerModules = 0;
        int totalMaxReceiveModules = 0;
        int totalSpeedModules = 0;

        for (int i = 3; i <= 6; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.has(ModDataComponents.ACCUMULATORBONUS.get())) {
                    totalAccumulatorModules += stack.getCount();
                }
                if (stack.has(ModDataComponents.TRANSFORMERBONUS.get())) {
                    totalTransformerModules += stack.getCount();
                }
                if (stack.has(ModDataComponents.SPEEDBONUS.get())) {
                    totalSpeedModules += stack.getCount();
                }
            }
        }

        // --- Пересчет Максимальной Емкости ---
        int newCapacity = 10000;
        if (totalAccumulatorModules > 0) {
            long calcCap = 10000L + (5000L * totalAccumulatorModules);
            newCapacity = (int) Math.min(calcCap, Integer.MAX_VALUE);
        }
        this.currentMaxEnergyStored = newCapacity;

        // --- Пересчет Тира Энергии ---
        int newEnergyTier = 1;
        if (totalTransformerModules > 0) {
            newEnergyTier = 1 + totalTransformerModules;
            if (newEnergyTier > 5) {
                newEnergyTier = 5;
            }
        }
        this.currentEnergyTier = newEnergyTier;

        // --- Пересчет Максимального Приема за тик ---
        int newMaxReceiveAmount = 32;
        if (totalMaxReceiveModules > 0) {
            newMaxReceiveAmount = 32 + (16 * totalMaxReceiveModules);
        } else if (totalTransformerModules > 0) {
            newMaxReceiveAmount = (int) (32 * Math.pow(4, this.currentEnergyTier - 1));
        }
        this.currentMaxReceiveAmount = newMaxReceiveAmount;

        if (this.energyStored > this.currentMaxEnergyStored) {
            this.energyStored = this.currentMaxEnergyStored;
        }

        // --- Пересчет скорости ---
        if (totalSpeedModules > 0) {
            for (int i = 0  ; i <= totalSpeedModules; i++) {
                this.currentSpeedMultiplier = Math.pow(1.5, totalSpeedModules);
            }
        } else {
            this.currentSpeedMultiplier = 1.0;
        }

        this.setChanged();
    }

    private void triggerExplosion() {
        float explosionRadius = 4.0F;
        Level.ExplosionInteraction explosionType = Level.ExplosionInteraction.TNT;
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(
                    null,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D,
                    explosionRadius,
                    explosionType
            );
        }
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MetalformerBE.this.energyStored;
                case 1 -> MetalformerBE.this.currentMaxEnergyStored;
                case 2 -> MetalformerBE.this.progress;
                case 3 -> MetalformerBE.this.maxProgress;
                case 4 -> mode.ordinal();
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { if (index == 4) mode = MetalformerMode.values()[value]; }

        @Override
        public int getCount() {
            return 5;
        }
    };
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.metalformer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MetalformerMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 3 && slot <= 6) {
                MetalformerBE.this.recalculateBonuses();
            }
            setChanged();
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void toggleMode() {
        this.mode = this.mode.next();
        setChanged();
    }

    public MetalformerMode getMode() { return mode; }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean isLit = false;
        Optional<RecipeHolder<MetalformerRecipe>> recipeHolder = level.getRecipeManager().getRecipeFor(getCurrentRecipeType(), new SingleRecipeInput(inventory.getStackInSlot(0)), level);

        ItemStack chargeStack = inventory.getStackInSlot(2);
        if (!chargeStack.isEmpty() && energyStored < currentMaxEnergyStored) {
            if (chargeStack.is(Items.REDSTONE)) {
                int energyGain = 400;
                if (energyStored + energyGain <= currentMaxEnergyStored) {
                    energyStored += energyGain;
                    chargeStack.shrink(1);
                    setChanged();
                }
            } else if (chargeStack.getItem() instanceof mopk.tmmod.energy_network.CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= this.currentEnergyTier) {
                    int space = currentMaxEnergyStored - energyStored;
                    // Скорость разрядки - это минимум из скорости приема машины и скорости предмета
                    int transferRate = Math.min(this.currentMaxReceiveAmount, energyItem.getTransferRate(chargeStack));
                    int toExtract = Math.min(space, transferRate);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) {
                        energyStored += extracted;
                        setChanged();
                    }
                }
            }
        }

        if (recipeHolder.isPresent()) {
            MetalformerRecipe recipe = recipeHolder.get().value();
            this.maxProgress = recipe.time();
            int energyPerTick = 10;

            maxProgress = (int) (maxProgress / currentSpeedMultiplier);
            energyPerTick = (int) (energyPerTick * (currentSpeedMultiplier / 2));

            if (canCraft(recipe)) {
                if (energyStored >= energyPerTick) {
                    extractEnergy(energyPerTick, false);
                    progress++;
                    isLit = true;

                    if (progress >= maxProgress) {
                        craft(recipe);
                        progress = 0;
                    }
                }
            } else {
                progress = 0;
                isLit = false;
            }
        } else {
            progress = 0;
            isLit = false;
        }

        boolean wasLit = state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit && state.hasProperty(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
            isLit = true;
        }

        if (isLit) {
            setChanged();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
        }
    }

    private boolean canCraft(MetalformerRecipe recipe) {
        ItemStack outputSlot = inventory.getStackInSlot(1);
        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.isSameItem(outputSlot, recipe.output())) return false;
        return outputSlot.getCount() + recipe.output().getCount() <= outputSlot.getMaxStackSize();
    }

    private void craft(MetalformerRecipe recipe) {
        inventory.getStackInSlot(0).shrink(1);
        ItemStack result = recipe.output().copy();
        inventory.insertItem(1, result, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energyStored);
        tag.putInt("maxCapacity", currentMaxEnergyStored);
        tag.putInt("energyTier", currentEnergyTier);
        tag.putInt("maxReceiveAmount", currentMaxReceiveAmount);
        byte[] receiveSidesBytes = new byte[6];
        byte[] extractSidesBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            receiveSidesBytes[i] = (byte) (canReceiveSides[i] ? 1 : 0);
            extractSidesBytes[i] = (byte) (canExtractSides[i] ? 1 : 0);
        }
        tag.putByteArray("canReceiveSides", receiveSidesBytes);
        tag.putByteArray("canExtractSides", extractSidesBytes);

        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("Mode", this.mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStored = tag.getInt("energy");
        currentMaxEnergyStored = tag.getInt("maxCapacity");
        currentEnergyTier = tag.getInt("energyTier");
        currentMaxReceiveAmount = tag.getInt("maxReceiveAmount");
        byte[] loadedReceiveSides = tag.getByteArray("canReceiveSides");
        if (loadedReceiveSides.length == 6) {
            for (int i = 0; i < 6; i++) {
                canReceiveSides[i] = loadedReceiveSides[i] == 1;
            }
        }
        byte[] loadedExtractSides = tag.getByteArray("canExtractSides");
        if (loadedExtractSides.length == 6) {
            for (int i = 0; i < 6; i++) {
                canExtractSides[i] = loadedExtractSides[i] == 1;
            }
        }
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        int modeIndex = tag.getInt("Mode");
        if (modeIndex >= 0 && modeIndex < MetalformerMode.values().length) {
            this.mode = MetalformerMode.values()[modeIndex];
        }
    }
}
