package net.minecraft.client.gui.components;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class SplashRenderer {
   public static final SplashRenderer CHRISTMAS = new SplashRenderer("Merry X-mas!");
   public static final SplashRenderer NEW_YEAR = new SplashRenderer("Happy new year!");
   public static final SplashRenderer HALLOWEEN = new SplashRenderer("OOoooOOOoooo! Spooky!");
   private static final int WIDTH_OFFSET = 123;
   private static final int HEIGH_OFFSET = 69;
   private final String splash;

   public SplashRenderer(String s) {
      this.splash = s;
   }

   public void render(GuiGraphics guigraphics, int i, Font font, int j) {
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((float)i / 2.0F + 123.0F, 69.0F, 0.0F);
      guigraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
      float f = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
      f = f * 100.0F / (float)(font.width(this.splash) + 32);
      guigraphics.pose().scale(f, f, f);
      guigraphics.drawCenteredString(font, this.splash, 0, -8, 16776960 | j);
      guigraphics.pose().popPose();
   }
}
