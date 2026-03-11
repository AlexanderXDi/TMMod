package mopk.tmmod.etc;

import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.etc.BatteryBlock.BatteryBlockBE;
import mopk.tmmod.etc.Cables.CableBE;
import mopk.tmmod.etc.Cables.CableTier;
import mopk.tmmod.etc.Crusher.CrusherBE;
import mopk.tmmod.etc.Generator.GeneratorBE;
import mopk.tmmod.etc.IronFurnace.IronFurnaceBE;
import mopk.tmmod.blocks.singleblocks.CableBlock;

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