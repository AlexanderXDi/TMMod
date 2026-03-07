package mopk.tmmod.events_and_else;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = "tmmod", bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        // Регистрируем энергию для Генератора
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK, // Какую возможность регистрируем? (Энергию)
                ModBlockEntities.GENERATOR_BE.get(), // Для какого BlockEntity?
                (blockEntity, direction) -> {
                    return blockEntity.getEnergyStorage();
                }
        );

    }
}
