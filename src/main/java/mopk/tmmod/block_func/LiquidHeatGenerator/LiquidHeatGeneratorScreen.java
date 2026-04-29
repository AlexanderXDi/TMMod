package mopk.tmmod.block_func.LiquidHeatGenerator;

import com.mojang.blaze3d.systems.RenderSystem;
import mopk.tmmod.Tmmod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LiquidHeatGeneratorScreen extends AbstractContainerScreen<LiquidHeatGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Tmmod.MODID, "textures/gui/liquid_heat_generator.png");

    public LiquidHeatGeneratorScreen(LiquidHeatGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgress(guiGraphics, x, y);
        renderFluid(guiGraphics, x, y);
    }

    private void renderProgress(GuiGraphics guiGraphics, int x, int y) {
        if (menu.getBurnTime() > 0) {
            int h = (int) (14 * ((float) menu.getBurnTime() / menu.getMaxBurnTime()));
            guiGraphics.blit(TEXTURE, x + 80, y + 36 + 14 - h, 176, 14 - h, 14, h);
        }
    }

    private void renderFluid(GuiGraphics guiGraphics, int x, int y) {
        int amount = menu.getFluidAmount();
        int capacity = menu.getFluidCapacity();
        if (amount > 0) {
            int h = (int) (48 * ((float) amount / capacity));
            // Рисуем жидкость (в текстуре это будет полоска)
            guiGraphics.blit(TEXTURE, x + 7, y + 20 + 48 - h, 176, 14, 16, h);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
