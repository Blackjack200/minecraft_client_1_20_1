package net.minecraft.client.particle;

import java.util.Optional;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SuspendedParticle extends TextureSheetParticle {
   SuspendedParticle(ClientLevel clientlevel, SpriteSet spriteset, double d0, double d1, double d2) {
      super(clientlevel, d0, d1 - 0.125D, d2);
      this.setSize(0.01F, 0.01F);
      this.pickSprite(spriteset);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
      this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      this.hasPhysics = false;
      this.friction = 1.0F;
      this.gravity = 0.0F;
   }

   SuspendedParticle(ClientLevel clientlevel, SpriteSet spriteset, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1 - 0.125D, d2, d3, d4, d5);
      this.setSize(0.01F, 0.01F);
      this.pickSprite(spriteset);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.6F;
      this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      this.hasPhysics = false;
      this.friction = 1.0F;
      this.gravity = 0.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public static class CrimsonSporeProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public CrimsonSporeProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         RandomSource randomsource = clientlevel.random;
         double d6 = randomsource.nextGaussian() * (double)1.0E-6F;
         double d7 = randomsource.nextGaussian() * (double)1.0E-4F;
         double d8 = randomsource.nextGaussian() * (double)1.0E-6F;
         SuspendedParticle suspendedparticle = new SuspendedParticle(clientlevel, this.sprite, d0, d1, d2, d6, d7, d8);
         suspendedparticle.setColor(0.9F, 0.4F, 0.5F);
         return suspendedparticle;
      }
   }

   public static class SporeBlossomAirProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public SporeBlossomAirProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedParticle suspendedparticle = new SuspendedParticle(clientlevel, this.sprite, d0, d1, d2, 0.0D, (double)-0.8F, 0.0D) {
            public Optional<ParticleGroup> getParticleGroup() {
               return Optional.of(ParticleGroup.SPORE_BLOSSOM);
            }
         };
         suspendedparticle.lifetime = Mth.randomBetweenInclusive(clientlevel.random, 500, 1000);
         suspendedparticle.gravity = 0.01F;
         suspendedparticle.setColor(0.32F, 0.5F, 0.22F);
         return suspendedparticle;
      }
   }

   public static class UnderwaterProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public UnderwaterProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedParticle suspendedparticle = new SuspendedParticle(clientlevel, this.sprite, d0, d1, d2);
         suspendedparticle.setColor(0.4F, 0.4F, 0.7F);
         return suspendedparticle;
      }
   }

   public static class WarpedSporeProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public WarpedSporeProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         double d6 = (double)clientlevel.random.nextFloat() * -1.9D * (double)clientlevel.random.nextFloat() * 0.1D;
         SuspendedParticle suspendedparticle = new SuspendedParticle(clientlevel, this.sprite, d0, d1, d2, 0.0D, d6, 0.0D);
         suspendedparticle.setColor(0.1F, 0.1F, 0.3F);
         suspendedparticle.setSize(0.001F, 0.001F);
         return suspendedparticle;
      }
   }
}
