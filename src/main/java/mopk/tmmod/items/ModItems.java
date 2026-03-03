package mopk.tmmod.items;

import mopk.tmmod.Tmmod;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tmmod.MODID);

    public static final DeferredItem<Item> TAB_ICON = ITEMS.register("tab_icon",
            () -> new Item(new Item.Properties()));
}
