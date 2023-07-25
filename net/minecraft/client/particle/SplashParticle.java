package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SplashParticle extends WaterDropParticle {
   SplashParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2);
      this.gravity = 0.04F;
      if (d4 == 0.0D && (d3 != 0.0D || d5 != 0.0D)) {
         this.xd = d3;
         this.yd = 0.1D;
         this.zd = d5;
      }

   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SplashParticle splashparticle = new SplashParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         splashparticle.pickSprite(this.sprite);
         return splashparticle;
      }
   }
}
