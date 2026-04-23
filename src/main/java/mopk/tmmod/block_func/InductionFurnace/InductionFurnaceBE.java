package mopk.tmmod.block_func.InductionFurnace;

import mopk.tmmod.energy_network.CustomHeatStorage;
import mopk.tmmod.registration.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class InductionFurnaceBE extends BlockEntity implements CustomHeatStorage {
    private int heatStored = 0;
    private final int maxHeatStored = 10000;
    private boolean receivedHeatThisTick = false;

    public InductionFurnaceBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUCTION_FURNACE_BE.get(), pos, state);
    }

    // --- HEAT STORAGE ---
    public CustomHeatStorage getHeatStorage(Direction side) { return this; }

    @Override public int getHeatStored() { return heatStored; }
    @Override public int getMaxHeatStored() { return maxHeatStored; }

    @Override
    public int receiveHeat(int maxReceive, boolean simulate) {
        int space = maxHeatStored - heatStored;
        int received = Math.min(maxReceive, space);
        if (!simulate && received > 0) {
            heatStored += received;
            receivedHeatThisTick = true;
            setChanged();
        }
        return received;
    }

    @Override public int extractHeat(int maxExtract, boolean simulate) { return 0; } // Не отдает тепло

    @Override
    public boolean canConnectHeat(Direction side) {
        // Принимает тепло сзади или по бокам (просто как пример, в IC2 печь принимает сзади)
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing; 
    }

    // --- LOGIC ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // Имитируем рассеивание тепла или использование (чтобы буфер не забился навсегда)
        if (heatStored > 0) {
            heatStored -= Math.min(heatStored, 20); // Теряет 20 hU в тик
            setChanged();
        }

        boolean isLit = receivedHeatThisTick;
        boolean wasLit = state.getValue(BlockStateProperties.LIT);

        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }
        
        // Сброс флага
        receivedHeatThisTick = false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("heat", heatStored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heatStored = tag.getInt("heat");
    }
}
