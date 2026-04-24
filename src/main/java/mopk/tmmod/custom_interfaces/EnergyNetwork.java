package mopk.tmmod.custom_interfaces;

import mopk.tmmod.block_func.Cables.CableBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Упрощенная энергетическая сеть (IC2 Style).
 * Энергия передается от Producers к Consumers через виртуальную сеть кабелей.
 */
public class EnergyNetwork {
    private final UUID networkId;
    private Level level;

    private final Set<BlockPos> cables = new HashSet<>();
    private final List<BlockPos> producers = new ArrayList<>();
    private final List<BlockPos> consumers = new ArrayList<>();

    private int lastConsumerIndex = 0;
    private int networkTransferLimit = 0;
    private int currentHighestTier = 0;
    private boolean isBurning = false;

    public EnergyNetwork(Level level, UUID id) {
        this.level = level;
        this.networkId = id;
    }

    public boolean isBurning() {
        return isBurning;
    }

    public void tick() {
        if (level == null || level.isClientSide || cables.isEmpty() || isBurning) return;

        EnergyNetworkManager manager = EnergyNetworkManager.get((net.minecraft.server.level.ServerLevel) level);

        // 1. Считаем общий запрос (Demand)
        // Используем тир 0 для симуляции, чтобы блоки не "пугались" высокого напряжения раньше времени
        int totalDemand = 0;
        for (BlockPos pos : consumers) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage) {
                totalDemand += storage.receiveEnergy(networkTransferLimit, 0, true);
            }
        }

        if (totalDemand <= 0) return;

        // 2. Собираем энергию у генераторов (Supply)
        currentHighestTier = 0;
        int totalSupplied = 0;
        for (BlockPos pos : producers) {
            if (totalSupplied >= totalDemand) break;

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage) {
                int producerTier = storage.getEnergyTier();
                int needed = totalDemand - totalSupplied;
                
                // Проверяем, может ли генератор отдать энергию в данный момент
                int extractedSim = storage.extractEnergy(Math.min(needed, networkTransferLimit), true);
                
                if (extractedSim > 0) {
                    // Только если энергия реально будет передана, проверяем тир на прогар
                    if (producerTier > 0) {
                        if (checkCablesForBurnout(producerTier)) {
                            burnoutChain();
                            return;
                        }
                        currentHighestTier = Math.max(currentHighestTier, producerTier);
                    }

                    int extracted = storage.extractEnergy(Math.min(needed, networkTransferLimit), false);
                    if (extracted > 0) {
                        totalSupplied += extracted;
                        manager.reportOut(pos, extracted);
                    }
                }
            }
        }

        if (totalSupplied <= 0) return;

        // 3. Распределяем собранную энергию по потребителям
        distributeToNodes(consumers, totalSupplied, manager);

        // Репортим проход через кабели (для вольтметра) и наносим урон
        boolean canDamage = currentHighestTier > 0;
        for (BlockPos cablePos : cables) {
            manager.reportIn(cablePos, totalSupplied);
            manager.reportOut(cablePos, totalSupplied);
            
            if (canDamage) {
                applyCableDamage(cablePos);
            }
        }
    }

    private void applyCableDamage(BlockPos pos) {
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof mopk.tmmod.blocks.CableBlock cableBlock)) return;

        // Стеклянный кабель (тир 5) и изолированные кабели не наносят урон
        if (cableBlock.getTier() == mopk.tmmod.block_func.Cables.CableTier.glass || cableBlock.getInsulationLevel() > 0) {
            return;
        }

        // Радиус 1.5 - 2 блока
        double radius = 1.5;
        AABB area = new AABB(pos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        if (!entities.isEmpty()) {
            // Урон зависит от тира: Tier 1 = 2.0, Tier 2 = 4.0, Tier 3 = 8.0, Tier 4 = 12.0
            float damage = (float) (currentHighestTier * 2.0);
            for (LivingEntity entity : entities) {
                // Наносим урон раз в полсекунды (чтобы не убить мгновенно)
                if (entity.tickCount % 10 == 0) {
                    entity.hurt(level.damageSources().lightningBolt(), damage);
                }
            }
        }
    }

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

    private void distributeToNodes(List<BlockPos> nodes, int amountToDistribute, EnergyNetworkManager manager) {
        if (nodes.isEmpty() || amountToDistribute <= 0) return;

        int size = nodes.size();
        int startIndex = lastConsumerIndex;
        int remaining = amountToDistribute;

        for (int i = 0; i < size; i++) {
            if (remaining <= 0) break;

            int currentIndex = (startIndex + i) % size;
            BlockPos pos = nodes.get(currentIndex);

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CustomEnergyStorage storage) {
                // Здесь передаем реальный тир. Если он слишком высок - блок взорвется внутри receiveEnergy
                int received = storage.receiveEnergy(Math.min(remaining, networkTransferLimit), currentHighestTier, false);
                if (received > 0) {
                    remaining -= received;
                    manager.reportIn(pos, received);
                    lastConsumerIndex = (currentIndex + 1) % size;
                }
            }
        }
    }

    public void recalculateStats() {
        if (level == null || level.isClientSide) return;

        int maxTransfer = 0;
        if (cables.isEmpty()) {
            this.networkTransferLimit = 0;
            return;
        }

        for (BlockPos pos : cables) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBE cable) {
                maxTransfer = Math.max(maxTransfer, cable.getTransfer());
            }
        }
        this.networkTransferLimit = maxTransfer;
    }

    private void burnoutChain() {
        this.isBurning = true;
        
        // Сначала удаляем сеть из менеджера, чтобы при разрушении блоков 
        // не запускался процесс перестроения сетей.
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            EnergyNetworkManager.get(serverLevel).invalidateNetwork(this.networkId);
        }

        Set<BlockPos> copy = new HashSet<>(cables);
        cables.clear();
        producers.clear();
        consumers.clear();

        for (BlockPos pos : copy) {
            // Взрыв и удаление блока
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.2f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(pos, false);
        }
    }

    public void absorb(EnergyNetwork other) {
        this.cables.addAll(other.cables);
        for (BlockPos pos : other.producers) if (!producers.contains(pos)) producers.add(pos);
        for (BlockPos pos : other.consumers) if (!consumers.contains(pos)) consumers.add(pos);
        recalculateStats();
    }

    public void addCable(BlockPos pos) { if (cables.add(pos)) recalculateStats(); }
    public void removeCable(BlockPos pos) { if (cables.remove(pos)) recalculateStats(); }

    public void addProducer(BlockPos pos) { if (!producers.contains(pos)) producers.add(pos); }
    public void removeProducer(BlockPos pos) { producers.remove(pos); }

    public void addConsumer(BlockPos pos) { if (!consumers.contains(pos)) consumers.add(pos); }
    public void removeConsumer(BlockPos pos) { consumers.remove(pos); }

    public boolean isEmpty() { return cables.isEmpty(); }
    public Set<BlockPos> getCables() { return cables; }
    public UUID getNetworkId() { return networkId; }
    public void setLevel(Level level) { this.level = level; }

    public void save(CompoundTag tag) {
        tag.putUUID("Id", networkId);
        tag.put("Cables", savePosList(new ArrayList<>(cables)));
        tag.put("Producers", savePosList(producers));
        tag.put("Consumers", savePosList(consumers));
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
        loadPosList(tag.getList("Cables", Tag.TAG_COMPOUND), network.cables);
        loadPosList(tag.getList("Producers", Tag.TAG_COMPOUND), network.producers);
        loadPosList(tag.getList("Consumers", Tag.TAG_COMPOUND), network.consumers);
        network.recalculateStats();
        return network;
    }

    private static void loadPosList(ListTag list, Collection<BlockPos> target) {
        for (int i = 0; i < list.size(); i++) {
            NbtUtils.readBlockPos(list.getCompound(i), "p").ifPresent(target::add);
        }
    }
}
