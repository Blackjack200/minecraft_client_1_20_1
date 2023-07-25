package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EndBiomes {
   private static Biome baseEndBiome(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.endSpawns(mobspawnsettings_builder);
      return (new Biome.BiomeBuilder()).hasPrecipitation(false).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   public static Biome endBarrens(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      return baseEndBiome(biomegenerationsettings_builder);
   }

   public static Biome theEnd(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = (new BiomeGenerationSettings.Builder(holdergetter, holdergetter1)).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_SPIKE);
      return baseEndBiome(biomegenerationsettings_builder);
   }

   public static Biome endMidlands(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      return baseEndBiome(biomegenerationsettings_builder);
   }

   public static Biome endHighlands(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = (new BiomeGenerationSettings.Builder(holdergetter, holdergetter1)).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_GATEWAY_RETURN).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, EndPlacements.CHORUS_PLANT);
      return baseEndBiome(biomegenerationsettings_builder);
   }

   public static Biome smallEndIslands(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = (new BiomeGenerationSettings.Builder(holdergetter, holdergetter1)).addFeature(GenerationStep.Decoration.RAW_GENERATION, EndPlacements.END_ISLAND_DECORATED);
      return baseEndBiome(biomegenerationsettings_builder);
   }
}
