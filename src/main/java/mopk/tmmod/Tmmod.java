package mopk.tmmod;

import static mopk.tmmod.events_and_else.ModBlockEntities.BLOCK_ENTITIES;
import static mopk.tmmod.events_and_else.ModDataComponents.COMPONENTS;
import static mopk.tmmod.events_and_else.ModMenuTypes.MENUS;
import static mopk.tmmod.items.ModItems.*;
import static mopk.tmmod.blocks.ModBlocks.*;
import static mopk.tmmod.CreativeTab.CREATIVE_MODE_TABS;
import static mopk.tmmod.CreativeTab.MOD_TAB;

import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.events_and_else.BatteryBlock.BatteryBlockModePacket;
import mopk.tmmod.events_and_else.BatteryBlock.BatteryBlockScreen;
import mopk.tmmod.events_and_else.Generator.GeneratorScreen;
import mopk.tmmod.events_and_else.IronFurnace.IronFurnaceScreen;
import mopk.tmmod.events_and_else.ModBlockEntities;
import mopk.tmmod.events_and_else.ModDataComponents;
import mopk.tmmod.events_and_else.ModMenuTypes;
import mopk.tmmod.events_and_else.ModNetwork;
import mopk.tmmod.items.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Tmmod.MODID)
public class Tmmod {
    public static final String MODID = "tmmod";

    public Tmmod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerNetworking);

        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);
        COMPONENTS.register(modEventBus);

        modEventBus.addListener(this::buildCreativeTabs);
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("tmmod");

        registrar.playToServer(
                BatteryBlockModePacket.TYPE,
                BatteryBlockModePacket.CODEC,
                ModNetwork::handleBatteryMode
        );
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.GENERATOR_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.CABLE_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.BATTERY_BLOCK_BE.get(),
                (blockEntity, dir) -> blockEntity.getEnergyStorage(dir)
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.BATTERY.get(),
                    ResourceLocation.fromNamespaceAndPath("tmmod", "charged"),
                    (stack, level, entity, seed) -> {
                        boolean isCharged = stack.getOrDefault(ModDataComponents.IS_CHARGED.get(), false);
                        return isCharged ? 1.0F : 0.0F;
                    }
            );
        });
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.IRON_FURNACE_MENU.get(), IronFurnaceScreen::new);
        event.register(ModMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
        event.register(ModMenuTypes.BATTERY_BLOCK_MENU.get(), BatteryBlockScreen::new);
    }

    private void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == MOD_TAB.getKey()) {
            event.accept(IRON_HAMMER.get());
            event.accept(STEEL_INGOT.get());
            event.accept(IRON_FURNACE.get());
            event.accept(GENERATOR.get());
            event.accept(BATTERY.get());
            ModBlocks.CABLES.values().forEach(cableBlock -> {
                event.accept(cableBlock.get());
            });
            event.accept(BATTERY_BLOCK.get());
        }
    }
}