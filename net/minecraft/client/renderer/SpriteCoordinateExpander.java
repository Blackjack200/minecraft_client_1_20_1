package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteCoordinateExpander implements VertexConsumer {
   private final VertexConsumer delegate;
   private final TextureAtlasSprite sprite;

   public SpriteCoordinateExpander(VertexConsumer vertexconsumer, TextureAtlasSprite textureatlassprite) {
      this.delegate = vertexconsumer;
      this.sprite = textureatlassprite;
   }

   public VertexConsumer vertex(double d0, double d1, double d2) {
      return this.delegate.vertex(d0, d1, d2);
   }

   public VertexConsumer color(int i, int j, int k, int l) {
      return this.delegate.color(i, j, k, l);
   }

   public VertexConsumer uv(float f, float f1) {
      return this.delegate.uv(this.sprite.getU((double)(f * 16.0F)), this.sprite.getV((double)(f1 * 16.0F)));
   }

   public VertexConsumer overlayCoords(int i, int j) {
      return this.delegate.overlayCoords(i, j);
   }

   public VertexConsumer uv2(int i, int j) {
      return this.delegate.uv2(i, j);
   }

   public VertexConsumer normal(float f, float f1, float f2) {
      return this.delegate.normal(f, f1, f2);
   }

   public void endVertex() {
      this.delegate.endVertex();
   }

   public void defaultColor(int i, int j, int k, int l) {
      this.delegate.defaultColor(i, j, k, l);
   }

   public void unsetDefaultColor() {
      this.delegate.unsetDefaultColor();
   }

   public void vertex(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int j, float f9, float f10, float f11) {
      this.delegate.vertex(f, f1, f2, f3, f4, f5, f6, this.sprite.getU((double)(f7 * 16.0F)), this.sprite.getV((double)(f8 * 16.0F)), i, j, f9, f10, f11);
   }
}
