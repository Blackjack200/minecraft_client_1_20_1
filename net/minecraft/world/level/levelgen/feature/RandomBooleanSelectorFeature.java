package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
   public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RandomBooleanFeatureConfiguration> featureplacecontext) {
      RandomSource randomsource = featureplacecontext.random();
      RandomBooleanFeatureConfiguration randombooleanfeatureconfiguration = featureplacecontext.config();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      ChunkGenerator chunkgenerator = featureplacecontext.chunkGenerator();
      BlockPos blockpos = featureplacecontext.origin();
      boolean flag = randomsource.nextBoolean();
      return ((PlacedFeature)(flag ? randombooleanfeatureconfiguration.featureTrue : randombooleanfeatureconfiguration.featureFalse).value()).place(worldgenlevel, chunkgenerator, randomsource, blockpos);
   }
}
