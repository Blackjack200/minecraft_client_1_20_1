package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;

public class DustParticle extends DustParticleBase<DustParticleOptions> {
   protected DustParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, DustParticleOptions dustparticleoptions, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5, dustparticleoptions, spriteset);
   }

   public static class Provider implements ParticleProvider<DustParticleOptions> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(DustParticleOptions dustparticleoptions, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new DustParticle(clientlevel, d0, d1, d2, d3, d4, d5, dustparticleoptions, this.sprites);
      }
   }
}
