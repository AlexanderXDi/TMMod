package mopk.tmmod.registration;

import mopk.tmmod.block_func.Cables.CableBE;
import mopk.tmmod.block_func.Cables.CableTier;
import mopk.tmmod.block_func.Compressor.CompressorBE;
import mopk.tmmod.block_func.Crusher.CrusherBE;
import mopk.tmmod.block_func.Extractor.ExtractorBE;
import mopk.tmmod.block_func.Metalformer.MetalformerBE;
import mopk.tmmod.block_func.ElectricFurnace.ElectricFurnaceBE;
import mopk.tmmod.block_func.Generator.GeneratorBE;
import mopk.tmmod.block_func.IronFurnace.IronFurnaceBE;
import mopk.tmmod.blocks.CableBlock;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
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

    public static final Supplier<BlockEntityType<CrusherBE>> CRUSHER_BE =
            BLOCK_ENTITIES.register("crusher_be",
                    () -> BlockEntityType.Builder.of(
                            CrusherBE::new,
                            ModBlocks.CRUSHER.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<ExtractorBE>> EXTRACTOR_BE =
            BLOCK_ENTITIES.register("extractor_be",
                    () -> BlockEntityType.Builder.of(
                            ExtractorBE::new,
                            ModBlocks.EXTRACTOR.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<CompressorBE>> COMPRESSOR_BE =
            BLOCK_ENTITIES.register("compressor_be",
                    () -> BlockEntityType.Builder.of(
                            CompressorBE::new,
                            ModBlocks.COMPRESSOR.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<mopk.tmmod.block_func.ElectricHeatGenerator.ElectricHeatGeneratorBE>> ELECTRIC_HEAT_GENERATOR_BE =
            BLOCK_ENTITIES.register("electric_heat_generator_be",
                    () -> BlockEntityType.Builder.of(
                            mopk.tmmod.block_func.ElectricHeatGenerator.ElectricHeatGeneratorBE::new,
                            ModBlocks.ELECTRIC_HEAT_GENERATOR.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<mopk.tmmod.block_func.InductionFurnace.InductionFurnaceBE>> INDUCTION_FURNACE_BE =
            BLOCK_ENTITIES.register("induction_furnace_be",
                    () -> BlockEntityType.Builder.of(
                            mopk.tmmod.block_func.InductionFurnace.InductionFurnaceBE::new,
                            ModBlocks.INDUCTION_FURNACE.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<MetalformerBE>> METALFORMER_BE =
            BLOCK_ENTITIES.register("metalformer_be",
                    () -> BlockEntityType.Builder.of(
                            MetalformerBE::new,
                            ModBlocks.METALFORMER.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<ElectricFurnaceBE>> ELECTRIC_FURNACE_BE =
            BLOCK_ENTITIES.register("electric_furnace_be",
                    () -> BlockEntityType.Builder.of(
                            ElectricFurnaceBE::new,
                            ModBlocks.ELECTRIC_FURNACE.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<mopk.tmmod.block_func.Accumulators.AccumulatorBE>> ACCUMULATOR_BE =
            BLOCK_ENTITIES.register("accumulator_be",
                    () -> {
                        List<Block> blocks = new ArrayList<>();
                        ModBlocks.ALL_ACCUMULATORS.values().forEach(pair -> {
                            blocks.add(pair.get(0).get());
                            blocks.add(pair.get(1).get());
                        });
                        return BlockEntityType.Builder.of(
                                (pos, state) -> {
                                    mopk.tmmod.blocks.AccumulatorBlock block = (mopk.tmmod.blocks.AccumulatorBlock) state.getBlock();
                                    return new mopk.tmmod.block_func.Accumulators.AccumulatorBE(pos, state, block.getTier(), block.isChargePad());
                                },
                                blocks.toArray(new Block[0])
                        ).build(null);
                    });

    public static final Supplier<BlockEntityType<mopk.tmmod.block_func.Transformers.TransformerBE>> TRANSFORMER_BE =
            BLOCK_ENTITIES.register("transformer_be",
                    () -> {
                        List<Block> blocks = new ArrayList<>();
                        ModBlocks.ALL_TRANSFORMERS.values().forEach(holder -> blocks.add(holder.get()));
                        return BlockEntityType.Builder.of(
                                (pos, state) -> {
                                    mopk.tmmod.blocks.TransformerBlock block = (mopk.tmmod.blocks.TransformerBlock) state.getBlock();
                                    return new mopk.tmmod.block_func.Transformers.TransformerBE(pos, state, block.getTier());
                                },
                                blocks.toArray(new Block[0])
                        ).build(null);
                    });

    public static final Supplier<BlockEntityType<CableBE>> CABLE_BE =
            BLOCK_ENTITIES.register("cable_be",
                    () -> {
                        // Собираем ВСЕ блоки кабелей из всех тиров и всех уровней изоляции
                        List<Block> allCableBlocks = new ArrayList<>();
                        ModBlocks.ALL_CABLES.values().forEach(list -> 
                            list.forEach(holder -> allCableBlocks.add(holder.get()))
                        );
                        
                        return BlockEntityType.Builder.of(
                                (pos, state) -> {
                                    CableTier tier = ((CableBlock) state.getBlock()).getTier();
                                    return new CableBE(pos, state, tier);
                                },
                                allCableBlocks.toArray(new Block[0])
                        ).build(null);
                    });
}
