package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

public class OverlayTexture implements AutoCloseable {
   private static final int SIZE = 16;
   public static final int NO_WHITE_U = 0;
   public static final int RED_OVERLAY_V = 3;
   public static final int WHITE_OVERLAY_V = 10;
   public static final int NO_OVERLAY = pack(0, 10);
   private final DynamicTexture texture = new DynamicTexture(16, 16, false);

   public OverlayTexture() {
      NativeImage nativeimage = this.texture.getPixels();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            if (i < 8) {
               nativeimage.setPixelRGBA(j, i, -1308622593);
            } else {
               int k = (int)((1.0F - (float)j / 15.0F * 0.75F) * 255.0F);
               nativeimage.setPixelRGBA(j, i, k << 24 | 16777215);
            }
         }
      }

      RenderSystem.activeTexture(33985);
      this.texture.bind();
      nativeimage.upload(0, 0, 0, 0, 0, nativeimage.getWidth(), nativeimage.getHeight(), false, true, false, false);
      RenderSystem.activeTexture(33984);
   }

   public void close() {
      this.texture.close();
   }

   public void setupOverlayColor() {
      RenderSystem.setupOverlayColor(this.texture::getId, 16);
   }

   public static int u(float f) {
      return (int)(f * 15.0F);
   }

   public static int v(boolean flag) {
      return flag ? 3 : 10;
   }

   public static int pack(int i, int j) {
      return i | j << 16;
   }

   public static int pack(float f, boolean flag) {
      return pack(u(f), v(flag));
   }

   public void teardownOverlayColor() {
      RenderSystem.teardownOverlayColor();
   }
}
