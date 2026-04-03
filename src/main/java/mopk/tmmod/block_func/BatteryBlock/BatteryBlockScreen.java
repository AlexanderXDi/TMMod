package mopk.tmmod.block_func.BatteryBlock;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;


public class BatteryBlockScreen extends AbstractContainerScreen<BatteryBlockMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/battery_block/battery_block_bg.png");
    private static final ResourceLocation ENERGY_SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/battery_block/energy_slot.png");
    private static final ResourceLocation SLOT =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation BAR_BG =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");

    public BatteryBlockScreen(BatteryBlockMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    int RTWBG = 176;
    int RTHBG = 166;

    int RTWSlot = 18;
    int RTHSlot = 18;

    int barHeight = 62;
    int barWidth = 16;

    int barBGHeight = barHeight + 2;
    int barBGWidth = barWidth + 2;

    @Override
    protected void init() {
        super.init();
        Component buttonModeText = Component.translatable("gui.tmmod.battery_block.button_mode_text");
        this.addRenderableWidget(Button.builder(Component.literal(buttonModeText.getString()), button -> {
            PacketDistributor.sendToServer(new BatteryBlockModePacket(menu.getBlockEntity().getBlockPos()));
        }).bounds(this.leftPos + 8, this.topPos + 50, 56, 20).build());
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
        Component energyText = Component.translatable("gui.tmmod.battery_block.energy_text");
        String modeKey = "gui.tmmod.battery_block.mode." + menu.getMode().name().toLowerCase();
        Component modeText = Component.translatable("gui.tmmod.battery_block.mode_text", Component.translatable(modeKey));

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int modeFontx = x + 8;
        int modeFonty = y + 20;

        int energyFontx = x + 8;
        int energyFonty = y + 30;

        guiGraphics.blit(BG, x, y, 0, 0, RTWBG, RTHBG, imageWidth, imageHeight);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (i == 0) {
                guiGraphics.blit(ENERGY_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
                continue;
            }
            guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, RTWSlot, RTHSlot, RTWSlot, RTHSlot);
        }

        guiGraphics.drawString(this.font, energyText.getString() + menu.getEnergy() + " / " + menu.getMaxEnergy(), energyFontx, energyFonty, 0x404040, false);
        guiGraphics.drawString(this.font, modeText.getString(), modeFontx, modeFonty, 0x404040, false);

        renderEnergyBar(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
