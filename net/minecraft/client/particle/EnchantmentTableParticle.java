package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class EnchantmentTableParticle extends TextureSheetParticle {
   private final double xStart;
   private final double yStart;
   private final double zStart;

   EnchantmentTableParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2);
      this.xd = d3;
      this.yd = d4;
      this.zd = d5;
      this.xStart = d0;
      this.yStart = d1;
      this.zStart = d2;
      this.xo = d0 + d3;
      this.yo = d1 + d4;
      this.zo = d2 + d5;
      this.x = this.xo;
      this.y = this.yo;
      this.z = this.zo;
      this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.rCol = 0.9F * f;
      this.gCol = 0.9F * f;
      this.bCol = f;
      this.hasPhysics = false;
      this.lifetime = (int)(Math.random() * 10.0D) + 30;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double d0, double d1, double d2) {
      this.setBoundingBox(this.getBoundingBox().move(d0, d1, d2));
      this.setLocationFromBoundingbox();
   }

   public int getLightColor(float f) {
      int i = super.getLightColor(f);
      float f1 = (float)this.age / (float)this.lifetime;
      f1 *= f1;
      f1 *= f1;
      int j = i & 255;
      int k = i >> 16 & 255;
      k += (int)(f1 * 15.0F * 16.0F);
      if (k > 240) {
         k = 240;
      }

      return j | k << 16;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = (float)this.age / (float)this.lifetime;
         f = 1.0F - f;
         float f1 = 1.0F - f;
         f1 *= f1;
         f1 *= f1;
         this.x = this.xStart + this.xd * (double)f;
         this.y = this.yStart + this.yd * (double)f - (double)(f1 * 1.2F);
         this.z = this.zStart + this.zd * (double)f;
      }
   }

   public static class NautilusProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public NautilusProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         EnchantmentTableParticle enchantmenttableparticle = new EnchantmentTableParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         enchantmenttableparticle.pickSprite(this.sprite);
         return enchantmenttableparticle;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         EnchantmentTableParticle enchantmenttableparticle = new EnchantmentTableParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         enchantmenttableparticle.pickSprite(this.sprite);
         return enchantmenttableparticle;
      }
   }
}
