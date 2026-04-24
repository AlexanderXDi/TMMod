package mopk.tmmod.registration;

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
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .persistent(Codec.INT)
                            .build());

    public static final Supplier<DataComponentType<Boolean>> SPEEDBONUS =
            DATA_COMPONENTS.register("speed_bonus",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .build());

    public static final Supplier<DataComponentType<Boolean>> ACCUMULATORBONUS =
            DATA_COMPONENTS.register("accumulator_bonus",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .build());

    public static final Supplier<DataComponentType<Boolean>> TRANSFORMERBONUS =
            DATA_COMPONENTS.register("transformer_bonus",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .build());

    public static final Supplier<DataComponentType<Integer>> EJECTOR_DIRECTION =
            DATA_COMPONENTS.register("ejector_direction",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .build());

    public static final Supplier<DataComponentType<Boolean>> EJECTOR_ACTIVE =
            DATA_COMPONENTS.register("ejector_active",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .build());
}

