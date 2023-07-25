package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FlameParticle extends RisingParticle {
   FlameParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double d0, double d1, double d2) {
      this.setBoundingBox(this.getBoundingBox().move(d0, d1, d2));
      this.setLocationFromBoundingbox();
   }

   public float getQuadSize(float f) {
      float f1 = ((float)this.age + f) / (float)this.lifetime;
      return this.quadSize * (1.0F - f1 * f1 * 0.5F);
   }

   public int getLightColor(float f) {
      float f1 = ((float)this.age + f) / (float)this.lifetime;
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      int i = super.getLightColor(f);
      int j = i & 255;
      int k = i >> 16 & 255;
      j += (int)(f1 * 15.0F * 16.0F);
      if (j > 240) {
         j = 240;
      }

      return j | k << 16;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         FlameParticle flameparticle = new FlameParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         flameparticle.pickSprite(this.sprite);
         return flameparticle;
      }
   }

   public static class SmallFlameProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public SmallFlameProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         FlameParticle flameparticle = new FlameParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         flameparticle.pickSprite(this.sprite);
         flameparticle.scale(0.5F);
         return flameparticle;
      }
   }
}
