package mopk.tmmod.blocks.singleblocks;

import mopk.tmmod.events_and_else.Generator.GeneratorBE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class Generator extends BaseEntityBlock {

    public static final MapCodec<Generator> CODEC = simpleCodec(Generator::new);

    public Generator(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBE(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);

            // Проверяем, что наш BE реализует MenuProvider (интерфейс для GUI)
            if (be instanceof MenuProvider menuProvider) {
                // Открываем меню через игрока.
                // В NeoForge рекомендуется передавать BlockPos для контекста.
                player.openMenu(menuProvider, pos);
            }
        }
        // InteractionResult.SUCCESS сообщает игре, что действие выполнено (рука качнется)
        return InteractionResult.SUCCESS;
    }
}
