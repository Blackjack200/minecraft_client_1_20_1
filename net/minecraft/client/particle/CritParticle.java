package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class CritParticle extends TextureSheetParticle {
   CritParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.friction = 0.7F;
      this.gravity = 0.5F;
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += d3 * 0.4D;
      this.yd += d4 * 0.4D;
      this.zd += d5 * 0.4D;
      float f = (float)(Math.random() * (double)0.3F + (double)0.6F);
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.quadSize *= 0.75F;
      this.lifetime = Math.max((int)(6.0D / (Math.random() * 0.8D + 0.6D)), 1);
      this.hasPhysics = false;
      this.tick();
   }

   public float getQuadSize(float f) {
      return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.gCol *= 0.96F;
      this.bCol *= 0.9F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public static class DamageIndicatorProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public DamageIndicatorProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         CritParticle critparticle = new CritParticle(clientlevel, d0, d1, d2, d3, d4 + 1.0D, d5);
         critparticle.setLifetime(20);
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }

   public static class MagicProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public MagicProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         CritParticle critparticle = new CritParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         critparticle.rCol *= 0.3F;
         critparticle.gCol *= 0.8F;
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         CritParticle critparticle = new CritParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }
}
