package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import org.slf4j.Logger;

public class BitmapProvider implements GlyphProvider {
   static final Logger LOGGER = LogUtils.getLogger();
   private final NativeImage image;
   private final CodepointMap<BitmapProvider.Glyph> glyphs;

   BitmapProvider(NativeImage nativeimage, CodepointMap<BitmapProvider.Glyph> codepointmap) {
      this.image = nativeimage;
      this.glyphs = codepointmap;
   }

   public void close() {
      this.image.close();
   }

   @Nullable
   public GlyphInfo getGlyph(int i) {
      return this.glyphs.get(i);
   }

   public IntSet getSupportedGlyphs() {
      return IntSets.unmodifiable(this.glyphs.keySet());
   }

   public static record Definition(ResourceLocation file, int height, int ascent, int[][] codepointGrid) implements GlyphProviderDefinition {
      private static final Codec<int[][]> CODEPOINT_GRID_CODEC = ExtraCodecs.validate(Codec.STRING.listOf().xmap((list) -> {
         int i = list.size();
         int[][] aint = new int[i][];

         for(int j = 0; j < i; ++j) {
            aint[j] = list.get(j).codePoints().toArray();
         }

         return aint;
      }, (aint) -> {
         List<String> list = new ArrayList<>(aint.length);

         for(int[] aint1 : aint) {
            list.add(new String(aint1, 0, aint1.length));
         }

         return list;
      }), BitmapProvider.Definition::validateDimensions);
      public static final MapCodec<BitmapProvider.Definition> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("file").forGetter(BitmapProvider.Definition::file), Codec.INT.optionalFieldOf("height", Integer.valueOf(8)).forGetter(BitmapProvider.Definition::height), Codec.INT.fieldOf("ascent").forGetter(BitmapProvider.Definition::ascent), CODEPOINT_GRID_CODEC.fieldOf("chars").forGetter(BitmapProvider.Definition::codepointGrid)).apply(recordcodecbuilder_instance, BitmapProvider.Definition::new)), BitmapProvider.Definition::validate);

      private static DataResult<int[][]> validateDimensions(int[][] aint) {
         int i = aint.length;
         if (i == 0) {
            return DataResult.error(() -> "Expected to find data in codepoint grid");
         } else {
            int[] aint1 = aint[0];
            int j = aint1.length;
            if (j == 0) {
               return DataResult.error(() -> "Expected to find data in codepoint grid");
            } else {
               for(int k = 1; k < i; ++k) {
                  int[] aint2 = aint[k];
                  if (aint2.length != j) {
                     return DataResult.error(() -> "Lines in codepoint grid have to be the same length (found: " + aint2.length + " codepoints, expected: " + j + "), pad with \\u0000");
                  }
               }

               return DataResult.success(aint);
            }
         }
      }

      private static DataResult<BitmapProvider.Definition> validate(BitmapProvider.Definition bitmapprovider_definition) {
         return bitmapprovider_definition.ascent > bitmapprovider_definition.height ? DataResult.error(() -> "Ascent " + bitmapprovider_definition.ascent + " higher than height " + bitmapprovider_definition.height) : DataResult.success(bitmapprovider_definition);
      }

      public GlyphProviderType type() {
         return GlyphProviderType.BITMAP;
      }

      public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
         return Either.left(this::load);
      }

      private GlyphProvider load(ResourceManager resourcemanager) throws IOException {
         ResourceLocation resourcelocation = this.file.withPrefix("textures/");
         InputStream inputstream = resourcemanager.open(resourcelocation);

         BitmapProvider var22;
         try {
            NativeImage nativeimage = NativeImage.read(NativeImage.Format.RGBA, inputstream);
            int i = nativeimage.getWidth();
            int j = nativeimage.getHeight();
            int k = i / this.codepointGrid[0].length;
            int l = j / this.codepointGrid.length;
            float f = (float)this.height / (float)l;
            CodepointMap<BitmapProvider.Glyph> codepointmap = new CodepointMap<>((k2) -> new BitmapProvider.Glyph[k2], (j2) -> new BitmapProvider.Glyph[j2][]);

            for(int i1 = 0; i1 < this.codepointGrid.length; ++i1) {
               int j1 = 0;

               for(int k1 : this.codepointGrid[i1]) {
                  int l1 = j1++;
                  if (k1 != 0) {
                     int i2 = this.getActualGlyphWidth(nativeimage, k, l, l1, i1);
                     BitmapProvider.Glyph bitmapprovider_glyph = codepointmap.put(k1, new BitmapProvider.Glyph(f, nativeimage, l1 * k, i1 * l, k, l, (int)(0.5D + (double)((float)i2 * f)) + 1, this.ascent));
                     if (bitmapprovider_glyph != null) {
                        BitmapProvider.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(k1), resourcelocation);
                     }
                  }
               }
            }

            var22 = new BitmapProvider(nativeimage, codepointmap);
         } catch (Throwable var21) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var20) {
                  var21.addSuppressed(var20);
               }
            }

            throw var21;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var22;
      }

      private int getActualGlyphWidth(NativeImage nativeimage, int i, int j, int k, int l) {
         int i1;
         for(i1 = i - 1; i1 >= 0; --i1) {
            int j1 = k * i + i1;

            for(int k1 = 0; k1 < j; ++k1) {
               int l1 = l * j + k1;
               if (nativeimage.getLuminanceOrAlpha(j1, l1) != 0) {
                  return i1 + 1;
               }
            }
         }

         return i1 + 1;
      }
   }

   static record Glyph(float scale, NativeImage image, int offsetX, int offsetY, int width, int height, int advance, int ascent) implements GlyphInfo {
      final float scale;
      final NativeImage image;
      final int offsetX;
      final int offsetY;
      final int width;
      final int height;
      final int ascent;

      public float getAdvance() {
         return (float)this.advance;
      }

      public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
         return function.apply(new SheetGlyphInfo() {
            public float getOversample() {
               return 1.0F / Glyph.this.scale;
            }

            public int getPixelWidth() {
               return Glyph.this.width;
            }

            public int getPixelHeight() {
               return Glyph.this.height;
            }

            public float getBearingY() {
               return SheetGlyphInfo.super.getBearingY() + 7.0F - (float)Glyph.this.ascent;
            }

            public void upload(int i, int j) {
               Glyph.this.image.upload(0, i, j, Glyph.this.offsetX, Glyph.this.offsetY, Glyph.this.width, Glyph.this.height, false, false);
            }

            public boolean isColored() {
               return Glyph.this.image.format().components() > 1;
            }
         });
      }
   }
}
