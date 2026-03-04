package mopk.tmmod.events_and_else;


import mopk.tmmod.blocks.ModBlocks;
import mopk.tmmod.events_and_else.Generator.GeneratorBE;
import mopk.tmmod.events_and_else.IronFurnace.IronFurnaceBE;

import net.minecraft.core.registries.BuiltInRegistries;
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
}