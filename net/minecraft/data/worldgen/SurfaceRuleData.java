package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class SurfaceRuleData {
   private static final SurfaceRules.RuleSource AIR = makeStateRule(Blocks.AIR);
   private static final SurfaceRules.RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);
   private static final SurfaceRules.RuleSource WHITE_TERRACOTTA = makeStateRule(Blocks.WHITE_TERRACOTTA);
   private static final SurfaceRules.RuleSource ORANGE_TERRACOTTA = makeStateRule(Blocks.ORANGE_TERRACOTTA);
   private static final SurfaceRules.RuleSource TERRACOTTA = makeStateRule(Blocks.TERRACOTTA);
   private static final SurfaceRules.RuleSource RED_SAND = makeStateRule(Blocks.RED_SAND);
   private static final SurfaceRules.RuleSource RED_SANDSTONE = makeStateRule(Blocks.RED_SANDSTONE);
   private static final SurfaceRules.RuleSource STONE = makeStateRule(Blocks.STONE);
   private static final SurfaceRules.RuleSource DEEPSLATE = makeStateRule(Blocks.DEEPSLATE);
   private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
   private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
   private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
   private static final SurfaceRules.RuleSource MYCELIUM = makeStateRule(Blocks.MYCELIUM);
   private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
   private static final SurfaceRules.RuleSource CALCITE = makeStateRule(Blocks.CALCITE);
   private static final SurfaceRules.RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);
   private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);
   private static final SurfaceRules.RuleSource SANDSTONE = makeStateRule(Blocks.SANDSTONE);
   private static final SurfaceRules.RuleSource PACKED_ICE = makeStateRule(Blocks.PACKED_ICE);
   private static final SurfaceRules.RuleSource SNOW_BLOCK = makeStateRule(Blocks.SNOW_BLOCK);
   private static final SurfaceRules.RuleSource MUD = makeStateRule(Blocks.MUD);
   private static final SurfaceRules.RuleSource POWDER_SNOW = makeStateRule(Blocks.POWDER_SNOW);
   private static final SurfaceRules.RuleSource ICE = makeStateRule(Blocks.ICE);
   private static final SurfaceRules.RuleSource WATER = makeStateRule(Blocks.WATER);
   private static final SurfaceRules.RuleSource LAVA = makeStateRule(Blocks.LAVA);
   private static final SurfaceRules.RuleSource NETHERRACK = makeStateRule(Blocks.NETHERRACK);
   private static final SurfaceRules.RuleSource SOUL_SAND = makeStateRule(Blocks.SOUL_SAND);
   private static final SurfaceRules.RuleSource SOUL_SOIL = makeStateRule(Blocks.SOUL_SOIL);
   private static final SurfaceRules.RuleSource BASALT = makeStateRule(Blocks.BASALT);
   private static final SurfaceRules.RuleSource BLACKSTONE = makeStateRule(Blocks.BLACKSTONE);
   private static final SurfaceRules.RuleSource WARPED_WART_BLOCK = makeStateRule(Blocks.WARPED_WART_BLOCK);
   private static final SurfaceRules.RuleSource WARPED_NYLIUM = makeStateRule(Blocks.WARPED_NYLIUM);
   private static final SurfaceRules.RuleSource NETHER_WART_BLOCK = makeStateRule(Blocks.NETHER_WART_BLOCK);
   private static final SurfaceRules.RuleSource CRIMSON_NYLIUM = makeStateRule(Blocks.CRIMSON_NYLIUM);
   private static final SurfaceRules.RuleSource ENDSTONE = makeStateRule(Blocks.END_STONE);

   private static SurfaceRules.RuleSource makeStateRule(Block block) {
      return SurfaceRules.state(block.defaultBlockState());
   }

   public static SurfaceRules.RuleSource overworld() {
      return overworldLike(true, false, true);
   }

   public static SurfaceRules.RuleSource overworldLike(boolean flag, boolean flag1, boolean flag2) {
      SurfaceRules.ConditionSource surfacerules_conditionsource = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(97), 2);
      SurfaceRules.ConditionSource surfacerules_conditionsource1 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource2 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(63), -1);
      SurfaceRules.ConditionSource surfacerules_conditionsource3 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(74), 1);
      SurfaceRules.ConditionSource surfacerules_conditionsource4 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource5 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource6 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource7 = SurfaceRules.waterBlockCheck(-1, 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource8 = SurfaceRules.waterBlockCheck(0, 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource9 = SurfaceRules.waterStartCheck(-6, -1);
      SurfaceRules.ConditionSource surfacerules_conditionsource10 = SurfaceRules.hole();
      SurfaceRules.ConditionSource surfacerules_conditionsource11 = SurfaceRules.isBiome(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN);
      SurfaceRules.ConditionSource surfacerules_conditionsource12 = SurfaceRules.steep();
      SurfaceRules.RuleSource surfacerules_rulesource = SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource8, GRASS_BLOCK), DIRT);
      SurfaceRules.RuleSource surfacerules_rulesource1 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SANDSTONE), SAND);
      SurfaceRules.RuleSource surfacerules_rulesource2 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, STONE), GRAVEL);
      SurfaceRules.ConditionSource surfacerules_conditionsource13 = SurfaceRules.isBiome(Biomes.WARM_OCEAN, Biomes.BEACH, Biomes.SNOWY_BEACH);
      SurfaceRules.ConditionSource surfacerules_conditionsource14 = SurfaceRules.isBiome(Biomes.DESERT);
      SurfaceRules.RuleSource surfacerules_rulesource3 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.STONY_PEAKS), SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.CALCITE, -0.0125D, 0.0125D), CALCITE), STONE)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.STONY_SHORE), SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, -0.05D, 0.05D), surfacerules_rulesource2), STONE)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_HILLS), SurfaceRules.ifTrue(surfaceNoiseAbove(1.0D), STONE)), SurfaceRules.ifTrue(surfacerules_conditionsource13, surfacerules_rulesource1), SurfaceRules.ifTrue(surfacerules_conditionsource14, surfacerules_rulesource1), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.DRIPSTONE_CAVES), STONE));
      SurfaceRules.RuleSource surfacerules_rulesource4 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.45D, 0.58D), SurfaceRules.ifTrue(surfacerules_conditionsource8, POWDER_SNOW));
      SurfaceRules.RuleSource surfacerules_rulesource5 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.35D, 0.6D), SurfaceRules.ifTrue(surfacerules_conditionsource8, POWDER_SNOW));
      SurfaceRules.RuleSource surfacerules_rulesource6 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.FROZEN_PEAKS), SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource12, PACKED_ICE), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, -0.5D, 0.2D), PACKED_ICE), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, -0.0625D, 0.025D), ICE), SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.SNOWY_SLOPES), SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource12, STONE), surfacerules_rulesource4, SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.JAGGED_PEAKS), STONE), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.GROVE), SurfaceRules.sequence(surfacerules_rulesource4, DIRT)), surfacerules_rulesource3, SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_SAVANNA), SurfaceRules.ifTrue(surfaceNoiseAbove(1.75D), STONE)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS), SurfaceRules.sequence(SurfaceRules.ifTrue(surfaceNoiseAbove(2.0D), surfacerules_rulesource2), SurfaceRules.ifTrue(surfaceNoiseAbove(1.0D), STONE), SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0D), DIRT), surfacerules_rulesource2)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MANGROVE_SWAMP), MUD), DIRT);
      SurfaceRules.RuleSource surfacerules_rulesource7 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.FROZEN_PEAKS), SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource12, PACKED_ICE), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, 0.0D, 0.2D), PACKED_ICE), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, 0.0D, 0.025D), ICE), SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.SNOWY_SLOPES), SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource12, STONE), surfacerules_rulesource5, SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.JAGGED_PEAKS), SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource12, STONE), SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.GROVE), SurfaceRules.sequence(surfacerules_rulesource5, SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK))), surfacerules_rulesource3, SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_SAVANNA), SurfaceRules.sequence(SurfaceRules.ifTrue(surfaceNoiseAbove(1.75D), STONE), SurfaceRules.ifTrue(surfaceNoiseAbove(-0.5D), COARSE_DIRT))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS), SurfaceRules.sequence(SurfaceRules.ifTrue(surfaceNoiseAbove(2.0D), surfacerules_rulesource2), SurfaceRules.ifTrue(surfaceNoiseAbove(1.0D), STONE), SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0D), surfacerules_rulesource), surfacerules_rulesource2)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA), SurfaceRules.sequence(SurfaceRules.ifTrue(surfaceNoiseAbove(1.75D), COARSE_DIRT), SurfaceRules.ifTrue(surfaceNoiseAbove(-0.95D), PODZOL))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.ICE_SPIKES), SurfaceRules.ifTrue(surfacerules_conditionsource8, SNOW_BLOCK)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MANGROVE_SWAMP), MUD), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUSHROOM_FIELDS), MYCELIUM), surfacerules_rulesource);
      SurfaceRules.ConditionSource surfacerules_conditionsource15 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.909D, -0.5454D);
      SurfaceRules.ConditionSource surfacerules_conditionsource16 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.1818D, 0.1818D);
      SurfaceRules.ConditionSource surfacerules_conditionsource17 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.5454D, 0.909D);
      SurfaceRules.RuleSource surfacerules_rulesource8 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WOODED_BADLANDS), SurfaceRules.ifTrue(surfacerules_conditionsource, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource15, COARSE_DIRT), SurfaceRules.ifTrue(surfacerules_conditionsource16, COARSE_DIRT), SurfaceRules.ifTrue(surfacerules_conditionsource17, COARSE_DIRT), surfacerules_rulesource))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.SWAMP), SurfaceRules.ifTrue(surfacerules_conditionsource5, SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource6), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0D), WATER)))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MANGROVE_SWAMP), SurfaceRules.ifTrue(surfacerules_conditionsource4, SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource6), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0D), WATER)))))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS), SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource1, ORANGE_TERRACOTTA), SurfaceRules.ifTrue(surfacerules_conditionsource3, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource15, TERRACOTTA), SurfaceRules.ifTrue(surfacerules_conditionsource16, TERRACOTTA), SurfaceRules.ifTrue(surfacerules_conditionsource17, TERRACOTTA), SurfaceRules.bandlands())), SurfaceRules.ifTrue(surfacerules_conditionsource7, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, RED_SANDSTONE), RED_SAND)), SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource10), ORANGE_TERRACOTTA), SurfaceRules.ifTrue(surfacerules_conditionsource9, WHITE_TERRACOTTA), surfacerules_rulesource2)), SurfaceRules.ifTrue(surfacerules_conditionsource2, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource6, SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource3), ORANGE_TERRACOTTA)), SurfaceRules.bandlands())), SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.ifTrue(surfacerules_conditionsource9, WHITE_TERRACOTTA)))), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.ifTrue(surfacerules_conditionsource7, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource11, SurfaceRules.ifTrue(surfacerules_conditionsource10, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource8, AIR), SurfaceRules.ifTrue(SurfaceRules.temperature(), ICE), WATER))), surfacerules_rulesource7))), SurfaceRules.ifTrue(surfacerules_conditionsource9, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.ifTrue(surfacerules_conditionsource11, SurfaceRules.ifTrue(surfacerules_conditionsource10, WATER))), SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, surfacerules_rulesource6), SurfaceRules.ifTrue(surfacerules_conditionsource13, SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, SANDSTONE)), SurfaceRules.ifTrue(surfacerules_conditionsource14, SurfaceRules.ifTrue(SurfaceRules.VERY_DEEP_UNDER_FLOOR, SANDSTONE)))), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS), STONE), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN), surfacerules_rulesource1), surfacerules_rulesource2)));
      ImmutableList.Builder<SurfaceRules.RuleSource> immutablelist_builder = ImmutableList.builder();
      if (flag1) {
         immutablelist_builder.add(SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), BEDROCK));
      }

      if (flag2) {
         immutablelist_builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK));
      }

      SurfaceRules.RuleSource surfacerules_rulesource9 = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), surfacerules_rulesource8);
      immutablelist_builder.add(flag ? surfacerules_rulesource9 : surfacerules_rulesource8);
      immutablelist_builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)), DEEPSLATE));
      return SurfaceRules.sequence(immutablelist_builder.build().toArray((i) -> new SurfaceRules.RuleSource[i]));
   }

   public static SurfaceRules.RuleSource nether() {
      SurfaceRules.ConditionSource surfacerules_conditionsource = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource1 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(32), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource2 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(30), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource3 = SurfaceRules.not(SurfaceRules.yStartCheck(VerticalAnchor.absolute(35), 0));
      SurfaceRules.ConditionSource surfacerules_conditionsource4 = SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0);
      SurfaceRules.ConditionSource surfacerules_conditionsource5 = SurfaceRules.hole();
      SurfaceRules.ConditionSource surfacerules_conditionsource6 = SurfaceRules.noiseCondition(Noises.SOUL_SAND_LAYER, -0.012D);
      SurfaceRules.ConditionSource surfacerules_conditionsource7 = SurfaceRules.noiseCondition(Noises.GRAVEL_LAYER, -0.012D);
      SurfaceRules.ConditionSource surfacerules_conditionsource8 = SurfaceRules.noiseCondition(Noises.PATCH, -0.012D);
      SurfaceRules.ConditionSource surfacerules_conditionsource9 = SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.54D);
      SurfaceRules.ConditionSource surfacerules_conditionsource10 = SurfaceRules.noiseCondition(Noises.NETHER_WART, 1.17D);
      SurfaceRules.ConditionSource surfacerules_conditionsource11 = SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.0D);
      SurfaceRules.RuleSource surfacerules_rulesource = SurfaceRules.ifTrue(surfacerules_conditionsource8, SurfaceRules.ifTrue(surfacerules_conditionsource2, SurfaceRules.ifTrue(surfacerules_conditionsource3, GRAVEL)));
      return SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK), SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), BEDROCK), SurfaceRules.ifTrue(surfacerules_conditionsource4, NETHERRACK), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.BASALT_DELTAS), SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, BASALT), SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(surfacerules_rulesource, SurfaceRules.ifTrue(surfacerules_conditionsource11, BASALT), BLACKSTONE)))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.SOUL_SAND_VALLEY), SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource11, SOUL_SAND), SOUL_SOIL)), SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(surfacerules_rulesource, SurfaceRules.ifTrue(surfacerules_conditionsource11, SOUL_SAND), SOUL_SOIL)))), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource1), SurfaceRules.ifTrue(surfacerules_conditionsource5, LAVA)), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WARPED_FOREST), SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource9), SurfaceRules.ifTrue(surfacerules_conditionsource, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource10, WARPED_WART_BLOCK), WARPED_NYLIUM)))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.CRIMSON_FOREST), SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource9), SurfaceRules.ifTrue(surfacerules_conditionsource, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource10, NETHER_WART_BLOCK), CRIMSON_NYLIUM)))))), SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.NETHER_WASTES), SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.ifTrue(surfacerules_conditionsource6, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource5), SurfaceRules.ifTrue(surfacerules_conditionsource2, SurfaceRules.ifTrue(surfacerules_conditionsource3, SOUL_SAND))), NETHERRACK))), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.ifTrue(surfacerules_conditionsource, SurfaceRules.ifTrue(surfacerules_conditionsource3, SurfaceRules.ifTrue(surfacerules_conditionsource7, SurfaceRules.sequence(SurfaceRules.ifTrue(surfacerules_conditionsource1, GRAVEL), SurfaceRules.ifTrue(SurfaceRules.not(surfacerules_conditionsource5), GRAVEL)))))))), NETHERRACK);
   }

   public static SurfaceRules.RuleSource end() {
      return ENDSTONE;
   }

   public static SurfaceRules.RuleSource air() {
      return AIR;
   }

   private static SurfaceRules.ConditionSource surfaceNoiseAbove(double d0) {
      return SurfaceRules.noiseCondition(Noises.SURFACE, d0 / 8.25D, Double.MAX_VALUE);
   }
}
