package mopk.tmmod.energy_network;

import mopk.tmmod.block_func.Cables.CableBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Глобальный менеджер энергетических сетей.
 */
public class EnergyNetworkManager extends SavedData {
    private static final String DATA_NAME = "tmmod_energy_networks";

    private final Map<UUID, EnergyNetwork> networks = new HashMap<>();
    private final Map<BlockPos, UUID> cableToNetworkMap = new HashMap<>();

    // Статистика для вольтметра
    private final Map<BlockPos, Integer> lastTickIn = new HashMap<>();
    private final Map<BlockPos, Integer> lastTickOut = new HashMap<>();
    private final Map<BlockPos, long[]> historyIn = new HashMap<>(); // [0-19] values, [20] index
    private final Map<BlockPos, long[]> historyOut = new HashMap<>();

    public EnergyNetworkManager() {}

    public void reportIn(BlockPos pos, int amount) {
        lastTickIn.merge(pos, amount, Integer::sum);
    }

    public void reportOut(BlockPos pos, int amount) {
        lastTickOut.merge(pos, amount, Integer::sum);
    }

    public void tick(Level level) {
        if (level.isClientSide) return;

        updateAverages();
        lastTickIn.clear();
        lastTickOut.clear();
        
        List<EnergyNetwork> networkList = new ArrayList<>(networks.values());
        for (EnergyNetwork network : networkList) {
            network.setLevel(level);
            network.recalculateStats(); // Форсируем пересчет лимитов
            network.tick();
        }
    }

    public void onCableAdded(Level level, BlockPos pos) {
        if (cableToNetworkMap.containsKey(pos)) return;

        Set<UUID> neighboringNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            UUID networkId = cableToNetworkMap.get(pos.relative(dir));
            if (networkId != null) neighboringNetworks.add(networkId);
        }

        EnergyNetwork targetNetwork;
        if (neighboringNetworks.isEmpty()) {
            targetNetwork = new EnergyNetwork(level, UUID.randomUUID());
            networks.put(targetNetwork.getNetworkId(), targetNetwork);
        } else {
            Iterator<UUID> it = neighboringNetworks.iterator();
            targetNetwork = networks.get(it.next());
            while (it.hasNext()) {
                mergeNetworks(targetNetwork, networks.get(it.next()));
            }
        }

        targetNetwork.addCable(pos);
        cableToNetworkMap.put(pos, targetNetwork.getNetworkId());
        
