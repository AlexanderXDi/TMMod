package mopk.tmmod.block_func.RadioisotopeHeatGenerator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class RadioisotopeHeatGeneratorScreen extends AbstractContainerScreen<RadioisotopeHeatGeneratorMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/generator_bg.png");
    private static final ResourceLocation FUEL_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/fuel_slot.png");

    public RadioisotopeHeatGeneratorScreen(RadioisotopeHeatGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, imageWidth, imageHeight);

        Slot slot = menu.slots.get(0);
        guiGraphics.blit(FUEL_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
        
        // Отображение текущей выработки тепла
        int heatOutput = menu.getHeatOutput();
        guiGraphics.drawString(this.font, Component.literal("Heat: " + heatOutput + " HU/t"), x + 8, y + 70, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
