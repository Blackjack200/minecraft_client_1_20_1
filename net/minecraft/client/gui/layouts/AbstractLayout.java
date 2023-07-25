package net.minecraft.client.gui.layouts;

import net.minecraft.util.Mth;

public abstract class AbstractLayout implements Layout {
   private int x;
   private int y;
   protected int width;
   protected int height;

   public AbstractLayout(int i, int j, int k, int l) {
      this.x = i;
      this.y = j;
      this.width = k;
      this.height = l;
   }

   public void setX(int i) {
      this.visitChildren((layoutelement) -> {
         int k = layoutelement.getX() + (i - this.getX());
         layoutelement.setX(k);
      });
      this.x = i;
   }

   public void setY(int i) {
      this.visitChildren((layoutelement) -> {
         int k = layoutelement.getY() + (i - this.getY());
         layoutelement.setY(k);
      });
      this.y = i;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   protected abstract static class AbstractChildWrapper {
      public final LayoutElement child;
      public final LayoutSettings.LayoutSettingsImpl layoutSettings;

      protected AbstractChildWrapper(LayoutElement layoutelement, LayoutSettings layoutsettings) {
         this.child = layoutelement;
         this.layoutSettings = layoutsettings.getExposed();
      }

      public int getHeight() {
         return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
      }

      public int getWidth() {
         return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
      }

      public void setX(int i, int j) {
         float f = (float)this.layoutSettings.paddingLeft;
         float f1 = (float)(j - this.child.getWidth() - this.layoutSettings.paddingRight);
         int k = (int)Mth.lerp(this.layoutSettings.xAlignment, f, f1);
         this.child.setX(k + i);
      }

      public void setY(int i, int j) {
         float f = (float)this.layoutSettings.paddingTop;
         float f1 = (float)(j - this.child.getHeight() - this.layoutSettings.paddingBottom);
         int k = Math.round(Mth.lerp(this.layoutSettings.yAlignment, f, f1));
         this.child.setY(k + i);
      }
   }
}
