package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class PlayerFaceRenderer {
   public static final int SKIN_HEAD_U = 8;
   public static final int SKIN_HEAD_V = 8;
   public static final int SKIN_HEAD_WIDTH = 8;
   public static final int SKIN_HEAD_HEIGHT = 8;
   public static final int SKIN_HAT_U = 40;
   public static final int SKIN_HAT_V = 8;
   public static final int SKIN_HAT_WIDTH = 8;
   public static final int SKIN_HAT_HEIGHT = 8;
   public static final int SKIN_TEX_WIDTH = 64;
   public static final int SKIN_TEX_HEIGHT = 64;

   public static void draw(GuiGraphics guigraphics, ResourceLocation resourcelocation, int i, int j, int k) {
      draw(guigraphics, resourcelocation, i, j, k, true, false);
   }

   public static void draw(GuiGraphics guigraphics, ResourceLocation resourcelocation, int i, int j, int k, boolean flag, boolean flag1) {
      int l = 8 + (flag1 ? 8 : 0);
      int i1 = 8 * (flag1 ? -1 : 1);
      guigraphics.blit(resourcelocation, i, j, k, k, 8.0F, (float)l, 8, i1, 64, 64);
      if (flag) {
         drawHat(guigraphics, resourcelocation, i, j, k, flag1);
      }

   }

   private static void drawHat(GuiGraphics guigraphics, ResourceLocation resourcelocation, int i, int j, int k, boolean flag) {
      int l = 8 + (flag ? 8 : 0);
      int i1 = 8 * (flag ? -1 : 1);
      RenderSystem.enableBlend();
      guigraphics.blit(resourcelocation, i, j, k, k, 40.0F, (float)l, 8, i1, 64, 64);
      RenderSystem.disableBlend();
   }
}
