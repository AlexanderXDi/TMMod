package mopk.tmmod.etc;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "tmmod");

    public static final Supplier<DataComponentType<Integer>> CHARGE =
            DATA_COMPONENTS.register("charge",
                    () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
}

