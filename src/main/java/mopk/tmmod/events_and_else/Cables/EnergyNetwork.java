package mopk.tmmod.events_and_else.Cables;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.Set;


public class EnergyNetwork {
    private final Set<BlockPos> cablePositions = new HashSet<>();
    private int networkEnergy = 0;
    private int networkCapacity = 0;
    private int networkTransferLimit = Integer.MAX_VALUE;

    public void addCable(Level level, BlockPos pos) {
        cablePositions.add(pos);
        recalculateStats(level);
    }

    public void recalculateStats(Level level) {
        int minTransfer = Integer.MAX_VALUE;
        int totalCapacity = 0;

        for (BlockPos pos : cablePositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBE cable) {
                totalCapacity += cable.getTierCapacity();
                minTransfer = Math.min(minTransfer, cable.getTierTransfer());
            }
        }

        // Записываем обновленные данные в поля класса
        this.networkCapacity = totalCapacity;
        this.networkTransferLimit = cablePositions.isEmpty() ? 0 : minTransfer;

        if (this.networkEnergy > this.networkCapacity) {
            this.networkEnergy = this.networkCapacity;
        }
    }
}

