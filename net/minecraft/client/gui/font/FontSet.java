package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FontSet implements AutoCloseable {
   private static final RandomSource RANDOM = RandomSource.create();
   private static final float LARGE_FORWARD_ADVANCE = 32.0F;
   private final TextureManager textureManager;
   private final ResourceLocation name;
   private BakedGlyph missingGlyph;
   private BakedGlyph whiteGlyph;
   private final List<GlyphProvider> providers = Lists.newArrayList();
   private final CodepointMap<BakedGlyph> glyphs = new CodepointMap<>((l) -> new BakedGlyph[l], (k) -> new BakedGlyph[k][]);
   private final CodepointMap<FontSet.GlyphInfoFilter> glyphInfos = new CodepointMap<>((j) -> new FontSet.GlyphInfoFilter[j], (i) -> new FontSet.GlyphInfoFilter[i][]);
   private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
   private final List<FontTexture> textures = Lists.newArrayList();

   public FontSet(TextureManager texturemanager, ResourceLocation resourcelocation) {
      this.textureManager = texturemanager;
      this.name = resourcelocation;
   }

   public void reload(List<GlyphProvider> list) {
      this.closeProviders();
      this.closeTextures();
      this.glyphs.clear();
      this.glyphInfos.clear();
      this.glyphsByWidth.clear();
      this.missingGlyph = SpecialGlyphs.MISSING.bake(this::stitch);
      this.whiteGlyph = SpecialGlyphs.WHITE.bake(this::stitch);
      IntSet intset = new IntOpenHashSet();

      for(GlyphProvider glyphprovider : list) {
         intset.addAll(glyphprovider.getSupportedGlyphs());
      }

      Set<GlyphProvider> set = Sets.newHashSet();
      intset.forEach((i) -> {
         for(GlyphProvider glyphprovider1 : list) {
            GlyphInfo glyphinfo = glyphprovider1.getGlyph(i);
            if (glyphinfo != null) {
               set.add(glyphprovider1);
               if (glyphinfo != SpecialGlyphs.MISSING) {
                  this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphinfo.getAdvance(false)), (j) -> new IntArrayList()).add(i);
               }
               break;
            }
         }

      });
      list.stream().filter(set::contains).forEach(this.providers::add);
   }

   public void close() {
      this.closeProviders();
      this.closeTextures();
   }

   private void closeProviders() {
      for(GlyphProvider glyphprovider : this.providers) {
         glyphprovider.close();
      }

      this.providers.clear();
   }

   private void closeTextures() {
      for(FontTexture fonttexture : this.textures) {
         fonttexture.close();
      }

      this.textures.clear();
   }

   private static boolean hasFishyAdvance(GlyphInfo glyphinfo) {
      float f = glyphinfo.getAdvance(false);
      if (!(f < 0.0F) && !(f > 32.0F)) {
         float f1 = glyphinfo.getAdvance(true);
         return f1 < 0.0F || f1 > 32.0F;
      } else {
         return true;
      }
   }

   private FontSet.GlyphInfoFilter computeGlyphInfo(int i) {
      GlyphInfo glyphinfo = null;

      for(GlyphProvider glyphprovider : this.providers) {
         GlyphInfo glyphinfo1 = glyphprovider.getGlyph(i);
         if (glyphinfo1 != null) {
            if (glyphinfo == null) {
               glyphinfo = glyphinfo1;
            }

            if (!hasFishyAdvance(glyphinfo1)) {
               return new FontSet.GlyphInfoFilter(glyphinfo, glyphinfo1);
            }
         }
      }

      return glyphinfo != null ? new FontSet.GlyphInfoFilter(glyphinfo, SpecialGlyphs.MISSING) : FontSet.GlyphInfoFilter.MISSING;
   }

   public GlyphInfo getGlyphInfo(int i, boolean flag) {
      return this.glyphInfos.computeIfAbsent(i, this::computeGlyphInfo).select(flag);
   }

   private BakedGlyph computeBakedGlyph(int i) {
      for(GlyphProvider glyphprovider : this.providers) {
         GlyphInfo glyphinfo = glyphprovider.getGlyph(i);
         if (glyphinfo != null) {
            return glyphinfo.bake(this::stitch);
         }
      }

      return this.missingGlyph;
   }

   public BakedGlyph getGlyph(int i) {
      return this.glyphs.computeIfAbsent(i, this::computeBakedGlyph);
   }

   private BakedGlyph stitch(SheetGlyphInfo sheetglyphinfo) {
      for(FontTexture fonttexture : this.textures) {
         BakedGlyph bakedglyph = fonttexture.add(sheetglyphinfo);
         if (bakedglyph != null) {
            return bakedglyph;
         }
      }

      ResourceLocation resourcelocation = this.name.withSuffix("/" + this.textures.size());
      boolean flag = sheetglyphinfo.isColored();
      GlyphRenderTypes glyphrendertypes = flag ? GlyphRenderTypes.createForColorTexture(resourcelocation) : GlyphRenderTypes.createForIntensityTexture(resourcelocation);
      FontTexture fonttexture1 = new FontTexture(glyphrendertypes, flag);
      this.textures.add(fonttexture1);
      this.textureManager.register(resourcelocation, fonttexture1);
      BakedGlyph bakedglyph1 = fonttexture1.add(sheetglyphinfo);
      return bakedglyph1 == null ? this.missingGlyph : bakedglyph1;
   }

   public BakedGlyph getRandomGlyph(GlyphInfo glyphinfo) {
      IntList intlist = this.glyphsByWidth.get(Mth.ceil(glyphinfo.getAdvance(false)));
      return intlist != null && !intlist.isEmpty() ? this.getGlyph(intlist.getInt(RANDOM.nextInt(intlist.size()))) : this.missingGlyph;
   }

   public BakedGlyph whiteGlyph() {
      return this.whiteGlyph;
   }

   static record GlyphInfoFilter(GlyphInfo glyphInfo, GlyphInfo glyphInfoNotFishy) {
      static final FontSet.GlyphInfoFilter MISSING = new FontSet.GlyphInfoFilter(SpecialGlyphs.MISSING, SpecialGlyphs.MISSING);

      GlyphInfo select(boolean flag) {
         return flag ? this.glyphInfoNotFishy : this.glyphInfo;
      }
   }
}
