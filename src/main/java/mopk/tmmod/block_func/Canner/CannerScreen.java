/*
package mopk.tmmod.block_func.Canner;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CannerScreen extends AbstractContainerScreen<CannerMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/metalformer/metalformer_bg.png"); // Reusing BG
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/slot.png");
    private static final ResourceLocation BAR_BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_bg.png");
    private static final ResourceLocation BAR_PARTITION = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/bar_partition.png");
    private static final ResourceLocation ARROW_STATIC = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/metalformer/arrow_static_texture.png");
    private static final ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/metalformer/arrow_texture.png");
    private static final ResourceLocation MODULES_BG = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/metalformer/metalformer_modules_bg.png");
    private static final ResourceLocation ENERGY_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/metalformer/energy_slot.png");
    private static final ResourceLocation MODULES_SLOT = ResourceLocation.fromNamespaceAndPath("tmmod", "textures/gui/metalformer/modules_slot.png");

    private final int barHeight = 62;
    private final int barWidth = 16;

    public CannerScreen(CannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.tmmod.canner.button_mode"), button -> {
            PacketDistributor.sendToServer(new CannerModePacket(menu.getBlockEntity().getBlockPos()));
        }).bounds(this.leftPos + 8, this.topPos + 60, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.tmmod.canner.button_swap"), button -> {
            PacketDistributor.sendToServer(new CannerSwapFluidPacket(menu.getBlockEntity().getBlockPos()));
        }).bounds(this.leftPos + 68, this.topPos + 50, 40, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BG, x, y, 0, 0, 176, 166);
        guiGraphics.blit(MODULES_BG, x + 176, y, 0, 0, 26, 80);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (i == 3) guiGraphics.blit(ENERGY_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
            else if (i >= 4 && i <= 7) guiGraphics.blit(MODULES_SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
            else guiGraphics.blit(SLOT, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
        }

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        // renderFluidTank(guiGraphics, menu.getFluidInTank(0), x + 12, y + 10); // Input Tank
        // renderFluidTank(guiGraphics, menu.getFluidInTank(1), x + 152, y + 10); // Output Tank
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        int progressBar = (maxProgress != 0 && progress != 0) ? (progress * 30 / maxProgress) : 0;
        guiGraphics.blit(ARROW_STATIC, x + 77, y + 34, 0, 0, 30, 17, 30, 17);
        guiGraphics.blit(ARROW, x + 77, y + 34, 0, 0, progressBar, 17, 30, 17);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        int scaledEnergy = (maxEnergy != 0 && energy != 0) ? (energy * barHeight / maxEnergy) : 0;

        int energyBarX = x + 152 + 20; // Shifted for fluids
        int energyBarY = y + 10;

        guiGraphics.blit(BAR_BG, energyBarX - 1, energyBarY - 1, 0, 0, 18, 64, 18, 64);
        guiGraphics.fill(energyBarX, energyBarY + (barHeight - scaledEnergy), energyBarX + barWidth, energyBarY + barHeight, 0xFFE31B1B);
        guiGraphics.blit(BAR_PARTITION, energyBarX, energyBarY, 0, 0, barWidth, barHeight, barWidth, barHeight);
    }

    private void renderFluidTank(GuiGraphics guiGraphics, FluidStack fluidStack, int x, int y) {
        guiGraphics.blit(BAR_BG, x - 1, y - 1, 0, 0, 18, 64, 18, 64);
        if (!fluidStack.isEmpty()) {
            int capacity = 10000;
            int scaledAmount = fluidStack.getAmount() * barHeight / capacity;
            
            IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation still = props.getStillTexture(fluidStack);
            int color = props.getTintColor(fluidStack);
            
            guiGraphics.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
            // Render fluid using blit with correct parameters for 1.21
            guiGraphics.blit(still, x, y + barHeight - scaledAmount, 0, 0, barWidth, scaledAmount, 16, 16);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        guiGraphics.blit(BAR_PARTITION, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Energy tooltip
        int energyX = x + 152 + 20;
        if (mouseX >= energyX && mouseX < energyX + barWidth && mouseY >= y + 10 && mouseY < y + 10 + barHeight) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " EU")), mouseX, mouseY);
        }

        // Fluid tooltips
        renderFluidTooltip(guiGraphics, menu.getFluidInTank(0), x + 12, y + 10, mouseX, mouseY);
        renderFluidTooltip(guiGraphics, menu.getFluidInTank(1), x + 152, y + 10, mouseX, mouseY);

        // Mode text
        String modeKey = "gui.tmmod.canner.mode." + menu.getMode().name().toLowerCase();
        guiGraphics.drawString(this.font, Component.translatable("gui.tmmod.canner.mode_text", Component.translatable(modeKey)), x + 8, y + 5, 0x404040, false);
    }

    private void renderFluidTooltip(GuiGraphics guiGraphics, FluidStack fluidStack, int x, int y, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX < x + barWidth && mouseY >= y && mouseY < y + barHeight) {
            List<Component> tooltip = new ArrayList<>();
            if (fluidStack.isEmpty()) {
                tooltip.add(Component.translatable("gui.tmmod.canner.empty"));
            } else {
                tooltip.add(fluidStack.getHoverName());
                tooltip.add(Component.literal(fluidStack.getAmount() + " / 10000 mB"));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}
*/
