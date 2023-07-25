package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;

public class MipmapGenerator {
   private static final int ALPHA_CUTOUT_CUTOFF = 96;
   private static final float[] POW22 = Util.make(new float[256], (afloat) -> {
      for(int i = 0; i < afloat.length; ++i) {
         afloat[i] = (float)Math.pow((double)((float)i / 255.0F), 2.2D);
      }

   });

   private MipmapGenerator() {
   }

   public static NativeImage[] generateMipLevels(NativeImage[] anativeimage, int i) {
      if (i + 1 <= anativeimage.length) {
         return anativeimage;
      } else {
         NativeImage[] anativeimage1 = new NativeImage[i + 1];
         anativeimage1[0] = anativeimage[0];
         boolean flag = hasTransparentPixel(anativeimage1[0]);

         for(int j = 1; j <= i; ++j) {
            if (j < anativeimage.length) {
               anativeimage1[j] = anativeimage[j];
            } else {
               NativeImage nativeimage = anativeimage1[j - 1];
               NativeImage nativeimage1 = new NativeImage(nativeimage.getWidth() >> 1, nativeimage.getHeight() >> 1, false);
               int k = nativeimage1.getWidth();
               int l = nativeimage1.getHeight();

               for(int i1 = 0; i1 < k; ++i1) {
                  for(int j1 = 0; j1 < l; ++j1) {
                     nativeimage1.setPixelRGBA(i1, j1, alphaBlend(nativeimage.getPixelRGBA(i1 * 2 + 0, j1 * 2 + 0), nativeimage.getPixelRGBA(i1 * 2 + 1, j1 * 2 + 0), nativeimage.getPixelRGBA(i1 * 2 + 0, j1 * 2 + 1), nativeimage.getPixelRGBA(i1 * 2 + 1, j1 * 2 + 1), flag));
                  }
               }

               anativeimage1[j] = nativeimage1;
            }
         }

         return anativeimage1;
      }
   }

   private static boolean hasTransparentPixel(NativeImage nativeimage) {
      for(int i = 0; i < nativeimage.getWidth(); ++i) {
         for(int j = 0; j < nativeimage.getHeight(); ++j) {
            if (nativeimage.getPixelRGBA(i, j) >> 24 == 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static int alphaBlend(int i, int j, int k, int l, boolean flag) {
      if (flag) {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         float f3 = 0.0F;
         if (i >> 24 != 0) {
            f += getPow22(i >> 24);
            f1 += getPow22(i >> 16);
            f2 += getPow22(i >> 8);
            f3 += getPow22(i >> 0);
         }

         if (j >> 24 != 0) {
            f += getPow22(j >> 24);
            f1 += getPow22(j >> 16);
            f2 += getPow22(j >> 8);
            f3 += getPow22(j >> 0);
         }

         if (k >> 24 != 0) {
            f += getPow22(k >> 24);
            f1 += getPow22(k >> 16);
            f2 += getPow22(k >> 8);
            f3 += getPow22(k >> 0);
         }

         if (l >> 24 != 0) {
            f += getPow22(l >> 24);
            f1 += getPow22(l >> 16);
            f2 += getPow22(l >> 8);
            f3 += getPow22(l >> 0);
         }

         f /= 4.0F;
         f1 /= 4.0F;
         f2 /= 4.0F;
         f3 /= 4.0F;
         int i1 = (int)(Math.pow((double)f, 0.45454545454545453D) * 255.0D);
         int j1 = (int)(Math.pow((double)f1, 0.45454545454545453D) * 255.0D);
         int k1 = (int)(Math.pow((double)f2, 0.45454545454545453D) * 255.0D);
         int l1 = (int)(Math.pow((double)f3, 0.45454545454545453D) * 255.0D);
         if (i1 < 96) {
            i1 = 0;
         }

         return i1 << 24 | j1 << 16 | k1 << 8 | l1;
      } else {
         int i2 = gammaBlend(i, j, k, l, 24);
         int j2 = gammaBlend(i, j, k, l, 16);
         int k2 = gammaBlend(i, j, k, l, 8);
         int l2 = gammaBlend(i, j, k, l, 0);
         return i2 << 24 | j2 << 16 | k2 << 8 | l2;
      }
   }

   private static int gammaBlend(int i, int j, int k, int l, int i1) {
      float f = getPow22(i >> i1);
      float f1 = getPow22(j >> i1);
      float f2 = getPow22(k >> i1);
      float f3 = getPow22(l >> i1);
      float f4 = (float)((double)((float)Math.pow((double)(f + f1 + f2 + f3) * 0.25D, 0.45454545454545453D)));
      return (int)((double)f4 * 255.0D);
   }

   private static float getPow22(int i) {
      return POW22[i & 255];
   }
}
