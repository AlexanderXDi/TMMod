package mopk.tmmod;

import static mopk.tmmod.events_and_else.ModBlockEntities.BLOCK_ENTITIES;
import static mopk.tmmod.events_and_else.ModMenuTypes.MENUS;
import static mopk.tmmod.items.ModItems.*;
import static mopk.tmmod.blocks.ModBlocks.*;
import static mopk.tmmod.player_interface.CreativeTab.CREATIVE_MODE_TABS;
import static mopk.tmmod.player_interface.CreativeTab.MOD_TAB;

import mopk.tmmod.events_and_else.Generator.GeneratorScreen;
import mopk.tmmod.events_and_else.IronFurnace.IronFurnaceScreen;
import mopk.tmmod.events_and_else.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Tmmod.MODID)
public class Tmmod {
    public static final String MODID = "tmmod";

    public Tmmod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerScreens);

        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);

        modEventBus.addListener(this::buildCreativeTabs);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.IRON_FURNACE_MENU.get(), IronFurnaceScreen::new);
        event.register(ModMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
    }

    private void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == MOD_TAB.getKey()) {
            event.accept(IRON_HAMMER.get());
            event.accept(STEEL_INGOT.get());
            event.accept(IRON_FURNACE.get());
            event.accept(GENERATOR.get());
        }
    }
}