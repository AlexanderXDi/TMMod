package mopk.tmmod.events_and_else.Generator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;


public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/generator/generator_bg.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int RTW_BG = 176;
        int RTH_BG = 166;

        int RTW_SLOT = 18;
        int RTH_SLOT = 18;

        guiGraphics.blit(BG_TEXTURE, x, y, 0, 0, RTW_BG, RTH_BG, imageWidth, imageHeight);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            guiGraphics.blit(SLOT_TEXTURE, x + slot.x - 1, y + slot.y - 1, 0, 0, RTW_SLOT, RTH_SLOT, RTW_SLOT, RTH_SLOT);
        }

        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        int barHeight = 64;
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (energy * barHeight / maxEnergy) : 0;

        guiGraphics.fill(x + 152, y + 10 + (barHeight - scaledEnergy), x + 165, y + 10 + barHeight, 0xFFE31B1B);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
