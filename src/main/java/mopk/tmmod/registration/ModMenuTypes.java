package mopk.tmmod.registration;

import mopk.tmmod.block_func.Compressor.CompressorMenu;
import mopk.tmmod.block_func.Crusher.CrusherMenu;
import mopk.tmmod.block_func.Recycler.RecyclerMenu;
import mopk.tmmod.block_func.Extractor.ExtractorMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<CrusherMenu>> CRUSHER_MENU = MENUS.register("crusher_menu",
            () -> IMenuTypeExtension.create(CrusherMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RecyclerMenu>> RECYCLER_MENU = MENUS.register("recycler_menu",
            () -> IMenuTypeExtension.create(RecyclerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorMenu>> EXTRACTOR_MENU = MENUS.register("extractor_menu",
            () -> IMenuTypeExtension.create(ExtractorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CompressorMenu>> COMPRESSOR_MENU = MENUS.register("compressor_menu",
            () -> IMenuTypeExtension.create(CompressorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.ElectricHeatGenerator.ElectricHeatGeneratorMenu>> ELECTRIC_HEAT_GENERATOR_MENU = MENUS.register("electric_heat_generator_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.ElectricHeatGenerator.ElectricHeatGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.SolidFuelHeatGenerator.SolidFuelHeatGeneratorMenu>> SOLID_FUEL_HEAT_GENERATOR_MENU = MENUS.register("solid_fuel_heat_generator_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.SolidFuelHeatGenerator.SolidFuelHeatGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.LiquidHeatGenerator.LiquidHeatGeneratorMenu>> LIQUID_HEAT_GENERATOR_MENU = MENUS.register("liquid_heat_generator_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.LiquidHeatGenerator.LiquidHeatGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.LiquidHeatExchanger.LiquidHeatExchangerMenu>> LIQUID_HEAT_EXCHANGER_MENU = MENUS.register("liquid_heat_exchanger_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.LiquidHeatExchanger.LiquidHeatExchangerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MetalformerMenu>> METALFORMER_MENU = MENUS.register("metalformer_menu",
            () -> IMenuTypeExtension.create(MetalformerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE_MENU = MENUS.register("electric_furnace_menu",
            () -> IMenuTypeExtension.create(ElectricFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.Accumulators.AccumulatorMenu>> ACCUMULATOR_MENU = MENUS.register("accumulator_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.Accumulators.AccumulatorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.InductionFurnace.InductionFurnaceMenu>> INDUCTION_FURNACE_MENU = MENUS.register("induction_furnace_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.InductionFurnace.InductionFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.Transformers.TransformerMenu>> TRANSFORMER_MENU = MENUS.register("transformer_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.Transformers.TransformerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<mopk.tmmod.block_func.Canner.CannerMenu>> CANNER_MENU = MENUS.register("canner_menu",
            () -> IMenuTypeExtension.create(mopk.tmmod.block_func.Canner.CannerMenu::new));
}

