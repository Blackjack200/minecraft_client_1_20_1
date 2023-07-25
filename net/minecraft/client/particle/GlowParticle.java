package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class GlowParticle extends TextureSheetParticle {
   static final RandomSource RANDOM = RandomSource.create();
   private final SpriteSet sprites;

   GlowParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.friction = 0.96F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.sprites = spriteset;
      this.quadSize *= 0.75F;
      this.hasPhysics = false;
      this.setSpriteFromAge(spriteset);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }

   public static class ElectricSparkProvider implements ParticleProvider<SimpleParticleType> {
      private final double SPEED_FACTOR = 0.25D;
      private final SpriteSet sprite;

      public ElectricSparkProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         GlowParticle glowparticle = new GlowParticle(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D, this.sprite);
         glowparticle.setColor(1.0F, 0.9F, 1.0F);
         glowparticle.setParticleSpeed(d3 * 0.25D, d4 * 0.25D, d5 * 0.25D);
         int i = 2;
         int j = 4;
         glowparticle.setLifetime(clientlevel.random.nextInt(2) + 2);
         return glowparticle;
      }
   }

   public static class GlowSquidProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public GlowSquidProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         GlowParticle glowparticle = new GlowParticle(clientlevel, d0, d1, d2, 0.5D - GlowParticle.RANDOM.nextDouble(), d4, 0.5D - GlowParticle.RANDOM.nextDouble(), this.sprite);
         if (clientlevel.random.nextBoolean()) {
            glowparticle.setColor(0.6F, 1.0F, 0.8F);
         } else {
            glowparticle.setColor(0.08F, 0.4F, 0.4F);
         }

         glowparticle.yd *= (double)0.2F;
         if (d3 == 0.0D && d5 == 0.0D) {
            glowparticle.xd *= (double)0.1F;
            glowparticle.zd *= (double)0.1F;
         }

         glowparticle.setLifetime((int)(8.0D / (clientlevel.random.nextDouble() * 0.8D + 0.2D)));
         return glowparticle;
      }
   }

   public static class ScrapeProvider implements ParticleProvider<SimpleParticleType> {
      private final double SPEED_FACTOR = 0.01D;
      private final SpriteSet sprite;

      public ScrapeProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         GlowParticle glowparticle = new GlowParticle(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D, this.sprite);
         if (clientlevel.random.nextBoolean()) {
            glowparticle.setColor(0.29F, 0.58F, 0.51F);
         } else {
            glowparticle.setColor(0.43F, 0.77F, 0.62F);
         }

         glowparticle.setParticleSpeed(d3 * 0.01D, d4 * 0.01D, d5 * 0.01D);
         int i = 10;
         int j = 40;
         glowparticle.setLifetime(clientlevel.random.nextInt(30) + 10);
         return glowparticle;
      }
   }

   public static class WaxOffProvider implements ParticleProvider<SimpleParticleType> {
      private final double SPEED_FACTOR = 0.01D;
      private final SpriteSet sprite;

      public WaxOffProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         GlowParticle glowparticle = new GlowParticle(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D, this.sprite);
         glowparticle.setColor(1.0F, 0.9F, 1.0F);
         glowparticle.setParticleSpeed(d3 * 0.01D / 2.0D, d4 * 0.01D, d5 * 0.01D / 2.0D);
         int i = 10;
         int j = 40;
         glowparticle.setLifetime(clientlevel.random.nextInt(30) + 10);
         return glowparticle;
      }
   }

   public static class WaxOnProvider implements ParticleProvider<SimpleParticleType> {
      private final double SPEED_FACTOR = 0.01D;
      private final SpriteSet sprite;

      public WaxOnProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         GlowParticle glowparticle = new GlowParticle(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D, this.sprite);
         glowparticle.setColor(0.91F, 0.55F, 0.08F);
         glowparticle.setParticleSpeed(d3 * 0.01D / 2.0D, d4 * 0.01D, d5 * 0.01D / 2.0D);
         int i = 10;
         int j = 40;
         glowparticle.setLifetime(clientlevel.random.nextInt(30) + 10);
         return glowparticle;
      }
   }
}
