package mopk.tmmod.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class GasLiquidBlock extends EffectLiquidBlock {
    public GasLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties, null, false);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            // Запланировать подъем через 5 тиков (0.25 сек)
            level.scheduleTick(pos, this, 5);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Газ летит вверх только если это "источник" (полный блок)
        if (state.getValue(LEVEL) == 0) {
            if (pos.getY() >= level.getMaxBuildHeight()) {
                level.removeBlock(pos, false);
                return;
            }

            BlockPos upPos = pos.above();
            BlockState upState = level.getBlockState(upPos);

            if (upState.isAir() || upState.canBeReplaced()) {
                // Перемещаем блок выше
                level.setBlock(upPos, state, 3);
                level.removeBlock(pos, false);
            } else {
                // Если сверху препятствие, газ остается на месте, 
                // но можно добавить логику растекания. Пока просто ждем.
                level.scheduleTick(pos, this, 20);
            }
        } else {
            // Если это не источник (растекающийся газ), он быстро исчезает
            level.removeBlock(pos, false);
        }
    }
}
