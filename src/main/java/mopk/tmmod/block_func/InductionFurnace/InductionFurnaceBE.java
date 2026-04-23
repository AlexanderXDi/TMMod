package mopk.tmmod.block_func.InductionFurnace;

import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.CustomHeatStorage;
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
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class InductionFurnaceBE extends BlockEntity implements MenuProvider, CustomEnergyStorage, CustomHeatStorage {
    private int energyStored = 0;
    private final int maxEnergyStored = 40000;
    private final int energyTier = 2;
    private final int maxReceiveAmount = 128;

    private int currentHeatFlow = 0; // Накоплено за текущий тик
    private int lastHeatFlow = 0;    // Для отображения в GUI (hU/t -> hU/s)
    
    private int progress = 0;
    private int maxProgress = 0;
    private double currentSpeedMultiplier = 1.0;

    public InductionFurnaceBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUCTION_FURNACE_BE.get(), pos, state);
    }

    // --- ENERGY STORAGE ---
    public CustomEnergyStorage getEnergyStorage(Direction side) { return this; }
    @Override public int getEnergyStored() { return energyStored; }
    @Override public int getMaxEnergyStored() { return maxEnergyStored; }
    @Override public int getEnergyTier() { return energyTier; }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > energyTier) {
            if (!simulate) triggerExplosion();
            return 0;
        }
        int accepted = Math.min(maxReceive, maxReceiveAmount);
        int space = maxEnergyStored - energyStored;
        int received = Math.min(accepted, space);
        if (!simulate) { energyStored += received; setChanged(); }
        return received;
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public boolean canReceive(Direction side) { return true; }
    @Override public boolean canExtract(Direction side) { return false; }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, 4.0F, Level.ExplosionInteraction.TNT);
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    // --- HEAT STORAGE ---
    public CustomHeatStorage getHeatStorage(Direction side) { return this; }
    @Override public int getHeatStored() { return 0; } // Безбуферная система
    @Override public int getMaxHeatStored() { return 1000; } // Номинальный предел для входящего потока

    @Override
    public int receiveHeat(int maxReceive, boolean simulate) {
        if (!simulate) {
            currentHeatFlow += maxReceive;
            setChanged();
        }
        return maxReceive; // Принимаем всё, буфера нет
    }

    @Override public int extractHeat(int maxExtract, boolean simulate) { return 0; }

    @Override
    public boolean canConnectHeat(Direction side) {
        // Вход для тепла с левой стороны блока относительно фронта
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing.getCounterClockWise(); 
    }

    // --- INVENTORY ---
    public final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 5 && slot <= 8) recalculateBonuses();
            setChanged();
        }
    };

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

    // --- GUI ---
    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energyStored;
                case 1 -> maxEnergyStored;
                case 2 -> progress;
                case 3 -> maxProgress;
                case 4 -> lastHeatFlow; // hU за прошлый тик
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 5; }
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

    // --- LOGIC ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // 1. Зарядка (из слота 4)
        ItemStack chargeStack = inventory.getStackInSlot(4);
        if (!chargeStack.isEmpty() && energyStored < maxEnergyStored) {
            if (chargeStack.getItem() instanceof mopk.tmmod.energy_network.CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= energyTier) {
                    int toExtract = Math.min(maxEnergyStored - energyStored, maxReceiveAmount);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) { energyStored += extracted; setChanged(); }
                }
            } else if (chargeStack.is(Items.REDSTONE)) {
                int energyGain = 400;
                if (energyStored + energyGain <= maxEnergyStored) { energyStored += energyGain; chargeStack.shrink(1); setChanged(); }
            }
        }

        // 2. Крафт
        Optional<RecipeHolder<InductionFurnaceRecipe>> recipeHolder = level.getRecipeManager().getRecipeFor(
                ModRecipes.INDUCTION_FURNACE_TYPE.get(), 
                new RecipeInput() {
                    @Override public ItemStack getItem(int i) { return inventory.getStackInSlot(i); }
                    @Override public int size() { return 2; }
                }, 
                level
        );

        boolean isCrafting = false;
        if (recipeHolder.isPresent()) {
            InductionFurnaceRecipe recipe = recipeHolder.get().value();
            this.maxProgress = recipe.time();
            int euPerTick = recipe.euPerTick();
            int requiredHeatPerTick = recipe.heatPerSecond() / 20;

            if (canCraft(recipe) && energyStored >= euPerTick) {
                energyStored -= euPerTick;
                
                // Расчет замедления от нехватки тепла
                double heatEfficiency = 1.0;
                if (requiredHeatPerTick > 0) {
                    heatEfficiency = Math.min(1.0, (double) currentHeatFlow / requiredHeatPerTick);
                }
                
                if (heatEfficiency > 0) {
                    progress += (int) (100 * heatEfficiency * currentSpeedMultiplier); // Используем 100 как базу для плавности
                    isCrafting = true;
                    if (progress >= maxProgress * 100) {
                        craft(recipe);
                        progress = 0;
                    }
                }
            } else { progress = 0; }
        } else { progress = 0; }

        lastHeatFlow = currentHeatFlow;
        currentHeatFlow = 0; // Сброс для следующего тика

        boolean isLit = isCrafting;
        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }
        setChanged();
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
    }

    @Override public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
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
        shrinkIfMatches(0, recipe.input1());
        shrinkIfMatches(1, recipe.input2());
        shrinkIfMatches(0, recipe.input2()); 
        shrinkIfMatches(1, recipe.input1());

        if (!recipe.output1().isEmpty()) inventory.insertItem(2, recipe.output1().copy(), false);
        if (!recipe.output2().isEmpty()) inventory.insertItem(3, recipe.output2().copy(), false);
    }

    private void shrinkIfMatches(int slot, net.minecraft.world.item.crafting.Ingredient ing) {
        if (ing.test(inventory.getStackInSlot(slot))) inventory.getStackInSlot(slot).shrink(1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStored);
        tag.putInt("progress", progress);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getInt("energy");
        progress = tag.getInt("progress");
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        recalculateBonuses();
    }
}
