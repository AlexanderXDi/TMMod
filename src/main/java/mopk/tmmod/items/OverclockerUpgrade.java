package mopk.tmmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;


public class OverclockerUpgrade extends Item {
    public OverclockerUpgrade(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        double currentSpeedMultiplier = 1;
        int totalSpeedModules = stack.getCount();
        if (totalSpeedModules > 0) {
            for (int i = 0  ; i <= totalSpeedModules; i++) {
                currentSpeedMultiplier = Math.pow(1.5, totalSpeedModules);
            }
        } else {
            currentSpeedMultiplier = 1.0;
        }

        Component text = Component.translatable("gui.tmmod.overclocker_upgrade.text");
        tooltipComponents.add(Component.literal( (int) (100 * currentSpeedMultiplier) + "% " + text.getString()));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
