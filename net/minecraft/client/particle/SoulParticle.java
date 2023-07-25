package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SoulParticle extends RisingParticle {
   private final SpriteSet sprites;
   protected boolean isGlowing;

   SoulParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.sprites = spriteset;
      this.scale(1.5F);
      this.setSpriteFromAge(spriteset);
   }

   public int getLightColor(float f) {
      return this.isGlowing ? 240 : super.getLightColor(f);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }

   public static class EmissiveProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public EmissiveProvider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SoulParticle soulparticle = new SoulParticle(clientlevel, d0, d1, d2, d3, d4, d5, this.sprite);
         soulparticle.setAlpha(1.0F);
         soulparticle.isGlowing = true;
         return soulparticle;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SoulParticle soulparticle = new SoulParticle(clientlevel, d0, d1, d2, d3, d4, d5, this.sprite);
         soulparticle.setAlpha(1.0F);
         return soulparticle;
      }
   }
}
