package mopk.tmmod.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class UniversalCapsuleItem extends Item {
    public UniversalCapsuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty()) {
                return Component.translatable("item.tmmod.liquid_capsule", fluidStack.getHoverName());
            }
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        IFluidHandlerItem fluidHandler = itemstack.getCapability(Capabilities.FluidHandler.ITEM);

        if (fluidHandler == null) {
            return InteractionResultHolder.pass(itemstack);
        }

        FluidStack currentFluid = fluidHandler.getFluidInTank(0);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player,
                currentFluid.isEmpty() ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);

        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            BlockPos blockpos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos blockpos1 = blockpos.relative(direction);

            if (!level.mayInteract(player, blockpos) || !player.mayUseItemAt(blockpos1, direction, itemstack)) {
                return InteractionResultHolder.fail(itemstack);
            }

            if (currentFluid.isEmpty()) {
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.getBlock() instanceof BucketPickup bucketpickup) {
                    ItemStack pickupResult = bucketpickup.pickupBlock(player, level, blockpos, blockstate);
                    if (!pickupResult.isEmpty()) {
                        Fluid fluid = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(pickupResult).map(FluidStack::getFluid).orElse(Fluids.EMPTY);
                        if (fluid != Fluids.EMPTY) {
                            FluidStack newFluid = new FluidStack(fluid, 1000);
                            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
                            SoundEvent soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
                            player.playSound(soundevent, 1.0F, 1.0F);
                            level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);

                            ItemStack resultStack = itemstack.copy();
                            resultStack.setCount(1);
                            IFluidHandlerItem newHandler = resultStack.getCapability(Capabilities.FluidHandler.ITEM);
                            if (newHandler != null) {
                                newHandler.fill(newFluid, IFluidHandler.FluidAction.EXECUTE);
                                ItemStack filledStack = newHandler.getContainer();
                                return InteractionResultHolder.sidedSuccess(ItemUtils.createFilledResult(itemstack, player, filledStack), level.isClientSide());
                            }
                        }
                    }
                }
                return InteractionResultHolder.fail(itemstack);
            } else {
                net.neoforged.neoforge.fluids.FluidActionResult result = net.neoforged.neoforge.fluids.FluidUtil.tryPlaceFluid(player, level, hand, blockpos1, itemstack, currentFluid);
                if (result.isSuccess()) {
                    if (!player.isCreative()) {
                        ItemStack resultStack = itemstack.copy();
                        resultStack.shrink(1);
                        if (resultStack.isEmpty()) {
                            return InteractionResultHolder.sidedSuccess(result.getResult(), level.isClientSide());
                        } else {
                            player.getInventory().placeItemBackInInventory(result.getResult());
                            return InteractionResultHolder.sidedSuccess(resultStack, level.isClientSide());
                        }
                    }
                    return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
                }
                return InteractionResultHolder.fail(itemstack);
            }
        }
    }
}
