package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class NoteParticle extends TextureSheetParticle {
   NoteParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.friction = 0.66F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.xd *= (double)0.01F;
      this.yd *= (double)0.01F;
      this.zd *= (double)0.01F;
      this.yd += 0.2D;
      this.rCol = Math.max(0.0F, Mth.sin(((float)d3 + 0.0F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
      this.gCol = Math.max(0.0F, Mth.sin(((float)d3 + 0.33333334F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
      this.bCol = Math.max(0.0F, Mth.sin(((float)d3 + 0.6666667F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
      this.quadSize *= 1.5F;
      this.lifetime = 6;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public float getQuadSize(float f) {
      return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         NoteParticle noteparticle = new NoteParticle(clientlevel, d0, d1, d2, d3);
         noteparticle.pickSprite(this.sprite);
         return noteparticle;
      }
   }
}
