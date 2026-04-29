package mopk.tmmod.block_func.LiquidHeatExchanger;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LiquidHeatExchangerScreen extends AbstractContainerScreen<LiquidHeatExchangerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/liquid_heat_exchanger.png");

    public LiquidHeatExchangerScreen(LiquidHeatExchangerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressBars(guiGraphics, x, y);
    }

    private void renderProgressBars(GuiGraphics guiGraphics, int x, int y) {
        // 7 - Бар выработки тепловой энергии (сверху)
        // Допустим, бар находится по координатам 44, 10 и имеет длину 88
        int maxHeat = menu.getMaxHeat();
        if (maxHeat > 0) {
            int currentHeat = menu.getCurrentHeat();
            int scaledHeat = (int) (88.0 * currentHeat / 100.0); // 100 HU/t - макс
            guiGraphics.blit(TEXTURE, x + 44, y + 10, 176, 0, scaledHeat, 10);
        }

        // 4, 5 - Резервуары (простейшая отрисовка полосок)
        if (menu.getHotCapacity() > 0) {
            int hotScaled = (int) (50.0 * menu.getHotAmount() / menu.getHotCapacity());
            guiGraphics.blit(TEXTURE, x + 8, y + 54 - hotScaled, 176, 10, 16, hotScaled);
        }
        if (menu.getColdCapacity() > 0) {
            int coldScaled = (int) (50.0 * menu.getColdAmount() / menu.getColdCapacity());
            guiGraphics.blit(TEXTURE, x + 134, y + 54 - coldScaled, 176, 60, 16, coldScaled);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752);
        
        String heatText = menu.getCurrentHeat() + " / " + menu.getMaxHeat() + " HU/t";
        guiGraphics.drawString(this.font, heatText, 44, 22, 0xFF5555, false);
    }
}
