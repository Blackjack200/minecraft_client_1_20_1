package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class HugeExplosionParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected HugeExplosionParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.lifetime = 6 + this.random.nextInt(4);
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.quadSize = 2.0F * (1.0F - (float)d3 * 0.5F);
      this.sprites = spriteset;
      this.setSpriteFromAge(spriteset);
   }

   public int getLightColor(float f) {
      return 15728880;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.setSpriteFromAge(this.sprites);
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_LIT;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new HugeExplosionParticle(clientlevel, d0, d1, d2, d3, this.sprites);
      }
   }
}
