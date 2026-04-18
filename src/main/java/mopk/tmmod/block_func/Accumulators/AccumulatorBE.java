package mopk.tmmod.block_func.Accumulators;

import mopk.tmmod.energy_network.CustomEnergyItemInterface;
import mopk.tmmod.energy_network.CustomEnergyStorage;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import mopk.tmmod.registration.CustomCapabilities;
import mopk.tmmod.registration.ModBlockEntities;
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
import org.jetbrains.annotations.Nullable;

public class AccumulatorBE extends BlockEntity implements MenuProvider, CustomEnergyStorage {
    private final AccumulatorTier tier;
    private final boolean isChargePad;
    private int energyStored = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStored & 0xFFFF;
                case 1 -> (energyStored >> 16) & 0xFFFF;
                case 2 -> tier.getCapacity() & 0xFFFF;
                case 3 -> (tier.getCapacity() >> 16) & 0xFFFF;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 4; }
    };

    public AccumulatorBE(BlockPos pos, BlockState state, AccumulatorTier tier, boolean isChargePad) {
        super(ModBlockEntities.ACCUMULATOR_BE.get(), pos, state);
        this.tier = tier;
        this.isChargePad = isChargePad;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public boolean isChargePad() {
        return isChargePad;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tmmod." + tier.getName() + (isChargePad ? "_charge_pad" : ""));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AccumulatorMenu(id, inv, this, this.data);
    }

    @Override
    public int receiveEnergy(int maxReceive, int tier, boolean simulate) {
        if (tier > this.tier.getTier()) {
            if (!simulate) triggerExplosion();
            return 0;
        }
        int space = this.tier.getCapacity() - energyStored;
        int toReceive = Math.min(maxReceive, Math.min(space, this.tier.getTransfer()));
        if (!simulate) {
            energyStored += toReceive;
            setChanged();
        }
        return toReceive;
    }

    private void triggerExplosion() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
            this.level.destroyBlock(this.worldPosition, false);
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int toExtract = Math.min(maxExtract, Math.min(energyStored, this.tier.getTransfer()));
        if (!simulate) {
            energyStored -= toExtract;
            setChanged();
        }
        return toExtract;
    }

    @Override
    public int getEnergyStored() { return energyStored; }
    @Override
    public int getMaxEnergyStored() { return tier.getCapacity(); }
    @Override
    public int getEnergyTier() { return tier.getTier(); }

    @Override
    public boolean canReceive(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side != facing;
    }

    @Override
    public boolean canExtract(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return side == facing;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeAdded(level, this.worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) EnergyNetworkManager.get((ServerLevel) level).onNodeRemoved(this.worldPosition);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // 1. Charge items in slots (0 = main, 1-4 = armor)
        int numSlots = isChargePad ? 1 : 5;
        for (int i = 0; i < numSlots; i++) {
            ItemStack chargeStack = inventory.getStackInSlot(i);
            if (!chargeStack.isEmpty() && energyStored > 0) {
                if (chargeStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                    int toGive = Math.min(energyStored, tier.getTransfer());
                    int accepted = energyItem.receiveEnergy(chargeStack, toGive, false);
                    if (accepted > 0) {
                        energyStored -= accepted;
                        setChanged();
                    }
                }
            }
        }

        // 2. Charge block from redstone or items (slot 5)
        ItemStack chargeInStack = inventory.getStackInSlot(5);
        if (!chargeInStack.isEmpty() && energyStored < tier.getCapacity()) {
            if (chargeInStack.is(Items.REDSTONE)) {
                int energyGain = 500;
                if (energyStored + energyGain <= tier.getCapacity()) {
                    energyStored += energyGain;
                    chargeInStack.shrink(1);
                    setChanged();
                }
            } else if (chargeInStack.is(Items.REDSTONE_BLOCK)) {
                int energyGain = 4500;
                if (energyStored + energyGain <= tier.getCapacity()) {
                    energyStored += energyGain;
                    chargeInStack.shrink(1);
                    setChanged();
                }
            } else if (chargeInStack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                int space = tier.getCapacity() - energyStored;
                int toExtract = Math.min(space, tier.getTransfer());
                int extracted = energyItem.extractEnergy(chargeInStack, toExtract, false);
                if (extracted > 0) {
                    energyStored += extracted;
                    setChanged();
                }
            }
        }

        // 4. Charge entities on top (Charge Pad only)
        if (isChargePad && energyStored > 0) {
            net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(pos).inflate(0.1).expandTowards(0, 1.5, 0);
            java.util.List<Player> players = level.getEntitiesOfClass(Player.class, area);
            for (Player player : players) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (energyStored <= 0) break;
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof CustomEnergyItemInterface energyItem) {
                        int toGive = Math.min(energyStored, tier.getTransfer());
                        int accepted = energyItem.receiveEnergy(stack, toGive, false);
                        if (accepted > 0) {
                            energyStored -= accepted;
                            setChanged();
                        }
                    }
                }
            }
        }

        // 5. Output energy to facing side
        if (energyStored > 0) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos targetPos = pos.relative(facing);
            CustomEnergyStorage target = level.getCapability(CustomCapabilities.ENERGY, targetPos, facing.getOpposite());
            if (target != null && target.canReceive(facing.getOpposite())) {
                int toExtract = Math.min(energyStored, tier.getTransfer());
                int accepted = target.receiveEnergy(toExtract, getEnergyTier(), false);
                if (accepted > 0) {
                    energyStored -= accepted;
                    setChanged();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energyStored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energyStored = tag.getInt("Energy");
    }
}
