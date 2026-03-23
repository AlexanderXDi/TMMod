package mopk.tmmod.registration;

import static mopk.tmmod.registration.ModBlocks.IRON_FURNACE;

import mopk.tmmod.Tmmod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;




public class CreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Tmmod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("mod_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(IRON_FURNACE.get()))
                    .title(Component.translatable("creativetab.tmmod.mod_tab"))
                    .displayItems((params, output) -> {})
                    .build());
}