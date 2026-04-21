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
import mopk.tmmod.block_func.Crusher.CrusherScreen;
import mopk.tmmod.block_func.Metalformer.MetalformerScreen;
import mopk.tmmod.block_func.ElectricFurnace.ElectricFurnaceScreen;
import mopk.tmmod.block_func.Generator.GeneratorScreen;
import mopk.tmmod.block_func.IronFurnace.IronFurnaceScreen;
import mopk.tmmod.block_func.Accumulators.AccumulatorScreen;
import mopk.tmmod.block_func.Transformers.TransformerModePacket;
import mopk.tmmod.block_func.Transformers.TransformerScreen;
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
import mopk.tmmod.registration.ModTreeDecorators;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Tmmod.MODID)
public class Tmmod {

    public static final String MODID = "tmmod";

    public Tmmod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerMetalformerNetworking);
        modEventBus.addListener(this::registerTransformerNetworking);
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
        // System.out.println("Server Tick Fired!");
        for (ServerLevel level : event.getServer().getAllLevels()) {
            EnergyNetworkManager.get(level).tick(level);
        }
    }

    private void registerMetalformerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("tmmod");
        registrar.playToServer(
                MetalformerModePacket.TYPE,
                MetalformerModePacket.CODEC,
                ModNetwork::handleMetalformerMode
        );
    }

    private void registerTransformerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("tmmod");
        registrar.playToServer(
                TransformerModePacket.TYPE,
                TransformerModePacket.CODEC,
                ModNetwork::handleTransformerMode
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
                ModBlockEntities.ACCUMULATOR_BE.get(),
                (blockEntity, direction) -> blockEntity
        );

        event.registerBlockEntity(
                CustomCapabilities.ENERGY,
                ModBlockEntities.TRANSFORMER_BE.get(),
                (blockEntity, direction) -> blockEntity
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
            ItemProperties.register(ModItems.ADVANCED_BATTERY.get(),
                ResourceLocation.fromNamespaceAndPath("tmmod", "charged"),
                (stack, level, entity, seed) -> {
                    int energy = stack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
                    return energy > 0 ? 1.0F : 0.0F;
                }
            );
            ItemProperties.register(ModItems.ENERGY_CRYSTAL.get(),
                ResourceLocation.fromNamespaceAndPath("tmmod", "charged"),
                (stack, level, entity, seed) -> {
                    int energy = stack.getOrDefault(ModDataComponents.CHARGE.get(), 0);
                    return energy > 0 ? 1.0F : 0.0F;
                }
            );
            ItemProperties.register(ModItems.LAPOTRON_CRYSTAL.get(),
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
        event.register(ModMenuTypes.ACCUMULATOR_MENU.get(), AccumulatorScreen::new);
        event.register(ModMenuTypes.TRANSFORMER_MENU.get(), TransformerScreen::new);
        event.register(ModMenuTypes.CRUSHER_MENU.get(), CrusherScreen::new);
        event.register(ModMenuTypes.METALFORMER_MENU.get(), MetalformerScreen::new);
        event.register(ModMenuTypes.ELECTRIC_FURNACE_MENU.get(), ElectricFurnaceScreen::new);
    }

    private void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == MOD_TAB.getKey()) {
            event.accept(TREETAP.get());
            event.accept(VOLTMETER.get());
            event.accept(IRON_HAMMER.get());
            event.accept(STEEL_INGOT.get());
            
            event.accept(TIN_INGOT.get());
            event.accept(COPPER_INGOT.get());
            event.accept(LEAD_INGOT.get());

            event.accept(RAW_TIN.get());
            event.accept(RAW_COPPER.get());
            event.accept(RAW_LEAD.get());

            event.accept(IRON_DUST.get());
            event.accept(GOLD_DUST.get());
            event.accept(COPPER_DUST.get());
            event.accept(TIN_DUST.get());
            event.accept(LEAD_DUST.get());

            event.accept(IRON_FURNACE.get());
            event.accept(GENERATOR.get());
            event.accept(BATTERY.get());
            event.accept(ADVANCED_BATTERY.get());
            event.accept(ENERGY_CRYSTAL.get());
            event.accept(LAPOTRON_CRYSTAL.get());
            
            event.accept(URANIUM_ORE.get());
            event.accept(DEEPSLATE_URANIUM_ORE.get());
            event.accept(TIN_ORE.get());
            event.accept(DEEPSLATE_TIN_ORE.get());
            event.accept(COPPER_ORE.get());
            event.accept(DEEPSLATE_COPPER_ORE.get());
            event.accept(LEAD_ORE.get());
            event.accept(DEEPSLATE_LEAD_ORE.get());
            
            ModBlocks.ALL_CABLES.values().forEach(variants -> {
                variants.forEach(cableBlock -> event.accept(cableBlock.get()));
            });

            ModBlocks.ALL_ACCUMULATORS.values().forEach(variants -> {
                variants.forEach(accBlock -> event.accept(accBlock.get()));
            });
            
            ModBlocks.ALL_TRANSFORMERS.values().forEach(transformerBlock -> {
                event.accept(transformerBlock.get());
            });

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