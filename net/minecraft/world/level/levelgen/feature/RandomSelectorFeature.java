package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
   public RandomSelectorFeature(Codec<RandomFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> featureplacecontext) {
      RandomFeatureConfiguration randomfeatureconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      ChunkGenerator chunkgenerator = featureplacecontext.chunkGenerator();
      BlockPos blockpos = featureplacecontext.origin();

      for(WeightedPlacedFeature weightedplacedfeature : randomfeatureconfiguration.features) {
         if (randomsource.nextFloat() < weightedplacedfeature.chance) {
            return weightedplacedfeature.place(worldgenlevel, chunkgenerator, randomsource, blockpos);
         }
      }

      return randomfeatureconfiguration.defaultFeature.value().place(worldgenlevel, chunkgenerator, randomsource, blockpos);
   }
}
