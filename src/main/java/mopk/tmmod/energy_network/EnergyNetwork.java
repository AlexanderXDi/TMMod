package mopk.tmmod.energy_network;

import mopk.tmmod.block_func.Cables.CableBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

//класс сети(как будет работать)
public class EnergyNetwork {
    private final UUID networkId;
    private final Level level;

    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> producers = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private final Set<BlockPos> storages = new HashSet<>();

    private int networkEnergy = 0;
    private int networkCapacity = 0;
    private int networkTransferLimit = Integer.MAX_VALUE; // "Узкое место" по количеству (EU/t)
    private int networkTierLimit = Integer.MAX_VALUE;     // "Узкое место" по вольтажу (Tier)

    public EnergyNetwork(Level level, UUID id) {
        this.level = level;
        this.networkId = id;
    }

    public void tick() {
        if (level.isClientSide || cables.isEmpty()) return;

        // взял энергию
        collectEnergyFromProducers();

        // 2. Проверка на критическую перегрузку (Overload Check)
        // В IC2 энергия не "хранится" в кабелях бесконечно, если ее некуда девать и
        // входящий вольтаж выше лимита сети — происходит выгорание.
        if (networkEnergy > networkCapacity) {
            // Логика избыточного накопления (опционально: взрыв или потеря)
            networkEnergy = networkCapacity;
        }

        // 3. Распределение энергии потребителям (Push Phase)
        distributeEnergyToConsumers();
    }

    /**
     * Извлекает доступную энергию из всех зарегистрированных генераторов.
     */
    private void collectEnergyFromProducers() {
        for (BlockPos pos : producers) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage) {
                // Проверяем тир генератора. Если он выше лимита сети — сеть выгорает.
                if (storage.getEnergyTier() > networkTierLimit) {
                    burnoutChain();
                    return;
                }

                // Извлекаем энергию (максимум, что может пропустить сеть)
                int available = storage.extractEnergy(networkTransferLimit, true);
                int accepted = addEnergyToNetwork(available);
                storage.extractEnergy(accepted, false);
            }
        }
    }

    /**
     * Распределяет накопленную в сети энергию между потребителями.
     */
    private void distributeEnergyToConsumers() {
        if (networkEnergy <= 0) return;

        // Для честного распределения можно использовать список
        List<BlockPos> consumerList = new ArrayList<>(consumers);
        // Можно добавить перемешивание (Collections.shuffle), чтобы избежать приоритета координат

        for (BlockPos pos : consumerList) {
            if (networkEnergy <= 0) break;

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage && storage.canReceive(Direction.UP)) { // Direction для примера
                // Потребитель сам решает, сколько он может принять
                int received = storage.receiveEnergy(networkEnergy, networkTierLimit, false);
                networkEnergy -= received;
            }
        }
    }

    /**
     * Добавляет энергию в буфер сети (кабели + хранилища).
     */
    private int addEnergyToNetwork(int amount) {
        int space = networkCapacity - networkEnergy;
        int toAdd = Math.min(amount, space);
        networkEnergy += toAdd;
        return toAdd;
    }

    /**
     * Полный пересчет характеристик сети при изменении её структуры.
     */
    public void recalculateStats() {
        int totalCapacity = 0;
        int minTransfer = Integer.MAX_VALUE;
        int minTier = Integer.MAX_VALUE;

        // Опрашиваем только кабели для определения лимитов проводимости
        for (BlockPos pos : cables) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBE cable) {
                totalCapacity += cable.getCapacity();
                minTransfer = Math.min(minTransfer, cable.getTransfer());
                minTier = Math.min(minTier, cable.getTier().getTier());
            }
        }

        this.networkCapacity = totalCapacity;
        this.networkTransferLimit = cables.isEmpty() ? 0 : minTransfer;
        this.networkTierLimit = cables.isEmpty() ? 0 : minTier;
    }

    /**
     * Логика катастрофического разрушения сети при перегрузке.
     */
    private void burnoutChain() {
        // Копируем список, так как destroyBlock вызовет изменение структуры
        Set<BlockPos> toDestroy = new HashSet<>(cables);
        for (BlockPos pos : toDestroy) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.2f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(pos, false);
        }
        // Очистка данных сети после уничтожения
        cables.clear();
        producers.clear();
        consumers.clear();
        networkEnergy = 0;
    }

    // Методы регистрации узлов
    public void addCable(BlockPos pos) { cables.add(pos); recalculateStats(); }
    public void addProducer(BlockPos pos) { producers.add(pos); }
    public void addConsumer(BlockPos pos) { consumers.add(pos); }

    public UUID getNetworkId() { return networkId; }
}


