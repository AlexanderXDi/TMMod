package mopk.tmmod.block_func.Transformers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

public class TransformerScreen extends AbstractContainerScreen<TransformerMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/accumulator/accumulator_bg.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");

    public TransformerScreen(TransformerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.tmmod.transformer.button_mode_text"), button -> {
            PacketDistributor.sendToServer(new TransformerModePacket(menu.getBlockEntity().getBlockPos()));
        }).bounds(this.leftPos + 8, this.topPos + 50, 80, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, imageWidth, imageHeight);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
        }

        String modeKey = "gui.tmmod.transformer.mode." + menu.getMode().name().toLowerCase();
        Component modeText = Component.translatable("gui.tmmod.transformer.mode_text", Component.translatable(modeKey));
        guiGraphics.drawString(this.font, modeText, x + 8, y + 20, 0x404040, false);
        
        Component euInText = Component.translatable("gui.tmmod.transformer.eu_in", menu.getEuIn());
        Component euOutText = Component.translatable("gui.tmmod.transformer.eu_out", menu.getEuOut());
        
        guiGraphics.drawString(this.font, euInText, x + 8, y + 30, 0x404040, false);
        guiGraphics.drawString(this.font, euOutText, x + 8, y + 40, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
