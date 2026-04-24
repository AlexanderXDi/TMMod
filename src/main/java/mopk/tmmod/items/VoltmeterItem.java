package mopk.tmmod.items;

import mopk.tmmod.custom_interfaces.EnergyNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class VoltmeterItem extends Item {
    public VoltmeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos();
            EnergyNetworkManager manager = EnergyNetworkManager.get((ServerLevel) level);

            int euIn = manager.getEuIn(pos);
            int euOut = manager.getEuOut(pos);
            double avgIn = manager.getAvgIn(pos);
            double avgOut = manager.getAvgOut(pos);

            context.getPlayer().sendSystemMessage(Component.literal("--- Voltmeter Readings ---").withStyle(ChatFormatting.GOLD));
            context.getPlayer().sendSystemMessage(Component.translatable("gui.tmmod.voltmeter.in", euIn).withStyle(ChatFormatting.AQUA));
            context.getPlayer().sendSystemMessage(Component.translatable("gui.tmmod.voltmeter.out", euOut).withStyle(ChatFormatting.GREEN));
            context.getPlayer().sendSystemMessage(Component.translatable("gui.tmmod.voltmeter.avg_in", String.format("%.1f", avgIn)).withStyle(ChatFormatting.DARK_AQUA));
            context.getPlayer().sendSystemMessage(Component.translatable("gui.tmmod.voltmeter.avg_out", String.format("%.1f", avgOut)).withStyle(ChatFormatting.DARK_GREEN));
        }
        return InteractionResult.PASS;
    }
}
