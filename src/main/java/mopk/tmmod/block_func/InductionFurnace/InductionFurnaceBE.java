package mopk.tmmod.block_func.InductionFurnace;

import mopk.tmmod.custom_interfaces.CustomEnergyStorage;
import mopk.tmmod.custom_interfaces.CustomHeatStorage;
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
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;


public class InductionFurnaceBE extends BlockEntity implements MenuProvider, CustomEnergyStorage, CustomHeatStorage {
    private int energyStored;
    private int currentMaxEnergyStored;
    private int currentEnergyTier;
    private int currentMaxReceiveAmount;
    private int currentHeatFlow = 0;
    private int lastHeatFlow = 0;
    private int progress = 0;
    private int maxProgress = 0;
    private double currentSpeedMultiplier = 1.0;


    public InductionFurnaceBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUCTION_FURNACE_BE.get(), pos, state);
        this.energyStored = 0;
        this.currentMaxEnergyStored = 40000;
        this.currentEnergyTier = 2;
        this.currentMaxReceiveAmount = 128;
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
        return 0;
    }

    public boolean canReceive(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing.getCounterClockWise();
    }

    public boolean canExtract(Direction side) {
        return false;
    }

    public CustomHeatStorage getHeatStorage(Direction side) {
        return this;
    }

    @Override
    public int getHeatStored() {
        return 0;
    }

    @Override
    public int getMaxHeatStored() {
        return 1000;
    }

    @Override
    public int receiveHeat(int maxReceive, boolean simulate) {
        if (!simulate) {
            currentHeatFlow += maxReceive;
            setChanged();
        }
        return maxReceive;
    }

    @Override
    public int extractHeat(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing.getCounterClockWise();
    }

    private void recalculateBonuses() {
        int totalSpeedModules = 0;
        for (int i = 5; i <= 8; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.has(ModDataComponents.SPEEDBONUS.get())) {
                totalSpeedModules += stack.getCount();
            }
        }
        this.currentSpeedMultiplier = Math.pow(1.5, totalSpeedModules);
        setChanged();
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
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> InductionFurnaceBE.this.energyStored;
                case 1 -> InductionFurnaceBE.this.currentMaxEnergyStored;
                case 2 -> InductionFurnaceBE.this.progress;
                case 3 -> InductionFurnaceBE.this.maxProgress;
                case 4 -> InductionFurnaceBE.this.lastHeatFlow;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tmmod.induction_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new InductionFurnaceMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 5 && slot <= 8) {
                recalculateBonuses();
            }
            setChanged();
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // Выталкивание предметов
        mopk.tmmod.registration.InventoryUtils.handleEjection(level, pos, inventory, new int[]{5, 6, 7, 8}, new int[]{2, 3}, level.getGameTime());

        ItemStack chargeStack = inventory.getStackInSlot(4);
        if (!chargeStack.isEmpty() && energyStored < currentMaxEnergyStored) {
            if (chargeStack.getItem() instanceof mopk.tmmod.custom_interfaces.CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= this.currentEnergyTier) {
                    int space = currentMaxEnergyStored - energyStored;
                    int transferRate = Math.min(this.currentMaxReceiveAmount, energyItem.getTransferRate(chargeStack));
                    int toExtract = Math.min(space, transferRate);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) {
                        energyStored += extracted;
                        setChanged();
                    }
                }
            } else if (chargeStack.is(Items.REDSTONE)) {
                int energyGain = 400;
                if (energyStored + energyGain <= currentMaxEnergyStored) {
                    energyStored += energyGain;
                    chargeStack.shrink(1);
                    setChanged();
                }
            }
        }

        RecipeInput input = new RecipeInput() {
            @Override public ItemStack getItem(int i) { return inventory.getStackInSlot(i); }
            @Override public int size() { return 2; }
        };

        Optional<RecipeHolder<InductionFurnaceRecipe>> recipeHolder = level.getRecipeManager().getRecipeFor(
                ModRecipes.INDUCTION_FURNACE_TYPE.get(),
                input,
                level
        );

        boolean isCrafting = false;
        if (recipeHolder.isPresent()) {
            InductionFurnaceRecipe recipe = recipeHolder.get().value();
            // System.out.println("Recipe found: " + recipeHolder.get().id()); // Раскомментируйте для отладки
            this.maxProgress = recipe.time();
            int euPerTick = recipe.euPerTick();
            int requiredHeatPerTick = recipe.heatPerSecond() / 20;

            if (canCraft(recipe) && energyStored >= euPerTick) {
                energyStored -= euPerTick;

                double heatEfficiency = 1.0;
                if (requiredHeatPerTick > 0) {
                    heatEfficiency = Math.min(1.0, (double) currentHeatFlow / requiredHeatPerTick);
                }

                if (heatEfficiency > 0) {
                    progress += (int) (100 * heatEfficiency * currentSpeedMultiplier);
                    isCrafting = true;
                    if (progress >= maxProgress * 100) {
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

        lastHeatFlow = currentHeatFlow;
        currentHeatFlow = 0;

        boolean isLit = isCrafting;
        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
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

    private boolean canCraft(InductionFurnaceRecipe recipe) {
        return canFitOutput(recipe.output1(), 2) && canFitOutput(recipe.output2(), 3);
    }

    private boolean canFitOutput(ItemStack stack, int slot) {
        if (stack.isEmpty()) return true;
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItem(existing, stack)) return false;
        return existing.getCount() + stack.getCount() <= existing.getMaxStackSize();
    }

    private void craft(InductionFurnaceRecipe recipe) {
        if (recipe.input2().isEmpty()) {
            if (recipe.input1().test(inventory.getStackInSlot(0))) {
                inventory.getStackInSlot(0).shrink(recipe.count1());
            } else if (recipe.input1().test(inventory.getStackInSlot(1))) {
                inventory.getStackInSlot(1).shrink(recipe.count1());
            }
        } else {
            // Проверяем, какой ингредиент в каком слоте
            if (recipe.input1().test(inventory.getStackInSlot(0)) && recipe.input2().test(inventory.getStackInSlot(1))) {
                inventory.getStackInSlot(0).shrink(recipe.count1());
                inventory.getStackInSlot(1).shrink(recipe.count2());
            } else if (recipe.input1().test(inventory.getStackInSlot(1)) && recipe.input2().test(inventory.getStackInSlot(0))) {
                inventory.getStackInSlot(1).shrink(recipe.count1());
                inventory.getStackInSlot(0).shrink(recipe.count2());
            }
        }

        if (!recipe.output1().isEmpty()) inventory.insertItem(2, recipe.output1().copy(), false);
        if (!recipe.output2().isEmpty()) inventory.insertItem(3, recipe.output2().copy(), false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energyStored);
        tag.putInt("maxCapacity", currentMaxEnergyStored);
        tag.putInt("energyTier", currentEnergyTier);
        tag.putInt("maxReceiveAmount", currentMaxReceiveAmount);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStored = tag.getInt("energy");
        currentMaxEnergyStored = tag.getInt("maxCapacity");
        currentEnergyTier = tag.getInt("energyTier");
        currentMaxReceiveAmount = tag.getInt("maxReceiveAmount");
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        recalculateBonuses();
    }
}
