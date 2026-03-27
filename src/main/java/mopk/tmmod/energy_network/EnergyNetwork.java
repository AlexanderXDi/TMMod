package mopk.tmmod.energy_network;

import mopk.tmmod.block_func.Cables.CableBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

/**
 * Исправленный класс энергетической сети с проверкой вольтажа (тира).
 */
public class EnergyNetwork {
    private final UUID networkId;
    private Level level;

    private final Set<BlockPos> cables = new HashSet<>();
    private final List<BlockPos> producers = new ArrayList<>();
    private final List<BlockPos> consumers = new ArrayList<>();
    private final List<BlockPos> storages = new ArrayList<>();

    private int lastConsumerIndex = 0;
    private int lastStorageIndex = 0;

    private int networkEnergy = 0;
    private int networkCapacity = 0;
    private int networkTransferLimit = 0;
    private int currentHighestTier = 0; // Наивысший тир энергии в сети в данный момент

    public EnergyNetwork(Level level, UUID id) {
        this.level = level;
        this.networkId = id;
    }

    public void tick() {
        if (level == null || level.isClientSide || cables.isEmpty()) return;

        // 1. Сбор энергии и определение текущего тира
        currentHighestTier = 0;
        collectEnergy();

        // 2. Ограничение емкостью
        if (networkEnergy > networkCapacity) {
            networkEnergy = networkCapacity;
        }

        // 3. Распределение
        if (networkEnergy > 0) {
            distributeToNodes(consumers, true);
            if (networkEnergy > 0) {
                distributeToNodes(storages, false);
            }
        }
    }

    private void collectEnergy() {
        for (BlockPos pos : producers) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage) {
                int producerTier = storage.getEnergyTier();
                
                // Проверяем, выдерживают ли наши провода такой тир
                if (producerTier > 0) {
                    if (checkCablesForBurnout(producerTier)) {
                        burnoutChain(); // Взрыв, если провода не тянут
                        return;
                    }
                    currentHighestTier = Math.max(currentHighestTier, producerTier);
                }

                int canExtract = storage.extractEnergy(networkTransferLimit, true);
                int toAdd = Math.min(canExtract, networkCapacity - networkEnergy);
                if (toAdd > 0) {
                    int actualExtracted = storage.extractEnergy(toAdd, false);
                    networkEnergy += actualExtracted;
                }
            }
        }
    }

    /**
     * Проверяет, есть ли в сети провода, чей тир ниже переданного вольтажа.
     */
    private boolean checkCablesForBurnout(int voltage) {
        for (BlockPos pos : cables) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBE cable) {
                if (cable.getTier().getTier() < voltage) {
                    return true;
                }
            }
        }
        return false;
    }

    private void distributeToNodes(List<BlockPos> nodes, boolean isConsumer) {
        if (nodes.isEmpty()) return;

        int size = nodes.size();
        int startIndex = isConsumer ? lastConsumerIndex : lastStorageIndex;
        
        for (int i = 0; i < size; i++) {
            if (networkEnergy <= 0) break;
            
            int currentIndex = (startIndex + i) % size;
            BlockPos pos = nodes.get(currentIndex);
            
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage) {
                // Передаем энергию с текущим наивысшим тиром сети
                int received = storage.receiveEnergy(Math.min(networkEnergy, networkTransferLimit), currentHighestTier, false);
                networkEnergy -= received;
                
                if (isConsumer) lastConsumerIndex = (currentIndex + 1) % size;
                else lastStorageIndex = (currentIndex + 1) % size;
            }
        }
    }

    public void recalculateStats() {
        if (level == null || level.isClientSide) return;

        long totalCapacity = 0;
        int maxTransfer = 0;

        if (cables.isEmpty()) {
            this.networkCapacity = 0;
            this.networkTransferLimit = 0;
            return;
        }

        for (BlockPos pos : cables) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBE cable) {
                totalCapacity += cable.getCapacity();
                maxTransfer = Math.max(maxTransfer, cable.getTransfer());
            }
        }

        this.networkCapacity = (int) Math.min(totalCapacity, Integer.MAX_VALUE);
        this.networkTransferLimit = maxTransfer;
    }

    private void burnoutChain() {
        Set<BlockPos> copy = new HashSet<>(cables);
        cables.clear();
        producers.clear();
        consumers.clear();
        storages.clear();
        networkEnergy = 0;

        for (BlockPos pos : copy) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.2f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(pos, false);
        }
    }

    public void absorb(EnergyNetwork other) {
        this.cables.addAll(other.cables);
        this.networkEnergy = (int) Math.min((long)this.networkEnergy + other.networkEnergy, networkCapacity);
        
        updateNodes(other.producers, producers);
        updateNodes(other.consumers, consumers);
        updateNodes(other.storages, storages);
        
        recalculateStats();
    }

    private void updateNodes(List<BlockPos> from, List<BlockPos> to) {
        for (BlockPos pos : from) {
            if (!to.contains(pos)) to.add(pos);
        }
    }

    public void addCable(BlockPos pos) { if (cables.add(pos)) recalculateStats(); }
    public void removeCable(BlockPos pos) { if (cables.remove(pos)) recalculateStats(); }

    public void addProducer(BlockPos pos) { if (!producers.contains(pos)) producers.add(pos); }
    public void removeProducer(BlockPos pos) { producers.remove(pos); }

    public void addConsumer(BlockPos pos) { if (!consumers.contains(pos)) consumers.add(pos); }
    public void removeConsumer(BlockPos pos) { consumers.remove(pos); }

    public void addStorage(BlockPos pos) { if (!storages.contains(pos)) storages.add(pos); }
    public void removeStorage(BlockPos pos) { storages.remove(pos); }

    public boolean isEmpty() { return cables.isEmpty(); }
    public Set<BlockPos> getCables() { return cables; }
    public UUID getNetworkId() { return networkId; }
    public void setLevel(Level level) { this.level = level; }

    public void save(CompoundTag tag) {
        tag.putUUID("Id", networkId);
        tag.putInt("Energy", networkEnergy);
        tag.put("Cables", savePosList(new ArrayList<>(cables)));
        tag.put("Producers", savePosList(producers));
        tag.put("Consumers", savePosList(consumers));
        tag.put("Storages", savePosList(storages));
    }

    private ListTag savePosList(Collection<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag posTag = new CompoundTag();
            posTag.put("p", NbtUtils.writeBlockPos(pos));
            list.add(posTag);
        }
        return list;
    }

    public static EnergyNetwork load(Level level, CompoundTag tag) {
        EnergyNetwork network = new EnergyNetwork(level, tag.getUUID("Id"));
        network.networkEnergy = tag.getInt("Energy");
        loadPosList(tag.getList("Cables", Tag.TAG_COMPOUND), network.cables);
        loadPosList(tag.getList("Producers", Tag.TAG_COMPOUND), network.producers);
        loadPosList(tag.getList("Consumers", Tag.TAG_COMPOUND), network.consumers);
        loadPosList(tag.getList("Storages", Tag.TAG_COMPOUND), network.storages);
        network.recalculateStats();
        return network;
    }

    private static void loadPosList(ListTag list, Collection<BlockPos> target) {
        for (int i = 0; i < list.size(); i++) {
            NbtUtils.readBlockPos(list.getCompound(i), "p").ifPresent(target::add);
        }
    }
}
