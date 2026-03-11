package mopk.tmmod.etc;

import mopk.tmmod.etc.BatteryBlock.BatteryBlockBE;
import mopk.tmmod.etc.BatteryBlock.BatteryBlockModePacket;

import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ModNetwork {
    // Этот метод обрабатывает нажатие кнопки на сервере
    public static void handleBatteryMode(BatteryBlockModePacket data, IPayloadContext context) {
        // enqueueWork гарантирует, что код выполнится в основном потоке сервера
        context.enqueueWork(() -> {
            // Ищем наш аккумулятор по координатам из пакета и переключаем режим
            if (context.player().level().getBlockEntity(data.pos()) instanceof BatteryBlockBE be) {
                be.toggleMode();
            }
        });
    }
}
