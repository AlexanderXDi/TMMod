package mopk.tmmod.blocks.events_and_else;

import mopk.tmmod.blocks.events_and_else.IronFurnace.IronFurnaceMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "tmmod");

    public static final DeferredHolder<MenuType<?>, MenuType<IronFurnaceMenu>> IRON_FURNACE_MENU = MENUS.register("iron_furnace_menu",
            () -> IMenuTypeExtension.create(IronFurnaceMenu::new));
}

