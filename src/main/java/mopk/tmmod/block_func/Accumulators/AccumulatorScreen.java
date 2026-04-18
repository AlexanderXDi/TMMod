package mopk.tmmod.block_func.Accumulators;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class AccumulatorScreen extends AbstractContainerScreen<AccumulatorMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/accumulator_bg.png");
    private static final ResourceLocation ENERGY_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/energy_slot.png");
    private static final ResourceLocation HELMET_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/helmet_slot.png");
    private static final ResourceLocation CHESTPLATE_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/chestplate_slot.png");
    private static final ResourceLocation LEGGINGS_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/leggings_slot.png");
    private static final ResourceLocation BOOTS_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/boots_slot.png");
    private static final ResourceLocation REDSTONE_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/redstone_slot.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation BAR_BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");

    private static final int BAR_HEIGHT = 62;
    private static final int BAR_WIDTH = 16;

    public AccumulatorScreen(AccumulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, imageWidth, imageHeight);

        int machineSlots = menu.getBlockEntity().isChargePad() ? 2 : 6;

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            ResourceLocation texture;
            
            if (i < machineSlots) {
                if (i == 0) texture = ENERGY_SLOT;
                else if (i == machineSlots - 1) texture = REDSTONE_SLOT; // Last machine slot is redstone
                else if (i == 1) texture = HELMET_SLOT;
                else if (i == 2) texture = CHESTPLATE_SLOT;
                else if (i == 3) texture = LEGGINGS_SLOT;
                else texture = BOOTS_SLOT;
            } else {
                texture = SLOT;
            }
            
            guiGraphics.blit(texture, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
        }

        renderEnergyBar(guiGraphics, x, y, mouseX, mouseY);
        
        Component energyText = Component.translatable("gui.tmmod.accumulator.energy_text");
        guiGraphics.drawString(this.font, energyText.getString() + menu.getEnergy() + " / " + menu.getMaxEnergy() + " EU", x + 8, y + 20, 0x404040, false);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (int)((long)energy * BAR_HEIGHT / maxEnergy) : 0;

        int energyBarX = x + 145;
        int energyBarY = y + 10;

        guiGraphics.blit(BAR_BG, energyBarX - 1, energyBarY - 1, 0, 0, BAR_WIDTH + 2, BAR_HEIGHT + 2, BAR_WIDTH + 2, BAR_HEIGHT + 2);
        guiGraphics.fill(energyBarX, energyBarY + (BAR_HEIGHT - scaledEnergy), energyBarX + BAR_WIDTH, energyBarY + BAR_HEIGHT, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, energyBarX, energyBarY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        if (mouseX >= energyBarX && mouseX < energyBarX + BAR_WIDTH && mouseY >= energyBarY && mouseY < energyBarY + BAR_HEIGHT) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal(energy + " / " + maxEnergy + " EU")), mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
