package mopk.tmmod.events_and_else;

import mopk.tmmod.events_and_else.BatteryBlock.BatteryBlockBE;
import mopk.tmmod.events_and_else.BatteryBlock.BatteryBlockModePacket;

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
