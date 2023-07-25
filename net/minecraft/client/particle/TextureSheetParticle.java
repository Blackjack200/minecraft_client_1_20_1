package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class TextureSheetParticle extends SingleQuadParticle {
   protected TextureAtlasSprite sprite;

   protected TextureSheetParticle(ClientLevel clientlevel, double d0, double d1, double d2) {
      super(clientlevel, d0, d1, d2);
   }

   protected TextureSheetParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
   }

   protected void setSprite(TextureAtlasSprite textureatlassprite) {
      this.sprite = textureatlassprite;
   }

   protected float getU0() {
      return this.sprite.getU0();
   }

   protected float getU1() {
      return this.sprite.getU1();
   }

   protected float getV0() {
      return this.sprite.getV0();
   }

   protected float getV1() {
      return this.sprite.getV1();
   }

   public void pickSprite(SpriteSet spriteset) {
      this.setSprite(spriteset.get(this.random));
   }

   public void setSpriteFromAge(SpriteSet spriteset) {
      if (!this.removed) {
         this.setSprite(spriteset.get(this.age, this.lifetime));
      }

   }
}
