package mopk.tmmod.blocks.events_and_else.IronFurnace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class IronFurnaceScreen extends AbstractContainerScreen<IronFurnaceMenu> {
    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/iron_furnace_gui.png");
    private static final ResourceLocation FIRE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/fire_texture.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/arrow_texture.png");
    private static final ResourceLocation ARROW_STATIC_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/arrow_static_texture.png");
    private static final ResourceLocation FIRE_STATIC_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/fire_static_texture.png");

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

        int RTW_ARROW = 24;
        int RTH_ARROW = 17;

        guiGraphics.blit(BG_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, RTW_BG, RTH_BG);


        guiGraphics.blit(FIRE_STATIC_TEXTURE, x + 56, y + 35, 176, 166, 14, 14, RTW_FIRE, RTH_FIRE);
        if (menu.isLit()) {
            int k = menu.getLitProgress();
            guiGraphics.blit(FIRE_TEXTURE, x + 56, y + 36 + 12 - k, 176, 12 - k, 14, k + 1, RTW_FIRE, RTH_FIRE);
        }

        // 3. Отрисовка стрелочки (прогресс плавки)
        int l = menu.getBurnProgress();

        guiGraphics.blit(ARROW_STATIC_TEXTURE, x + 79, y + 34, 0, 0, 24, 17, RTW_ARROW, RTH_ARROW);
        guiGraphics.blit(ARROW_TEXTURE, x + 79, y + 34, 0, 0, l + 1, 17, RTW_ARROW, RTH_ARROW);


    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
