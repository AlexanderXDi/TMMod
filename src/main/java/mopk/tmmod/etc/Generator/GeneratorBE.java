package mopk.tmmod.etc.Generator;

import mopk.tmmod.etc.ModBlockEntities;
import mopk.tmmod.etc.ModDataComponents;
import mopk.tmmod.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class GeneratorBE extends BlockEntity implements MenuProvider {
    private static class GeneratorEnergyStorage extends EnergyStorage {
        public GeneratorEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        @Override
        public boolean canReceive() { return false; }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        public void generateInternal(int amount) {
            this.energy = Math.min(this.capacity, this.energy + amount);
        }
    }

    private int burningTimeRemaining = 0;
    private final int energyPerTick = 12;
    public final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage(50000, 1000, 1000);

    public ItemStackHandler getInventory() {
        return inventory;
    }
    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public GeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR_BE.get(), pos, state);
    }

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getBurnTime(null) > 0;
                case 1 -> {
                    //if (stack.is(ModItems.BATTERY.get())) yield true;

                    if (stack.getCapability(Capabilities.EnergyStorage.ITEM) != null) yield true;

                    yield stack.has(ModDataComponents.CHARGE.get());
                }
                default -> false;
            };
        }


        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> GeneratorBE.this.energyStorage.getEnergyStored();
                case 1 -> GeneratorBE.this.energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
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

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean isLit = false;

        if (this.burningTimeRemaining > 0) {
            this.burningTimeRemaining--;
            if (this.energyStorage.getEnergyStored() < this.energyStorage.getMaxEnergyStored()) {
                this.energyStorage.generateInternal(energyPerTick);
            }
            isLit = true;
        }

        if (this.burningTimeRemaining <= 0 && this.energyStorage.getEnergyStored() < this.energyStorage.getMaxEnergyStored()) {
            ItemStack fuelStack = inventory.getStackInSlot(0);
            int burnTime = fuelStack.getBurnTime(null);
            if (burnTime > 0) {
                this.burningTimeRemaining = burnTime;
                if (fuelStack.hasCraftingRemainingItem()) {
                    inventory.setStackInSlot(0, fuelStack.getCraftingRemainingItem());
                } else {
                    fuelStack.shrink(1);
                }
                isLit = true;
            }
        }

        ItemStack chargeStack = inventory.getStackInSlot(1);
        if (!chargeStack.isEmpty() && this.energyStorage.getEnergyStored() > 0) {
            IEnergyStorage itemEnergy = chargeStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemEnergy != null && itemEnergy.canReceive()) {
                int chargeSpeed = 50;
                int extracted = this.energyStorage.extractEnergy(chargeSpeed, true);
                int accepted = itemEnergy.receiveEnergy(extracted, false);

                if (accepted > 0) {
                    this.energyStorage.extractEnergy(accepted, false);
                    isLit = true;
                }
            }
            else if (chargeStack.has(ModDataComponents.CHARGE.get())) {
                int currentItemEnergy = chargeStack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
                int maxItemEnergy = 10000;

                if (currentItemEnergy < maxItemEnergy) {
                    int chargeSpeed = 50;
                    int needed = maxItemEnergy - currentItemEnergy;
                    int energyToGive = Math.min(Math.min(chargeSpeed, needed), this.energyStorage.getEnergyStored());

                    if (energyToGive > 0) {
                        this.energyStorage.extractEnergy(energyToGive, false);
                        chargeStack.set(ModDataComponents.CHARGE.get(), currentItemEnergy + energyToGive);
                        isLit = true;
                    }
                }
            }
        }

        if (this.energyStorage.getEnergyStored() > 0) {
            for (Direction direction : Direction.values()) {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());

                if (target != null && target.canReceive()) {
                    int extracted = this.energyStorage.extractEnergy(1000, true);
                    int accepted = target.receiveEnergy(extracted, false);

                    if (accepted > 0) {
                        this.energyStorage.extractEnergy(accepted, false);
                        isLit = true;
                    }
                    setChanged();
                }
            }
        }

        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);

        if (isLit) {
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("generator.energy")) {
            this.energyStorage.deserializeNBT(registries, tag.get("generator.energy"));
        }
        this.burningTimeRemaining = tag.getInt("generator.burnTime");
    }
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("generator.energy", this.energyStorage.serializeNBT(registries));
        tag.putInt("generator.burnTime", this.burningTimeRemaining);
    }
}
