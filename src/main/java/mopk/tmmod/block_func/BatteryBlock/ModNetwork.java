package mopk.tmmod.block_func.BatteryBlock;

import net.neoforged.neoforge.network.handling.IPayloadContext;

// синхронизирует кнопку с сервером
public class ModNetwork {
    public static void handleBatteryMode(BatteryBlockModePacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(data.pos()) instanceof BatteryBlockBE be) {
                be.toggleMode();
            }
        });
    }
}
