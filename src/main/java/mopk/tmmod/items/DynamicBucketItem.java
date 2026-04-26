package mopk.tmmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class DynamicBucketItem extends BucketItem {
    private final Fluid fluid;

    public DynamicBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
        this.fluid = fluid;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component fluidName = fluid.getFluidType().getDescription();
        return Component.translatable("item.tmmod.liquid_bucket", fluidName);
    }
}
