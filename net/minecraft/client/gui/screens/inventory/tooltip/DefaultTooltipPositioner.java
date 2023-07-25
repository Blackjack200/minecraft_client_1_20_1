package net.minecraft.client.gui.screens.inventory.tooltip;

import org.joml.Vector2i;
import org.joml.Vector2ic;

public class DefaultTooltipPositioner implements ClientTooltipPositioner {
   public static final ClientTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

   private DefaultTooltipPositioner() {
   }

   public Vector2ic positionTooltip(int i, int j, int k, int l, int i1, int j1) {
      Vector2i vector2i = (new Vector2i(k, l)).add(12, -12);
      this.positionTooltip(i, j, vector2i, i1, j1);
      return vector2i;
   }

   private void positionTooltip(int i, int j, Vector2i vector2i, int k, int l) {
      if (vector2i.x + k > i) {
         vector2i.x = Math.max(vector2i.x - 24 - k, 4);
      }

      int i1 = l + 3;
      if (vector2i.y + i1 > j) {
         vector2i.y = j - i1;
      }

   }
}
