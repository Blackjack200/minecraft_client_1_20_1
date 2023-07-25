package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class BakedGlyph {
   private final GlyphRenderTypes renderTypes;
   private final float u0;
   private final float u1;
   private final float v0;
   private final float v1;
   private final float left;
   private final float right;
   private final float up;
   private final float down;

   public BakedGlyph(GlyphRenderTypes glyphrendertypes, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
      this.renderTypes = glyphrendertypes;
      this.u0 = f;
      this.u1 = f1;
      this.v0 = f2;
      this.v1 = f3;
      this.left = f4;
      this.right = f5;
      this.up = f6;
      this.down = f7;
   }

   public void render(boolean flag, float f, float f1, Matrix4f matrix4f, VertexConsumer vertexconsumer, float f2, float f3, float f4, float f5, int i) {
      int j = 3;
      float f6 = f + this.left;
      float f7 = f + this.right;
      float f8 = this.up - 3.0F;
      float f9 = this.down - 3.0F;
      float f10 = f1 + f8;
      float f11 = f1 + f9;
      float f12 = flag ? 1.0F - 0.25F * f8 : 0.0F;
      float f13 = flag ? 1.0F - 0.25F * f9 : 0.0F;
      vertexconsumer.vertex(matrix4f, f6 + f12, f10, 0.0F).color(f2, f3, f4, f5).uv(this.u0, this.v0).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, f6 + f13, f11, 0.0F).color(f2, f3, f4, f5).uv(this.u0, this.v1).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, f7 + f13, f11, 0.0F).color(f2, f3, f4, f5).uv(this.u1, this.v1).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, f7 + f12, f10, 0.0F).color(f2, f3, f4, f5).uv(this.u1, this.v0).uv2(i).endVertex();
   }

   public void renderEffect(BakedGlyph.Effect bakedglyph_effect, Matrix4f matrix4f, VertexConsumer vertexconsumer, int i) {
      vertexconsumer.vertex(matrix4f, bakedglyph_effect.x0, bakedglyph_effect.y0, bakedglyph_effect.depth).color(bakedglyph_effect.r, bakedglyph_effect.g, bakedglyph_effect.b, bakedglyph_effect.a).uv(this.u0, this.v0).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, bakedglyph_effect.x1, bakedglyph_effect.y0, bakedglyph_effect.depth).color(bakedglyph_effect.r, bakedglyph_effect.g, bakedglyph_effect.b, bakedglyph_effect.a).uv(this.u0, this.v1).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, bakedglyph_effect.x1, bakedglyph_effect.y1, bakedglyph_effect.depth).color(bakedglyph_effect.r, bakedglyph_effect.g, bakedglyph_effect.b, bakedglyph_effect.a).uv(this.u1, this.v1).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, bakedglyph_effect.x0, bakedglyph_effect.y1, bakedglyph_effect.depth).color(bakedglyph_effect.r, bakedglyph_effect.g, bakedglyph_effect.b, bakedglyph_effect.a).uv(this.u1, this.v0).uv2(i).endVertex();
   }

   public RenderType renderType(Font.DisplayMode font_displaymode) {
      return this.renderTypes.select(font_displaymode);
   }

   public static class Effect {
      protected final float x0;
      protected final float y0;
      protected final float x1;
      protected final float y1;
      protected final float depth;
      protected final float r;
      protected final float g;
      protected final float b;
      protected final float a;

      public Effect(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
         this.x0 = f;
         this.y0 = f1;
         this.x1 = f2;
         this.y1 = f3;
         this.depth = f4;
         this.r = f5;
         this.g = f6;
         this.b = f7;
         this.a = f8;
      }
   }
}
