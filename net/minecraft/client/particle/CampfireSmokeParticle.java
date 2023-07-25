package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class CampfireSmokeParticle extends TextureSheetParticle {
   CampfireSmokeParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, boolean flag) {
      super(clientlevel, d0, d1, d2);
      this.scale(3.0F);
      this.setSize(0.25F, 0.25F);
      if (flag) {
         this.lifetime = this.random.nextInt(50) + 280;
      } else {
         this.lifetime = this.random.nextInt(50) + 80;
      }

      this.gravity = 3.0E-6F;
      this.xd = d3;
      this.yd = d4 + (double)(this.random.nextFloat() / 500.0F);
      this.zd = d5;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
         this.xd += (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
         this.zd += (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
         this.yd -= (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
            this.alpha -= 0.015F;
         }

      } else {
         this.remove();
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public static class CosyProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public CosyProvider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         CampfireSmokeParticle campfiresmokeparticle = new CampfireSmokeParticle(clientlevel, d0, d1, d2, d3, d4, d5, false);
         campfiresmokeparticle.setAlpha(0.9F);
         campfiresmokeparticle.pickSprite(this.sprites);
         return campfiresmokeparticle;
      }
   }

   public static class SignalProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public SignalProvider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         CampfireSmokeParticle campfiresmokeparticle = new CampfireSmokeParticle(clientlevel, d0, d1, d2, d3, d4, d5, true);
         campfiresmokeparticle.setAlpha(0.95F);
         campfiresmokeparticle.pickSprite(this.sprites);
         return campfiresmokeparticle;
      }
   }
}
