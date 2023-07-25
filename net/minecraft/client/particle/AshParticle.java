package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class AshParticle extends BaseAshSmokeParticle {
   protected AshParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, float f, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, 0.1F, -0.1F, 0.1F, d3, d4, d5, f, spriteset, 0.5F, 20, 0.1F, false);
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new AshParticle(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D, 1.0F, this.sprites);
      }
   }
}
