package net.minecraft.client.renderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;

public class BiomeColors {
   public static final ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
   public static final ColorResolver FOLIAGE_COLOR_RESOLVER = (biome, d0, d1) -> biome.getFoliageColor();
   public static final ColorResolver WATER_COLOR_RESOLVER = (biome, d0, d1) -> biome.getWaterColor();

   private static int getAverageColor(BlockAndTintGetter blockandtintgetter, BlockPos blockpos, ColorResolver colorresolver) {
      return blockandtintgetter.getBlockTint(blockpos, colorresolver);
   }

   public static int getAverageGrassColor(BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
      return getAverageColor(blockandtintgetter, blockpos, GRASS_COLOR_RESOLVER);
   }

   public static int getAverageFoliageColor(BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
      return getAverageColor(blockandtintgetter, blockpos, FOLIAGE_COLOR_RESOLVER);
   }

   public static int getAverageWaterColor(BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
      return getAverageColor(blockandtintgetter, blockpos, WATER_COLOR_RESOLVER);
   }
}
