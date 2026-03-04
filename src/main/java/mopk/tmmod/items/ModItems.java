package mopk.tmmod.items;

import mopk.tmmod.Tmmod;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tmmod.MODID);

    public static final DeferredItem<Item> IRON_HAMMER = ITEMS.register("iron_hammer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));
}
