package mopk.tmmod.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class TransformingLiquidBlock extends EffectLiquidBlock {
    private final Supplier<Block> targetBlock;
    private final int transformChance;

    public TransformingLiquidBlock(FlowingFluid fluid, Properties properties, Supplier<MobEffectInstance> effect, boolean setsOnFire, Supplier<Block> targetBlock, int transformChance) {
        super(fluid, properties, effect, setsOnFire);
        this.targetBlock = targetBlock;
        this.transformChance = transformChance;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (this.targetBlock != null && random.nextInt(this.transformChance) == 0) {
            Block target = this.targetBlock.get();
            if (target instanceof net.minecraft.world.level.block.LiquidBlock) {
                level.setBlockAndUpdate(pos, target.defaultBlockState().setValue(LEVEL, state.getValue(LEVEL)));
            } else if (state.getValue(LEVEL) == 0) {
                level.setBlockAndUpdate(pos, target.defaultBlockState());
            }
        }
    }
}
