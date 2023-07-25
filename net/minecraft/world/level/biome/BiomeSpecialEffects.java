package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;

public class BiomeSpecialEffects {
   public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("fog_color").forGetter((biomespecialeffects11) -> biomespecialeffects11.fogColor), Codec.INT.fieldOf("water_color").forGetter((biomespecialeffects10) -> biomespecialeffects10.waterColor), Codec.INT.fieldOf("water_fog_color").forGetter((biomespecialeffects9) -> biomespecialeffects9.waterFogColor), Codec.INT.fieldOf("sky_color").forGetter((biomespecialeffects8) -> biomespecialeffects8.skyColor), Codec.INT.optionalFieldOf("foliage_color").forGetter((biomespecialeffects7) -> biomespecialeffects7.foliageColorOverride), Codec.INT.optionalFieldOf("grass_color").forGetter((biomespecialeffects6) -> biomespecialeffects6.grassColorOverride), BiomeSpecialEffects.GrassColorModifier.CODEC.optionalFieldOf("grass_color_modifier", BiomeSpecialEffects.GrassColorModifier.NONE).forGetter((biomespecialeffects5) -> biomespecialeffects5.grassColorModifier), AmbientParticleSettings.CODEC.optionalFieldOf("particle").forGetter((biomespecialeffects4) -> biomespecialeffects4.ambientParticleSettings), SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter((biomespecialeffects3) -> biomespecialeffects3.ambientLoopSoundEvent), AmbientMoodSettings.CODEC.optionalFieldOf("mood_sound").forGetter((biomespecialeffects2) -> biomespecialeffects2.ambientMoodSettings), AmbientAdditionsSettings.CODEC.optionalFieldOf("additions_sound").forGetter((biomespecialeffects1) -> biomespecialeffects1.ambientAdditionsSettings), Music.CODEC.optionalFieldOf("music").forGetter((biomespecialeffects) -> biomespecialeffects.backgroundMusic)).apply(recordcodecbuilder_instance, BiomeSpecialEffects::new));
   private final int fogColor;
   private final int waterColor;
   private final int waterFogColor;
   private final int skyColor;
   private final Optional<Integer> foliageColorOverride;
   private final Optional<Integer> grassColorOverride;
   private final BiomeSpecialEffects.GrassColorModifier grassColorModifier;
   private final Optional<AmbientParticleSettings> ambientParticleSettings;
   private final Optional<Holder<SoundEvent>> ambientLoopSoundEvent;
   private final Optional<AmbientMoodSettings> ambientMoodSettings;
   private final Optional<AmbientAdditionsSettings> ambientAdditionsSettings;
   private final Optional<Music> backgroundMusic;

   BiomeSpecialEffects(int i, int j, int k, int l, Optional<Integer> optional, Optional<Integer> optional1, BiomeSpecialEffects.GrassColorModifier biomespecialeffects_grasscolormodifier, Optional<AmbientParticleSettings> optional2, Optional<Holder<SoundEvent>> optional3, Optional<AmbientMoodSettings> optional4, Optional<AmbientAdditionsSettings> optional5, Optional<Music> optional6) {
      this.fogColor = i;
      this.waterColor = j;
      this.waterFogColor = k;
      this.skyColor = l;
      this.foliageColorOverride = optional;
      this.grassColorOverride = optional1;
      this.grassColorModifier = biomespecialeffects_grasscolormodifier;
      this.ambientParticleSettings = optional2;
      this.ambientLoopSoundEvent = optional3;
      this.ambientMoodSettings = optional4;
      this.ambientAdditionsSettings = optional5;
      this.backgroundMusic = optional6;
   }

   public int getFogColor() {
      return this.fogColor;
   }

   public int getWaterColor() {
      return this.waterColor;
   }

   public int getWaterFogColor() {
      return this.waterFogColor;
   }

   public int getSkyColor() {
      return this.skyColor;
   }

   public Optional<Integer> getFoliageColorOverride() {
      return this.foliageColorOverride;
   }

   public Optional<Integer> getGrassColorOverride() {
      return this.grassColorOverride;
   }

   public BiomeSpecialEffects.GrassColorModifier getGrassColorModifier() {
      return this.grassColorModifier;
   }

   public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
      return this.ambientParticleSettings;
   }

   public Optional<Holder<SoundEvent>> getAmbientLoopSoundEvent() {
      return this.ambientLoopSoundEvent;
   }

   public Optional<AmbientMoodSettings> getAmbientMoodSettings() {
      return this.ambientMoodSettings;
   }

   public Optional<AmbientAdditionsSettings> getAmbientAdditionsSettings() {
      return this.ambientAdditionsSettings;
   }

   public Optional<Music> getBackgroundMusic() {
      return this.backgroundMusic;
   }

   public static class Builder {
      private OptionalInt fogColor = OptionalInt.empty();
      private OptionalInt waterColor = OptionalInt.empty();
      private OptionalInt waterFogColor = OptionalInt.empty();
      private OptionalInt skyColor = OptionalInt.empty();
      private Optional<Integer> foliageColorOverride = Optional.empty();
      private Optional<Integer> grassColorOverride = Optional.empty();
      private BiomeSpecialEffects.GrassColorModifier grassColorModifier = BiomeSpecialEffects.GrassColorModifier.NONE;
      private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();
      private Optional<Holder<SoundEvent>> ambientLoopSoundEvent = Optional.empty();
      private Optional<AmbientMoodSettings> ambientMoodSettings = Optional.empty();
      private Optional<AmbientAdditionsSettings> ambientAdditionsSettings = Optional.empty();
      private Optional<Music> backgroundMusic = Optional.empty();

      public BiomeSpecialEffects.Builder fogColor(int i) {
         this.fogColor = OptionalInt.of(i);
         return this;
      }

      public BiomeSpecialEffects.Builder waterColor(int i) {
         this.waterColor = OptionalInt.of(i);
         return this;
      }

      public BiomeSpecialEffects.Builder waterFogColor(int i) {
         this.waterFogColor = OptionalInt.of(i);
         return this;
      }

      public BiomeSpecialEffects.Builder skyColor(int i) {
         this.skyColor = OptionalInt.of(i);
         return this;
      }

      public BiomeSpecialEffects.Builder foliageColorOverride(int i) {
         this.foliageColorOverride = Optional.of(i);
         return this;
      }

      public BiomeSpecialEffects.Builder grassColorOverride(int i) {
         this.grassColorOverride = Optional.of(i);
         return this;
      }

      public BiomeSpecialEffects.Builder grassColorModifier(BiomeSpecialEffects.GrassColorModifier biomespecialeffects_grasscolormodifier) {
         this.grassColorModifier = biomespecialeffects_grasscolormodifier;
         return this;
      }

      public BiomeSpecialEffects.Builder ambientParticle(AmbientParticleSettings ambientparticlesettings) {
         this.ambientParticle = Optional.of(ambientparticlesettings);
         return this;
      }

      public BiomeSpecialEffects.Builder ambientLoopSound(Holder<SoundEvent> holder) {
         this.ambientLoopSoundEvent = Optional.of(holder);
         return this;
      }

      public BiomeSpecialEffects.Builder ambientMoodSound(AmbientMoodSettings ambientmoodsettings) {
         this.ambientMoodSettings = Optional.of(ambientmoodsettings);
         return this;
      }

      public BiomeSpecialEffects.Builder ambientAdditionsSound(AmbientAdditionsSettings ambientadditionssettings) {
         this.ambientAdditionsSettings = Optional.of(ambientadditionssettings);
         return this;
      }

      public BiomeSpecialEffects.Builder backgroundMusic(@Nullable Music music) {
         this.backgroundMusic = Optional.ofNullable(music);
         return this;
      }

      public BiomeSpecialEffects build() {
         return new BiomeSpecialEffects(this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")), this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")), this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")), this.skyColor.orElseThrow(() -> new IllegalStateException("Missing 'sky' color.")), this.foliageColorOverride, this.grassColorOverride, this.grassColorModifier, this.ambientParticle, this.ambientLoopSoundEvent, this.ambientMoodSettings, this.ambientAdditionsSettings, this.backgroundMusic);
      }
   }

   public static enum GrassColorModifier implements StringRepresentable {
      NONE("none") {
         public int modifyColor(double d0, double d1, int i) {
            return i;
         }
      },
      DARK_FOREST("dark_forest") {
         public int modifyColor(double d0, double d1, int i) {
            return (i & 16711422) + 2634762 >> 1;
         }
      },
      SWAMP("swamp") {
         public int modifyColor(double d0, double d1, int i) {
            double d2 = Biome.BIOME_INFO_NOISE.getValue(d0 * 0.0225D, d1 * 0.0225D, false);
            return d2 < -0.1D ? 5011004 : 6975545;
         }
      };

      private final String name;
      public static final Codec<BiomeSpecialEffects.GrassColorModifier> CODEC = StringRepresentable.fromEnum(BiomeSpecialEffects.GrassColorModifier::values);

      public abstract int modifyColor(double d0, double d1, int i);

      GrassColorModifier(String s) {
         this.name = s;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
