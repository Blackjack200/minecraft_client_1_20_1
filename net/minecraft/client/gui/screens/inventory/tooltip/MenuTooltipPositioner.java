package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.Mth;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class MenuTooltipPositioner implements ClientTooltipPositioner {
   private static final int MARGIN = 5;
   private static final int MOUSE_OFFSET_X = 12;
   public static final int MAX_OVERLAP_WITH_WIDGET = 3;
   public static final int MAX_DISTANCE_TO_WIDGET = 5;
   private final AbstractWidget widget;

   public MenuTooltipPositioner(AbstractWidget abstractwidget) {
      this.widget = abstractwidget;
   }

   public Vector2ic positionTooltip(int i, int j, int k, int l, int i1, int j1) {
      Vector2i vector2i = new Vector2i(k + 12, l);
      if (vector2i.x + i1 > i - 5) {
         vector2i.x = Math.max(k - 12 - i1, 9);
      }

      vector2i.y += 3;
      int k1 = j1 + 3 + 3;
      int l1 = this.widget.getY() + this.widget.getHeight() + 3 + getOffset(0, 0, this.widget.getHeight());
      int i2 = j - 5;
      if (l1 + k1 <= i2) {
         vector2i.y += getOffset(vector2i.y, this.widget.getY(), this.widget.getHeight());
      } else {
         vector2i.y -= k1 + getOffset(vector2i.y, this.widget.getY() + this.widget.getHeight(), this.widget.getHeight());
      }

      return vector2i;
   }

   private static int getOffset(int i, int j, int k) {
      int l = Math.min(Math.abs(i - j), k);
      return Math.round(Mth.lerp((float)l / (float)k, (float)(k - 3), 5.0F));
   }
}
