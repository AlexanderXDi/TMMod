package mopk.tmmod.blocks.singleblocks;

import static mopk.tmmod.blocks.events_and_else.ModBlockEntities.IRON_FURNACE_BE;

import mopk.tmmod.blocks.events_and_else.IronFurnace.IronFurnaceBE;
import mopk.tmmod.blocks.events_and_else.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;


public class IronFurnace extends FurnaceBlock {
    public IronFurnace(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.LIT, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IRON_FURNACE_BE.get().create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            // Открываем меню через сущность блока
            this.openContainer(level, pos, player);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof IronFurnaceBE) { // Проверяем класс вашей сущности
            player.openMenu((MenuProvider)blockentity);
            // Дополнительная статистика (необязательно)
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, ModBlockEntities.IRON_FURNACE_BE.get());
    }
}