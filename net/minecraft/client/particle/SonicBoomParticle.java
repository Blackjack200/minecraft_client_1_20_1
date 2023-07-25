package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SonicBoomParticle extends HugeExplosionParticle {
   protected SonicBoomParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, spriteset);
      this.lifetime = 16;
      this.quadSize = 1.5F;
      this.setSpriteFromAge(spriteset);
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new SonicBoomParticle(clientlevel, d0, d1, d2, d3, this.sprites);
      }
   }
}
