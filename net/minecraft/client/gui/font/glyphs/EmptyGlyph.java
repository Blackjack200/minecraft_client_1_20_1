package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class EmptyGlyph extends BakedGlyph {
   public static final EmptyGlyph INSTANCE = new EmptyGlyph();

   public EmptyGlyph() {
      super(GlyphRenderTypes.createForColorTexture(new ResourceLocation("")), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   public void render(boolean flag, float f, float f1, Matrix4f matrix4f, VertexConsumer vertexconsumer, float f2, float f3, float f4, float f5, int i) {
   }
}
