package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractScrollWidget extends AbstractWidget implements Renderable, GuiEventListener {
   private static final int BORDER_COLOR_FOCUSED = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = -16777216;
   private static final int INNER_PADDING = 4;
   private double scrollAmount;
   private boolean scrolling;

   public AbstractScrollWidget(int i, int j, int k, int l, Component component) {
      super(i, j, k, l, component);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (!this.visible) {
         return false;
      } else {
         boolean flag = this.withinContentAreaPoint(d0, d1);
         boolean flag1 = this.scrollbarVisible() && d0 >= (double)(this.getX() + this.width) && d0 <= (double)(this.getX() + this.width + 8) && d1 >= (double)this.getY() && d1 < (double)(this.getY() + this.height);
         if (flag1 && i == 0) {
            this.scrolling = true;
            return true;
         } else {
            return flag || flag1;
         }
      }
   }

   public boolean mouseReleased(double d0, double d1, int i) {
      if (i == 0) {
         this.scrolling = false;
      }

      return super.mouseReleased(d0, d1, i);
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (this.visible && this.isFocused() && this.scrolling) {
         if (d1 < (double)this.getY()) {
            this.setScrollAmount(0.0D);
         } else if (d1 > (double)(this.getY() + this.height)) {
            this.setScrollAmount((double)this.getMaxScrollAmount());
         } else {
            int j = this.getScrollBarHeight();
            double d4 = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - j));
            this.setScrollAmount(this.scrollAmount + d3 * d4);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      if (!this.visible) {
         return false;
      } else {
         this.setScrollAmount(this.scrollAmount - d2 * this.scrollRate());
         return true;
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      boolean flag = i == 265;
      boolean flag1 = i == 264;
      if (flag || flag1) {
         double d0 = this.scrollAmount;
         this.setScrollAmount(this.scrollAmount + (double)(flag ? -1 : 1) * this.scrollRate());
         if (d0 != this.scrollAmount) {
            return true;
         }
      }

      return super.keyPressed(i, j, k);
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.visible) {
         this.renderBackground(guigraphics);
         guigraphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0D, -this.scrollAmount, 0.0D);
         this.renderContents(guigraphics, i, j, f);
         guigraphics.pose().popPose();
         guigraphics.disableScissor();
         this.renderDecorations(guigraphics);
      }
   }

   private int getScrollBarHeight() {
      return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
   }

   protected void renderDecorations(GuiGraphics guigraphics) {
      if (this.scrollbarVisible()) {
         this.renderScrollBar(guigraphics);
      }

   }

   protected int innerPadding() {
      return 4;
   }

   protected int totalInnerPadding() {
      return this.innerPadding() * 2;
   }

   protected double scrollAmount() {
      return this.scrollAmount;
   }

   protected void setScrollAmount(double d0) {
      this.scrollAmount = Mth.clamp(d0, 0.0D, (double)this.getMaxScrollAmount());
   }

   protected int getMaxScrollAmount() {
      return Math.max(0, this.getContentHeight() - (this.height - 4));
   }

   private int getContentHeight() {
      return this.getInnerHeight() + 4;
   }

   protected void renderBackground(GuiGraphics guigraphics) {
      this.renderBorder(guigraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
   }

   protected void renderBorder(GuiGraphics guigraphics, int i, int j, int k, int l) {
      int i1 = this.isFocused() ? -1 : -6250336;
      guigraphics.fill(i, j, i + k, j + l, i1);
      guigraphics.fill(i + 1, j + 1, i + k - 1, j + l - 1, -16777216);
   }

   private void renderScrollBar(GuiGraphics guigraphics) {
      int i = this.getScrollBarHeight();
      int j = this.getX() + this.width;
      int k = this.getX() + this.width + 8;
      int l = Math.max(this.getY(), (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.getY());
      int i1 = l + i;
      guigraphics.fill(j, l, k, i1, -8355712);
      guigraphics.fill(j, l, k - 1, i1 - 1, -4144960);
   }

   protected boolean withinContentAreaTopBottom(int i, int j) {
      return (double)j - this.scrollAmount >= (double)this.getY() && (double)i - this.scrollAmount <= (double)(this.getY() + this.height);
   }

   protected boolean withinContentAreaPoint(double d0, double d1) {
      return d0 >= (double)this.getX() && d0 < (double)(this.getX() + this.width) && d1 >= (double)this.getY() && d1 < (double)(this.getY() + this.height);
   }

   protected boolean scrollbarVisible() {
      return this.getInnerHeight() > this.getHeight();
   }

   protected abstract int getInnerHeight();

   protected abstract double scrollRate();

   protected abstract void renderContents(GuiGraphics guigraphics, int i, int j, float f);
}
