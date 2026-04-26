package mopk.tmmod.block_func.Canner;

import net.minecraft.util.StringRepresentable;

public enum CannerMode implements StringRepresentable {
    ITEMS_TO_ITEM("items_to_item"),
    ITEM_TO_ITEM_FLUID("item_to_item_fluid"),
    ITEM_FLUID_TO_ITEM("item_fluid_to_item"),
    ITEMS_FLUID_TO_ITEM_FLUID("items_fluid_to_item_fluid");

    private final String name;

    CannerMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public CannerMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
