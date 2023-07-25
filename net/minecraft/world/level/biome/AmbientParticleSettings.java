package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

public class AmbientParticleSettings {
   public static final Codec<AmbientParticleSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ParticleTypes.CODEC.fieldOf("options").forGetter((ambientparticlesettings1) -> ambientparticlesettings1.options), Codec.FLOAT.fieldOf("probability").forGetter((ambientparticlesettings) -> ambientparticlesettings.probability)).apply(recordcodecbuilder_instance, AmbientParticleSettings::new));
   private final ParticleOptions options;
   private final float probability;

   public AmbientParticleSettings(ParticleOptions particleoptions, float f) {
      this.options = particleoptions;
      this.probability = f;
   }

   public ParticleOptions getOptions() {
      return this.options;
   }

   public boolean canSpawn(RandomSource randomsource) {
      return randomsource.nextFloat() <= this.probability;
   }
}
