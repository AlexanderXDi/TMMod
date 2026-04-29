package mopk.tmmod.block_func.SolidFuelHeatGenerator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class SolidFuelHeatGeneratorScreen extends AbstractContainerScreen<SolidFuelHeatGeneratorMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/generator_bg.png");
    private static final ResourceLocation SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation FUEL_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/fuel_slot.png");
    private static final ResourceLocation FIRE_STATIC =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/fire_static_texture.png");
    private static final ResourceLocation FIRE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/fire_texture.png");
    private static final ResourceLocation BAR_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");

    public SolidFuelHeatGeneratorScreen(SolidFuelHeatGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private void renderHeatBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int heat = this.menu.getHeat();
        int maxHeat = 1000;
        int barHeight = 62;
        int barWidth = 16;
        int scaledHeat = (heat != 0) ? (heat * barHeight / maxHeat) : 0;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int heatBarX = x + 12;
        int heatBarY = y + 10;

        guiGraphics.blit(BAR_BG, heatBarX - 1, heatBarY - 1, 0, 0, barWidth + 2, barHeight + 2, barWidth + 2, barHeight + 2);
        guiGraphics.fill(heatBarX, heatBarY + (barHeight - scaledHeat), heatBarX + barWidth, heatBarY + barHeight, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, heatBarX, heatBarY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (mouseX >= heatBarX && mouseX < heatBarX + barWidth && mouseY >= heatBarY && mouseY < heatBarY + barHeight) {
            Component text = Component.literal(heat + " / " + maxHeat + " HU");
            guiGraphics.renderComponentTooltip(this.font, List.of(text), mouseX, mouseY);
        }
    }

    private void renderFire(GuiGraphics guiGraphics) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int burnTime = menu.getBurnTime();
        int maxBurnTime = menu.getMaxBurnTime();
        
        guiGraphics.blit(FIRE_STATIC, x + 56, y + 54, 0, 0, 14, 14, 14, 14);
        if (burnTime > 0 && maxBurnTime > 0) {
            int scaledBurn = burnTime * 13 / maxBurnTime;
            guiGraphics.blit(FIRE, x + 56, y + 54 + 12 - scaledBurn, 0, 12 - scaledBurn, 14, scaledBurn + 1, 14, 14);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, imageWidth, imageHeight);

        for (int i = 0; i < 2; i++) {
            Slot slot = menu.slots.get(i);
            if (i == 0) {
                guiGraphics.blit(FUEL_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
            } else {
                guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
            }
        }
        
        renderFire(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderHeatBar(guiGraphics, mouseX, mouseY);
    }
}
