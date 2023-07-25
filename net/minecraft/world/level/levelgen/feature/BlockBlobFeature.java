package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature extends Feature<BlockStateConfiguration> {
   public BlockBlobFeature(Codec<BlockStateConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockStateConfiguration> featureplacecontext) {
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();

      BlockStateConfiguration blockstateconfiguration;
      for(blockstateconfiguration = featureplacecontext.config(); blockpos.getY() > worldgenlevel.getMinBuildHeight() + 3; blockpos = blockpos.below()) {
         if (!worldgenlevel.isEmptyBlock(blockpos.below())) {
            BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
            if (isDirt(blockstate) || isStone(blockstate)) {
               break;
            }
         }
      }

      if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 3) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            int j = randomsource.nextInt(2);
            int k = randomsource.nextInt(2);
            int l = randomsource.nextInt(2);
            float f = (float)(j + k + l) * 0.333F + 0.5F;

            for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-j, -k, -l), blockpos.offset(j, k, l))) {
               if (blockpos1.distSqr(blockpos) <= (double)(f * f)) {
                  worldgenlevel.setBlock(blockpos1, blockstateconfiguration.state, 3);
               }
            }

            blockpos = blockpos.offset(-1 + randomsource.nextInt(2), -randomsource.nextInt(2), -1 + randomsource.nextInt(2));
         }

         return true;
      }
   }
}
