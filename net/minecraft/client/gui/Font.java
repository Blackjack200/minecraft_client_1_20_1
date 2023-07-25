package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Font {
   private static final float EFFECT_DEPTH = 0.01F;
   private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
   public static final int ALPHA_CUTOFF = 8;
   public final int lineHeight = 9;
   public final RandomSource random = RandomSource.create();
   private final Function<ResourceLocation, FontSet> fonts;
   final boolean filterFishyGlyphs;
   private final StringSplitter splitter;

   public Font(Function<ResourceLocation, FontSet> function, boolean flag) {
      this.fonts = function;
      this.filterFishyGlyphs = flag;
      this.splitter = new StringSplitter((i, style) -> this.getFontSet(style.getFont()).getGlyphInfo(i, this.filterFishyGlyphs).getAdvance(style.isBold()));
   }

   FontSet getFontSet(ResourceLocation resourcelocation) {
      return this.fonts.apply(resourcelocation);
   }

   public String bidirectionalShaping(String s) {
      try {
         Bidi bidi = new Bidi((new ArabicShaping(8)).shape(s), 127);
         bidi.setReorderingMode(0);
         return bidi.writeReordered(2);
      } catch (ArabicShapingException var3) {
         return s;
      }
   }

   public int drawInBatch(String s, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k) {
      return this.drawInBatch(s, f, f1, i, flag, matrix4f, multibuffersource, font_displaymode, j, k, this.isBidirectional());
   }

   public int drawInBatch(String s, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k, boolean flag1) {
      return this.drawInternal(s, f, f1, i, flag, matrix4f, multibuffersource, font_displaymode, j, k, flag1);
   }

   public int drawInBatch(Component component, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k) {
      return this.drawInBatch(component.getVisualOrderText(), f, f1, i, flag, matrix4f, multibuffersource, font_displaymode, j, k);
   }

   public int drawInBatch(FormattedCharSequence formattedcharsequence, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k) {
      return this.drawInternal(formattedcharsequence, f, f1, i, flag, matrix4f, multibuffersource, font_displaymode, j, k);
   }

   public void drawInBatch8xOutline(FormattedCharSequence formattedcharsequence, float f, float f1, int i, int j, Matrix4f matrix4f, MultiBufferSource multibuffersource, int k) {
      int l = adjustColor(j);
      Font.StringRenderOutput font_stringrenderoutput = new Font.StringRenderOutput(multibuffersource, 0.0F, 0.0F, l, false, matrix4f, Font.DisplayMode.NORMAL, k);

      for(int i1 = -1; i1 <= 1; ++i1) {
         for(int j1 = -1; j1 <= 1; ++j1) {
            if (i1 != 0 || j1 != 0) {
               float[] afloat = new float[]{f};
               int k1 = i1;
               int l1 = j1;
               formattedcharsequence.accept((l2, style, i3) -> {
                  boolean flag = style.isBold();
                  FontSet fontset = this.getFontSet(style.getFont());
                  GlyphInfo glyphinfo = fontset.getGlyphInfo(i3, this.filterFishyGlyphs);
                  font_stringrenderoutput.x = afloat[0] + (float)k1 * glyphinfo.getShadowOffset();
                  font_stringrenderoutput.y = f1 + (float)l1 * glyphinfo.getShadowOffset();
                  afloat[0] += glyphinfo.getAdvance(flag);
                  return font_stringrenderoutput.accept(l2, style.withColor(l), i3);
               });
            }
         }
      }

      Font.StringRenderOutput font_stringrenderoutput1 = new Font.StringRenderOutput(multibuffersource, f, f1, adjustColor(i), false, matrix4f, Font.DisplayMode.POLYGON_OFFSET, k);
      formattedcharsequence.accept(font_stringrenderoutput1);
      font_stringrenderoutput1.finish(0, f);
   }

   private static int adjustColor(int i) {
      return (i & -67108864) == 0 ? i | -16777216 : i;
   }

   private int drawInternal(String s, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k, boolean flag1) {
      if (flag1) {
         s = this.bidirectionalShaping(s);
      }

      i = adjustColor(i);
      Matrix4f matrix4f1 = new Matrix4f(matrix4f);
      if (flag) {
         this.renderText(s, f, f1, i, true, matrix4f, multibuffersource, font_displaymode, j, k);
         matrix4f1.translate(SHADOW_OFFSET);
      }

      f = this.renderText(s, f, f1, i, false, matrix4f1, multibuffersource, font_displaymode, j, k);
      return (int)f + (flag ? 1 : 0);
   }

   private int drawInternal(FormattedCharSequence formattedcharsequence, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k) {
      i = adjustColor(i);
      Matrix4f matrix4f1 = new Matrix4f(matrix4f);
      if (flag) {
         this.renderText(formattedcharsequence, f, f1, i, true, matrix4f, multibuffersource, font_displaymode, j, k);
         matrix4f1.translate(SHADOW_OFFSET);
      }

      f = this.renderText(formattedcharsequence, f, f1, i, false, matrix4f1, multibuffersource, font_displaymode, j, k);
      return (int)f + (flag ? 1 : 0);
   }

   private float renderText(String s, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k) {
      Font.StringRenderOutput font_stringrenderoutput = new Font.StringRenderOutput(multibuffersource, f, f1, i, flag, matrix4f, font_displaymode, k);
      StringDecomposer.iterateFormatted(s, Style.EMPTY, font_stringrenderoutput);
      return font_stringrenderoutput.finish(j, f);
   }

   private float renderText(FormattedCharSequence formattedcharsequence, float f, float f1, int i, boolean flag, Matrix4f matrix4f, MultiBufferSource multibuffersource, Font.DisplayMode font_displaymode, int j, int k) {
      Font.StringRenderOutput font_stringrenderoutput = new Font.StringRenderOutput(multibuffersource, f, f1, i, flag, matrix4f, font_displaymode, k);
      formattedcharsequence.accept(font_stringrenderoutput);
      return font_stringrenderoutput.finish(j, f);
   }

   void renderChar(BakedGlyph bakedglyph, boolean flag, boolean flag1, float f, float f1, float f2, Matrix4f matrix4f, VertexConsumer vertexconsumer, float f3, float f4, float f5, float f6, int i) {
      bakedglyph.render(flag1, f1, f2, matrix4f, vertexconsumer, f3, f4, f5, f6, i);
      if (flag) {
         bakedglyph.render(flag1, f1 + f, f2, matrix4f, vertexconsumer, f3, f4, f5, f6, i);
      }

   }

   public int width(String s) {
      return Mth.ceil(this.splitter.stringWidth(s));
   }

   public int width(FormattedText formattedtext) {
      return Mth.ceil(this.splitter.stringWidth(formattedtext));
   }

   public int width(FormattedCharSequence formattedcharsequence) {
      return Mth.ceil(this.splitter.stringWidth(formattedcharsequence));
   }

   public String plainSubstrByWidth(String s, int i, boolean flag) {
      return flag ? this.splitter.plainTailByWidth(s, i, Style.EMPTY) : this.splitter.plainHeadByWidth(s, i, Style.EMPTY);
   }

   public String plainSubstrByWidth(String s, int i) {
      return this.splitter.plainHeadByWidth(s, i, Style.EMPTY);
   }

   public FormattedText substrByWidth(FormattedText formattedtext, int i) {
      return this.splitter.headByWidth(formattedtext, i, Style.EMPTY);
   }

   public int wordWrapHeight(String s, int i) {
      return 9 * this.splitter.splitLines(s, i, Style.EMPTY).size();
   }

   public int wordWrapHeight(FormattedText formattedtext, int i) {
      return 9 * this.splitter.splitLines(formattedtext, i, Style.EMPTY).size();
   }

   public List<FormattedCharSequence> split(FormattedText formattedtext, int i) {
      return Language.getInstance().getVisualOrder(this.splitter.splitLines(formattedtext, i, Style.EMPTY));
   }

   public boolean isBidirectional() {
      return Language.getInstance().isDefaultRightToLeft();
   }

   public StringSplitter getSplitter() {
      return this.splitter;
   }

   public static enum DisplayMode {
      NORMAL,
      SEE_THROUGH,
      POLYGON_OFFSET;
   }

   class StringRenderOutput implements FormattedCharSink {
      final MultiBufferSource bufferSource;
      private final boolean dropShadow;
      private final float dimFactor;
      private final float r;
      private final float g;
      private final float b;
      private final float a;
      private final Matrix4f pose;
      private final Font.DisplayMode mode;
      private final int packedLightCoords;
      float x;
      float y;
      @Nullable
      private List<BakedGlyph.Effect> effects;

      private void addEffect(BakedGlyph.Effect bakedglyph_effect) {
         if (this.effects == null) {
            this.effects = Lists.newArrayList();
         }

         this.effects.add(bakedglyph_effect);
      }

      public StringRenderOutput(MultiBufferSource multibuffersource, float f, float f1, int i, boolean flag, Matrix4f matrix4f, Font.DisplayMode font_displaymode, int j) {
         this.bufferSource = multibuffersource;
         this.x = f;
         this.y = f1;
         this.dropShadow = flag;
         this.dimFactor = flag ? 0.25F : 1.0F;
         this.r = (float)(i >> 16 & 255) / 255.0F * this.dimFactor;
         this.g = (float)(i >> 8 & 255) / 255.0F * this.dimFactor;
         this.b = (float)(i & 255) / 255.0F * this.dimFactor;
         this.a = (float)(i >> 24 & 255) / 255.0F;
         this.pose = matrix4f;
         this.mode = font_displaymode;
         this.packedLightCoords = j;
      }

      public boolean accept(int i, Style style, int j) {
         FontSet fontset = Font.this.getFontSet(style.getFont());
         GlyphInfo glyphinfo = fontset.getGlyphInfo(j, Font.this.filterFishyGlyphs);
         BakedGlyph bakedglyph = style.isObfuscated() && j != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(j);
         boolean flag = style.isBold();
         float f = this.a;
         TextColor textcolor = style.getColor();
         float f1;
         float f2;
         float f3;
         if (textcolor != null) {
            int k = textcolor.getValue();
            f1 = (float)(k >> 16 & 255) / 255.0F * this.dimFactor;
            f2 = (float)(k >> 8 & 255) / 255.0F * this.dimFactor;
            f3 = (float)(k & 255) / 255.0F * this.dimFactor;
         } else {
            f1 = this.r;
            f2 = this.g;
            f3 = this.b;
         }

         if (!(bakedglyph instanceof EmptyGlyph)) {
            float f7 = flag ? glyphinfo.getBoldOffset() : 0.0F;
            float f8 = this.dropShadow ? glyphinfo.getShadowOffset() : 0.0F;
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
            Font.this.renderChar(bakedglyph, flag, style.isItalic(), f7, this.x + f8, this.y + f8, this.pose, vertexconsumer, f1, f2, f3, f, this.packedLightCoords);
         }

         float f9 = glyphinfo.getAdvance(flag);
         float f10 = this.dropShadow ? 1.0F : 0.0F;
         if (style.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(this.x + f10 - 1.0F, this.y + f10 + 4.5F, this.x + f10 + f9, this.y + f10 + 4.5F - 1.0F, 0.01F, f1, f2, f3, f));
         }

         if (style.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(this.x + f10 - 1.0F, this.y + f10 + 9.0F, this.x + f10 + f9, this.y + f10 + 9.0F - 1.0F, 0.01F, f1, f2, f3, f));
         }

         this.x += f9;
         return true;
      }

      public float finish(int i, float f) {
         if (i != 0) {
            float f1 = (float)(i >> 24 & 255) / 255.0F;
            float f2 = (float)(i >> 16 & 255) / 255.0F;
            float f3 = (float)(i >> 8 & 255) / 255.0F;
            float f4 = (float)(i & 255) / 255.0F;
            this.addEffect(new BakedGlyph.Effect(f - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f2, f3, f4, f1));
         }

         if (this.effects != null) {
            BakedGlyph bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));

            for(BakedGlyph.Effect bakedglyph_effect : this.effects) {
               bakedglyph.renderEffect(bakedglyph_effect, this.pose, vertexconsumer, this.packedLightCoords);
            }
         }

         return this.x;
      }
   }
}
