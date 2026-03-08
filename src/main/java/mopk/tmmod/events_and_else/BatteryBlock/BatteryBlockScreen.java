package mopk.tmmod.events_and_else.BatteryBlock;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;


public class BatteryBlockScreen extends AbstractContainerScreen<BatteryBlockMenu> {
    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/battery_block/battery_block_bg.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");

    public BatteryBlockScreen(BatteryBlockMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Mode"), button -> {
            PacketDistributor.sendToServer(new BatteryBlockModePacket(menu.getBlockEntity().getBlockPos()));
        }).bounds(this.leftPos + 60, this.topPos + 50, 56, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int RTW_BG = 176;
        int RTH_BG = 166;

        int RTW_SLOT = 18;
        int RTH_SLOT = 18;

        guiGraphics.blit(BG_TEXTURE, x, y, 0, 0, RTW_BG, RTH_BG, imageWidth, imageHeight);

        if (maxEnergy > 0) {
            int height = (int) (50.0f * ((float) energy / maxEnergy));
            guiGraphics.fill(this.leftPos + 10, this.topPos + 60 - height, this.leftPos + 20, this.topPos + 60, 0xFFFF0000);
        }

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, "Energy: " + menu.getEnergy() + " / " + menu.getMaxEnergy(), this.leftPos + 30, this.topPos + 20, 0x404040, false);
        guiGraphics.drawString(this.font, "Mode: " + menu.getMode().name(), this.leftPos + 30, this.topPos + 35, 0x404040, false);
    }
}

