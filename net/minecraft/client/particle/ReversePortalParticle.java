package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class ReversePortalParticle extends PortalParticle {
   ReversePortalParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.quadSize *= 1.5F;
      this.lifetime = (int)(Math.random() * 2.0D) + 60;
   }

   public float getQuadSize(float f) {
      float f1 = 1.0F - ((float)this.age + f) / ((float)this.lifetime * 1.5F);
      return this.quadSize * f1;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = (float)this.age / (float)this.lifetime;
         this.x += this.xd * (double)f;
         this.y += this.yd * (double)f;
         this.z += this.zd * (double)f;
      }
   }

   public static class ReversePortalProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public ReversePortalProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         ReversePortalParticle reverseportalparticle = new ReversePortalParticle(clientlevel, d0, d1, d2, d3, d4, d5);
         reverseportalparticle.pickSprite(this.sprite);
         return reverseportalparticle;
      }
   }
}
