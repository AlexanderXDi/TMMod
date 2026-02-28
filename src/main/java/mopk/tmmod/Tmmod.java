package mopk.tmmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Tmmod.MODID)
public class Tmmod {
    public static final String MODID = "tmmod";

    public Tmmod(IEventBus modEventBus) {

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}
}