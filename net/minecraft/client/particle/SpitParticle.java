package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SpitParticle extends ExplodeParticle {
   SpitParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5, spriteset);
      this.gravity = 0.5F;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new SpitParticle(clientlevel, d0, d1, d2, d3, d4, d5, this.sprites);
      }
   }
}
