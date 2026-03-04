package mopk.tmmod.blocks;

import mopk.tmmod.Tmmod;
import mopk.tmmod.blocks.singleblocks.Generator;
import mopk.tmmod.blocks.singleblocks.IronFurnace;
import mopk.tmmod.items.ModItems.*;
import static mopk.tmmod.items.ModItems.ITEMS;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Tmmod.MODID);

    public static final DeferredBlock<Block> IRON_FURNACE = registerBlock("iron_furnace",
            () -> new IronFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)));

    public static final DeferredBlock<Block> GENERATOR = registerBlock("generator",
            () -> new Generator(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
