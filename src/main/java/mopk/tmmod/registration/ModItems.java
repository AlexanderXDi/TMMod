package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;

import mopk.tmmod.items.*;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tmmod.MODID);

    public static final DeferredItem<Item> TREETAP = ITEMS.register("treetap",
            () -> new TreetapItem(new Item.Properties().durability(50)));

    public static final DeferredItem<Item> VOLTMETER = ITEMS.register("voltmeter",
            () -> new VoltmeterItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> IRON_HAMMER = ITEMS.register("iron_hammer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STICKY_RESIN = ITEMS.register("sticky_resin",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL = ITEMS.register("coil",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ELECTRIC_MOTOR = ITEMS.register("electric_motor",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RUBBER = ITEMS.register("rubber",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT = ITEMS.register("electronic_circuit",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> RAW_TIN = ITEMS.register("raw_tin",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_COPPER = ITEMS.register("raw_copper",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_LEAD = ITEMS.register("raw_lead",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TIN_INGOT = ITEMS.register("tin_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COPPER_INGOT = ITEMS.register("copper_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LEAD_INGOT = ITEMS.register("lead_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_DUST = ITEMS.register("iron_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GOLD_DUST = ITEMS.register("gold_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COPPER_DUST = ITEMS.register("copper_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TIN_DUST = ITEMS.register("tin_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LEAD_DUST = ITEMS.register("lead_dust",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> OVERCLOCKER_UPGRADE = ITEMS.register("overclocker_upgrade",
            () -> new OverclockerUpgrade(new Item.Properties()
                    .component(ModDataComponents.SPEEDBONUS.get(), true)
            ));
    public static final DeferredItem<Item> ACCUMULATOR_UPGRADE = ITEMS.register("accumulator_upgrade",
            () -> new AccumulatorUpgrade(new Item.Properties()
                    .component(ModDataComponents.ACCUMULATORBONUS.get(), true)
            ));
    public static final DeferredItem<Item> TRANSFORMER_UPGRADE = ITEMS.register("transformer_upgrade",
            () -> new TransformerUpgrade(new Item.Properties()
                    .component(ModDataComponents.TRANSFORMERBONUS.get(), true)
            ));
    public static final DeferredItem<Item> BATTERY = ITEMS.register("battery",
            () -> new BatteryItem(BatteryTier.BATTERY, new Item.Properties()));
    public static final DeferredItem<Item> ADVANCED_BATTERY = ITEMS.register("advanced_battery",
            () -> new BatteryItem(BatteryTier.ADVANCED_BATTERY, new Item.Properties()));
    public static final DeferredItem<Item> ENERGY_CRYSTAL = ITEMS.register("energy_crystal",
            () -> new BatteryItem(BatteryTier.ENERGY_CRYSTAL, new Item.Properties()));
    public static final DeferredItem<Item> LAPOTRON_CRYSTAL = ITEMS.register("lapotron_crystal",
            () -> new BatteryItem(BatteryTier.LAPOTRON_CRYSTAL, new Item.Properties()));
}