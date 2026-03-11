package mopk.tmmod.items;

import mopk.tmmod.etc.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BatteryItem extends Item {
    public BatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int currentEnergy = stack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
        int maxEnergy = 10000;

        tooltipComponents.add(Component.literal("§eЭнергия: §f" + currentEnergy + " / " + maxEnergy + " FE"));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.has(ModDataComponents.CHARGE.get());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int currentEnergy = stack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
        int maxEnergy = 10000;
        return Math.round(13.0F * currentEnergy / maxEnergy);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }
}

