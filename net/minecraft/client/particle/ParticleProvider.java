package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

public interface ParticleProvider<T extends ParticleOptions> {
   @Nullable
   Particle createParticle(T particleoptions, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5);

   public interface Sprite<T extends ParticleOptions> {
      @Nullable
      TextureSheetParticle createParticle(T particleoptions, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5);
   }
}
