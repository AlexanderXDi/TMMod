package mopk.tmmod.block_func.Generator;

import mopk.tmmod.block_func.Cables.CableBE;
import mopk.tmmod.energy_network.CustomEnergyItemInterface;
import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;


public class GeneratorBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private int energyStored;
    private int currentMaxEnergyStored;
    private int currentEnergyTier;
    private int currentMaxReceiveAmount;
    private boolean[] canReceiveSides = new boolean[6];
    private boolean[] canExtractSides = new boolean[6];
    private int burningTimeRemaining = 0;
    private int burnTime = 0;
    private final int energyPerTick = 12;

    public GeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR_BE.get(), pos, state);
        this.energyStored = 0;
        this.currentMaxEnergyStored = 10000;
        this.currentEnergyTier = 1;
        this.currentMaxReceiveAmount = 32;
        for (int i = 0; i < 6; i++) {
            canReceiveSides[i] = false;
            canExtractSides[i] = true;
        }
    }

    public CustomEnergyStorage getEnergyStorage(Direction side) {
        return this;
    }

    public int getEnergyStored() {
        return this.energyStored;
    }

    public int getMaxEnergyStored() {
        return this.currentMaxEnergyStored;
    }

    public int getEnergyTier() {
        return this.currentEnergyTier;
    }

    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        int actualMaxReceive = Math.min(maxReceive, this.currentMaxReceiveAmount);

        int energyReceived = Math.min(actualMaxReceive, this.currentMaxEnergyStored - this.energyStored);

        if (!simulate) {
            this.energyStored += energyReceived;
            this.setChanged();
        }
        return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(maxExtract, this.energyStored);

        if (!simulate) {
            this.energyStored -= energyExtracted;
            this.setChanged();
        }
        return energyExtracted;
    }

    public boolean canReceive(Direction side) {
        return canReceiveSides[side.ordinal()];
    }

    public boolean canExtract(Direction side) {
        return canExtractSides[side.ordinal()];
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStored;
                case 1 -> currentMaxEnergyStored;
                case 2 -> burningTimeRemaining;
                case 3 -> burnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu(id, inventory, this, this.data);
    }

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getBurnTime(null) > 0;
                case 1 -> stack.has(ModDataComponents.CHARGE.get());
                default -> false;
            };
        }


        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        // 1. Предварительные проверки
        if (level == null || level.isClientSide) return;

        boolean isLit = false;
        boolean stateChanged = false;
        boolean canAcceptEnergy = this.energyStored < this.currentMaxEnergyStored;

        // =========================================
        // ФАЗА 1: ПОТРЕБЛЕНИЕ ТОПЛИВА (Инициация)
        // =========================================
        // Если мы не горим, но нам нужна энергия и есть топливо - поджигаем его.
        if (this.burningTimeRemaining <= 0 && canAcceptEnergy) {

            ItemStack fuelStack = inventory.getStackInSlot(0);
            int burnTime = fuelStack.getBurnTime(null);

            if (burnTime > 0) {
                this.burnTime = burnTime;
                this.burningTimeRemaining = burnTime;

                // Обработка ведер с лавой и подобного
                if (fuelStack.hasCraftingRemainingItem()) {
                    inventory.setStackInSlot(0, fuelStack.getCraftingRemainingItem());
                } else {
                    fuelStack.shrink(1);
                }
                isLit = true;
                stateChanged = true;
            }
        }

        // =========================================
        // ФАЗА 2: ГЕНЕРАЦИЯ ЭНЕРГИИ (Активное горение)
        // =========================================
        // Если мы горим, производим энергию каждый тик.
        if (this.burningTimeRemaining > 0) {
            this.burningTimeRemaining--;
            isLit = true;
            stateChanged = true;

            if (canAcceptEnergy) {
                // Вычисляем, сколько можем добавить, чтобы не превысить емкость
                int space = this.currentMaxEnergyStored - this.energyStored;
                int toGenerate = Math.min(this.energyPerTick, space);

                if (toGenerate > 0) {
                    this.energyStored += toGenerate;
                }
            }
        }

        // =========================================
        // ФАЗА 3: ЗАРЯДКА ПРЕДМЕТОВ В ИНВЕНТАРЕ
        // =========================================
        // Заряжаем аккумулятор, лежащий в слоте №1 (если есть энергия)
        ItemStack chargeStack = inventory.getStackInSlot(1);
        if (!chargeStack.isEmpty() && this.energyStored > 0) {
            if (chargeStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                // Проверяем тир предмета - Генератор (Т1) не должен заряжать предметы выше Т1
                if (energyItem.getTier(chargeStack) <= this.currentEnergyTier) {
                    int chargeSpeed = 32; // Стандартная скорость зарядки для Т1
                    // Сколько энергии мы реально можем отдать
                    int energyToGive = Math.min(this.energyStored, chargeSpeed);

                    // Пытаемся передать энергию предмету (симуляция отключена - передаем реально)
                    int accepted = energyItem.receiveEnergy(chargeStack, energyToGive, false);

                    if (accepted > 0) {
                        // Если предмет принял энергию, вычитаем её из буфера генератора
                        this.energyStored -= accepted;
                        stateChanged = true;
                    }
                }
            }
        }

        // =========================================
        // ФАЗА 4: РАСПРЕДЕЛЕНИЕ ЭНЕРГИИ (Push to Neighbors)
        // =========================================
        // Выталкиваем энергию в соседние блоки (машины), которые не подключены через кабели
        if (this.energyStored > 0) {
            for (Direction dir : Direction.values()) {
                if (this.energyStored <= 0) break; // Энергия кончилась, прерываем цикл

                // Проверяем, разрешен ли выход с этой стороны генератора
                if (!canExtract(dir)) continue;

                BlockPos neighborPos = worldPosition.relative(dir);
                BlockEntity neighborBE = level.getBlockEntity(neighborPos);

                // Если сосед - это кабель, пропускаем его! Кабели обслуживаются EnergyNetworkManager'ом.
                // Генератор не должен "вталкивать" энергию в кабель напрямую.
                if (neighborBE instanceof CableBE) continue;

                // Если сосед - это машина или хранилище (реализует CustomEnergyStorage)
                if (neighborBE instanceof CustomEnergyStorage neighborStorage) {
                    // Проверяем, может ли сосед принять энергию с этой стороны
                    if (neighborStorage.canReceive(dir.getOpposite())) {

                        // Сколько мы готовы отдать за тик
                        int extractLimit = Math.min(this.energyStored, this.currentMaxReceiveAmount);

                        // Пытаемся передать энергию соседу
                        int accepted = neighborStorage.receiveEnergy(extractLimit, this.currentEnergyTier, false);

                        if (accepted > 0) {
                            this.energyStored -= accepted;
                            stateChanged = true;
                        }
                    }
                }
            }
        }

        boolean wasLit = state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit && state.hasProperty(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }

        if (stateChanged) {
            setChanged();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energyStored);
        tag.putInt("maxCapacity", currentMaxEnergyStored);
        tag.putInt("energyTier", currentEnergyTier);
        tag.putInt("maxReceiveAmount", currentMaxReceiveAmount);
        byte[] receiveSidesBytes = new byte[6];
        byte[] extractSidesBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            receiveSidesBytes[i] = (byte) (canReceiveSides[i] ? 1 : 0);
            extractSidesBytes[i] = (byte) (canExtractSides[i] ? 1 : 0);
        }
        tag.putByteArray("canReceiveSides", receiveSidesBytes);
        tag.putByteArray("canExtractSides", extractSidesBytes);
        tag.putInt("generator.burningTimeRemaining", this.burningTimeRemaining);
        tag.putInt("generator.burnTime", this.burnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStored = tag.getInt("energy");
        currentMaxEnergyStored = tag.getInt("maxCapacity");
        currentEnergyTier = tag.getInt("energyTier");
        currentMaxReceiveAmount = tag.getInt("maxReceiveAmount");
        byte[] loadedReceiveSides = tag.getByteArray("canReceiveSides");
        if (loadedReceiveSides.length == 6) {
            for (int i = 0; i < 6; i++) {
                canReceiveSides[i] = loadedReceiveSides[i] == 1;
            }
        }
        byte[] loadedExtractSides = tag.getByteArray("canExtractSides");
        if (loadedExtractSides.length == 6) {
            for (int i = 0; i < 6; i++) {
                canExtractSides[i] = loadedExtractSides[i] == 1;
            }
        }
        this.burningTimeRemaining = tag.getInt("generator.burningTimeRemaining");
        this.burnTime = tag.getInt("generator.burnTime");
    }
}
