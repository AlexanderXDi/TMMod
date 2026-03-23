package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;

import mopk.tmmod.items.AccumulatorUpgrade;
import mopk.tmmod.items.BatteryItem;
import mopk.tmmod.items.OverclockerUpgrade;
import mopk.tmmod.items.TransformerUpgrade;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tmmod.MODID);

    public static final DeferredItem<Item> IRON_HAMMER = ITEMS.register("iron_hammer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
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
    public static final Supplier<Item> BATTERY = ITEMS.register("battery",
            () -> new BatteryItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.CHARGE.get(), 0)
            ));
}