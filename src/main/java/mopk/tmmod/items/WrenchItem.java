package mopk.tmmod.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction clickedFace = context.getClickedFace();

        if (state.hasProperty(BlockStateProperties.FACING) || state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            DirectionProperty facingProperty = state.hasProperty(BlockStateProperties.FACING) ? 
                    BlockStateProperties.FACING : BlockStateProperties.HORIZONTAL_FACING;
            
            Direction currentFacing = state.getValue(facingProperty);

            if (!level.isClientSide) {
                Direction newFacing = clickedFace.getOpposite();
                
                // Проверяем, допустимо ли направление
                boolean canFaceRequestedDirection = facingProperty.getPossibleValues().contains(newFacing);
                
                // СПЕЦИАЛЬНОЕ ПРАВИЛО (Вариант 1): 
                // Если у блока есть свойство LIT и при этом используется 6-сторонний FACING, 
                // считаем его горизонтальным (запрещаем UP/DOWN).
                if (facingProperty == BlockStateProperties.FACING && state.hasProperty(BlockStateProperties.LIT)) {
                    if (newFacing == Direction.UP || newFacing == Direction.DOWN) {
                        canFaceRequestedDirection = false;
                    }
                }

                // Выпадает если:
                // 1. Нажали на сторону, противоположную лицевой ("тыл")
                // 2. Нажали на сторону, в которую блок НЕ МОЖЕТ смотреть
                if (clickedFace == currentFacing.getOpposite() || !canFaceRequestedDirection) {
                    level.destroyBlock(pos, false);
                    ItemStack drop = new ItemStack(state.getBlock());
                    ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    level.addFreshEntity(entity);
                } else {
                    // Поворот блока
                    level.setBlock(pos, state.setValue(facingProperty, newFacing), 3);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
