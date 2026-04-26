package mopk.tmmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class LiquidCapsuleItem extends BucketItem {
    private final Supplier<? extends Fluid> fluidSupplier;
    private final Supplier<? extends Item> craftRemainderSupplier;

    public LiquidCapsuleItem(Supplier<? extends Fluid> fluidSupplier, Supplier<? extends Item> craftRemainderSupplier, Properties properties) {
        super(fluidSupplier.get(), properties);
        this.fluidSupplier = fluidSupplier;
        this.craftRemainderSupplier = craftRemainderSupplier;
    }

    public Fluid getFluid() {
        return fluidSupplier.get();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return new ItemStack(craftRemainderSupplier.get());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        Fluid fluid = getFluid();
        if (fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            return super.getName(stack);
        }
        Component fluidName = fluid.getFluidType().getDescription();
        return Component.translatable("item.tmmod.liquid_capsule", fluidName);
    }
}
