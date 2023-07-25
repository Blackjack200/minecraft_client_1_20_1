package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
   public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RandomPatchConfiguration> featureplacecontext) {
      RandomPatchConfiguration randompatchconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      int i = 0;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int j = randompatchconfiguration.xzSpread() + 1;
      int k = randompatchconfiguration.ySpread() + 1;

      for(int l = 0; l < randompatchconfiguration.tries(); ++l) {
         blockpos_mutableblockpos.setWithOffset(blockpos, randomsource.nextInt(j) - randomsource.nextInt(j), randomsource.nextInt(k) - randomsource.nextInt(k), randomsource.nextInt(j) - randomsource.nextInt(j));
         if (randompatchconfiguration.feature().value().place(worldgenlevel, featureplacecontext.chunkGenerator(), randomsource, blockpos_mutableblockpos)) {
            ++i;
         }
      }

      return i > 0;
   }
}
