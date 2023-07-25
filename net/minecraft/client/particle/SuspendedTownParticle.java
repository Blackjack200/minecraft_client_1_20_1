package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SuspendedTownParticle extends TextureSheetParticle {
   SuspendedTownParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      float f = this.random.nextFloat() * 0.1F + 0.2F;
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.setSize(0.02F, 0.02F);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
      this.xd *= (double)0.02F;
      this.yd *= (double)0.02F;
      this.zd *= (double)0.02F;
      this.lifetime = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double d0, double d1, double d2) {
      this.setBoundingBox(this.getBoundingBox().move(d0, d1, d2));
      this.setLocationFromBoundingbox();
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.99D;
         this.yd *= 0.99D;
         this.zd *= 0.99D;
      }
   }

   public static class ComposterFillProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public ComposterFillProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
         suspendedtownparticle.setLifetime(3 + clientlevel.getRandom().nextInt(5));
         return suspendedtownparticle;
      }
   }

   public static class DolphinSpeedProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public DolphinSpeedProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         suspendedtownparticle.setColor(0.3F, 0.5F, 1.0F);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setAlpha(1.0F - clientlevel.random.nextFloat() * 0.7F);
         suspendedtownparticle.setLifetime(suspendedtownparticle.getLifetime() / 2);
         return suspendedtownparticle;
      }
   }

   public static class EggCrackProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public EggCrackProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
         return suspendedtownparticle;
      }
   }

   public static class HappyVillagerProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public HappyVillagerProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
         return suspendedtownparticle;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         suspendedtownparticle.pickSprite(this.sprite);
         return suspendedtownparticle;
      }
   }
}
