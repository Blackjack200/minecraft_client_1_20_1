package net.minecraft.client.gui.font.providers;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastBufferedInputStream;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class UnihexProvider implements GlyphProvider {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int GLYPH_HEIGHT = 16;
   private static final int DIGITS_PER_BYTE = 2;
   private static final int DIGITS_FOR_WIDTH_8 = 32;
   private static final int DIGITS_FOR_WIDTH_16 = 64;
   private static final int DIGITS_FOR_WIDTH_24 = 96;
   private static final int DIGITS_FOR_WIDTH_32 = 128;
   private final CodepointMap<UnihexProvider.Glyph> glyphs;

   UnihexProvider(CodepointMap<UnihexProvider.Glyph> codepointmap) {
      this.glyphs = codepointmap;
   }

   @Nullable
   public GlyphInfo getGlyph(int i) {
      return this.glyphs.get(i);
   }

   public IntSet getSupportedGlyphs() {
      return this.glyphs.keySet();
   }

   @VisibleForTesting
   static void unpackBitsToBytes(IntBuffer intbuffer, int i, int j, int k) {
      int l = 32 - j - 1;
      int i1 = 32 - k - 1;

      for(int j1 = l; j1 >= i1; --j1) {
         if (j1 < 32 && j1 >= 0) {
            boolean flag = (i >> j1 & 1) != 0;
            intbuffer.put(flag ? -1 : 0);
         } else {
            intbuffer.put(0);
         }
      }

   }

   static void unpackBitsToBytes(IntBuffer intbuffer, UnihexProvider.LineData unihexprovider_linedata, int i, int j) {
      for(int k = 0; k < 16; ++k) {
         int l = unihexprovider_linedata.line(k);
         unpackBitsToBytes(intbuffer, l, i, j);
      }

   }

   @VisibleForTesting
   static void readFromStream(InputStream inputstream, UnihexProvider.ReaderOutput unihexprovider_readeroutput) throws IOException {
      int i = 0;
      ByteList bytelist = new ByteArrayList(128);

      while(true) {
         boolean flag = copyUntil(inputstream, bytelist, 58);
         int j = bytelist.size();
         if (j == 0 && !flag) {
            return;
         }

         if (!flag || j != 4 && j != 5 && j != 6) {
            throw new IllegalArgumentException("Invalid entry at line " + i + ": expected 4, 5 or 6 hex digits followed by a colon");
         }

         int k = 0;

         for(int l = 0; l < j; ++l) {
            k = k << 4 | decodeHex(i, bytelist.getByte(l));
         }

         bytelist.clear();
         copyUntil(inputstream, bytelist, 10);
         int i1 = bytelist.size();
         UnihexProvider.LineData var10000;
         switch (i1) {
            case 32:
               var10000 = UnihexProvider.ByteContents.read(i, bytelist);
               break;
            case 64:
               var10000 = UnihexProvider.ShortContents.read(i, bytelist);
               break;
            case 96:
               var10000 = UnihexProvider.IntContents.read24(i, bytelist);
               break;
            case 128:
               var10000 = UnihexProvider.IntContents.read32(i, bytelist);
               break;
            default:
               throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line");
         }

         UnihexProvider.LineData unihexprovider_linedata = var10000;
         unihexprovider_readeroutput.accept(k, unihexprovider_linedata);
         ++i;
         bytelist.clear();
      }
   }

   static int decodeHex(int i, ByteList bytelist, int j) {
      return decodeHex(i, bytelist.getByte(j));
   }

   private static int decodeHex(int i, byte b0) {
      byte var10000;
      switch (b0) {
         case 48:
            var10000 = 0;
            break;
         case 49:
            var10000 = 1;
            break;
         case 50:
            var10000 = 2;
            break;
         case 51:
            var10000 = 3;
            break;
         case 52:
            var10000 = 4;
            break;
         case 53:
            var10000 = 5;
            break;
         case 54:
            var10000 = 6;
            break;
         case 55:
            var10000 = 7;
            break;
         case 56:
            var10000 = 8;
            break;
         case 57:
            var10000 = 9;
            break;
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         default:
            throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex digit, got " + (char)b0);
         case 65:
            var10000 = 10;
            break;
         case 66:
            var10000 = 11;
            break;
         case 67:
            var10000 = 12;
            break;
         case 68:
            var10000 = 13;
            break;
         case 69:
            var10000 = 14;
            break;
         case 70:
            var10000 = 15;
      }

      return var10000;
   }

   private static boolean copyUntil(InputStream inputstream, ByteList bytelist, int i) throws IOException {
      while(true) {
         int j = inputstream.read();
         if (j == -1) {
            return false;
         }

         if (j == i) {
            return true;
         }

         bytelist.add((byte)j);
      }
   }

   static record ByteContents(byte[] contents) implements UnihexProvider.LineData {
      public int line(int i) {
         return this.contents[i] << 24;
      }

      static UnihexProvider.LineData read(int i, ByteList bytelist) {
         byte[] abyte = new byte[16];
         int j = 0;

         for(int k = 0; k < 16; ++k) {
            int l = UnihexProvider.decodeHex(i, bytelist, j++);
            int i1 = UnihexProvider.decodeHex(i, bytelist, j++);
            byte b0 = (byte)(l << 4 | i1);
            abyte[k] = b0;
         }

         return new UnihexProvider.ByteContents(abyte);
      }

      public int bitWidth() {
         return 8;
      }
   }

   public static class Definition implements GlyphProviderDefinition {
      public static final MapCodec<UnihexProvider.Definition> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("hex_file").forGetter((unihexprovider_definition1) -> unihexprovider_definition1.hexFile), UnihexProvider.OverrideRange.CODEC.listOf().fieldOf("size_overrides").forGetter((unihexprovider_definition) -> unihexprovider_definition.sizeOverrides)).apply(recordcodecbuilder_instance, UnihexProvider.Definition::new));
      private final ResourceLocation hexFile;
      private final List<UnihexProvider.OverrideRange> sizeOverrides;

      private Definition(ResourceLocation resourcelocation, List<UnihexProvider.OverrideRange> list) {
         this.hexFile = resourcelocation;
         this.sizeOverrides = list;
      }

      public GlyphProviderType type() {
         return GlyphProviderType.UNIHEX;
      }

      public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
         return Either.left(this::load);
      }

      private GlyphProvider load(ResourceManager resourcemanager) throws IOException {
         InputStream inputstream = resourcemanager.open(this.hexFile);

         UnihexProvider var3;
         try {
            var3 = this.loadData(inputstream);
         } catch (Throwable var6) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var3;
      }

      private UnihexProvider loadData(InputStream inputstream) throws IOException {
         CodepointMap<UnihexProvider.LineData> codepointmap = new CodepointMap<>((k2) -> new UnihexProvider.LineData[k2], (j2) -> new UnihexProvider.LineData[j2][]);
         UnihexProvider.ReaderOutput unihexprovider_readeroutput = codepointmap::put;
         ZipInputStream zipinputstream = new ZipInputStream(inputstream);

         UnihexProvider var17;
         try {
            ZipEntry zipentry;
            while((zipentry = zipinputstream.getNextEntry()) != null) {
               String s = zipentry.getName();
               if (s.endsWith(".hex")) {
                  UnihexProvider.LOGGER.info("Found {}, loading", (Object)s);
                  UnihexProvider.readFromStream(new FastBufferedInputStream(zipinputstream), unihexprovider_readeroutput);
               }
            }

            CodepointMap<UnihexProvider.Glyph> codepointmap1 = new CodepointMap<>((i2) -> new UnihexProvider.Glyph[i2], (l1) -> new UnihexProvider.Glyph[l1][]);

            for(UnihexProvider.OverrideRange unihexprovider_overriderange : this.sizeOverrides) {
               int i = unihexprovider_overriderange.from;
               int j = unihexprovider_overriderange.to;
               UnihexProvider.Dimensions unihexprovider_dimensions = unihexprovider_overriderange.dimensions;

               for(int k = i; k <= j; ++k) {
                  UnihexProvider.LineData unihexprovider_linedata = codepointmap.remove(k);
                  if (unihexprovider_linedata != null) {
                     codepointmap1.put(k, new UnihexProvider.Glyph(unihexprovider_linedata, unihexprovider_dimensions.left, unihexprovider_dimensions.right));
                  }
               }
            }

            codepointmap.forEach((l, unihexprovider_linedata1) -> {
               int i1 = unihexprovider_linedata1.calculateWidth();
               int j1 = UnihexProvider.Dimensions.left(i1);
               int k1 = UnihexProvider.Dimensions.right(i1);
               codepointmap1.put(l, new UnihexProvider.Glyph(unihexprovider_linedata1, j1, k1));
            });
            var17 = new UnihexProvider(codepointmap1);
         } catch (Throwable var15) {
            try {
               zipinputstream.close();
            } catch (Throwable var14) {
               var15.addSuppressed(var14);
            }

            throw var15;
         }

         zipinputstream.close();
         return var17;
      }
   }

   public static record Dimensions(int left, int right) {
      final int left;
      final int right;
      public static final MapCodec<UnihexProvider.Dimensions> MAP_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("left").forGetter(UnihexProvider.Dimensions::left), Codec.INT.fieldOf("right").forGetter(UnihexProvider.Dimensions::right)).apply(recordcodecbuilder_instance, UnihexProvider.Dimensions::new));
      public static final Codec<UnihexProvider.Dimensions> CODEC = MAP_CODEC.codec();

      public int pack() {
         return pack(this.left, this.right);
      }

      public static int pack(int i, int j) {
         return (i & 255) << 8 | j & 255;
      }

      public static int left(int i) {
         return (byte)(i >> 8);
      }

      public static int right(int i) {
         return (byte)i;
      }
   }

   static record Glyph(UnihexProvider.LineData contents, int left, int right) implements GlyphInfo {
      final UnihexProvider.LineData contents;
      final int left;
      final int right;

      public int width() {
         return this.right - this.left + 1;
      }

      public float getAdvance() {
         return (float)(this.width() / 2 + 1);
      }

      public float getShadowOffset() {
         return 0.5F;
      }

      public float getBoldOffset() {
         return 0.5F;
      }

      public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
         return function.apply(new SheetGlyphInfo() {
            public float getOversample() {
               return 2.0F;
            }

            public int getPixelWidth() {
               return Glyph.this.width();
            }

            public int getPixelHeight() {
               return 16;
            }

            public void upload(int i, int j) {
               IntBuffer intbuffer = MemoryUtil.memAllocInt(Glyph.this.width() * 16);
               UnihexProvider.unpackBitsToBytes(intbuffer, Glyph.this.contents, Glyph.this.left, Glyph.this.right);
               intbuffer.rewind();
               GlStateManager.upload(0, i, j, Glyph.this.width(), 16, NativeImage.Format.RGBA, intbuffer, MemoryUtil::memFree);
            }

            public boolean isColored() {
               return true;
            }
         });
      }
   }

   static record IntContents(int[] contents, int bitWidth) implements UnihexProvider.LineData {
      private static final int SIZE_24 = 24;

      public int line(int i) {
         return this.contents[i];
      }

      static UnihexProvider.LineData read24(int i, ByteList bytelist) {
         int[] aint = new int[16];
         int j = 0;
         int k = 0;

         for(int l = 0; l < 16; ++l) {
            int i1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int j1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int k1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int l1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int i2 = UnihexProvider.decodeHex(i, bytelist, k++);
            int j2 = UnihexProvider.decodeHex(i, bytelist, k++);
            int k2 = i1 << 20 | j1 << 16 | k1 << 12 | l1 << 8 | i2 << 4 | j2;
            aint[l] = k2 << 8;
            j |= k2;
         }

         return new UnihexProvider.IntContents(aint, 24);
      }

      public static UnihexProvider.LineData read32(int i, ByteList bytelist) {
         int[] aint = new int[16];
         int j = 0;
         int k = 0;

         for(int l = 0; l < 16; ++l) {
            int i1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int j1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int k1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int l1 = UnihexProvider.decodeHex(i, bytelist, k++);
            int i2 = UnihexProvider.decodeHex(i, bytelist, k++);
            int j2 = UnihexProvider.decodeHex(i, bytelist, k++);
            int k2 = UnihexProvider.decodeHex(i, bytelist, k++);
            int l2 = UnihexProvider.decodeHex(i, bytelist, k++);
            int i3 = i1 << 28 | j1 << 24 | k1 << 20 | l1 << 16 | i2 << 12 | j2 << 8 | k2 << 4 | l2;
            aint[l] = i3;
            j |= i3;
         }

         return new UnihexProvider.IntContents(aint, 32);
      }
   }

   public interface LineData {
      int line(int i);

      int bitWidth();

      default int mask() {
         int i = 0;

         for(int j = 0; j < 16; ++j) {
            i |= this.line(j);
         }

         return i;
      }

      default int calculateWidth() {
         int i = this.mask();
         int j = this.bitWidth();
         int k;
         int l;
         if (i == 0) {
            k = 0;
            l = j;
         } else {
            k = Integer.numberOfLeadingZeros(i);
            l = 32 - Integer.numberOfTrailingZeros(i) - 1;
         }

         return UnihexProvider.Dimensions.pack(k, l);
      }
   }

   static record OverrideRange(int from, int to, UnihexProvider.Dimensions dimensions) {
      final int from;
      final int to;
      final UnihexProvider.Dimensions dimensions;
      private static final Codec<UnihexProvider.OverrideRange> RAW_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.CODEPOINT.fieldOf("from").forGetter(UnihexProvider.OverrideRange::from), ExtraCodecs.CODEPOINT.fieldOf("to").forGetter(UnihexProvider.OverrideRange::to), UnihexProvider.Dimensions.MAP_CODEC.forGetter(UnihexProvider.OverrideRange::dimensions)).apply(recordcodecbuilder_instance, UnihexProvider.OverrideRange::new));
      public static final Codec<UnihexProvider.OverrideRange> CODEC = ExtraCodecs.validate(RAW_CODEC, (unihexprovider_overriderange) -> unihexprovider_overriderange.from >= unihexprovider_overriderange.to ? DataResult.error(() -> "Invalid range: [" + unihexprovider_overriderange.from + ";" + unihexprovider_overriderange.to + "]") : DataResult.success(unihexprovider_overriderange));
   }

   @FunctionalInterface
   public interface ReaderOutput {
      void accept(int i, UnihexProvider.LineData unihexprovider_linedata);
   }

   static record ShortContents(short[] contents) implements UnihexProvider.LineData {
      public int line(int i) {
         return this.contents[i] << 16;
      }

      static UnihexProvider.LineData read(int i, ByteList bytelist) {
         short[] ashort = new short[16];
         int j = 0;

         for(int k = 0; k < 16; ++k) {
            int l = UnihexProvider.decodeHex(i, bytelist, j++);
            int i1 = UnihexProvider.decodeHex(i, bytelist, j++);
            int j1 = UnihexProvider.decodeHex(i, bytelist, j++);
            int k1 = UnihexProvider.decodeHex(i, bytelist, j++);
            short short0 = (short)(l << 12 | i1 << 8 | j1 << 4 | k1);
            ashort[k] = short0;
         }

         return new UnihexProvider.ShortContents(ashort);
      }

      public int bitWidth() {
         return 16;
      }
   }
}
