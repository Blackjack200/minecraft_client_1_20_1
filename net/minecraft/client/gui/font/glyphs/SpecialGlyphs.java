package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import java.util.function.Supplier;

public enum SpecialGlyphs implements GlyphInfo {
   WHITE(() -> generate(5, 8, (i, j) -> -1)),
   MISSING(() -> {
      int i = 5;
      int j = 8;
      return generate(5, 8, (k, l) -> {
         boolean flag = k == 0 || k + 1 == 5 || l == 0 || l + 1 == 8;
         return flag ? -1 : 0;
      });
   });

   final NativeImage image;

   private static NativeImage generate(int i, int j, SpecialGlyphs.PixelProvider specialglyphs_pixelprovider) {
      NativeImage nativeimage = new NativeImage(NativeImage.Format.RGBA, i, j, false);

      for(int k = 0; k < j; ++k) {
         for(int l = 0; l < i; ++l) {
            nativeimage.setPixelRGBA(l, k, specialglyphs_pixelprovider.getColor(l, k));
         }
      }

      nativeimage.untrack();
      return nativeimage;
   }

   private SpecialGlyphs(Supplier<NativeImage> supplier) {
      this.image = supplier.get();
   }

   public float getAdvance() {
      return (float)(this.image.getWidth() + 1);
   }

   public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
      return function.apply(new SheetGlyphInfo() {
         public int getPixelWidth() {
            return SpecialGlyphs.this.image.getWidth();
         }

         public int getPixelHeight() {
            return SpecialGlyphs.this.image.getHeight();
         }

         public float getOversample() {
            return 1.0F;
         }

         public void upload(int i, int j) {
            SpecialGlyphs.this.image.upload(0, i, j, false);
         }

         public boolean isColored() {
            return true;
         }
      });
   }

   @FunctionalInterface
   interface PixelProvider {
      int getColor(int i, int j);
   }
}
