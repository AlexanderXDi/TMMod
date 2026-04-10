package mopk.tmmod;

import static mopk.tmmod.registration.ModBlockEntities.BLOCK_ENTITIES;
import static mopk.tmmod.registration.ModDataComponents.DATA_COMPONENTS;
import static mopk.tmmod.registration.ModMenuTypes.MENUS;
import static mopk.tmmod.registration.ModRecipes.RECIPE_TYPES;
import static mopk.tmmod.registration.ModRecipes.SERIALIZERS;
import static mopk.tmmod.registration.ModSounds.SOUND_EVENTS;
import static mopk.tmmod.registration.ModItems.*;
import static mopk.tmmod.registration.ModBlocks.*;
import static mopk.tmmod.registration.CreativeTab.CREATIVE_MODE_TABS;
import static mopk.tmmod.registration.CreativeTab.MOD_TAB;

import mopk.tmmod.block_func.Metalformer.MetalformerModePacket;
import mopk.tmmod.energy_network.EnergyNetworkManager;
import mopk.tmmod.registration.*;
import mopk.tmmod.block_func.BatteryBlock.BatteryBlockModePacket;
import mopk.tmmod.block_func.BatteryBlock.BatteryBlockScreen;
import mopk.tmmod.block_func.Crusher.CrusherScreen;
import mopk.tmmod.block_func.Metalformer.MetalformerScreen;
import mopk.tmmod.block_func.ElectricFurnace.ElectricFurnaceScreen;
import mopk.tmmod.block_func.Generator.GeneratorScreen;
import mopk.tmmod.block_func.IronFurnace.IronFurnaceScreen;
import mopk.tmmod.registration.ModNetwork;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import mopk.tmmod.worldgen.ModTreeDecorators;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Tmmod.MODID)
public class Tmmod {

    public static final String MODID = "tmmod";

    public Tmmod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerBatteryNetworking);
        modEventBus.addListener(this::registerMetalformerNetworking);
        modEventBus.addListener(this::gatherData);


        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        ModTreeDecorators.TREE_DECORATORS.register(modEventBus);

        modEventBus.addListener(this::buildCreativeTabs);

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            EnergyNetworkManager.get(level).tick(level);
        }
    }

    private void registerBatteryNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("tmmod");
        registrar.playToServer(
                BatteryBlockModePacket.TYPE,
                BatteryBlockModePacket.CODEC,
                ModNetwork::handleBatteryMode

        );
    }

    private void registerMetalformerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("tmmod");
        registrar.playToServer(
                MetalformerModePacket.TYPE,
                MetalformerModePacket.CODEC,
                ModNetwork::handleMetalformerMode
        );
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(
                event.includeServer(),
                new ModRecipeProvider(packOutput, event.getLookupProvider())
        );
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.GENERATOR_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage(direction)
        );

        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.CABLE_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage(direction)
        );

        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.BATTERY_BLOCK_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage(direction)
        );

        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.CRUSHER_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage(direction)
        );

        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.METALFORMER_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage(direction)
        );

        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.ELECTRIC_FURNACE_BE.get(),
                (blockEntity, direction) -> blockEntity.getEnergyStorage(direction)
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.BATTERY.get(),
                ResourceLocation.fromNamespaceAndPath("tmmod", "charged"),
                (stack, level, entity, seed) -> {
                    int energy = stack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
                    return energy > 0 ? 1.0F : 0.0F;
                }
            );
        });
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.IRON_FURNACE_MENU.get(), IronFurnaceScreen::new);
        event.register(ModMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
        event.register(ModMenuTypes.BATTERY_BLOCK_MENU.get(), BatteryBlockScreen::new);
        event.register(ModMenuTypes.CRUSHER_MENU.get(), CrusherScreen::new);
        event.register(ModMenuTypes.METALFORMER_MENU.get(), MetalformerScreen::new);
        event.register(ModMenuTypes.ELECTRIC_FURNACE_MENU.get(), ElectricFurnaceScreen::new);
    }

    private void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == MOD_TAB.getKey()) {
            event.accept(TREETAP.get());
            event.accept(IRON_HAMMER.get());
            event.accept(STEEL_INGOT.get());
            event.accept(IRON_FURNACE.get());
            event.accept(GENERATOR.get());
            event.accept(BATTERY.get());
            
            ModBlocks.ALL_CABLES.values().forEach(variants -> {
                variants.forEach(cableBlock -> event.accept(cableBlock.get()));
            });

            event.accept(BATTERY_BLOCK.get());
            event.accept(CRUSHER.get());
            event.accept(METALFORMER.get());
            event.accept(OVERCLOCKER_UPGRADE.get());
            event.accept(ACCUMULATOR_UPGRADE.get());
            event.accept(TRANSFORMER_UPGRADE.get());
            event.accept(STICKY_RESIN.get());
            event.accept(COIL.get());
            event.accept(ELECTRIC_MOTOR.get());
            event.accept(RUBBER.get());
            event.accept(RUBBER_LOG.get());
            event.accept(RUBBER_LEAVES.get());
            event.accept(RUBBER_SAPLING.get());
            event.accept(ELECTRONIC_CIRCUIT.get());
         }
     }
}