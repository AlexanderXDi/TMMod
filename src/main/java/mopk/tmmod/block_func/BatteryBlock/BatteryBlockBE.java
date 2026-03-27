package mopk.tmmod.block_func.BatteryBlock;

import mopk.tmmod.energy_network.CustomEnergyItemInterface;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import mopk.tmmod.registration.CustomCapabilities;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class BatteryBlockBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private int energyStored = 0;
    private final int maxEnergyStored = 1000000;
    
    // Тиры и скорости (Трансформатор: вход 1, выход 2)
    private final int inputTier = 1;
    private final int outputTier = 2;
    private final int receiveRate = 32;   // Лимит входа для Tier 1
    private final int extractRate = 128; // Лимит выхода для Tier 2
    
    private BatteryBlockMode mode = BatteryBlockMode.BOTH;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public BatteryBlockBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BLOCK_BE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (mode == BatteryBlockMode.OUTPUT) return 0;

        // Взрыв, если входящий тир выше допустимого (1)
        if (tier > inputTier) {
            if (!simulate) {
                triggerExplosion();
            }
            return 0;
        }

        int space = maxEnergyStored - energyStored;
        int toReceive = Math.min(maxReceive, Math.min(space, receiveRate));
        if (!simulate) {
            energyStored += toReceive;
            setChanged();
        }
        return toReceive;
    }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(
                    null,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D,
                    4.0F,
                    Level.ExplosionInteraction.TNT
            );
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (mode == BatteryBlockMode.INPUT) return 0;
        int toExtract = Math.min(maxExtract, Math.min(energyStored, extractRate));
        if (!simulate) {
            energyStored -= toExtract;
            setChanged();
        }
        return toExtract;
    }

    @Override
    public int getEnergyTier() {
        return outputTier; // Выдаем Tier 2
    }

    @Override
    public boolean canReceive(Direction side) {
        if (mode == BatteryBlockMode.OUTPUT) return false;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing;
    }

    @Override
    public boolean canExtract(Direction side) {
        if (mode == BatteryBlockMode.INPUT) return false;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    @Override
    public int getEnergyStored() { return energyStored; }

    @Override
    public int getMaxEnergyStored() { return maxEnergyStored; }

    public CustomEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return this;
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

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStored & 0xFFFF;
                case 1 -> (energyStored >> 16) & 0xFFFF;
                case 2 -> maxEnergyStored & 0xFFFF;
                case 3 -> (maxEnergyStored >> 16) & 0xFFFF;
                case 4 -> mode.ordinal();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { if (index == 4) mode = BatteryBlockMode.values()[value]; }
        @Override public int getCount() { return 5; }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tmmod.battery_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BatteryBlockMenu(id, inv, this, this.data);
    }

    public void toggleMode() {
        this.mode = this.mode.next();
        setChanged();
    }

    public BatteryBlockMode getMode() { return mode; }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // 1. Зарядка предмета в слоте
        ItemStack chargeStack = inventory.getStackInSlot(0);
        if (!chargeStack.isEmpty() && energyStored > 0) {
            if (chargeStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                int chargeSpeed = 100;
                int toGive = Math.min(energyStored, chargeSpeed);
                int accepted = energyItem.receiveEnergy(chargeStack, toGive, false);
                if (accepted > 0) {
                    energyStored -= accepted;
                    setChanged();
                }
            }
        }

        // 2. Отдача энергии в сеть/соседние блоки
        if (mode != BatteryBlockMode.INPUT && energyStored > 0) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos targetPos = pos.relative(facing);

            CustomEnergyStorage target = level.getCapability(CustomCapabilities.ENERGY, targetPos, facing.getOpposite());
            if (target != null && target.canReceive(facing.getOpposite())) {
                // Передача по лимиту отдачи (Tier 2)
                int toExtract = Math.min(energyStored, extractRate);
                int accepted = target.receiveEnergy(toExtract, getEnergyTier(), false);
                if (accepted > 0) {
                    energyStored -= accepted;
                    setChanged();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energyStored);
        tag.putInt("Mode", mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energyStored = tag.getInt("energy"); // Исправлено на строчное 'energy' для совместимости
        if (tag.contains("Mode")) mode = BatteryBlockMode.values()[tag.getInt("Mode")];
    }
}
