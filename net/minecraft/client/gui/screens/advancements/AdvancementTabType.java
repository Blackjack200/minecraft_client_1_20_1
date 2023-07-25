package net.minecraft.client.gui.screens.advancements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

enum AdvancementTabType {
   ABOVE(0, 0, 28, 32, 8),
   BELOW(84, 0, 28, 32, 8),
   LEFT(0, 64, 32, 28, 5),
   RIGHT(96, 64, 32, 28, 5);

   private final int textureX;
   private final int textureY;
   private final int width;
   private final int height;
   private final int max;

   private AdvancementTabType(int i, int j, int k, int l, int i1) {
      this.textureX = i;
      this.textureY = j;
      this.width = k;
      this.height = l;
      this.max = i1;
   }

   public int getMax() {
      return this.max;
   }

   public void draw(GuiGraphics guigraphics, int i, int j, boolean flag, int k) {
      int l = this.textureX;
      if (k > 0) {
         l += this.width;
      }

      if (k == this.max - 1) {
         l += this.width;
      }

      int i1 = flag ? this.textureY + this.height : this.textureY;
      guigraphics.blit(AdvancementsScreen.TABS_LOCATION, i + this.getX(k), j + this.getY(k), l, i1, this.width, this.height);
   }

   public void drawIcon(GuiGraphics guigraphics, int i, int j, int k, ItemStack itemstack) {
      int l = i + this.getX(k);
      int i1 = j + this.getY(k);
      switch (this) {
         case ABOVE:
            l += 6;
            i1 += 9;
            break;
         case BELOW:
            l += 6;
            i1 += 6;
            break;
         case LEFT:
            l += 10;
            i1 += 5;
            break;
         case RIGHT:
            l += 6;
            i1 += 5;
      }

      guigraphics.renderFakeItem(itemstack, l, i1);
   }

   public int getX(int i) {
      switch (this) {
         case ABOVE:
            return (this.width + 4) * i;
         case BELOW:
            return (this.width + 4) * i;
         case LEFT:
            return -this.width + 4;
         case RIGHT:
            return 248;
         default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
      }
   }

   public int getY(int i) {
      switch (this) {
         case ABOVE:
            return -this.height + 4;
         case BELOW:
            return 136;
         case LEFT:
            return this.height * i;
         case RIGHT:
            return this.height * i;
         default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
      }
   }

   public boolean isMouseOver(int i, int j, int k, double d0, double d1) {
      int l = i + this.getX(k);
      int i1 = j + this.getY(k);
      return d0 > (double)l && d0 < (double)(l + this.width) && d1 > (double)i1 && d1 < (double)(i1 + this.height);
   }
}
