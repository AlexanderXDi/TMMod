package mopk.tmmod.etc.IronFurnace;

import mopk.tmmod.etc.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class IronFurnaceBE extends AbstractFurnaceBlockEntity {
    public IronFurnaceBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.iron_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return new IronFurnaceMenu(id, player, this, this.dataAccess);
    }

    @Override
    protected int getBurnDuration(ItemStack pFuel) {
        int vanillaDuration = super.getBurnDuration(pFuel);
        if (vanillaDuration > 0) {
            return (int) (vanillaDuration * 0.1);
        }
        return vanillaDuration;
    }
}
