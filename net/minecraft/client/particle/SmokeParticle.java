package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SmokeParticle extends BaseAshSmokeParticle {
   protected SmokeParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, float f, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, 0.1F, 0.1F, 0.1F, d3, d4, d5, f, spriteset, 0.3F, 8, -0.1F, true);
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new SmokeParticle(clientlevel, d0, d1, d2, d3, d4, d5, 1.0F, this.sprites);
      }
   }
}
