package mopk.tmmod.block_func.ElectricFurnace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;


public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/electric_furnace/electric_furnace_bg.png");
    private static final ResourceLocation SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation BAR_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");
    private static final ResourceLocation ARROW_STATIC =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/electric_furnace/arrow_static_texture.png");
    private static final ResourceLocation ARROW =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/electric_furnace/arrow_texture.png");
    private static final ResourceLocation MODULES_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/electric_furnace/electric_furnace_modules_bg.png");
    private static final ResourceLocation ENERGY_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/electric_furnace/energy_slot.png");
    private static final ResourceLocation MODULES_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/electric_furnace/modules_slot.png");
    private static final ResourceLocation REDSTONE_CHARGE_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/redstone_charge_slot.png");

    int RTWSlot = 18;
    int RTHSlot = 18;

    int RTWBG = 176;
    int RTHBG = 166;

    int RTWModulesBG = 26;
    int RTHModulesBG = 80;

    int RTWArrow = 30;
    int RTHArrow = 17;

    int barHeight = 62;
    int barWidth = 16;

    int barBGHeight = barHeight + 2;
    int barBGWidth = barWidth + 2;

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (energy * barHeight / maxEnergy) : 0;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int energyBarX = x  + 150;
        int energyBarY = y + 10;

        int energyBarBGX = energyBarX  - 1;
        int energyBarBGY = energyBarY - 1;

        guiGraphics.blit(BAR_BG, energyBarBGX, energyBarBGY, 0, 0, barBGWidth, barBGHeight, barBGWidth, barBGHeight);
        guiGraphics.fill(energyBarX, energyBarY + (barHeight - scaledEnergy), energyBarX + barWidth, energyBarY + barHeight, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, energyBarX , energyBarY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        if (mouseX >= energyBarX && mouseX < energyBarX + barWidth && mouseY >= energyBarY && mouseY < energyBarY + barHeight) {
            Component text = Component.literal(energy + " / " + maxEnergy);
            guiGraphics.renderComponentTooltip(this.font, List.of(text), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        int progressBar = (maxProgress != 0 && progress != 0) ? (progress * RTWArrow / maxProgress) : 0;

        guiGraphics.blit(BG, x, y, 0, 0, RTWBG, RTHBG, imageWidth, imageHeight);
        guiGraphics.blit(MODULES_BG, x + RTWBG, y, 0, 0, RTWModulesBG, RTHModulesBG, RTWModulesBG, RTHModulesBG);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (i == 2) {
                guiGraphics.blit(REDSTONE_CHARGE_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            } else if (i >= 3 && i <= 6) {
                guiGraphics.blit(MODULES_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            }
            guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
        }

        guiGraphics.blit(ARROW_STATIC, x + 77, y + 34, 0, 0, RTWArrow, RTHArrow, RTWArrow, RTHArrow);
        guiGraphics.blit(ARROW, x + 77, y + 34, 0, 0, progressBar, RTHArrow, RTWArrow, RTHArrow);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyBar(guiGraphics, mouseX, mouseY);
    }
}

