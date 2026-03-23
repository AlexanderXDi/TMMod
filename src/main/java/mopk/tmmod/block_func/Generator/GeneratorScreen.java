package mopk.tmmod.block_func.Generator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;


public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/generator_bg.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation BAR_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");

    int RTW_SLOT = 18;
    int RTH_SLOT = 18;

    int RTW_BG = 176;
    int RTH_BG = 166;

    int barHeight = 62;
    int barWidth = 16;

    int barBGHeight = barHeight + 2;
    int barBGWidth = barWidth + 2;

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (energy * barHeight / maxEnergy) : 0;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int energyBarX = x  + 145;
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

        guiGraphics.blit(BG_TEXTURE, x, y, 0, 0, RTW_BG, RTH_BG, imageWidth, imageHeight);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            guiGraphics.blit(SLOT_TEXTURE, x + slot.x - 1, y + slot.y - 1, 0, 0, RTW_SLOT, RTH_SLOT, RTW_SLOT, RTH_SLOT);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyBar(guiGraphics, mouseX, mouseY);
    }
}
