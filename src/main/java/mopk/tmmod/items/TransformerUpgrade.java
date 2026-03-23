package mopk.tmmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;


public class TransformerUpgrade extends Item {
    public TransformerUpgrade(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component text = Component.translatable("gui.tmmod.transformer_upgrade.text");
        tooltipComponents.add(Component.literal("+1 " + text.getString()));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
