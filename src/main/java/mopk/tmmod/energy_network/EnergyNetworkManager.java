package mopk.tmmod.energy_network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

//менеджер который контролирует сеть
public class EnergyNetworkManager extends SavedData {
    private static final String DATA_NAME = "tmmod_energy_networks";

    // Реестр всех активных сетей по их уникальному ID
    private final Map<UUID, EnergyNetwork> networks = new HashMap<>();
    // Индекс для мгновенного поиска ID сети по координате кабеля (O(1) lookup)
    private final Map<BlockPos, UUID> cableToNetworkMap = new HashMap<>();

    public EnergyNetworkManager() {}

    /**
     * Вызывается каждый тик из ServerTickEvent или LevelTickEvent.
     */
    public void tick(Level level) {
        if (level.isClientSide) return;
        // Итерируемся по всем зарегистрированным сетям и инициируем их логику
        for (EnergyNetwork network : networks.values()) {
            network.tick();
        }
    }

    /**
     * Логика добавления нового кабеля в систему.
     * Выполняет автоматическое слияние (Merge) смежных сетей.
     */
    public void onCableAdded(Level level, BlockPos pos) {
        Set<UUID> neighboringNetworks = new HashSet<>();

        // 1. Поиск смежных сетей во всех 6 направлениях
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            UUID networkId = cableToNetworkMap.get(neighborPos);
            if (networkId != null) {
                neighboringNetworks.add(networkId);
            }
        }

        EnergyNetwork targetNetwork;

        if (neighboringNetworks.isEmpty()) {
            // 2а. Смежных сетей нет — создаем новую сеть
            targetNetwork = new EnergyNetwork(level, UUID.randomUUID());
            networks.put(targetNetwork.getNetworkId(), targetNetwork);
        } else {
            // 2б. Берем первую найденную сеть как основную
            Iterator<UUID> it = neighboringNetworks.iterator();
            targetNetwork = networks.get(it.next());

            // 3. Слияние (Merge) если найдено более одной сети
            while (it.hasNext()) {
                UUID otherId = it.next();
                EnergyNetwork otherNetwork = networks.get(otherId);
                if (otherNetwork != null && otherNetwork != targetNetwork) {
                    mergeNetworks(targetNetwork, otherNetwork);
                }
            }

            // проверка на отсутствие вообще
            if (targetNetwork == null) {
                targetNetwork = new EnergyNetwork(level, UUID.randomUUID());
                networks.put(targetNetwork.getNetworkId(), targetNetwork);
            }
        }

        // 4. Регистрация нового кабеля в выбранной сети
        targetNetwork.addCable(pos);
        cableToNetworkMap.put(pos, targetNetwork.getNetworkId());
        setDirty(); // Помечаем SavedData для сохранения на диск
    }

    /**
     * Логика удаления кабеля. Инициирует проверку на разделение (Split) сети.
     */
    public void onCableRemoved(Level level, BlockPos pos) {
        UUID networkId = cableToNetworkMap.remove(pos);
        if (networkId == null) return;

        EnergyNetwork network = networks.get(networkId);
        if (network != null) {
            // В реальной реализации здесь вызывается логика разделения сети (Flood Fill)
            // Для упрощения: если сеть пуста — удаляем её
            // network.removeCable(pos);
            if (/* network.isEmpty() */ false) {
                networks.remove(networkId);
            }
            setDirty();
        }
    }

    /**
     * Объединяет две сети в одну.
     */
    private void mergeNetworks(EnergyNetwork main, EnergyNetwork secondary) {
        // Перенос всех BlockPos из вторичной сети в основную (реализовать в EnergyNetwork)
        // main.absorb(secondary);

        // Обновляем индекс координат для всех кабелей из поглощенной сети
        // for (BlockPos pos : secondary.getCables()) {
        //     cableToNetworkMap.put(pos, main.getNetworkId());
        // }

        networks.remove(secondary.getNetworkId());
    }

    // --- СЕКЦИЯ СОХРАНЕНИЯ (NBT) ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag networkList = new ListTag();
        for (EnergyNetwork network : networks.values()) {
            CompoundTag nbt = new CompoundTag();
            // network.save(nbt); // Метод сохранения в классе EnergyNetwork
            networkList.add(nbt);
        }
        tag.put("Networks", networkList);
        return tag;
    }

    public static EnergyNetworkManager load(CompoundTag tag, HolderLookup.Provider registries) {
        EnergyNetworkManager manager = new EnergyNetworkManager();
        ListTag networkList = tag.getList("Networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networkList.size(); i++) {
            CompoundTag nbt = networkList.getCompound(i);
            // EnergyNetwork network = EnergyNetwork.load(level, nbt);
            // manager.networks.put(network.getNetworkId(), network);
            // Заполнение cableToNetworkMap на основе загруженных данных...
        }
        return manager;
    }

    /**
     * Статический метод доступа к менеджеру в конкретном мире.
     */
    public static EnergyNetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(EnergyNetworkManager::new, EnergyNetworkManager::load),
                DATA_NAME
        );
    }
}

