package mopk.tmmod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RubberLogBlock extends RotatedPillarBlock {
    public static final EnumProperty<ResinState> RESIN_STATE = EnumProperty.create("resin_state", ResinState.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public enum ResinState implements StringRepresentable {
        NONE("none"),
        EMPTY("empty"),
        FULL("full");

        private final String name;
        ResinState(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public RubberLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(RESIN_STATE, ResinState.NONE)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        // Если отверстие было пустым — наполняем его
        if (state.getValue(RESIN_STATE) == ResinState.EMPTY) {
            level.setBlock(pos, state.setValue(RESIN_STATE, ResinState.FULL), 3);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RESIN_STATE, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // При ручной установке ставится обычное бревно без отверстий
        return super.getStateForPlacement(context)
                .setValue(RESIN_STATE, ResinState.NONE)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
