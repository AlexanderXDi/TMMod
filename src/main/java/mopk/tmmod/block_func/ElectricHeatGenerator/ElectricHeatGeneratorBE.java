package mopk.tmmod.block_func.ElectricHeatGenerator;

import mopk.tmmod.custom_interfaces.CustomEnergyItemInterface;
import mopk.tmmod.custom_interfaces.CustomEnergyStorage;
import mopk.tmmod.custom_interfaces.CustomHeatStorage;
import mopk.tmmod.custom_interfaces.EnergyNetworkManager;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
import mopk.tmmod.registration.ModItems;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;


public class ElectricHeatGeneratorBE extends BlockEntity implements CustomEnergyStorage, CustomHeatStorage, MenuProvider {
    private int energyStored;
    private int currentMaxEnergyStored;
    private int currentEnergyTier;
    private int currentMaxReceiveAmount;
    private int activeCoils = 0;
    private float energyFraction = 0.0f;


    public ElectricHeatGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_HEAT_GENERATOR_BE.get(), pos, state);
        this.energyStored = 0;
        this.currentMaxEnergyStored = 10000;
        this.currentEnergyTier = 1;
        this.currentMaxReceiveAmount = 32;
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
        if (tier > this.currentEnergyTier) {
            if (!simulate) {
                triggerExplosion();
                this.energyStored = 0;
                this.setChanged();
            }
            return 0;
        }

        int actualMaxReceive = Math.min(maxReceive, this.currentMaxReceiveAmount);

        int energyReceived = Math.min(actualMaxReceive, this.currentMaxEnergyStored - this.energyStored);

        if (!simulate) {
            this.energyStored += energyReceived;
            this.setChanged();
        }
        return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    public boolean canReceive(Direction side) {
        return side != getBlockState().getValue(BlockStateProperties.FACING);
    }

    public boolean canExtract(Direction side) {
        return false;
    }

    public CustomHeatStorage getHeatStorage(Direction side) {
        return this;
    }

    @Override
    public int getHeatStored() {
        return 0;
    }

    @Override
    public int getMaxHeatStored() {
        return 0;
    }

    @Override
    public int receiveHeat(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractHeat(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    private void recalculateCoils() {
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            if (inventory.getStackInSlot(i).is(ModItems.COIL.get())) {
                count++;
            }
        }
        this.activeCoils = count;
    }

    private void triggerExplosion() {
        float explosionRadius = 4.0F;
        Level.ExplosionInteraction explosionType = Level.ExplosionInteraction.TNT;
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(
                    null,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D,
                    explosionRadius,
                    explosionType
            );
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ElectricHeatGeneratorBE.this.energyStored;
                case 1 -> ElectricHeatGeneratorBE.this.currentMaxEnergyStored;
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
        return Component.translatable("container.tmmod.electric_heat_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ElectricHeatGeneratorMenu(id, inventory, this, this.data);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(13) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 1 && slot <= 10) {
                recalculateCoils();
            }
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= 1 && slot <= 10) {
                return stack.is(ModItems.COIL.get());
            }
            return super.isItemValid(slot, stack);
        }
    };

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean isLit = false;

        ItemStack chargeStack = inventory.getStackInSlot(0);
        if (!chargeStack.isEmpty() && energyStored < currentMaxEnergyStored) {
            if (chargeStack.is(Items.REDSTONE)) {
                int energyGain = 400;
                if (energyStored + energyGain <= currentMaxEnergyStored) {
                    energyStored += energyGain;
                    chargeStack.shrink(1);
                    setChanged();
                }
            } else if (chargeStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                if (energyItem.getTier(chargeStack) <= this.currentEnergyTier) {
                    int space = currentMaxEnergyStored - energyStored;
                    int transferRate = Math.min(this.currentMaxReceiveAmount, energyItem.getTransferRate(chargeStack));
                    int toExtract = Math.min(space, transferRate);
                    int extracted = energyItem.extractEnergy(chargeStack, toExtract, false);
                    if (extracted > 0) {
                        energyStored += extracted;
                        setChanged();
                    }
                }
            }
        }

        if (activeCoils > 0) {
            int maxHeatToGenerate = activeCoils * 10;
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos targetPos = pos.relative(facing);
            BlockEntity targetBE = level.getBlockEntity(targetPos);

            if (targetBE != null) {
                CustomHeatStorage targetStorage = level.getCapability(CustomCapabilities.HEAT, targetPos, targetBE.getBlockState(), targetBE, facing.getOpposite());
                if (targetStorage != null && targetStorage.canConnectHeat(facing.getOpposite())) {
                    // Пробуем передать тепло. Прием "без ограничений" вернет всё переданное.
                    int accepted = targetStorage.receiveHeat(maxHeatToGenerate, false);
                    if (accepted > 0) {
                        // Расчет стоимости EU пропорционально переданному теплу (100 HU = 32 EU)
                        float exactCost = accepted * 0.32f;
                        int intCost = (int) exactCost;
                        energyFraction += (exactCost - intCost);
                        if (energyFraction >= 1.0f) {
                            intCost += 1;
                            energyFraction -= 1.0f;
                        }

                        if (energyStored >= intCost) {
                            energyStored -= intCost;
                            isLit = true;
                            setChanged();
                        } else {
                            // Если энергии не хватило, возвращаем тепло (имитация отката, так как буфера нет)
                            // В реальности при "без ограничений" мы просто сожжем энергию до 0
                            energyStored = 0;
                            isLit = energyStored > 0;
                        }
                    }
                }
            }
        }

        boolean wasLit = state.getValue(BlockStateProperties.LIT);
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, isLit), 3);
        }

        if (isLit) {
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energyStored = tag.getInt("energy");
        currentMaxEnergyStored = tag.getInt("maxCapacity");
        currentEnergyTier = tag.getInt("energyTier");
        currentMaxReceiveAmount = tag.getInt("maxReceiveAmount");
        recalculateCoils();
    }
}
