package net.minecraft.data.worldgen;

import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
   public static void addDefaultCarversAndLakes(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
      biomegenerationsettings_builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
      biomegenerationsettings_builder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE);
   }

   public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.MONSTER_ROOM);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.MONSTER_ROOM_DEEP);
   }

   public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRAVEL);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRANITE_UPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRANITE_LOWER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIORITE_UPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIORITE_LOWER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_ANDESITE_UPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_ANDESITE_LOWER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_TUFF);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
   }

   public static void addDripstone(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.DRIPSTONE_CLUSTER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
   }

   public static void addSculk(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_VEIN);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_PATCH_DEEP_DARK);
   }

   public static void addDefaultOres(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      addDefaultOres(biomegenerationsettings_builder, false);
   }

   public static void addDefaultOres(BiomeGenerationSettings.Builder biomegenerationsettings_builder, boolean flag) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_COAL_UPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_COAL_LOWER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_UPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_MIDDLE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_SMALL);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD_LOWER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_REDSTONE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_REDSTONE_LOWER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_LARGE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_BURIED);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_LAPIS);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_LAPIS_BURIED);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, flag ? OrePlacements.ORE_COPPER_LARGE : OrePlacements.ORE_COPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CavePlacements.UNDERWATER_MAGMA);
   }

   public static void addExtraGold(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD_EXTRA);
   }

   public static void addExtraEmeralds(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_EMERALD);
   }

   public static void addInfestedStone(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_INFESTED);
   }

   public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_SAND);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_GRAVEL);
   }

   public static void addSwampClayDisk(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
   }

   public static void addMangroveSwampDisks(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_GRASS);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
   }

   public static void addMossyStoneBlock(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.FOREST_ROCK);
   }

   public static void addFerns(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_LARGE_FERN);
   }

   public static void addRareBerryBushes(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BERRY_RARE);
   }

   public static void addCommonBerryBushes(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BERRY_COMMON);
   }

   public static void addLightBambooVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO_LIGHT);
   }

   public static void addBambooVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO_VEGETATION);
   }

   public static void addTaigaTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_TAIGA);
   }

   public static void addGroveTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_GROVE);
   }

   public static void addWaterTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WATER);
   }

   public static void addBirchTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH);
   }

   public static void addOtherBirchTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH_AND_OAK);
   }

   public static void addTallBirchTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BIRCH_TALL);
   }

   public static void addSavannaTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SAVANNA);
   }

   public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_SAVANNA);
   }

   public static void addLushCavesVegetationFeatures(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_CEILING_VEGETATION);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CAVE_VINES);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_CLAY);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_VEGETATION);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.ROOTED_AZALEA_TREE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.SPORE_BLOSSOM);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
   }

   public static void addLushCavesSpecialOres(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
   }

   public static void addMountainTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_HILLS);
   }

   public static void addMountainForestTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_FOREST);
   }

   public static void addJungleTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_JUNGLE);
   }

   public static void addSparseJungleTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SPARSE_JUNGLE);
   }

   public static void addBadlandsTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BADLANDS);
   }

   public static void addSnowyTrees(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SNOWY);
   }

   public static void addJungleGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_JUNGLE);
   }

   public static void addSavannaGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS);
   }

   public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
   }

   public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_SAVANNA);
   }

   public static void addBadlandGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH_BADLANDS);
   }

   public static void addForestFlowers(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FOREST_FLOWERS);
   }

   public static void addForestGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_FOREST);
   }

   public static void addSwampVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SWAMP);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_SWAMP);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_SWAMP);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_SWAMP);
   }

   public static void addMangroveSwampVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_MANGROVE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
   }

   public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.MUSHROOM_ISLAND_VEGETATION);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_TAIGA);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_PLAINS);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
   }

   public static void addDesertVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH_2);
   }

   public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_TAIGA);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_OLD_GROWTH);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_OLD_GROWTH);
   }

   public static void addDefaultFlowers(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_DEFAULT);
   }

   public static void addCherryGroveVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_CHERRY);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_CHERRY);
   }

   public static void addMeadowVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_MEADOW);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_MEADOW);
   }

   public static void addWarmFlowers(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_WARM);
   }

   public static void addDefaultGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
   }

   public static void addTaigaGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_TAIGA_2);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_TAIGA);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainGrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS_2);
   }

   public static void addDefaultMushrooms(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NORMAL);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_NORMAL);
   }

   public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
   }

   public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_BADLANDS);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_CACTUS_DECORATED);
   }

   public static void addJungleMelons(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_MELON);
   }

   public static void addSparseJungleMelons(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_MELON_SPARSE);
   }

   public static void addJungleVines(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.VINES);
   }

   public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_DESERT);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_CACTUS_DESERT);
   }

   public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_SWAMP);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
   }

   public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.DESERT_WELL);
   }

   public static void addFossilDecoration(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.FOSSIL_UPPER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.FOSSIL_LOWER);
   }

   public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.KELP_COLD);
   }

   public static void addDefaultSeagrass(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SIMPLE);
   }

   public static void addLukeWarmKelp(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.KELP_WARM);
   }

   public static void addDefaultSprings(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_WATER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
   }

   public static void addFrozenSprings(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA_FROZEN);
   }

   public static void addIcebergs(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.ICEBERG_PACKED);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.ICEBERG_BLUE);
   }

   public static void addBlueIce(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.BLUE_ICE);
   }

   public static void addSurfaceFreezing(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.FREEZE_TOP_LAYER);
   }

   public static void addNetherDefaultOres(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GRAVEL_NETHER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_BLACKSTONE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GOLD_NETHER);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_QUARTZ_NETHER);
      addAncientDebris(biomegenerationsettings_builder);
   }

   public static void addAncientDebris(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_ANCIENT_DEBRIS_LARGE);
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_ANCIENT_DEBRIS_SMALL);
   }

   public static void addDefaultCrystalFormations(BiomeGenerationSettings.Builder biomegenerationsettings_builder) {
      biomegenerationsettings_builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.AMETHYST_GEODE);
   }

   public static void farmAnimals(MobSpawnSettings.Builder mobspawnsettings_builder) {
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
   }

   public static void caveSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      mobspawnsettings_builder.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
      mobspawnsettings_builder.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
   }

   public static void commonSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      caveSpawns(mobspawnsettings_builder);
      monsters(mobspawnsettings_builder, 95, 5, 100, false);
   }

   public static void oceanSpawns(MobSpawnSettings.Builder mobspawnsettings_builder, int i, int j, int k) {
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, i, 1, j));
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, k, 3, 6));
      commonSpawns(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
   }

   public static void warmOceanSpawns(MobSpawnSettings.Builder mobspawnsettings_builder, int i, int j) {
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, i, j, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      mobspawnsettings_builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      commonSpawns(mobspawnsettings_builder);
   }

   public static void plainsSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
      commonSpawns(mobspawnsettings_builder);
   }

   public static void snowySpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 10, 2, 3));
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      caveSpawns(mobspawnsettings_builder);
      monsters(mobspawnsettings_builder, 95, 5, 20, false);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 80, 4, 4));
   }

   public static void desertSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      caveSpawns(mobspawnsettings_builder);
      monsters(mobspawnsettings_builder, 19, 1, 100, false);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 4, 4));
   }

   public static void dripstoneCavesSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      caveSpawns(mobspawnsettings_builder);
      int i = 95;
      monsters(mobspawnsettings_builder, 95, 5, 100, false);
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 95, 4, 4));
   }

   public static void monsters(MobSpawnSettings.Builder mobspawnsettings_builder, int i, int j, int k, boolean flag) {
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(flag ? EntityType.DROWNED : EntityType.ZOMBIE, i, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, j, 1, 1));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, k, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
   }

   public static void mooshroomSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
      caveSpawns(mobspawnsettings_builder);
   }

   public static void baseJungleSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      farmAnimals(mobspawnsettings_builder);
      mobspawnsettings_builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
      commonSpawns(mobspawnsettings_builder);
   }

   public static void endSpawns(MobSpawnSettings.Builder mobspawnsettings_builder) {
      mobspawnsettings_builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
   }
}
