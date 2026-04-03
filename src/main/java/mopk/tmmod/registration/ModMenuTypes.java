package mopk.tmmod.registration;

import mopk.tmmod.block_func.BatteryBlock.BatteryBlockMenu;
import mopk.tmmod.block_func.Crusher.CrusherMenu;
import mopk.tmmod.block_func.Metalformer.MetalformerMenu;
import mopk.tmmod.block_func.ElectricFurnace.ElectricFurnaceMenu;
import mopk.tmmod.block_func.Generator.GeneratorMenu;
import mopk.tmmod.block_func.IronFurnace.IronFurnaceMenu;

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

    public static final DeferredHolder<MenuType<?>, MenuType<CrusherMenu>> CRUSHER_MENU = MENUS.register("crusher_menu",
            () -> IMenuTypeExtension.create(CrusherMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MetalformerMenu>> METALFORMER_MENU = MENUS.register("metalformer_menu",
            () -> IMenuTypeExtension.create(MetalformerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE_MENU = MENUS.register("electric_furnace_menu",
            () -> IMenuTypeExtension.create(ElectricFurnaceMenu::new));
}

