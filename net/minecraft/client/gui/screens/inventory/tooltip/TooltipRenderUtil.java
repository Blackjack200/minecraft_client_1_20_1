package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.GuiGraphics;

public class TooltipRenderUtil {
   public static final int MOUSE_OFFSET = 12;
   private static final int PADDING = 3;
   public static final int PADDING_LEFT = 3;
   public static final int PADDING_RIGHT = 3;
   public static final int PADDING_TOP = 3;
   public static final int PADDING_BOTTOM = 3;
   private static final int BACKGROUND_COLOR = -267386864;
   private static final int BORDER_COLOR_TOP = 1347420415;
   private static final int BORDER_COLOR_BOTTOM = 1344798847;

   public static void renderTooltipBackground(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      int j1 = i - 3;
      int k1 = j - 3;
      int l1 = k + 3 + 3;
      int i2 = l + 3 + 3;
      renderHorizontalLine(guigraphics, j1, k1 - 1, l1, i1, -267386864);
      renderHorizontalLine(guigraphics, j1, k1 + i2, l1, i1, -267386864);
      renderRectangle(guigraphics, j1, k1, l1, i2, i1, -267386864);
      renderVerticalLine(guigraphics, j1 - 1, k1, i2, i1, -267386864);
      renderVerticalLine(guigraphics, j1 + l1, k1, i2, i1, -267386864);
      renderFrameGradient(guigraphics, j1, k1 + 1, l1, i2, i1, 1347420415, 1344798847);
   }

   private static void renderFrameGradient(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1) {
      renderVerticalLineGradient(guigraphics, i, j, l - 2, i1, j1, k1);
      renderVerticalLineGradient(guigraphics, i + k - 1, j, l - 2, i1, j1, k1);
      renderHorizontalLine(guigraphics, i, j - 1, k, i1, j1);
      renderHorizontalLine(guigraphics, i, j - 1 + l - 1, k, i1, k1);
   }

   private static void renderVerticalLine(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      guigraphics.fill(i, j, i + 1, j + k, l, i1);
   }

   private static void renderVerticalLineGradient(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1) {
      guigraphics.fillGradient(i, j, i + 1, j + k, l, i1, j1);
   }

   private static void renderHorizontalLine(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      guigraphics.fill(i, j, i + k, j + 1, l, i1);
   }

   private static void renderRectangle(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1) {
      guigraphics.fill(i, j, i + k, j + l, i1, j1);
   }
}
