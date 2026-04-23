package mopk.tmmod.block_func.InductionFurnace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class InductionFurnaceScreen extends AbstractContainerScreen<InductionFurnaceMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/induction_furnace_bg.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation INPUT_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/input_2_slot.png");
    private static final ResourceLocation OUTPUT_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/output_2_slot.png");
    private static final ResourceLocation ENERGY_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/energy_slot.png");
    private static final ResourceLocation MODULES_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/crusher/modules_slot.png");
    private static final ResourceLocation BAR_BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");
    private static final ResourceLocation ARROW_STATIC = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/arrow_static_texture.png");
    private static final ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/induction_furnace/arrow_texture.png");

    private final int barHeight = 62;
    private final int barWidth = 16;

    public InductionFurnaceScreen(InductionFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    private void renderBars(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Energy Bar
        int energy = this.menu.getData(0);
        int maxEnergy = this.menu.getData(1);
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (energy * barHeight / maxEnergy) : 0;

        int energyX = x + 152;
        int energyY = y + 10;
        guiGraphics.blit(BAR_BG, energyX - 1, energyY - 1, 0, 0, barWidth + 2, barHeight + 2, barWidth + 2, barHeight + 2);
        guiGraphics.fill(energyX, energyY + (barHeight - scaledEnergy), energyX + barWidth, energyY + barHeight, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, energyX, energyY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // Heat Flow Bar
        int flow = this.menu.getData(4);
        int scaledFlow = Math.min(barHeight, flow * barHeight / 100); // 100 hU/t = full

        int heatX = x + 8;
        int heatY = y + 10;
        guiGraphics.blit(BAR_BG, heatX - 1, heatY - 1, 0, 0, barWidth + 2, barHeight + 2, barWidth + 2, barHeight + 2);
        guiGraphics.fill(heatX, heatY + (barHeight - scaledFlow), heatX + barWidth, heatY + barHeight, 0xFFFFA500); // Orange
        guiGraphics.blit(BAR_PARTITION, heatX, heatY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // Tooltips
        if (mouseX >= energyX && mouseX < energyX + barWidth && mouseY >= energyY && mouseY < energyY + barHeight) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal(energy + " / " + maxEnergy + " EU")), mouseX, mouseY);
        }
        if (mouseX >= heatX && mouseX < heatX + barWidth && mouseY >= heatY && mouseY < heatY + barHeight) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal("Heat Flow: " + (flow * 20) + " hU/s")), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, imageWidth, imageHeight);

        // Slots
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.getSlot(i);
            ResourceLocation slotTex = SLOT;
            if (i == 0 || i == 1) slotTex = INPUT_SLOT;
            else if (i == 2 || i == 3) slotTex = OUTPUT_SLOT;
            else if (i == 4) slotTex = ENERGY_SLOT;
            else if (i >= 5 && i <= 8) slotTex = MODULES_SLOT;

            guiGraphics.blit(slotTex, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
        }

        // Progress Arrow
        int progress = this.menu.getData(2);
        int maxProgress = this.menu.getData(3);
        int scaledProgress = (maxProgress != 0 && progress != 0) ? (progress * 24 / (maxProgress * 100)) : 0;

        guiGraphics.blit(ARROW_STATIC, x + 82, y + 35, 0, 0, 24, 17, 24, 17);
        if (scaledProgress > 0) {
            guiGraphics.blit(ARROW, x + 82, y + 35, 0, 0, scaledProgress, 17, 24, 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderBars(guiGraphics, mouseX, mouseY);
    }
}
