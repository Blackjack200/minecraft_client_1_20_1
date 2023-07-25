package net.minecraft.core.particles;

import com.mojang.serialization.Codec;

public abstract class ParticleType<T extends ParticleOptions> {
   private final boolean overrideLimiter;
   private final ParticleOptions.Deserializer<T> deserializer;

   protected ParticleType(boolean flag, ParticleOptions.Deserializer<T> particleoptions_deserializer) {
      this.overrideLimiter = flag;
      this.deserializer = particleoptions_deserializer;
   }

   public boolean getOverrideLimiter() {
      return this.overrideLimiter;
   }

   public ParticleOptions.Deserializer<T> getDeserializer() {
      return this.deserializer;
   }

   public abstract Codec<T> codec();
}
