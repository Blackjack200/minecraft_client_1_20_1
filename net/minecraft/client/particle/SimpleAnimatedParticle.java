package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;

public class SimpleAnimatedParticle extends TextureSheetParticle {
   protected final SpriteSet sprites;
   private float fadeR;
   private float fadeG;
   private float fadeB;
   private boolean hasFade;

   protected SimpleAnimatedParticle(ClientLevel clientlevel, double d0, double d1, double d2, SpriteSet spriteset, float f) {
      super(clientlevel, d0, d1, d2);
      this.friction = 0.91F;
      this.gravity = f;
      this.sprites = spriteset;
   }

   public void setColor(int i) {
      float f = (float)((i & 16711680) >> 16) / 255.0F;
      float f1 = (float)((i & '\uff00') >> 8) / 255.0F;
      float f2 = (float)((i & 255) >> 0) / 255.0F;
      float f3 = 1.0F;
      this.setColor(f * 1.0F, f1 * 1.0F, f2 * 1.0F);
   }

   public void setFadeColor(int i) {
      this.fadeR = (float)((i & 16711680) >> 16) / 255.0F;
      this.fadeG = (float)((i & '\uff00') >> 8) / 255.0F;
      this.fadeB = (float)((i & 255) >> 0) / 255.0F;
      this.hasFade = true;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
      if (this.age > this.lifetime / 2) {
         this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
         if (this.hasFade) {
            this.rCol += (this.fadeR - this.rCol) * 0.2F;
            this.gCol += (this.fadeG - this.gCol) * 0.2F;
            this.bCol += (this.fadeB - this.bCol) * 0.2F;
         }
      }

   }

   public int getLightColor(float f) {
      return 15728880;
   }
}
