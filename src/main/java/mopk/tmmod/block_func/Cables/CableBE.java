package mopk.tmmod.block_func.Cables;

import mopk.tmmod.custom_interfaces.CustomEnergyStorage;
import mopk.tmmod.custom_interfaces.EnergyNetworkManager;
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

    @Override
    public int getEnergyStored() {
        return 0; // Кабель больше не хранит энергию
    }

    @Override
    public int getMaxEnergyStored() {
        return 0; // Кабель больше не имеет емкости
    }

    @Override
    public int getEnergyTier() {
        return this.tier.getTier();
    }

    @Override
    public int receiveEnergy(int maxReceive, int incomingTier, boolean simulate) {
        // Кабель не может принимать энергию напрямую, всё делает сеть в один клик.
        // Проверка тира теперь выполняется в EnergyNetwork.tick() для всей сети сразу.
        return 0; 
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0; // В кабеле больше нет энергии, которую можно было бы извлечь
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }
}
