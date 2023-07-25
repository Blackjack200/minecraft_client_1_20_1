package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.util.Mth;

public class DustParticleBase<T extends DustParticleOptionsBase> extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected DustParticleBase(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, T dustparticleoptionsbase, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.friction = 0.96F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.sprites = spriteset;
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      float f = this.random.nextFloat() * 0.4F + 0.6F;
      this.rCol = this.randomizeColor(dustparticleoptionsbase.getColor().x(), f);
      this.gCol = this.randomizeColor(dustparticleoptionsbase.getColor().y(), f);
      this.bCol = this.randomizeColor(dustparticleoptionsbase.getColor().z(), f);
      this.quadSize *= 0.75F * dustparticleoptionsbase.getScale();
      int i = (int)(8.0D / (this.random.nextDouble() * 0.8D + 0.2D));
      this.lifetime = (int)Math.max((float)i * dustparticleoptionsbase.getScale(), 1.0F);
      this.setSpriteFromAge(spriteset);
   }

   protected float randomizeColor(float f, float f1) {
      return (this.random.nextFloat() * 0.2F + 0.8F) * f * f1;
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
