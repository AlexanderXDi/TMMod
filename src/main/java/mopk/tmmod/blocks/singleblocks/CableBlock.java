package mopk.tmmod.blocks.singleblocks;

import mopk.tmmod.etc.Cables.CableBE;
import mopk.tmmod.etc.Cables.CableTier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

import org.jetbrains.annotations.Nullable;

public class CableBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE_CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH = Block.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_SOUTH = Block.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape SHAPE_EAST = Block.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_UP = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = Block.box(6, 0, 6, 10, 6, 10);

    private final CableTier tier;

    public CableBlock(CableTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return makeConnections(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level world) {
            return state.setValue(getConnectionProperty(direction), canConnectTo(world, neighborPos, direction.getOpposite()));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = SHAPE_CORE;

        // В 1.21.1 используем Shapes.or() вместо VoxelShapes.or()
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, SHAPE_UP);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, SHAPE_DOWN);
        }
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, SHAPE_NORTH);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SHAPE_SOUTH);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, SHAPE_EAST);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, SHAPE_WEST);
        }

        return shape;
    }

    public BlockState makeConnections(Level level, BlockPos pos) {
        return this.defaultBlockState()
                .setValue(NORTH, canConnectTo(level, pos.north(), Direction.SOUTH))
                .setValue(SOUTH, canConnectTo(level, pos.south(), Direction.NORTH))
                .setValue(EAST, canConnectTo(level, pos.east(), Direction.WEST))
                .setValue(WEST, canConnectTo(level, pos.west(), Direction.EAST))
                .setValue(UP, canConnectTo(level, pos.above(), Direction.DOWN))
                .setValue(DOWN, canConnectTo(level, pos.below(), Direction.UP));
    }

    private boolean canConnectTo(Level level, BlockPos neighborPos, Direction side) {
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, side) != null;
    }

    private BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public CableTier getTier() { return tier; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBE(pos, state, this.tier);
    }

}
