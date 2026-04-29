package mopk.tmmod.block_func.SolidFuelHeatGenerator;

import mopk.tmmod.custom_interfaces.CustomHeatStorage;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class SolidFuelHeatGeneratorBE extends BlockEntity implements CustomHeatStorage, MenuProvider {
    private int burnTime = 0;
    private int maxBurnTime = 0;

    public SolidFuelHeatGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLID_FUEL_HEAT_GENERATOR_BE.get(), pos, state);
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
                case 0 -> 0;
                case 1 -> SolidFuelHeatGeneratorBE.this.burnTime;
                case 2 -> SolidFuelHeatGeneratorBE.this.maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 1 -> SolidFuelHeatGeneratorBE.this.burnTime = value;
                case 2 -> SolidFuelHeatGeneratorBE.this.maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tmmod.solid_fuel_heat_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SolidFuelHeatGeneratorMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                // Проверяем, есть ли время горения и НЕ является ли предмет контейнером для жидкости (ведро лавы и т.д.)
                return stack.getBurnTime(null) > 0 && stack.getCapability(Capabilities.FluidHandler.ITEM) == null;
            }
            return slot == 1;
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean isLit = false;
        int heatToGenerate = 20;
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockEntity targetBE = level.getBlockEntity(targetPos);
        CustomHeatStorage targetStorage = null;

        if (targetBE != null) {
            targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
        }

        // Если топливо еще горит
        if (this.burnTime > 0) {
            isLit = true;
            if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                // Включаем теплогенератор и выдаем стабильно 20 hU/t
                int accepted = targetStorage.receiveHeat(heatToGenerate, false);
                if (accepted > 0) {
                    this.burnTime--;
                    setChanged();
                }
            }
            // Если потребителя нет, burnTime не уменьшается (пауза)
        }

        // Если не горит, пробуем начать новое сжигание, но только если есть потребитель
        if (this.burnTime <= 0) {
            if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                ItemStack fuel = inventory.getStackInSlot(0);
                if (!fuel.isEmpty()) {
                    int fuelBurnTime = fuel.getBurnTime(null);
                    if (fuelBurnTime > 0) {
                        this.maxBurnTime = fuelBurnTime;
                        this.burnTime = fuelBurnTime;
                        fuel.shrink(1);
                        
                        if (level.random.nextFloat() < 0.5f) {
                            ItemStack ash = new ItemStack(ModItems.ASH.get());
                            ItemStack currentAsh = inventory.getStackInSlot(1);
                            if (currentAsh.isEmpty()) {
                                inventory.setStackInSlot(1, ash);
                            } else if (ItemStack.isSameItem(currentAsh, ash) && currentAsh.getCount() < currentAsh.getMaxStackSize()) {
                                currentAsh.grow(1);
                            }
                        }
                        
                        isLit = true;
                        setChanged();
                    }
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
    }
}
