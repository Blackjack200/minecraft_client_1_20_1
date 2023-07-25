package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
   public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<SimpleRandomFeatureConfiguration> featureplacecontext) {
      RandomSource randomsource = featureplacecontext.random();
      SimpleRandomFeatureConfiguration simplerandomfeatureconfiguration = featureplacecontext.config();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      ChunkGenerator chunkgenerator = featureplacecontext.chunkGenerator();
      int i = randomsource.nextInt(simplerandomfeatureconfiguration.features.size());
      PlacedFeature placedfeature = simplerandomfeatureconfiguration.features.get(i).value();
      return placedfeature.place(worldgenlevel, chunkgenerator, randomsource, blockpos);
   }
}
