package mopk.tmmod.blocks;

import mopk.tmmod.block_func.Accumulators.AccumulatorBE;
import mopk.tmmod.block_func.Accumulators.AccumulatorTier;
import mopk.tmmod.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import mopk.tmmod.registration.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AccumulatorBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    
    private static final VoxelShape SHAPE_PAD = Block.box(0, 0, 0, 16, 15, 16);
    
    private final AccumulatorTier tier;
    private final boolean isChargePad;

    public AccumulatorBlock(AccumulatorTier tier, boolean isChargePad, Properties properties) {
        super(properties);
        this.tier = tier;
        this.isChargePad = isChargePad;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isChargePad ? SHAPE_PAD : Shapes.block();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (isChargePad && level.isClientSide) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, 
                    pos.getX() + level.random.nextDouble(), 
                    pos.getY() + 0.95, 
                    pos.getZ() + level.random.nextDouble(), 
                    0, 0.05, 0);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    public AccumulatorTier getTier() {
        return tier;
    }

    public boolean isChargePad() {
        return isChargePad;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AccumulatorBE(pos, state, tier, isChargePad);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AccumulatorBE accumulatorBE) {
                player.openMenu(accumulatorBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof AccumulatorBE accumulatorBE) accumulatorBE.tick(lvl, pos, st);
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && random.nextDouble() < 0.5D) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                    ModSounds.ACCUMULATOR_HUM.get(),
                    SoundSource.BLOCKS,
                    100F, 1.0F, false);
        }
    }
}
