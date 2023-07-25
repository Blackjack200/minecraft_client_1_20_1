package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;

public class SquidInkParticle extends SimpleAnimatedParticle {
   SquidInkParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, int i, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, spriteset, 0.0F);
      this.friction = 0.92F;
      this.quadSize = 0.5F;
      this.setAlpha(1.0F);
      this.setColor((float)FastColor.ARGB32.red(i), (float)FastColor.ARGB32.green(i), (float)FastColor.ARGB32.blue(i));
      this.lifetime = (int)((double)(this.quadSize * 12.0F) / (Math.random() * (double)0.8F + (double)0.2F));
      this.setSpriteFromAge(spriteset);
      this.hasPhysics = false;
      this.xd = d3;
      this.yd = d4;
      this.zd = d5;
   }

   public void tick() {
      super.tick();
      if (!this.removed) {
         this.setSpriteFromAge(this.sprites);
         if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
         }

         if (this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).isAir()) {
            this.yd -= (double)0.0074F;
         }
      }

   }

   public static class GlowInkProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public GlowInkProvider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new SquidInkParticle(clientlevel, d0, d1, d2, d3, d4, d5, FastColor.ARGB32.color(255, 204, 31, 102), this.sprites);
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new SquidInkParticle(clientlevel, d0, d1, d2, d3, d4, d5, FastColor.ARGB32.color(255, 255, 255, 255), this.sprites);
      }
   }
}
