package mopk.tmmod.registration;

import mopk.tmmod.block_func.BatteryBlock.BatteryBlockBE;
import mopk.tmmod.block_func.Cables.CableBE;
import mopk.tmmod.block_func.Cables.CableTier;
import mopk.tmmod.block_func.Crusher.CrusherBE;
import mopk.tmmod.block_func.ElectricFurnace.ElectricFurnaceBE;
import mopk.tmmod.block_func.Generator.GeneratorBE;
import mopk.tmmod.block_func.IronFurnace.IronFurnaceBE;
import mopk.tmmod.blocks.CableBlock;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "tmmod");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronFurnaceBE>> IRON_FURNACE_BE =
            BLOCK_ENTITIES.register("iron_furnace_be",
                    () -> BlockEntityType.Builder.of(
                            IronFurnaceBE::new,
                            ModBlocks.IRON_FURNACE.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<GeneratorBE>> GENERATOR_BE =
            BLOCK_ENTITIES.register("generator_be",
                    () -> BlockEntityType.Builder.of(
                            GeneratorBE::new,
                            ModBlocks.GENERATOR.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<BatteryBlockBE>> BATTERY_BLOCK_BE =
            BLOCK_ENTITIES.register("battery_block_be",
                    () -> BlockEntityType.Builder.of(
                            BatteryBlockBE::new,
                            ModBlocks.BATTERY_BLOCK.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<CrusherBE>> CRUSHER_BE =
            BLOCK_ENTITIES.register("crusher_be",
                    () -> BlockEntityType.Builder.of(
                            CrusherBE::new,
                            ModBlocks.CRUSHER.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<ElectricFurnaceBE>> ELECTRIC_FURNACE_BE =
            BLOCK_ENTITIES.register("electric_furnace_be",
                    () -> BlockEntityType.Builder.of(
                            ElectricFurnaceBE::new,
                            ModBlocks.ELECTRIC_FURNACE.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<CableBE>> CABLE_BE =
            BLOCK_ENTITIES.register("cable_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> {
                                CableTier tier = ((CableBlock) state.getBlock()).getTier();
                                return new CableBE(pos, state, tier);
                            },
                            ModBlocks.CABLES.values().stream().map(java.util.function.Supplier::get).toArray(Block[]::new)
                    ).build(null));
}