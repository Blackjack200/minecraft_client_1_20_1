package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class PortalParticle extends TextureSheetParticle {
   private final double xStart;
   private final double yStart;
   private final double zStart;

   protected PortalParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2);
      this.xd = d3;
      this.yd = d4;
      this.zd = d5;
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.xStart = this.x;
      this.yStart = this.y;
      this.zStart = this.z;
      this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.rCol = f * 0.9F;
      this.gCol = f * 0.3F;
      this.bCol = f;
      this.lifetime = (int)(Math.random() * 10.0D) + 40;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double d0, double d1, double d2) {
      this.setBoundingBox(this.getBoundingBox().move(d0, d1, d2));
      this.setLocationFromBoundingbox();
   }

   public float getQuadSize(float f) {
      float f1 = ((float)this.age + f) / (float)this.lifetime;
      f1 = 1.0F - f1;
      f1 *= f1;
      f1 = 1.0F - f1;
      return this.quadSize * f1;
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
         float var3 = -f + f * f * 2.0F;
         float var4 = 1.0F - var3;
         this.x = this.xStart + this.xd * (double)var4;
         this.y = this.yStart + this.yd * (double)var4 + (double)(1.0F - f);
         this.z = this.zStart + this.zd * (double)var4;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         PortalParticle portalparticle = new PortalParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         portalparticle.pickSprite(this.sprite);
         return portalparticle;
      }
   }
}
