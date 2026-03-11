package mopk.tmmod.etc.IronFurnace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;


public class IronFurnaceScreen extends AbstractContainerScreen<IronFurnaceMenu> {
    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/iron_furnace/iron_furnace_bg.png");
    private static final ResourceLocation FIRE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/iron_furnace/fire_texture.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/iron_furnace/arrow_texture.png");
    private static final ResourceLocation ARROW_STATIC_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/iron_furnace/arrow_static_texture.png");
    private static final ResourceLocation FIRE_STATIC_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/iron_furnace/fire_static_texture.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");

    public IronFurnaceScreen(IronFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int RTW_BG = 176;
        int RTH_BG = 166;

        int RTW_FIRE = 14;
        int RTH_FIRE = 14;

        int RTW_ARROW = 30;
        int RTH_ARROW = 17;

        int RTW_SLOT = 18;
        int RTH_SLOT = 18;

        guiGraphics.blit(BG_TEXTURE, x, y, 0, 0, RTW_BG, RTH_BG, RTW_BG, RTH_BG);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            guiGraphics.blit(SLOT_TEXTURE, x + slot.x - 1, y + slot.y - 1, 0, 0, RTW_SLOT, RTH_SLOT, RTW_SLOT, RTH_SLOT);
        }

        guiGraphics.blit(FIRE_STATIC_TEXTURE, x + 57, y + 36, 176, 166, RTW_FIRE, RTH_FIRE, RTW_FIRE, RTH_FIRE);
        if (menu.isLit()) {
            int k = menu.getLitProgress();
            guiGraphics.blit(FIRE_TEXTURE, x + 57, y + 49 - k, 176, 12 - k, RTW_FIRE, k + 1, RTW_FIRE, RTH_FIRE);
        }

        int l = menu.getBurnProgress();

        guiGraphics.blit(ARROW_STATIC_TEXTURE, x + 79, y + 34, 0, 0, RTW_ARROW, RTH_ARROW, RTW_ARROW, RTH_ARROW);
        guiGraphics.blit(ARROW_TEXTURE, x + 79, y + 34, 0, 0, l, RTH_ARROW, RTW_ARROW, RTH_ARROW);


    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
