package mopk.tmmod.registration;

import mopk.tmmod.Tmmod;
import mopk.tmmod.blocks.*;
import mopk.tmmod.block_func.Cables.CableTier;

import static mopk.tmmod.registration.ModItems.ITEMS;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import mopk.tmmod.worldgen.RubberTreeGrower;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Tmmod.MODID);

    public static final DeferredBlock<Block> RUBBER_LOG = registerBlock("rubber_log",
            () -> new RubberLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));

    public static final DeferredBlock<Block> RUBBER_LEAVES = registerBlock("rubber_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));

    public static final DeferredBlock<Block> RUBBER_SAPLING = registerBlock("rubber_sapling",
            () -> new SaplingBlock(RubberTreeGrower.RUBBER_TREE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));

    // Карта для быстрого доступа: Тир -> Список (индекс = уровень изоляции)
    public static final Map<CableTier, List<DeferredBlock<CableBlock>>> ALL_CABLES = new EnumMap<>(CableTier.class);

    static {
        for (CableTier tier : CableTier.values()) {
            List<DeferredBlock<CableBlock>> tierVariants = new ArrayList<>();
            
            // Регистрируем от 0 до max_insulation
            int maxInsulation = tier.getNeedsRubber();
            for (int i = 0; i <= maxInsulation; i++) {
                String name;
                if (i == 0) {
                    name = tier.name().toLowerCase() + "_cable";
                } else {
                    name = "insulated_" + tier.name().toLowerCase() + "_cable_x" + i;
                    // Для совместимости с твоими старыми названиями (если x1), можно сделать проверку:
                    if (maxInsulation == 1 && i == 1) {
                         name = "insulated_" + tier.name().toLowerCase() + "_cable";
                    }
                }

                final int currentInsulation = i;
                DeferredBlock<CableBlock> block = BLOCKS.register(name, 
                    () -> new CableBlock(tier, currentInsulation, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
                
                tierVariants.add(block);
                ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
            }
            ALL_CABLES.put(tier, tierVariants);
        }
    }

    public static final DeferredBlock<Block> IRON_FURNACE = registerBlock("iron_furnace",
            () -> new IronFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> GENERATOR = registerBlock("generator",
            () -> new Generator(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> CRUSHER = registerBlock("crusher",
            () -> new Crusher(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> RECYCLER = registerBlock("recycler",
            () -> new Recycler(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> EXTRACTOR = registerBlock("extractor",
            () -> new Extractor(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> COMPRESSOR = registerBlock("compressor",
            () -> new Compressor(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> ELECTRIC_HEAT_GENERATOR = registerBlock("electric_heat_generator",
            () -> new ElectricHeatGenerator(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> SOLID_FUEL_HEAT_GENERATOR = registerBlock("solid_fuel_heat_generator",
            () -> new SolidFuelHeatGenerator(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> LIQUID_HEAT_GENERATOR = registerBlock("liquid_heat_generator",
            () -> new LiquidHeatGenerator(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> LIQUID_HEAT_EXCHANGER = registerBlock("liquid_heat_exchanger",
            () -> new LiquidHeatExchanger(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> INDUCTION_FURNACE = registerBlock("induction_furnace",
            () -> new InductionFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> METALFORMER = registerBlock("metalformer",
            () -> new Metalformer(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> CANNER = registerBlock("canner",
            () -> new Canner(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> ELECTRIC_FURNACE = registerBlock("electric_furnace",
            () -> new ElectricFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> URANIUM_ORE = registerBlock("uranium_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = registerBlock("deepslate_uranium_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)));

    public static final DeferredBlock<Block> TIN_ORE = registerBlock("tin_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = registerBlock("deepslate_tin_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final DeferredBlock<Block> COPPER_ORE = registerBlock("copper_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_COPPER_ORE = registerBlock("deepslate_copper_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)));

    public static final DeferredBlock<Block> LEAD_ORE = registerBlock("lead_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = registerBlock("deepslate_lead_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final DeferredBlock<Block> TIN_BLOCK = registerBlock("tin_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> LEAD_BLOCK = registerBlock("lead_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> BRONZE_BLOCK = registerBlock("bronze_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> MACHINE_CASING = registerBlock("machine_casing",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final Map<mopk.tmmod.block_func.Accumulators.AccumulatorTier, List<DeferredBlock<AccumulatorBlock>>> ALL_ACCUMULATORS = new EnumMap<>(mopk.tmmod.block_func.Accumulators.AccumulatorTier.class);
    public static final Map<mopk.tmmod.block_func.Transformers.TransformerTier, DeferredBlock<TransformerBlock>> ALL_TRANSFORMERS = new EnumMap<>(mopk.tmmod.block_func.Transformers.TransformerTier.class);

    static {
        for (mopk.tmmod.block_func.Accumulators.AccumulatorTier tier : mopk.tmmod.block_func.Accumulators.AccumulatorTier.values()) {
            List<DeferredBlock<AccumulatorBlock>> variants = new ArrayList<>();
            
            // Обычный
            String baseName = tier.getName();
            DeferredBlock<AccumulatorBlock> baseBlock = registerBlock(baseName, 
                () -> new AccumulatorBlock(tier, false, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
            variants.add(baseBlock);
            
            // Charge Pad
            String chargePadName = baseName + "_charge_pad";
            DeferredBlock<AccumulatorBlock> chargePadBlock = registerBlock(chargePadName, 
                () -> new AccumulatorBlock(tier, true, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
            variants.add(chargePadBlock);
            
            ALL_ACCUMULATORS.put(tier, variants);
        }

        for (mopk.tmmod.block_func.Transformers.TransformerTier tier : mopk.tmmod.block_func.Transformers.TransformerTier.values()) {
            DeferredBlock<TransformerBlock> block = registerBlock(tier.getName(), 
                () -> new TransformerBlock(tier, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
            ALL_TRANSFORMERS.put(tier, block);
        }
    }
    
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
