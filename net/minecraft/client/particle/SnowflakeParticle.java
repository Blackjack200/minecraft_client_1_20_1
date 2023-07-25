package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SnowflakeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected SnowflakeParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2);
      this.gravity = 0.225F;
      this.friction = 1.0F;
      this.sprites = spriteset;
      this.xd = d3 + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
      this.yd = d4 + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
      this.zd = d5 + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
      this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 1.0F + 1.0F);
      this.lifetime = (int)(16.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D)) + 2;
      this.setSpriteFromAge(spriteset);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
      this.xd *= (double)0.95F;
      this.yd *= (double)0.9F;
      this.zd *= (double)0.95F;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SnowflakeParticle snowflakeparticle = new SnowflakeParticle(clientlevel, d0, d1, d2, d3, d4, d5, this.sprites);
         snowflakeparticle.setColor(0.923F, 0.964F, 0.999F);
         return snowflakeparticle;
      }
   }
}
