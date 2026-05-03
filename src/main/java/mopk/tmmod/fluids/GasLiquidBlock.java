package mopk.tmmod.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class GasLiquidBlock extends EffectLiquidBlock {
    public GasLiquidBlock(FlowingFluid fluid, Properties properties, Supplier<MobEffectInstance> effect, boolean setsOnFire) {
        super(fluid, properties, effect, setsOnFire);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 5);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) == 0) {
            if (pos.getY() >= level.getMaxBuildHeight()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }

            BlockPos upPos = pos.above();
            BlockState upState = level.getBlockState(upPos);

            if (upState.isAir() || upState.canBeReplaced()) {
                level.setBlock(upPos, state, 3);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                level.scheduleTick(pos, this, 20);
            }
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
