package net.minecraft.client.gui.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public record GlyphRenderTypes(RenderType normal, RenderType seeThrough, RenderType polygonOffset) {
   public static GlyphRenderTypes createForIntensityTexture(ResourceLocation resourcelocation) {
      return new GlyphRenderTypes(RenderType.textIntensity(resourcelocation), RenderType.textIntensitySeeThrough(resourcelocation), RenderType.textIntensityPolygonOffset(resourcelocation));
   }

   public static GlyphRenderTypes createForColorTexture(ResourceLocation resourcelocation) {
      return new GlyphRenderTypes(RenderType.text(resourcelocation), RenderType.textSeeThrough(resourcelocation), RenderType.textPolygonOffset(resourcelocation));
   }

   public RenderType select(Font.DisplayMode font_displaymode) {
      RenderType var10000;
      switch (font_displaymode) {
         case NORMAL:
            var10000 = this.normal;
            break;
         case SEE_THROUGH:
            var10000 = this.seeThrough;
            break;
         case POLYGON_OFFSET:
            var10000 = this.polygonOffset;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
