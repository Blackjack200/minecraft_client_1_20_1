package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class BaseAshSmokeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected BaseAshSmokeParticle(ClientLevel clientlevel, double d0, double d1, double d2, float f, float f1, float f2, double d3, double d4, double d5, float f3, SpriteSet spriteset, float f4, int i, float f5, boolean flag) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.friction = 0.96F;
      this.gravity = f5;
      this.speedUpWhenYMotionIsBlocked = true;
      this.sprites = spriteset;
      this.xd *= (double)f;
      this.yd *= (double)f1;
      this.zd *= (double)f2;
      this.xd += d3;
      this.yd += d4;
      this.zd += d5;
      float f6 = clientlevel.random.nextFloat() * f4;
      this.rCol = f6;
      this.gCol = f6;
      this.bCol = f6;
      this.quadSize *= 0.75F * f3;
      this.lifetime = (int)((double)i / ((double)clientlevel.random.nextFloat() * 0.8D + 0.2D) * (double)f3);
      this.lifetime = Math.max(this.lifetime, 1);
      this.setSpriteFromAge(spriteset);
      this.hasPhysics = flag;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public float getQuadSize(float f) {
      return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }
}