        // Поиск и регистрация соседних механизмов
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CustomEnergyStorage && !(be instanceof CableBE)) {
                registerNodeInNetwork(targetNetwork, neighborPos, be, dir.getOpposite());
            }
        }
        setDirty();
    }

    public void onCableRemoved(Level level, BlockPos pos) {
        UUID networkId = cableToNetworkMap.remove(pos);
        if (networkId == null) return;

        EnergyNetwork network = networks.get(networkId);
        if (network != null) {
            network.removeCable(pos);
            if (network.isEmpty()) {
                networks.remove(networkId);
            } else {
                rebuildNetworksFrom(level, pos, network);
            }
            setDirty();
        }
    }

    private void rebuildNetworksFrom(Level level, BlockPos removedPos, EnergyNetwork originalNetwork) {
        Set<BlockPos> allCables = new HashSet<>(originalNetwork.getCables());
        for (BlockPos pos : allCables) cableToNetworkMap.remove(pos);
        networks.remove(originalNetwork.getNetworkId());

        while (!allCables.isEmpty()) {
            BlockPos start = allCables.iterator().next();
            EnergyNetwork newNetwork = new EnergyNetwork(level, UUID.randomUUID());
            networks.put(newNetwork.getNetworkId(), newNetwork);
            
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(start);
            
            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                if (allCables.remove(current)) {
                    newNetwork.addCable(current);
                    cableToNetworkMap.put(current, newNetwork.getNetworkId());
                    
                    for (Direction dir : Direction.values()) {
                        BlockPos neighbor = current.relative(dir);
                        if (allCables.contains(neighbor)) {
                            queue.add(neighbor);
                        } else if (!neighbor.equals(removedPos)) {
                            BlockEntity be = level.getBlockEntity(neighbor);
                            if (be instanceof CustomEnergyStorage && !(be instanceof CableBE)) {
                                registerNodeInNetwork(newNetwork, neighbor, be, dir.getOpposite());
                            }
                        }
                    }
                }
            }
            newNetwork.recalculateStats();
        }
    }

    private void mergeNetworks(EnergyNetwork main, EnergyNetwork secondary) {
        if (main == null || secondary == null || main == secondary) return;
        for (BlockPos pos : secondary.getCables()) {
            cableToNetworkMap.put(pos, main.getNetworkId());
        }
        main.absorb(secondary);
        networks.remove(secondary.getNetworkId());
    }

    private void registerNodeInNetwork(EnergyNetwork net, BlockPos pos, BlockEntity be, Direction face) {
        if (!(be instanceof CustomEnergyStorage storage)) return;
        
        // В IC2 сторона либо отдает энергию, либо принимает. 
        // Если блок может отдавать через эту сторону - он Producer.
        // Если блок может принимать через эту сторону - он Consumer.
        
        if (storage.canExtract(face)) {
            net.addProducer(pos);
            // System.out.println("Node at " + pos + " registered as PRODUCER for side " + face);
        }
        if (storage.canReceive(face)) {
            net.addConsumer(pos);
            // System.out.println("Node at " + pos + " registered as CONSUMER for side " + face);
        }
    }

    public void onNodeAdded(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CustomEnergyStorage)) return;
        
        boolean found = false;
        for (Direction dir : Direction.values()) {
            UUID netId = cableToNetworkMap.get(pos.relative(dir));
            if (netId != null) {
                EnergyNetwork net = networks.get(netId);
                if (net != null) {
                    // face - это сторона БЛОКА, к которой присоединен кабель
                    registerNodeInNetwork(net, pos, be, dir.getOpposite());
                    found = true;
                }
            }
        }
        if (found) setDirty();
    }

    public void onNodeRemoved(BlockPos pos) {
        for (EnergyNetwork net : networks.values()) {
            net.removeProducer(pos);
            net.removeConsumer(pos);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag networkList = new ListTag();
        for (EnergyNetwork network : networks.values()) {
            CompoundTag nbt = new CompoundTag();
            network.save(nbt);
            networkList.add(nbt);
        }
        tag.put("Networks", networkList);
        return tag;
    }

    public static EnergyNetworkManager load(CompoundTag tag, HolderLookup.Provider registries) {
        EnergyNetworkManager manager = new EnergyNetworkManager();
        ListTag networkList = tag.getList("Networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networkList.size(); i++) {
            EnergyNetwork network = EnergyNetwork.load(null, networkList.getCompound(i));
            UUID id = network.getNetworkId();
            manager.networks.put(id, network);
            for (BlockPos pos : network.getCables()) manager.cableToNetworkMap.put(pos, id);
        }
        return manager;
    }

    public static EnergyNetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(EnergyNetworkManager::new, EnergyNetworkManager::load),
                DATA_NAME
        );
    }

    private void updateAverages() {
        updateMapAverages(lastTickIn, historyIn);
        updateMapAverages(lastTickOut, historyOut);
    }

    private void updateMapAverages(Map<BlockPos, Integer> current, Map<BlockPos, long[]> history) {
        // Добавляем текущие значения в историю
        for (Map.Entry<BlockPos, Integer> entry : current.entrySet()) {
            long[] hist = history.computeIfAbsent(entry.getKey(), k -> new long[21]);
            int idx = (int) hist[20];
            hist[idx] = entry.getValue();
            hist[20] = (idx + 1) % 20;
        }
        
        // Для тех, кто не прислал данных в этом тике, записываем 0
        for (BlockPos pos : history.keySet()) {
            if (!current.containsKey(pos)) {
                long[] hist = history.get(pos);
                int idx = (int) hist[20];
                hist[idx] = 0;
                hist[20] = (idx + 1) % 20;
            }
        }
    }

    public int getEuIn(BlockPos pos) { return lastTickIn.getOrDefault(pos, 0); }
    public int getEuOut(BlockPos pos) { return lastTickOut.getOrDefault(pos, 0); }

    public double getAvgIn(BlockPos pos) { return getAverage(historyIn.get(pos)); }
    public double getAvgOut(BlockPos pos) { return getAverage(historyOut.get(pos)); }

    private double getAverage(long[] hist) {
        if (hist == null) return 0;
        long sum = 0;
        for (int i = 0; i < 20; i++) sum += hist[i];
        return sum / 20.0;
    }
}
