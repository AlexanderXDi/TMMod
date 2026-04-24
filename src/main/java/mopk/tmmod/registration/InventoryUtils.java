package mopk.tmmod.registration;

import mopk.tmmod.items.EjectorUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class InventoryUtils {

    /**
     * Обрабатывает логику выталкивания для механизма.
     * 
     * @param level Уровень
     * @param pos Позиция механизма
     * @param inventory Инвентарь механизма
     * @param upgradeSlots Слоты, где могут лежать улучшения (например, 3-6)
     * @param outputSlots Слоты, из которых нужно выталкивать (например, 1 или 2,3)
     * @param tickCounter Счетчик тиков (обычно progress или отдельный)
     */
    public static void handleEjection(Level level, BlockPos pos, ItemStackHandler inventory, int[] upgradeSlots, int[] outputSlots, long tickCounter) {
        if (tickCounter % 4 != 0) return;

        for (int upgradeSlot : upgradeSlots) {
            ItemStack upgradeStack = inventory.getStackInSlot(upgradeSlot);
            if (!upgradeStack.isEmpty() && upgradeStack.getItem() instanceof EjectorUpgrade) {
                if (!upgradeStack.getOrDefault(ModDataComponents.EJECTOR_ACTIVE.get(), false)) continue;

                int dirValue = upgradeStack.getOrDefault(ModDataComponents.EJECTOR_DIRECTION.get(), 0);
                Direction targetDir = Direction.from3DDataValue(dirValue);
                BlockPos targetPos = pos.relative(targetDir);

                IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, targetDir.getOpposite());
                if (targetHandler != null) {
                    for (int outputSlot : outputSlots) {
                        ItemStack outputStack = inventory.getStackInSlot(outputSlot);
                        if (!outputStack.isEmpty()) {
                            ItemStack toPush = outputStack.copy();
                            toPush.setCount(1);

                            ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetHandler, toPush, false);
                            if (remainder.isEmpty()) {
                                inventory.extractItem(outputSlot, 1, false);
                                return; // Вытолкнули 1 предмет, выходим
                            }
                        }
                    }
                }
            }
        }
    }
}
