package mopk.tmmod.block_func.ElectricHeatGenerator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import java.util.List;

public class ElectricHeatGeneratorScreen extends AbstractContainerScreen<ElectricHeatGeneratorMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/crusher/crusher_bg.png"); // Используем фон от дробителя как базу
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation BAR_BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");

    public ElectricHeatGeneratorScreen(ElectricHeatGeneratorMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, 176, 166, imageWidth, imageHeight);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (i < 13) { // Отрисовываем фон только для слотов машины
                guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyBar(guiGraphics, mouseX, mouseY);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (energy * 62 / maxEnergy) : 0;
        int x = (width - imageWidth) / 2 + 12; // Полоса энергии слева
        int y = (height - imageHeight) / 2 + 10;

        guiGraphics.blit(BAR_BG, x - 1, y - 1, 0, 0, 18, 64, 18, 64);
        guiGraphics.fill(x, y + (62 - scaledEnergy), x + 16, y + 62, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, x, y, 0, 0, 16, 62, 16, 62);
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 62) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal(energy + " / " + maxEnergy + " EU")), mouseX, mouseY);
        }
    }
}
