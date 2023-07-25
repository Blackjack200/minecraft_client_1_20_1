package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;

public class ClientBundleTooltip implements ClientTooltipComponent {
   public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
   private static final int MARGIN_Y = 4;
   private static final int BORDER_WIDTH = 1;
   private static final int TEX_SIZE = 128;
   private static final int SLOT_SIZE_X = 18;
   private static final int SLOT_SIZE_Y = 20;
   private final NonNullList<ItemStack> items;
   private final int weight;

   public ClientBundleTooltip(BundleTooltip bundletooltip) {
      this.items = bundletooltip.getItems();
      this.weight = bundletooltip.getWeight();
   }

   public int getHeight() {
      return this.gridSizeY() * 20 + 2 + 4;
   }

   public int getWidth(Font font) {
      return this.gridSizeX() * 18 + 2;
   }

   public void renderImage(Font font, int i, int j, GuiGraphics guigraphics) {
      int k = this.gridSizeX();
      int l = this.gridSizeY();
      boolean flag = this.weight >= 64;
      int i1 = 0;

      for(int j1 = 0; j1 < l; ++j1) {
         for(int k1 = 0; k1 < k; ++k1) {
            int l1 = i + k1 * 18 + 1;
            int i2 = j + j1 * 20 + 1;
            this.renderSlot(l1, i2, i1++, flag, guigraphics, font);
         }
      }

      this.drawBorder(i, j, k, l, guigraphics);
   }

   private void renderSlot(int i, int j, int k, boolean flag, GuiGraphics guigraphics, Font font) {
      if (k >= this.items.size()) {
         this.blit(guigraphics, i, j, flag ? ClientBundleTooltip.Texture.BLOCKED_SLOT : ClientBundleTooltip.Texture.SLOT);
      } else {
         ItemStack itemstack = this.items.get(k);
         this.blit(guigraphics, i, j, ClientBundleTooltip.Texture.SLOT);
         guigraphics.renderItem(itemstack, i + 1, j + 1, k);
         guigraphics.renderItemDecorations(font, itemstack, i + 1, j + 1);
         if (k == 0) {
            AbstractContainerScreen.renderSlotHighlight(guigraphics, i + 1, j + 1, 0);
         }

      }
   }

   private void drawBorder(int i, int j, int k, int l, GuiGraphics guigraphics) {
      this.blit(guigraphics, i, j, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);
      this.blit(guigraphics, i + k * 18 + 1, j, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);

      for(int i1 = 0; i1 < k; ++i1) {
         this.blit(guigraphics, i + 1 + i1 * 18, j, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_TOP);
         this.blit(guigraphics, i + 1 + i1 * 18, j + l * 20, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
      }

      for(int j1 = 0; j1 < l; ++j1) {
         this.blit(guigraphics, i, j + j1 * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
         this.blit(guigraphics, i + k * 18 + 1, j + j1 * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
      }

      this.blit(guigraphics, i, j + l * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
      this.blit(guigraphics, i + k * 18 + 1, j + l * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
   }

   private void blit(GuiGraphics guigraphics, int i, int j, ClientBundleTooltip.Texture clientbundletooltip_texture) {
      guigraphics.blit(TEXTURE_LOCATION, i, j, 0, (float)clientbundletooltip_texture.x, (float)clientbundletooltip_texture.y, clientbundletooltip_texture.w, clientbundletooltip_texture.h, 128, 128);
   }

   private int gridSizeX() {
      return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.items.size() + 1.0D)));
   }

   private int gridSizeY() {
      return (int)Math.ceil(((double)this.items.size() + 1.0D) / (double)this.gridSizeX());
   }

   static enum Texture {
      SLOT(0, 0, 18, 20),
      BLOCKED_SLOT(0, 40, 18, 20),
      BORDER_VERTICAL(0, 18, 1, 20),
      BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
      BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
      BORDER_CORNER_TOP(0, 20, 1, 1),
      BORDER_CORNER_BOTTOM(0, 60, 1, 1);

      public final int x;
      public final int y;
      public final int w;
      public final int h;

      private Texture(int i, int j, int k, int l) {
         this.x = i;
         this.y = j;
         this.w = k;
         this.h = l;
      }
   }
}
