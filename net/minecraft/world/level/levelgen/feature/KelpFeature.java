package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class KelpFeature extends Feature<NoneFeatureConfiguration> {
   public KelpFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      int i = 0;
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      int j = worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockpos.getX(), blockpos.getZ());
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), j, blockpos.getZ());
      if (worldgenlevel.getBlockState(blockpos1).is(Blocks.WATER)) {
         BlockState blockstate = Blocks.KELP.defaultBlockState();
         BlockState blockstate1 = Blocks.KELP_PLANT.defaultBlockState();
         int k = 1 + randomsource.nextInt(10);

         for(int l = 0; l <= k; ++l) {
            if (worldgenlevel.getBlockState(blockpos1).is(Blocks.WATER) && worldgenlevel.getBlockState(blockpos1.above()).is(Blocks.WATER) && blockstate1.canSurvive(worldgenlevel, blockpos1)) {
               if (l == k) {
                  worldgenlevel.setBlock(blockpos1, blockstate.setValue(KelpBlock.AGE, Integer.valueOf(randomsource.nextInt(4) + 20)), 2);
                  ++i;
               } else {
                  worldgenlevel.setBlock(blockpos1, blockstate1, 2);
               }
            } else if (l > 0) {
               BlockPos blockpos2 = blockpos1.below();
               if (blockstate.canSurvive(worldgenlevel, blockpos2) && !worldgenlevel.getBlockState(blockpos2.below()).is(Blocks.KELP)) {
                  worldgenlevel.setBlock(blockpos2, blockstate.setValue(KelpBlock.AGE, Integer.valueOf(randomsource.nextInt(4) + 20)), 2);
                  ++i;
               }
               break;
            }

            blockpos1 = blockpos1.above();
         }
      }

      return i > 0;
   }
}
