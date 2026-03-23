package mopk.tmmod.block_func.Cables;

import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import static mopk.tmmod.registration.ModBlockEntities.CABLE_BE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CableBE extends BlockEntity implements CustomEnergyStorage {
    private final CableTier tier;
    private int energyStored = 0;

    public CableBE(BlockPos pos, BlockState state, CableTier tier) {
        super(CABLE_BE.get(), pos, state);
        this.tier = tier;
    }

    public CustomEnergyStorage getEnergyStorage(Direction side) {
        return this;
    }

    public CableTier getTier() {
        return this.tier;
    }

    public int getTransfer() {
        return this.tier.getTransfer();
    }

    public int getCapacity() {
        return this.tier.getCapacity();
    }

    @Override
    public int getEnergyStored() {
        return this.energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.tier.getCapacity();
    }

    @Override
    public int getEnergyTier() {
        return this.tier.getTier();
    }

    @Override
    public int receiveEnergy(int maxReceive, int incomingTier, boolean simulate) {
        // 1. ПРОВЕРКА НАПРЯЖЕНИЯ (Вольтаж):
        // Если входящий тир больше, чем может выдержать кабель, он сгорает.
        if (incomingTier > this.tier.getTier()) {
            if (!simulate) {
                burnout();
            }
            return 0; // Энергия не принята, кабель уничтожен
        }

        // 2. ПРОВЕРКА ПРОПУСКНОЙ СПОСОБНОСТИ:
        // Кабель не может принять за один раз больше, чем его параметр transfer.
        int actualReceiveLimit = Math.min(maxReceive, this.tier.getTransfer());

        // 3. РАСЧЕТ СВОБОДНОГО МЕСТА:
        int spaceRemaining = getMaxEnergyStored() - this.energyStored;
        int energyAccepted = Math.min(actualReceiveLimit, spaceRemaining);

        // 4. ПРИМЕНЕНИЕ ИЗМЕНЕНИЙ (если это не симуляция):
        if (!simulate && energyAccepted > 0) {
            this.energyStored += energyAccepted;
            setChanged(); // Отмечаем, что данные изменились и их нужно сохранить
        }

        return energyAccepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        // Ограничиваем извлечение пропускной способностью кабеля (transfer)
        int actualExtractLimit = Math.min(maxExtract, this.tier.getTransfer());

        // Сколько реально можно забрать из буфера
        int energyExtracted = Math.min(actualExtractLimit, this.energyStored);

        if (!simulate && energyExtracted > 0) {
            this.energyStored -= energyExtracted;
            setChanged();
        }

        return energyExtracted;
    }

    @Override
    public boolean canReceive(Direction side) {
        return true;
    }

    @Override
    public boolean canExtract(Direction side) {
        return true;
    }

   //загрузка кабеля в сеть при перезапуске
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onCableAdded(level, this.worldPosition);
        }
    }

    // хук для удаления в менеджере
    @Override
    public void setRemoved() {
        super.setRemoved();
        // Удаляем кабель из глобального менеджера
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onCableRemoved(level, this.worldPosition);
        }
    }

    private void burnout() {
        if (level != null && !level.isClientSide) {
            // Взрыв без разрушения блоков вокруг (или с минимальным уроном)
            level.explode(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                    1.0f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(this.worldPosition, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", this.energyStored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energyStored = tag.getInt("energy");
    }
}
