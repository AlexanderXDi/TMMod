package mopk.tmmod.events_and_else;

import mopk.tmmod.events_and_else.BatteryBlock.BatteryBlockMenu;
import mopk.tmmod.events_and_else.Generator.GeneratorMenu;
import mopk.tmmod.events_and_else.IronFurnace.IronFurnaceMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "tmmod");

    public static final DeferredHolder<MenuType<?>, MenuType<IronFurnaceMenu>> IRON_FURNACE_MENU = MENUS.register("iron_furnace_menu",
            () -> IMenuTypeExtension.create(IronFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GeneratorMenu>> GENERATOR_MENU = MENUS.register("generator_menu",
            () -> IMenuTypeExtension.create(GeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BatteryBlockMenu>> BATTERY_BLOCK_MENU = MENUS.register("battery_block_menu",
            () -> IMenuTypeExtension.create(BatteryBlockMenu::new));
}

