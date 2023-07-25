package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SculkChargeParticleOptions;

public class SculkChargeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   SculkChargeParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.friction = 0.96F;
      this.sprites = spriteset;
      this.scale(1.5F);
      this.hasPhysics = false;
      this.setSpriteFromAge(spriteset);
   }

   public int getLightColor(float f) {
      return 240;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }

   public static record Provider(SpriteSet sprite) implements ParticleProvider<SculkChargeParticleOptions> {
      public Particle createParticle(SculkChargeParticleOptions sculkchargeparticleoptions, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SculkChargeParticle sculkchargeparticle = new SculkChargeParticle(clientlevel, d0, d1, d2, d3, d4, d5, this.sprite);
         sculkchargeparticle.setAlpha(1.0F);
         sculkchargeparticle.setParticleSpeed(d3, d4, d5);
         sculkchargeparticle.oRoll = sculkchargeparticleoptions.roll();
         sculkchargeparticle.roll = sculkchargeparticleoptions.roll();
         sculkchargeparticle.setLifetime(clientlevel.random.nextInt(12) + 8);
         return sculkchargeparticle;
      }
   }
}
