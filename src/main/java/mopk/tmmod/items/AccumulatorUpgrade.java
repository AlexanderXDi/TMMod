package mopk.tmmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;


public class AccumulatorUpgrade extends Item {
    public AccumulatorUpgrade(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int currentAccumulatorBonus = 0;
        int totalAccumulatorModules = stack.getCount();
        if (totalAccumulatorModules > 0) {
            for (int i = 0  ; i <= totalAccumulatorModules; i++) {
                currentAccumulatorBonus = 5000 * i;
            }
        } else {
            currentAccumulatorBonus = 0;
        }

        Component text = Component.translatable("gui.tmmod.accumulator_upgrade.text");
        tooltipComponents.add(Component.literal( currentAccumulatorBonus + text.getString()));
    }
}
