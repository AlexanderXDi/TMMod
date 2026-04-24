package mopk.tmmod.items;

import mopk.tmmod.registration.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class EjectorUpgrade extends Item {
    public EjectorUpgrade(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        Direction side = context.getClickedFace();

        stack.set(ModDataComponents.EJECTOR_DIRECTION.get(), side.get3DDataValue());
        stack.set(ModDataComponents.EJECTOR_ACTIVE.get(), true);

        if (context.getLevel().isClientSide) {
            player.displayClientMessage(Component.translatable("message.tmmod.ejector_side_set", side.getName()), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (stack.getOrDefault(ModDataComponents.EJECTOR_ACTIVE.get(), false)) {
                stack.remove(ModDataComponents.EJECTOR_DIRECTION.get());
                stack.set(ModDataComponents.EJECTOR_ACTIVE.get(), false);
                if (level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.tmmod.ejector_reset"), true);
                }
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (stack.getOrDefault(ModDataComponents.EJECTOR_ACTIVE.get(), false)) {
            int dirValue = stack.getOrDefault(ModDataComponents.EJECTOR_DIRECTION.get(), 0);
            Direction dir = Direction.from3DDataValue(dirValue);
            tooltip.add(Component.translatable("tooltip.tmmod.ejector_side", dir.getName()).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.tmmod.ejector_not_set").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.tmmod.ejector_desc").withStyle(ChatFormatting.DARK_GRAY));
    }
}
