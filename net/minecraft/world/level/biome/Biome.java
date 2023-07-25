package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class Biome {
   public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Biome.ClimateSettings.CODEC.forGetter((biome3) -> biome3.climateSettings), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((biome2) -> biome2.specialEffects), BiomeGenerationSettings.CODEC.forGetter((biome1) -> biome1.generationSettings), MobSpawnSettings.CODEC.forGetter((biome) -> biome.mobSettings)).apply(recordcodecbuilder_instance, Biome::new));
   public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Biome.ClimateSettings.CODEC.forGetter((biome1) -> biome1.climateSettings), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((biome) -> biome.specialEffects)).apply(recordcodecbuilder_instance, (biome_climatesettings, biomespecialeffects) -> new Biome(biome_climatesettings, biomespecialeffects, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY)));
   public static final Codec<Holder<Biome>> CODEC = RegistryFileCodec.create(Registries.BIOME, DIRECT_CODEC);
   public static final Codec<HolderSet<Biome>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.BIOME, DIRECT_CODEC);
   private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(1234L)), ImmutableList.of(0));
   static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(3456L)), ImmutableList.of(-2, -1, 0));
   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), ImmutableList.of(0));
   private static final int TEMPERATURE_CACHE_SIZE = 1024;
   private final Biome.ClimateSettings climateSettings;
   private final BiomeGenerationSettings generationSettings;
   private final MobSpawnSettings mobSettings;
   private final BiomeSpecialEffects specialEffects;
   private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
         Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
            protected void rehash(int i) {
            }
         };
         long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
         return long2floatlinkedopenhashmap;
      }));

   Biome(Biome.ClimateSettings biome_climatesettings, BiomeSpecialEffects biomespecialeffects, BiomeGenerationSettings biomegenerationsettings, MobSpawnSettings mobspawnsettings) {
      this.climateSettings = biome_climatesettings;
      this.generationSettings = biomegenerationsettings;
      this.mobSettings = mobspawnsettings;
      this.specialEffects = biomespecialeffects;
   }

   public int getSkyColor() {
      return this.specialEffects.getSkyColor();
   }

   public MobSpawnSettings getMobSettings() {
      return this.mobSettings;
   }

   public boolean hasPrecipitation() {
      return this.climateSettings.hasPrecipitation();
   }

   public Biome.Precipitation getPrecipitationAt(BlockPos blockpos) {
      if (!this.hasPrecipitation()) {
         return Biome.Precipitation.NONE;
      } else {
         return this.coldEnoughToSnow(blockpos) ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
      }
   }

   private float getHeightAdjustedTemperature(BlockPos blockpos) {
      float f = this.climateSettings.temperatureModifier.modifyTemperature(blockpos, this.getBaseTemperature());
      if (blockpos.getY() > 80) {
         float f1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)blockpos.getX() / 8.0F), (double)((float)blockpos.getZ() / 8.0F), false) * 8.0D);
         return f - (f1 + (float)blockpos.getY() - 80.0F) * 0.05F / 40.0F;
      } else {
         return f;
      }
   }

   /** @deprecated */
   @Deprecated
   private float getTemperature(BlockPos blockpos) {
      long i = blockpos.asLong();
      Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = this.temperatureCache.get();
      float f = long2floatlinkedopenhashmap.get(i);
      if (!Float.isNaN(f)) {
         return f;
      } else {
         float f1 = this.getHeightAdjustedTemperature(blockpos);
         if (long2floatlinkedopenhashmap.size() == 1024) {
            long2floatlinkedopenhashmap.removeFirstFloat();
         }

         long2floatlinkedopenhashmap.put(i, f1);
         return f1;
      }
   }

   public boolean shouldFreeze(LevelReader levelreader, BlockPos blockpos) {
      return this.shouldFreeze(levelreader, blockpos, true);
   }

   public boolean shouldFreeze(LevelReader levelreader, BlockPos blockpos, boolean flag) {
      if (this.warmEnoughToRain(blockpos)) {
         return false;
      } else {
         if (blockpos.getY() >= levelreader.getMinBuildHeight() && blockpos.getY() < levelreader.getMaxBuildHeight() && levelreader.getBrightness(LightLayer.BLOCK, blockpos) < 10) {
            BlockState blockstate = levelreader.getBlockState(blockpos);
            FluidState fluidstate = levelreader.getFluidState(blockpos);
            if (fluidstate.getType() == Fluids.WATER && blockstate.getBlock() instanceof LiquidBlock) {
               if (!flag) {
                  return true;
               }

               boolean flag1 = levelreader.isWaterAt(blockpos.west()) && levelreader.isWaterAt(blockpos.east()) && levelreader.isWaterAt(blockpos.north()) && levelreader.isWaterAt(blockpos.south());
               if (!flag1) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean coldEnoughToSnow(BlockPos blockpos) {
      return !this.warmEnoughToRain(blockpos);
   }

   public boolean warmEnoughToRain(BlockPos blockpos) {
      return this.getTemperature(blockpos) >= 0.15F;
   }

   public boolean shouldMeltFrozenOceanIcebergSlightly(BlockPos blockpos) {
      return this.getTemperature(blockpos) > 0.1F;
   }

   public boolean shouldSnow(LevelReader levelreader, BlockPos blockpos) {
      if (this.warmEnoughToRain(blockpos)) {
         return false;
      } else {
         if (blockpos.getY() >= levelreader.getMinBuildHeight() && blockpos.getY() < levelreader.getMaxBuildHeight() && levelreader.getBrightness(LightLayer.BLOCK, blockpos) < 10) {
            BlockState blockstate = levelreader.getBlockState(blockpos);
            if ((blockstate.isAir() || blockstate.is(Blocks.SNOW)) && Blocks.SNOW.defaultBlockState().canSurvive(levelreader, blockpos)) {
               return true;
            }
         }

         return false;
      }
   }

   public BiomeGenerationSettings getGenerationSettings() {
      return this.generationSettings;
   }

   public int getFogColor() {
      return this.specialEffects.getFogColor();
   }

   public int getGrassColor(double d0, double d1) {
      int i = this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
      return this.specialEffects.getGrassColorModifier().modifyColor(d0, d1, i);
   }

   private int getGrassColorFromTexture() {
      double d2 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double d3 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return GrassColor.get(d2, d3);
   }

   public int getFoliageColor() {
      return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
   }

   private int getFoliageColorFromTexture() {
      double d0 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double d1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return FoliageColor.get(d0, d1);
   }

   public float getBaseTemperature() {
      return this.climateSettings.temperature;
   }

   public BiomeSpecialEffects getSpecialEffects() {
      return this.specialEffects;
   }

   public int getWaterColor() {
      return this.specialEffects.getWaterColor();
   }

   public int getWaterFogColor() {
      return this.specialEffects.getWaterFogColor();
   }

   public Optional<AmbientParticleSettings> getAmbientParticle() {
      return this.specialEffects.getAmbientParticleSettings();
   }

   public Optional<Holder<SoundEvent>> getAmbientLoop() {
      return this.specialEffects.getAmbientLoopSoundEvent();
   }

   public Optional<AmbientMoodSettings> getAmbientMood() {
      return this.specialEffects.getAmbientMoodSettings();
   }

   public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
      return this.specialEffects.getAmbientAdditionsSettings();
   }

   public Optional<Music> getBackgroundMusic() {
      return this.specialEffects.getBackgroundMusic();
   }

   public static class BiomeBuilder {
      private boolean hasPrecipitation = true;
      @Nullable
      private Float temperature;
      private Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
      @Nullable
      private Float downfall;
      @Nullable
      private BiomeSpecialEffects specialEffects;
      @Nullable
      private MobSpawnSettings mobSpawnSettings;
      @Nullable
      private BiomeGenerationSettings generationSettings;

      public Biome.BiomeBuilder hasPrecipitation(boolean flag) {
         this.hasPrecipitation = flag;
         return this;
      }

      public Biome.BiomeBuilder temperature(float f) {
         this.temperature = f;
         return this;
      }

      public Biome.BiomeBuilder downfall(float f) {
         this.downfall = f;
         return this;
      }

      public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects biomespecialeffects) {
         this.specialEffects = biomespecialeffects;
         return this;
      }

      public Biome.BiomeBuilder mobSpawnSettings(MobSpawnSettings mobspawnsettings) {
         this.mobSpawnSettings = mobspawnsettings;
         return this;
      }

      public Biome.BiomeBuilder generationSettings(BiomeGenerationSettings biomegenerationsettings) {
         this.generationSettings = biomegenerationsettings;
         return this;
      }

      public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier biome_temperaturemodifier) {
         this.temperatureModifier = biome_temperaturemodifier;
         return this;
      }

      public Biome build() {
         if (this.temperature != null && this.downfall != null && this.specialEffects != null && this.mobSpawnSettings != null && this.generationSettings != null) {
            return new Biome(new Biome.ClimateSettings(this.hasPrecipitation, this.temperature, this.temperatureModifier, this.downfall), this.specialEffects, this.generationSettings, this.mobSpawnSettings);
         } else {
            throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
         }
      }

      public String toString() {
         return "BiomeBuilder{\nhasPrecipitation=" + this.hasPrecipitation + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.mobSpawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n}";
      }
   }

   static record ClimateSettings(boolean hasPrecipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
      final float temperature;
      final Biome.TemperatureModifier temperatureModifier;
      final float downfall;
      public static final MapCodec<Biome.ClimateSettings> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.BOOL.fieldOf("has_precipitation").forGetter((biome_climatesettings3) -> biome_climatesettings3.hasPrecipitation), Codec.FLOAT.fieldOf("temperature").forGetter((biome_climatesettings2) -> biome_climatesettings2.temperature), Biome.TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE).forGetter((biome_climatesettings1) -> biome_climatesettings1.temperatureModifier), Codec.FLOAT.fieldOf("downfall").forGetter((biome_climatesettings) -> biome_climatesettings.downfall)).apply(recordcodecbuilder_instance, Biome.ClimateSettings::new));
   }

   public static enum Precipitation {
      NONE,
      RAIN,
      SNOW;
   }

   public static enum TemperatureModifier implements StringRepresentable {
      NONE("none") {
         public float modifyTemperature(BlockPos blockpos, float f) {
            return f;
         }
      },
      FROZEN("frozen") {
         public float modifyTemperature(BlockPos blockpos, float f) {
            double d0 = Biome.FROZEN_TEMPERATURE_NOISE.getValue((double)blockpos.getX() * 0.05D, (double)blockpos.getZ() * 0.05D, false) * 7.0D;
            double d1 = Biome.BIOME_INFO_NOISE.getValue((double)blockpos.getX() * 0.2D, (double)blockpos.getZ() * 0.2D, false);
            double d2 = d0 + d1;
            if (d2 < 0.3D) {
               double d3 = Biome.BIOME_INFO_NOISE.getValue((double)blockpos.getX() * 0.09D, (double)blockpos.getZ() * 0.09D, false);
               if (d3 < 0.8D) {
                  return 0.2F;
               }
            }

            return f;
         }
      };

      private final String name;
      public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(Biome.TemperatureModifier::values);

      public abstract float modifyTemperature(BlockPos blockpos, float f);

      TemperatureModifier(String s) {
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
