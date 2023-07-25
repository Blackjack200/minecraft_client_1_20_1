package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FittingMultiLineTextWidget extends AbstractScrollWidget {
   private final Font font;
   private final MultiLineTextWidget multilineWidget;

   public FittingMultiLineTextWidget(int i, int j, int k, int l, Component component, Font font) {
      super(i, j, k, l, component);
      this.font = font;
      this.multilineWidget = (new MultiLineTextWidget(0, 0, component, font)).setMaxWidth(this.getWidth() - this.totalInnerPadding());
   }

   public FittingMultiLineTextWidget setColor(int i) {
      this.multilineWidget.setColor(i);
      return this;
   }

   public void setWidth(int i) {
      super.setWidth(i);
      this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
   }

   protected int getInnerHeight() {
      return this.multilineWidget.getHeight();
   }

   protected double scrollRate() {
      return 9.0D;
   }

   protected void renderBackground(GuiGraphics guigraphics) {
      if (this.scrollbarVisible()) {
         super.renderBackground(guigraphics);
      } else if (this.isFocused()) {
         this.renderBorder(guigraphics, this.getX() - this.innerPadding(), this.getY() - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding());
      }

   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.visible) {
         if (!this.scrollbarVisible()) {
            this.renderBackground(guigraphics);
            guigraphics.pose().pushPose();
            guigraphics.pose().translate((float)this.getX(), (float)this.getY(), 0.0F);
            this.multilineWidget.render(guigraphics, i, j, f);
            guigraphics.pose().popPose();
         } else {
            super.renderWidget(guigraphics, i, j, f);
         }

      }
   }

   protected void renderContents(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((float)(this.getX() + this.innerPadding()), (float)(this.getY() + this.innerPadding()), 0.0F);
      this.multilineWidget.render(guigraphics, i, j, f);
      guigraphics.pose().popPose();
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, this.getMessage());
   }
}
