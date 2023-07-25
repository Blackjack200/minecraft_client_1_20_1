package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;

public class PanoramaRenderer {
   private final Minecraft minecraft;
   private final CubeMap cubeMap;
   private float spin;
   private float bob;

   public PanoramaRenderer(CubeMap cubemap) {
      this.cubeMap = cubemap;
      this.minecraft = Minecraft.getInstance();
   }

   public void render(float f, float f1) {
      float f2 = (float)((double)f * this.minecraft.options.panoramaSpeed().get());
      this.spin = wrap(this.spin + f2 * 0.1F, 360.0F);
      this.bob = wrap(this.bob + f2 * 0.001F, ((float)Math.PI * 2F));
      this.cubeMap.render(this.minecraft, 10.0F, -this.spin, f1);
   }

   private static float wrap(float f, float f1) {
      return f > f1 ? f - f1 : f;
   }
}
