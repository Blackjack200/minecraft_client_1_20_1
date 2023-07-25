package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public class LavaParticle extends TextureSheetParticle {
   LavaParticle(ClientLevel clientlevel, double d0, double d1, double d2) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.gravity = 0.75F;
      this.friction = 0.999F;
      this.xd *= (double)0.8F;
      this.yd *= (double)0.8F;
      this.zd *= (double)0.8F;
      this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
      this.quadSize *= this.random.nextFloat() * 2.0F + 0.2F;
      this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public int getLightColor(float f) {
      int i = super.getLightColor(f);
      int j = 240;
      int k = i >> 16 & 255;
      return 240 | k << 16;
   }

   public float getQuadSize(float f) {
      float f1 = ((float)this.age + f) / (float)this.lifetime;
      return this.quadSize * (1.0F - f1 * f1);
   }

   public void tick() {
      super.tick();
      if (!this.removed) {
         float f = (float)this.age / (float)this.lifetime;
         if (this.random.nextFloat() > f) {
            this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
         }
      }

   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         LavaParticle lavaparticle = new LavaParticle(clientlevel, d0, d1, d2);
         lavaparticle.pickSprite(this.sprite);
         return lavaparticle;
      }
   }
}
