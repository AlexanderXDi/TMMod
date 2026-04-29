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

    public static final DeferredItem<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1)));

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
    public static final DeferredItem<Item> SCRAP = ITEMS.register("scrap",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT = ITEMS.register("electronic_circuit",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> EMPTY_CAPSULE = ITEMS.register("empty_capsule",
            () -> new UniversalCapsuleItem(new Item.Properties().stacksTo(16)));

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
    public static final DeferredItem<Item> BRONZE_INGOT = ITEMS.register("bronze_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COMPOSITE_INGOT = ITEMS.register("composite_ingot",
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
    public static final DeferredItem<Item> BRONZE_DUST = ITEMS.register("bronze_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STEEL_DUST = ITEMS.register("steel_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COAL_DUST = ITEMS.register("coal_dust",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TINY_IRON_DUST = ITEMS.register("tiny_iron_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TINY_GOLD_DUST = ITEMS.register("tiny_gold_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TINY_COPPER_DUST = ITEMS.register("tiny_copper_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TINY_TIN_DUST = ITEMS.register("tiny_tin_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TINY_LEAD_DUST = ITEMS.register("tiny_lead_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TINY_BRONZE_DUST = ITEMS.register("tiny_bronze_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TINY_STEEL_DUST = ITEMS.register("tiny_steel_dust",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_PLATE = ITEMS.register("iron_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GOLD_PLATE = ITEMS.register("gold_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COPPER_PLATE = ITEMS.register("copper_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TIN_PLATE = ITEMS.register("tin_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LEAD_PLATE = ITEMS.register("lead_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BRONZE_PLATE = ITEMS.register("bronze_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STEEL_PLATE = ITEMS.register("steel_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COMPOSITE = ITEMS.register("composite",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DENSE_IRON_PLATE = ITEMS.register("dense_iron_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DENSE_GOLD_PLATE = ITEMS.register("dense_gold_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DENSE_COPPER_PLATE = ITEMS.register("dense_copper_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DENSE_TIN_PLATE = ITEMS.register("dense_tin_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DENSE_LEAD_PLATE = ITEMS.register("dense_lead_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DENSE_BRONZE_PLATE = ITEMS.register("dense_bronze_plate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DENSE_STEEL_PLATE = ITEMS.register("dense_steel_plate",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_CASING = ITEMS.register("iron_casing",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GOLD_CASING = ITEMS.register("gold_casing",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COPPER_CASING = ITEMS.register("copper_casing",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TIN_CASING = ITEMS.register("tin_casing",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LEAD_CASING = ITEMS.register("lead_casing",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BRONZE_CASING = ITEMS.register("bronze_casing",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STEEL_CASING = ITEMS.register("steel_casing",
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
    public static final DeferredItem<Item> EJECTOR_UPGRADE = ITEMS.register("ejector_upgrade",
            () -> new EjectorUpgrade(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.EJECTOR_ACTIVE.get(), false)
            ));
    public static final DeferredItem<Item> BATTERY = ITEMS.register("battery",
            () -> new BatteryItem(BatteryTier.BATTERY, new Item.Properties()));
    public static final DeferredItem<Item> ADVANCED_BATTERY = ITEMS.register("advanced_battery",
            () -> new BatteryItem(BatteryTier.ADVANCED_BATTERY, new Item.Properties()));
    public static final DeferredItem<Item> ENERGY_CRYSTAL = ITEMS.register("energy_crystal",
            () -> new BatteryItem(BatteryTier.ENERGY_CRYSTAL, new Item.Properties()));
    public static final DeferredItem<Item> LAPOTRON_CRYSTAL = ITEMS.register("lapotron_crystal",
            () -> new BatteryItem(BatteryTier.LAPOTRON_CRYSTAL, new Item.Properties()));

    public static final DeferredItem<Item> HEAT_CONDUCTOR = ITEMS.register("heat_conductor",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ASH = ITEMS.register("ash",
            () -> new Item(new Item.Properties()));
}