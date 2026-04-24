package mopk.tmmod.block_func.InductionFurnace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;


public class InductionFurnaceScreen extends AbstractContainerScreen<InductionFurnaceMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/induction_furnace_bg.png");
    private static final ResourceLocation SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation INPUT_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/input_slot_bg.png");
    private static final ResourceLocation OUTPUT_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/output_slot_bg.png");
    private static final ResourceLocation BAR_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");
    private static final ResourceLocation ARROW_STATIC =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/arrow_static_texture.png");
    private static final ResourceLocation ARROW =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/arrow_texture.png");
    private static final ResourceLocation MODULES_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/crusher/crusher_modules_bg.png");
    private static final ResourceLocation ENERGY_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/crusher/energy_slot.png");
    private static final ResourceLocation MODULES_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/crusher/modules_slot.png");
    private static final ResourceLocation HEAT_ICON =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/heat_icon.png");

    int RTWBG = 176;
    int RTHBG = 166;

    int RTWSlot = 18;
    int RTHSlot = 18;

    int RTWModulesBG = 26;
    int RTHModulesBG = 80;

    int RTWArrow = 30;
    int RTHArrow = 17;

    int barHeight = 62;
    int barWidth = 16;

    int barBGHeight = barHeight + 2;
    int barBGWidth = barWidth + 12; // Extra space for heat icon next to energy

    public InductionFurnaceScreen(InductionFurnaceMenu menu, Inventory inventory, Component title) {
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

        guiGraphics.blit(BAR_BG, energyBarBGX, energyBarBGY, 0, 0, 18, barBGHeight, 18, barBGHeight);
        guiGraphics.fill(energyBarX, energyBarY + (barHeight - scaledEnergy), energyBarX + barWidth, energyBarY + barHeight, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, energyBarX , energyBarY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        if (mouseX >= energyBarX && mouseX < energyBarX + barWidth && mouseY >= energyBarY && mouseY < energyBarY + barHeight) {
            Component text = Component.literal(energy + " / " + maxEnergy + " EU");
            guiGraphics.renderComponentTooltip(this.font, List.of(text), mouseX, mouseY);
        }

        // Рендеринг иконки тепла и значения
        int heatFlow = this.menu.getLastHeatFlow();
        int heatX = x + 130;
        int heatY = y + 35;
        guiGraphics.blit(HEAT_ICON, heatX, heatY, 0, 0, 16, 16, 16, 16);
        if (mouseX >= heatX && mouseX < heatX + 16 && mouseY >= heatY && mouseY < heatY + 16) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal("Heat: " + heatFlow + " hU/t")), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        int progressBar = (maxProgress != 0 && progress != 0) ? (progress * RTWArrow / (maxProgress * 100)) : 0;

        guiGraphics.blit(BG, x, y, 0, 0, RTWBG, RTHBG, imageWidth, imageHeight);
        guiGraphics.blit(MODULES_BG, x + RTWBG, y, 0, 0, RTWModulesBG, RTHModulesBG, RTWModulesBG, RTHModulesBG);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (i == 0 || i == 1) {
                guiGraphics.blit(INPUT_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            } else if (i == 2 || i == 3) {
                guiGraphics.blit(OUTPUT_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            } else if (i == 4) {
                guiGraphics.blit(ENERGY_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            } else if (i >= 5 && i <= 8) {
                guiGraphics.blit(MODULES_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            }
            guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
        }

        guiGraphics.blit(ARROW_STATIC, x + 74, y + 34, 0, 0, RTWArrow, RTHArrow, RTWArrow, RTHArrow);
        guiGraphics.blit(ARROW, x + 74, y + 34, 0, 0, progressBar, RTHArrow, RTWArrow, RTHArrow);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyBar(guiGraphics, mouseX, mouseY);
    }
}
