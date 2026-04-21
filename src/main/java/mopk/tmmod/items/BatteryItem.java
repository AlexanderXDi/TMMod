package mopk.tmmod.items;

import mopk.tmmod.energy_network.CustomEnergyItemInterface;
import mopk.tmmod.registration.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BatteryItem extends Item implements CustomEnergyItemInterface {
    private final BatteryTier tier;

    public BatteryItem(BatteryTier tier, Properties properties) {
        // Устанавливаем значение по умолчанию для компонента заряда при создании
        super(properties
                .stacksTo(64)
                .component(ModDataComponents.CHARGE.get(), 0));
        this.tier = tier;
    }

    @Override
    public int getEnergyStored(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
    }

    @Override
    public int getMaxEnergyStored(ItemStack stack) {
        return this.tier.getMaxEnergy();
    }

    @Override
    public int getTier(ItemStack stack) {
        return this.tier.getTier();
    }

    @Override
    public int getTransferRate(ItemStack stack) {
        return this.tier.getTransfer();
    }

    @Override
    public int receiveEnergy(ItemStack stack, int amount, boolean simulate) {
        int stored = getEnergyStored(stack);
        int canReceive = Math.min(getMaxEnergyStored(stack) - stored, amount);

        if (!simulate && canReceive > 0) {
            stack.set(ModDataComponents.CHARGE.get(), stored + canReceive);
        }
        return canReceive;
    }

    @Override
    public int extractEnergy(ItemStack stack, int amount, boolean simulate) {
        int stored = getEnergyStored(stack);
        int canExtract = Math.min(stored, amount);

        if (!simulate && canExtract > 0) {
            stack.set(ModDataComponents.CHARGE.get(), stored - canExtract);
        }
        return canExtract;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.literal(getEnergyStored(stack) + "/" + getMaxEnergyStored(stack) + "EU"));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergyStored(stack) / getMaxEnergyStored(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }
}

