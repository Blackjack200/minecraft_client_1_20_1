package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;

public class HopperScreen extends AbstractContainerScreen<HopperMenu> {
   private static final ResourceLocation HOPPER_LOCATION = new ResourceLocation("textures/gui/container/hopper.png");

   public HopperScreen(HopperMenu hoppermenu, Inventory inventory, Component component) {
      super(hoppermenu, inventory, component);
      this.imageHeight = 133;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(HOPPER_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
   }
}
