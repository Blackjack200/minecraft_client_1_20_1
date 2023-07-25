package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class TrueTypeGlyphProvider implements GlyphProvider {
   @Nullable
   private ByteBuffer fontMemory;
   @Nullable
   private STBTTFontinfo font;
   final float oversample;
   private final IntSet skip = new IntArraySet();
   final float shiftX;
   final float shiftY;
   final float pointScale;
   final float ascent;

   public TrueTypeGlyphProvider(ByteBuffer bytebuffer, STBTTFontinfo stbttfontinfo, float f, float f1, float f2, float f3, String s) {
      this.fontMemory = bytebuffer;
      this.font = stbttfontinfo;
      this.oversample = f1;
      s.codePoints().forEach(this.skip::add);
      this.shiftX = f2 * f1;
      this.shiftY = f3 * f1;
      this.pointScale = STBTruetype.stbtt_ScaleForPixelHeight(stbttfontinfo, f * f1);
      MemoryStack memorystack = MemoryStack.stackPush();

      try {
         IntBuffer intbuffer = memorystack.mallocInt(1);
         IntBuffer intbuffer1 = memorystack.mallocInt(1);
         IntBuffer intbuffer2 = memorystack.mallocInt(1);
         STBTruetype.stbtt_GetFontVMetrics(stbttfontinfo, intbuffer, intbuffer1, intbuffer2);
         this.ascent = (float)intbuffer.get(0) * this.pointScale;
      } catch (Throwable var13) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (memorystack != null) {
         memorystack.close();
      }

   }

   @Nullable
   public GlyphInfo getGlyph(int i) {
      STBTTFontinfo stbttfontinfo = this.validateFontOpen();
      if (this.skip.contains(i)) {
         return null;
      } else {
         MemoryStack memorystack = MemoryStack.stackPush();

         Object var17;
         label61: {
            GlyphInfo var18;
            label62: {
               try {
                  int j = STBTruetype.stbtt_FindGlyphIndex(stbttfontinfo, i);
                  if (j == 0) {
                     var17 = null;
                     break label61;
                  }

                  IntBuffer intbuffer = memorystack.mallocInt(1);
                  IntBuffer intbuffer1 = memorystack.mallocInt(1);
                  IntBuffer intbuffer2 = memorystack.mallocInt(1);
                  IntBuffer intbuffer3 = memorystack.mallocInt(1);
                  IntBuffer intbuffer4 = memorystack.mallocInt(1);
                  IntBuffer intbuffer5 = memorystack.mallocInt(1);
                  STBTruetype.stbtt_GetGlyphHMetrics(stbttfontinfo, j, intbuffer4, intbuffer5);
                  STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(stbttfontinfo, j, this.pointScale, this.pointScale, this.shiftX, this.shiftY, intbuffer, intbuffer1, intbuffer2, intbuffer3);
                  float f = (float)intbuffer4.get(0) * this.pointScale;
                  int k = intbuffer2.get(0) - intbuffer.get(0);
                  int l = intbuffer3.get(0) - intbuffer1.get(0);
                  if (k > 0 && l > 0) {
                     var18 = new TrueTypeGlyphProvider.Glyph(intbuffer.get(0), intbuffer2.get(0), -intbuffer1.get(0), -intbuffer3.get(0), f, (float)intbuffer5.get(0) * this.pointScale, j);
                     break label62;
                  }

                  var18 = () -> f / this.oversample;
               } catch (Throwable var16) {
                  if (memorystack != null) {
                     try {
                        memorystack.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (memorystack != null) {
                  memorystack.close();
               }

               return var18;
            }

            if (memorystack != null) {
               memorystack.close();
            }

            return var18;
         }

         if (memorystack != null) {
            memorystack.close();
         }

         return (GlyphInfo)var17;
      }
   }

   STBTTFontinfo validateFontOpen() {
      if (this.fontMemory != null && this.font != null) {
         return this.font;
      } else {
         throw new IllegalArgumentException("Provider already closed");
      }
   }

   public void close() {
      if (this.font != null) {
         this.font.free();
         this.font = null;
      }

      MemoryUtil.memFree(this.fontMemory);
      this.fontMemory = null;
   }

   public IntSet getSupportedGlyphs() {
      return IntStream.range(0, 65535).filter((i) -> !this.skip.contains(i)).collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
   }

   class Glyph implements GlyphInfo {
      final int width;
      final int height;
      final float bearingX;
      final float bearingY;
      private final float advance;
      final int index;

      Glyph(int i, int j, int k, int l, float f, float f1, int i1) {
         this.width = j - i;
         this.height = k - l;
         this.advance = f / TrueTypeGlyphProvider.this.oversample;
         this.bearingX = (f1 + (float)i + TrueTypeGlyphProvider.this.shiftX) / TrueTypeGlyphProvider.this.oversample;
         this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float)k + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
         this.index = i1;
      }

      public float getAdvance() {
         return this.advance;
      }

      public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
         return function.apply(new SheetGlyphInfo() {
            public int getPixelWidth() {
               return Glyph.this.width;
            }

            public int getPixelHeight() {
               return Glyph.this.height;
            }

            public float getOversample() {
               return TrueTypeGlyphProvider.this.oversample;
            }

            public float getBearingX() {
               return Glyph.this.bearingX;
            }

            public float getBearingY() {
               return Glyph.this.bearingY;
            }

            public void upload(int i, int j) {
               STBTTFontinfo stbttfontinfo = TrueTypeGlyphProvider.this.validateFontOpen();
               NativeImage nativeimage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);
               nativeimage.copyFromFont(stbttfontinfo, Glyph.this.index, Glyph.this.width, Glyph.this.height, TrueTypeGlyphProvider.this.pointScale, TrueTypeGlyphProvider.this.pointScale, TrueTypeGlyphProvider.this.shiftX, TrueTypeGlyphProvider.this.shiftY, 0, 0);
               nativeimage.upload(0, i, j, 0, 0, Glyph.this.width, Glyph.this.height, false, true);
            }

            public boolean isColored() {
               return false;
            }
         });
      }
   }
}
