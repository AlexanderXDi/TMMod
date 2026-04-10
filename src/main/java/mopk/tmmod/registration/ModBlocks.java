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
import net.minecraft.world.level.block.RotatedPillarBlock;
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

    public static final DeferredBlock<Block> BATTERY_BLOCK = registerBlock("battery_block",
            () -> new BatteryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> CRUSHER = registerBlock("crusher",
            () -> new Crusher(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> METALFORMER = registerBlock("metalformer",
            () -> new Metalformer(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> ELECTRIC_FURNACE = registerBlock("electric_furnace",
            () -> new ElectricFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
