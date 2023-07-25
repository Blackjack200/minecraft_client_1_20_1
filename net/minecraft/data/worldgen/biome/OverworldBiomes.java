package net.minecraft.data.worldgen.biome;

import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class OverworldBiomes {
   protected static final int NORMAL_WATER_COLOR = 4159204;
   protected static final int NORMAL_WATER_FOG_COLOR = 329011;
   private static final int OVERWORLD_FOG_COLOR = 12638463;
   @Nullable
   private static final Music NORMAL_MUSIC = null;

   protected static int calculateSkyColor(float f) {
      float f1 = f / 3.0F;
      f1 = Mth.clamp(f1, -1.0F, 1.0F);
      return Mth.hsvToRgb(0.62222224F - f1 * 0.05F, 0.5F + f1 * 0.1F, 1.0F);
   }

   private static Biome biome(boolean flag, float f, float f1, MobSpawnSettings.Builder mobspawnsettings_builder, BiomeGenerationSettings.Builder biomegenerationsettings_builder, @Nullable Music music) {
      return biome(flag, f, f1, 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   private static Biome biome(boolean flag, float f, float f1, int i, int j, @Nullable Integer integer, @Nullable Integer integer1, MobSpawnSettings.Builder mobspawnsettings_builder, BiomeGenerationSettings.Builder biomegenerationsettings_builder, @Nullable Music music) {
      BiomeSpecialEffects.Builder biomespecialeffects_builder = (new BiomeSpecialEffects.Builder()).waterColor(i).waterFogColor(j).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music);
      if (integer != null) {
         biomespecialeffects_builder.grassColorOverride(integer);
      }

      if (integer1 != null) {
         biomespecialeffects_builder.foliageColorOverride(integer1);
      }

      return (new Biome.BiomeBuilder()).hasPrecipitation(flag).temperature(f).downfall(f1).specialEffects(biomespecialeffects_builder.build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   private static void globalOverworldGeneration(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      BiomeDefaultFeatures.addDefaultCarversAndLakes(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings_builder);
   }

   public static Biome oldGrowthTaiga(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      if (flag) {
         BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      } else {
         BiomeDefaultFeatures.caveSpawns(mobspawnsettings_builder);
         BiomeDefaultFeatures.monsters(mobspawnsettings_builder, 100, 25, 100, false);
      }

      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addMossyStoneBlock(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addFerns(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, flag ? VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addGiantTaigaVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addCommonBerryBushes(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA);
      return biome(true, flag ? 0.25F : 0.3F, 0.8F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome sparseJungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings_builder);
      return baseJungle(holdergetter, holdergetter1, 0.8F, false, true, false, mobspawnsettings_builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SPARSE_JUNGLE));
   }

   public static Biome jungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 40, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
      return baseJungle(holdergetter, holdergetter1, 0.9F, false, false, true, mobspawnsettings_builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JUNGLE));
   }

   public static Biome bambooJungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 40, 1, 2)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
      return baseJungle(holdergetter, holdergetter1, 0.9F, true, false, true, mobspawnsettings_builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BAMBOO_JUNGLE));
   }

   private static Biome baseJungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, float f, boolean flag, boolean flag1, boolean flag2, MobSpawnSettings.Builder mobspawnsettings_builder, Music music) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag) {
         BiomeDefaultFeatures.addBambooVegetation(biomegenerationsettings_builder);
      } else {
         if (flag2) {
            BiomeDefaultFeatures.addLightBambooVegetation(biomegenerationsettings_builder);
         }

         if (flag1) {
            BiomeDefaultFeatures.addSparseJungleTrees(biomegenerationsettings_builder);
         } else {
            BiomeDefaultFeatures.addJungleTrees(biomegenerationsettings_builder);
         }
      }

      BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addJungleGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addJungleVines(biomegenerationsettings_builder);
      if (flag1) {
         BiomeDefaultFeatures.addSparseJungleMelons(biomegenerationsettings_builder);
      } else {
         BiomeDefaultFeatures.addJungleMelons(biomegenerationsettings_builder);
      }

      return biome(true, 0.95F, f, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome windsweptHills(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag) {
         BiomeDefaultFeatures.addMountainForestTrees(biomegenerationsettings_builder);
      } else {
         BiomeDefaultFeatures.addMountainTrees(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      return biome(true, 0.2F, 0.3F, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome desert(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.desertSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDesertVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDesertExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDesertExtraDecoration(biomegenerationsettings_builder);
      return biome(false, 2.0F, 0.0F, mobspawnsettings_builder, biomegenerationsettings_builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DESERT));
   }

   public static Biome plains(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag, boolean flag1, boolean flag2) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      if (flag1) {
         mobspawnsettings_builder.creatureGenerationProbability(0.07F);
         BiomeDefaultFeatures.snowySpawns(mobspawnsettings_builder);
         if (flag2) {
            biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
            biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
         }
      } else {
         BiomeDefaultFeatures.plainsSpawns(mobspawnsettings_builder);
         BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings_builder);
         if (flag) {
            biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
         }
      }

      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag1) {
         BiomeDefaultFeatures.addSnowyTrees(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      } else {
         BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      if (flag) {
         biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
         biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
      } else {
         BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      }

      float f = flag1 ? 0.0F : 0.8F;
      return biome(true, f, flag1 ? 0.5F : 0.4F, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome mushroomFields(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.mooshroomSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addMushroomFieldVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      return biome(true, 0.9F, 1.0F, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome savanna(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag, boolean flag1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      if (!flag) {
         BiomeDefaultFeatures.addSavannaGrass(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag) {
         BiomeDefaultFeatures.addShatteredSavannaTrees(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addShatteredSavannaGrass(biomegenerationsettings_builder);
      } else {
         BiomeDefaultFeatures.addSavannaTrees(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addSavannaExtraGrass(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      if (flag1) {
         mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
      }

      return biome(false, 2.0F, 0.0F, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome badlands(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraGold(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag) {
         BiomeDefaultFeatures.addBadlandsTrees(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addBadlandGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addBadlandExtraVegetation(biomegenerationsettings_builder);
      return (new Biome.BiomeBuilder()).hasPrecipitation(false).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BADLANDS)).build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   private static Biome baseOcean(MobSpawnSettings.Builder mobspawnsettings_builder, int i, int j, BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      return biome(true, 0.5F, 0.5F, i, j, (Integer)null, (Integer)null, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   private static BiomeGenerationSettings.Builder baseOceanGeneration(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      return biomegenerationsettings_builder;
   }

   public static Biome coldOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(mobspawnsettings_builder, 3, 4, 15);
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = baseOceanGeneration(holdergetter, holdergetter1);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, flag ? AquaticPlacements.SEAGRASS_DEEP_COLD : AquaticPlacements.SEAGRASS_COLD);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings_builder);
      return baseOcean(mobspawnsettings_builder, 4020182, 329011, biomegenerationsettings_builder);
   }

   public static Biome ocean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(mobspawnsettings_builder, 1, 4, 10);
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = baseOceanGeneration(holdergetter, holdergetter1);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, flag ? AquaticPlacements.SEAGRASS_DEEP : AquaticPlacements.SEAGRASS_NORMAL);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings_builder);
      return baseOcean(mobspawnsettings_builder, 4159204, 329011, biomegenerationsettings_builder);
   }

   public static Biome lukeWarmOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      if (flag) {
         BiomeDefaultFeatures.oceanSpawns(mobspawnsettings_builder, 8, 4, 8);
      } else {
         BiomeDefaultFeatures.oceanSpawns(mobspawnsettings_builder, 10, 2, 15);
      }

      mobspawnsettings_builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8)).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = baseOceanGeneration(holdergetter, holdergetter1);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, flag ? AquaticPlacements.SEAGRASS_DEEP_WARM : AquaticPlacements.SEAGRASS_WARM);
      if (flag) {
         BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addLukeWarmKelp(biomegenerationsettings_builder);
      return baseOcean(mobspawnsettings_builder, 4566514, 267827, biomegenerationsettings_builder);
   }

   public static Biome warmOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
      BiomeDefaultFeatures.warmOceanSpawns(mobspawnsettings_builder, 10, 4);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = baseOceanGeneration(holdergetter, holdergetter1).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
      return baseOcean(mobspawnsettings_builder, 4445678, 270131, biomegenerationsettings_builder);
   }

   public static Biome frozenOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      float f = flag ? 0.5F : 0.0F;
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      BiomeDefaultFeatures.addIcebergs(biomegenerationsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addBlueIce(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   public static Biome forest(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag, boolean flag1, boolean flag2) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      Music music;
      if (flag2) {
         music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FLOWER_FOREST);
         biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FOREST_FLOWERS);
      } else {
         music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST);
         BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag2) {
         biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_FLOWER_FOREST);
         biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FLOWER_FOREST);
         BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      } else {
         if (flag) {
            if (flag1) {
               BiomeDefaultFeatures.addTallBirchTrees(biomegenerationsettings_builder);
            } else {
               BiomeDefaultFeatures.addBirchTrees(biomegenerationsettings_builder);
            }
         } else {
            BiomeDefaultFeatures.addOtherBirchTrees(biomegenerationsettings_builder);
         }

         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
         BiomeDefaultFeatures.addForestGrass(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      if (flag2) {
         mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      } else if (!flag) {
         mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4));
      }

      float f = flag ? 0.6F : 0.7F;
      return biome(true, f, flag ? 0.6F : 0.8F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome taiga(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      float f = flag ? -0.5F : 0.25F;
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addFerns(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addTaigaTrees(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addTaigaGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      if (flag) {
         BiomeDefaultFeatures.addRareBerryBushes(biomegenerationsettings_builder);
      } else {
         BiomeDefaultFeatures.addCommonBerryBushes(biomegenerationsettings_builder);
      }

      return biome(true, f, flag ? 0.4F : 0.8F, flag ? 4020182 : 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome darkForest(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
      BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addForestGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   public static Biome swamp(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 5));
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addSwampClayDisk(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addSwampVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addSwampExtraVegetation(biomegenerationsettings_builder);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   public static Biome mangroveSwamp(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 5));
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addMangroveSwampDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addMangroveSwampVegetation(biomegenerationsettings_builder);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(3832426).waterFogColor(5077600).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(9285927).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(mobspawnsettings_builder.build()).generationSettings(biomegenerationsettings_builder.build()).build();
   }

   public static Biome river(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      MobSpawnSettings.Builder mobspawnsettings_builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, flag ? 1 : 100, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      if (!flag) {
         biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER);
      }

      float f = flag ? 0.0F : 0.5F;
      return biome(true, f, 0.5F, flag ? 3750089 : 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome beach(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag, boolean flag1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      boolean flag2 = !flag1 && !flag;
      if (flag2) {
         mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
      }

      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      float f;
      if (flag) {
         f = 0.05F;
      } else if (flag1) {
         f = 0.2F;
      } else {
         f = 0.8F;
      }

      return biome(true, f, flag2 ? 0.4F : 0.3F, flag ? 4020182 : 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings_builder, biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome theVoid(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.VOID_START_PLATFORM);
      return biome(false, 0.5F, 0.5F, new MobSpawnSettings.Builder(), biomegenerationsettings_builder, NORMAL_MUSIC);
   }

   public static Biome meadowOrCherryGrove(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1, boolean flag) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(flag ? EntityType.PIG : EntityType.DONKEY, 1, 1, 2)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 2, 2, 4));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      if (flag) {
         BiomeDefaultFeatures.addCherryGroveVegetation(biomegenerationsettings_builder);
      } else {
         BiomeDefaultFeatures.addMeadowVegetation(biomegenerationsettings_builder);
      }

      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(flag ? SoundEvents.MUSIC_BIOME_CHERRY_GROVE : SoundEvents.MUSIC_BIOME_MEADOW);
      return flag ? biome(true, 0.5F, 0.8F, 6141935, 6141935, 11983713, 11983713, mobspawnsettings_builder, biomegenerationsettings_builder, music) : biome(true, 0.5F, 0.8F, 937679, 329011, (Integer)null, (Integer)null, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome frozenPeaks(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS);
      return biome(true, -0.7F, 0.9F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome jaggedPeaks(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS);
      return biome(true, -0.7F, 0.9F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome stonyPeaks(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS);
      return biome(true, 1.0F, 0.3F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome snowySlopes(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SNOWY_SLOPES);
      return biome(true, -0.3F, 0.9F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome grove(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addGroveTrees(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_GROVE);
      return biome(true, -0.2F, 0.8F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome lushCaves(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      mobspawnsettings_builder.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addLushCavesSpecialOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addLushCavesVegetationFeatures(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
      return biome(true, 0.5F, 0.5F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome dripstoneCaves(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.dripstoneCavesSpawns(mobspawnsettings_builder);
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      globalOverworldGeneration(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder, true);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDripstone(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES);
      return biome(true, 0.8F, 0.4F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }

   public static Biome deepDark(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
      MobSpawnSettings.Builder mobspawnsettings_builder = new MobSpawnSettings.Builder();
      BiomeGenerationSettings.Builder biomegenerationsettings_builder = new BiomeGenerationSettings.Builder(holdergetter, holdergetter1);
      biomegenerationsettings_builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
      biomegenerationsettings_builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
      biomegenerationsettings_builder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings_builder);
      BiomeDefaultFeatures.addSculk(biomegenerationsettings_builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK);
      return biome(true, 0.8F, 0.4F, mobspawnsettings_builder, biomegenerationsettings_builder, music);
   }
}
