package mopk.tmmod.etc.Crusher;

import mopk.tmmod.etc.ModBlockEntities;
import mopk.tmmod.etc.ModRecipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;


public class CrusherBE extends BlockEntity implements MenuProvider {
    public final EnergyStorage energyStorage = new EnergyStorage(1000, 1000, 1000);

    private int progress = 0;
    private int maxProgress = 0;

    public ItemStackHandler getInventory() {
        return inventory;
    }
    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public CrusherBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER_BE.get(), pos, state);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CrusherBE.this.energyStorage.getEnergyStored();
                case 1 -> CrusherBE.this.energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.crusher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CrusherMenu(id, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean isLit = false;
        Optional<RecipeHolder<CrusherRecipe>> recipeHolder = level.getRecipeManager().getRecipeFor(ModRecipes.CRUSHER_TYPE.get(), new SingleRecipeInput(inventory.getStackInSlot(0)), level);

        if (recipeHolder.isPresent()) {
            CrusherRecipe recipe = recipeHolder.get().value();
            this.maxProgress = recipe.time();

            if (canCraft(recipe)) {
                if (energyStorage.getEnergyStored() >= recipe.energyPerTick()) {
                    energyStorage.extractEnergy(recipe.energyPerTick(), false);
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

        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);

        if (isLit) {
            setChanged();
        }
    }

    private boolean canCraft(CrusherRecipe recipe) {
        ItemStack outputSlot = inventory.getStackInSlot(1);
        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.isSameItem(outputSlot, recipe.output())) return false;
        return outputSlot.getCount() + recipe.output().getCount() <= outputSlot.getMaxStackSize();
    }

    private void craft(CrusherRecipe recipe) {
        inventory.getStackInSlot(0).shrink(1);
        ItemStack result = recipe.output().copy();
        inventory.insertItem(1, result, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
    }
}
