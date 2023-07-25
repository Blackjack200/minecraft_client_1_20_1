package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SculkChargePopParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   SculkChargePopParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.friction = 0.96F;
      this.sprites = spriteset;
      this.scale(1.0F);
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

   public static record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         SculkChargePopParticle sculkchargepopparticle = new SculkChargePopParticle(clientlevel, d0, d1, d2, d3, d4, d5, this.sprite);
         sculkchargepopparticle.setAlpha(1.0F);
         sculkchargepopparticle.setParticleSpeed(d3, d4, d5);
         sculkchargepopparticle.setLifetime(clientlevel.random.nextInt(4) + 6);
         return sculkchargepopparticle;
      }
   }
}
