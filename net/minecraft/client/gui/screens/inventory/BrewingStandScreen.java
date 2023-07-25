package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;

public class BrewingStandScreen extends AbstractContainerScreen<BrewingStandMenu> {
   private static final ResourceLocation BREWING_STAND_LOCATION = new ResourceLocation("textures/gui/container/brewing_stand.png");
   private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

   public BrewingStandScreen(BrewingStandMenu brewingstandmenu, Inventory inventory, Component component) {
      super(brewingstandmenu, inventory, component);
   }

   protected void init() {
      super.init();
      this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(BREWING_STAND_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      int i1 = this.menu.getFuel();
      int j1 = Mth.clamp((18 * i1 + 20 - 1) / 20, 0, 18);
      if (j1 > 0) {
         guigraphics.blit(BREWING_STAND_LOCATION, k + 60, l + 44, 176, 29, j1, 4);
      }

      int k1 = this.menu.getBrewingTicks();
      if (k1 > 0) {
         int l1 = (int)(28.0F * (1.0F - (float)k1 / 400.0F));
         if (l1 > 0) {
            guigraphics.blit(BREWING_STAND_LOCATION, k + 97, l + 16, 176, 0, 9, l1);
         }

         l1 = BUBBLELENGTHS[k1 / 2 % 7];
         if (l1 > 0) {
            guigraphics.blit(BREWING_STAND_LOCATION, k + 63, l + 14 + 29 - l1, 185, 29 - l1, 12, l1);
         }
      }

   }
}
