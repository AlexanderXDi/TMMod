package mopk.tmmod.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class GasFluid extends BaseFlowingFluid {
    protected GasFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSpreadTo(BlockGetter world, BlockPos pos, BlockState state, Direction direction, BlockPos spreadPos, BlockState spreadState, FluidState fluidState, Fluid fluid) {
        return false;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 8;
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean canSpreadTo(BlockGetter world, BlockPos pos, BlockState state, Direction direction, BlockPos spreadPos, BlockState spreadState, FluidState fluidState, Fluid fluid) {
            return false;
        }

        @Override
        protected int getDropOff(LevelReader level) {
            return 8;
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean canSpreadTo(BlockGetter world, BlockPos pos, BlockState state, Direction direction, BlockPos spreadPos, BlockState spreadState, FluidState fluidState, Fluid fluid) {
            return false;
        }

        @Override
        protected int getDropOff(LevelReader level) {
            return 8;
        }
    }
}
