package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.components.AbstractWidget;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class BelowOrAboveWidgetTooltipPositioner implements ClientTooltipPositioner {
   private final AbstractWidget widget;

   public BelowOrAboveWidgetTooltipPositioner(AbstractWidget abstractwidget) {
      this.widget = abstractwidget;
   }

   public Vector2ic positionTooltip(int i, int j, int k, int l, int i1, int j1) {
      Vector2i vector2i = new Vector2i();
      vector2i.x = this.widget.getX() + 3;
      vector2i.y = this.widget.getY() + this.widget.getHeight() + 3 + 1;
      if (vector2i.y + j1 + 3 > j) {
         vector2i.y = this.widget.getY() - j1 - 3 - 1;
      }

      if (vector2i.x + i1 > i) {
         vector2i.x = Math.max(this.widget.getX() + this.widget.getWidth() - i1 - 3, 4);
      }

      return vector2i;
   }
}
