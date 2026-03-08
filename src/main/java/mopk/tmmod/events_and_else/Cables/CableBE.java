package mopk.tmmod.events_and_else.Cables;

import mopk.tmmod.events_and_else.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CableBE extends BlockEntity {
    private final CableTier tier;

    public CableBE(BlockPos pos, BlockState state, CableTier tier) {
        super(ModBlockEntities.CABLE_BE.get(), pos, state);
        this.tier = tier;
    }

    // Методы для характеристик
    public int getTierCapacity() { return this.tier.getCapacity(); }
    public int getTierTransfer() { return this.tier.getTransfer(); }

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (level == null) return 0;

            int toTransfer = Math.min(maxReceive, tier.getTransfer());
            if (toTransfer <= 0) return 0;

            // ЭТАП 1: Разведка (ищем всех потребителей в сети)
            List<IEnergyStorage> consumers = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            findConsumers(visited, consumers);

            if (consumers.isEmpty()) return 0; // Потребителей нет, отказываемся от энергии

            // ЭТАП 2: Равномерное распределение
            int totalAccepted = 0;
            int energyPerConsumer = toTransfer / consumers.size(); // Делим поровну
            int remainder = toTransfer % consumers.size(); // Остаток (если 10 разделить на 3, остаток 1)

            for (int i = 0; i < consumers.size(); i++) {
                IEnergyStorage consumer = consumers.get(i);

                // Даем ровную часть + 1 FE некоторым, чтобы распределить остаток без потерь
                int amountToOffer = energyPerConsumer + (i < remainder ? 1 : 0);

                if (amountToOffer > 0) {
                    int accepted = consumer.receiveEnergy(amountToOffer, simulate);
                    totalAccepted += accepted;
                }
            }

            return totalAccepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override
        public int getEnergyStored() { return 0; }
        @Override
        public int getMaxEnergyStored() { return 0; }
        @Override
        public boolean canExtract() { return false; }
        @Override
        public boolean canReceive() { return true; }
    };

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    // Рекурсивный метод для поиска всех машин, подключенных к сети проводов
    private void findConsumers(Set<BlockPos> visited, List<IEnergyStorage> consumers) {
        // Если мы тут уже были - уходим (защита от бесконечного цикла, если провода стоят кольцом)
        if (!visited.add(this.worldPosition)) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(dir);

            if (visited.contains(neighborPos)) continue;

            BlockEntity be = level.getBlockEntity(neighborPos);

            // Если сосед - это тоже провод, идем по нему дальше
            if (be instanceof CableBE cable) {
                cable.findConsumers(visited, consumers);
            }
            // Если это не провод, проверяем, может ли он принимать энергию
            else {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, dir.getOpposite());
                if (target != null && target.canReceive()) {
                    visited.add(neighborPos); // Отмечаем механизм как "посещенный"
                    consumers.add(target);    // Добавляем в список на раздачу
                }
            }
        }
    }
}

