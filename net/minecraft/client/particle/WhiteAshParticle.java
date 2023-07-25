package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class WhiteAshParticle extends BaseAshSmokeParticle {
   private static final int COLOR_RGB24 = 12235202;

   protected WhiteAshParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, float f, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, 0.1F, -0.1F, 0.1F, d3, d4, d5, f, spriteset, 0.0F, 20, 0.0125F, false);
      this.rCol = 0.7294118F;
      this.gCol = 0.69411767F;
      this.bCol = 0.7607843F;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         RandomSource randomsource = clientlevel.random;
         double d6 = (double)randomsource.nextFloat() * -1.9D * (double)randomsource.nextFloat() * 0.1D;
         double d7 = (double)randomsource.nextFloat() * -0.5D * (double)randomsource.nextFloat() * 0.1D * 5.0D;
         double d8 = (double)randomsource.nextFloat() * -1.9D * (double)randomsource.nextFloat() * 0.1D;
         return new WhiteAshParticle(clientlevel, d0, d1, d2, d6, d7, d8, 1.0F, this.sprites);
      }
   }
}
