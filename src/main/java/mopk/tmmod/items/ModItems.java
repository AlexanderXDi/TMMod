package mopk.tmmod.items;

import mopk.tmmod.Tmmod;

import mopk.tmmod.etc.ModDataComponents;
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
    public static final Supplier<Item> BATTERY = ITEMS.register("battery",
            () -> new BatteryItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.CHARGE.get(), 0)
            ));
}