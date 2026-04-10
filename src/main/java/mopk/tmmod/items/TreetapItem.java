package mopk.tmmod.items;

import mopk.tmmod.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mopk.tmmod.blocks.RubberLogBlock;
import mopk.tmmod.registration.ModItems;
import net.minecraft.world.entity.item.ItemEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;

public class TreetapItem extends Item {
    public TreetapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (state.getBlock() instanceof RubberLogBlock) {
            if (!level.isClientSide) {
                // Всегда тратим прочность при клике по бревну гивеи
                if (player != null) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

                // Проверяем наличие латекса и правильную сторону клика
                RubberLogBlock.ResinState resinState = state.getValue(RubberLogBlock.RESIN_STATE);
                if (resinState == RubberLogBlock.ResinState.FULL && context.getClickedFace() == state.getValue(RubberLogBlock.FACING)) {
                    // Переключаем в состояние EMPTY
                    level.setBlock(pos, state.setValue(RubberLogBlock.RESIN_STATE, RubberLogBlock.ResinState.EMPTY), 3);

                    // Расчет таймера: 20 секунд = 400 тиков. 20% от 400 = 80 тиков.
                    int delay = 320 + level.random.nextInt(161);
                    level.scheduleTick(pos, state.getBlock(), delay);

                    // Выпадение предмета латекса (sticky_resin)
                    ItemStack resinStack = new ItemStack(ModItems.STICKY_RESIN.get());
                    ItemEntity itemEntity = new ItemEntity(level, 
                        pos.getX() + 0.5 + context.getClickedFace().getStepX() * 0.4, 
                        pos.getY() + 0.5 + context.getClickedFace().getStepY() * 0.4, 
                        pos.getZ() + 0.5 + context.getClickedFace().getStepZ() * 0.4, 
                        resinStack);
                    level.addFreshEntity(itemEntity);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
