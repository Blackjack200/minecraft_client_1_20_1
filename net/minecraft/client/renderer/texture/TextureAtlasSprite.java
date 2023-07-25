package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.resources.ResourceLocation;

public class TextureAtlasSprite {
   private final ResourceLocation atlasLocation;
   private final SpriteContents contents;
   final int x;
   final int y;
   private final float u0;
   private final float u1;
   private final float v0;
   private final float v1;

   protected TextureAtlasSprite(ResourceLocation resourcelocation, SpriteContents spritecontents, int i, int j, int k, int l) {
      this.atlasLocation = resourcelocation;
      this.contents = spritecontents;
      this.x = k;
      this.y = l;
      this.u0 = (float)k / (float)i;
      this.u1 = (float)(k + spritecontents.width()) / (float)i;
      this.v0 = (float)l / (float)j;
      this.v1 = (float)(l + spritecontents.height()) / (float)j;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public float getU0() {
      return this.u0;
   }

   public float getU1() {
      return this.u1;
   }

   public SpriteContents contents() {
      return this.contents;
   }

   @Nullable
   public TextureAtlasSprite.Ticker createTicker() {
      final SpriteTicker spriteticker = this.contents.createTicker();
      return spriteticker != null ? new TextureAtlasSprite.Ticker() {
         public void tickAndUpload() {
            spriteticker.tickAndUpload(TextureAtlasSprite.this.x, TextureAtlasSprite.this.y);
         }

         public void close() {
            spriteticker.close();
         }
      } : null;
   }

   public float getU(double d0) {
      float f = this.u1 - this.u0;
      return this.u0 + f * (float)d0 / 16.0F;
   }

   public float getUOffset(float f) {
      float f1 = this.u1 - this.u0;
      return (f - this.u0) / f1 * 16.0F;
   }

   public float getV0() {
      return this.v0;
   }

   public float getV1() {
      return this.v1;
   }

   public float getV(double d0) {
      float f = this.v1 - this.v0;
      return this.v0 + f * (float)d0 / 16.0F;
   }

   public float getVOffset(float f) {
      float f1 = this.v1 - this.v0;
      return (f - this.v0) / f1 * 16.0F;
   }

   public ResourceLocation atlasLocation() {
      return this.atlasLocation;
   }

   public String toString() {
      return "TextureAtlasSprite{contents='" + this.contents + "', u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + "}";
   }

   public void uploadFirstFrame() {
      this.contents.uploadFirstFrame(this.x, this.y);
   }

   private float atlasSize() {
      float f = (float)this.contents.width() / (this.u1 - this.u0);
      float f1 = (float)this.contents.height() / (this.v1 - this.v0);
      return Math.max(f1, f);
   }

   public float uvShrinkRatio() {
      return 4.0F / this.atlasSize();
   }

   public VertexConsumer wrap(VertexConsumer vertexconsumer) {
      return new SpriteCoordinateExpander(vertexconsumer, this);
   }

   public interface Ticker extends AutoCloseable {
      void tickAndUpload();

      void close();
   }
}
