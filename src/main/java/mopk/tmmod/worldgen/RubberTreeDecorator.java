package mopk.tmmod.worldgen;

import com.mojang.serialization.MapCodec;
import mopk.tmmod.blocks.RubberLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.List;

import mopk.tmmod.registration.ModBlocks;

public class RubberTreeDecorator extends TreeDecorator {
    public static final MapCodec<RubberTreeDecorator> CODEC = MapCodec.unit(() -> RubberTreeDecorator.INSTANCE);
    public static final RubberTreeDecorator INSTANCE = new RubberTreeDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.RUBBER_TREE_DECORATOR.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        List<BlockPos> logs = context.logs();

        if (logs.isEmpty()) return;

        for (BlockPos pos : logs) {
            // Шанс 25% для каждого блока ствола
            if (random.nextFloat() < 0.25f) {
                Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                context.setBlock(pos, ModBlocks.RUBBER_LOG.get().defaultBlockState()
                        .setValue(RubberLogBlock.RESIN_STATE, RubberLogBlock.ResinState.FULL)
                        .setValue(RubberLogBlock.FACING, facing));
            }
        }
    }
}
