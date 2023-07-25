package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeRedMushroomFeature extends AbstractHugeMushroomFeature {
   public HugeRedMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
      super(codec);
   }

   protected void makeCap(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos, HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration) {
      for(int j = i - 3; j <= i; ++j) {
         int k = j < i ? hugemushroomfeatureconfiguration.foliageRadius : hugemushroomfeatureconfiguration.foliageRadius - 1;
         int l = hugemushroomfeatureconfiguration.foliageRadius - 2;

         for(int i1 = -k; i1 <= k; ++i1) {
            for(int j1 = -k; j1 <= k; ++j1) {
               boolean flag = i1 == -k;
               boolean flag1 = i1 == k;
               boolean flag2 = j1 == -k;
               boolean flag3 = j1 == k;
               boolean flag4 = flag || flag1;
               boolean flag5 = flag2 || flag3;
               if (j >= i || flag4 != flag5) {
                  blockpos_mutableblockpos.setWithOffset(blockpos, i1, j, j1);
                  if (!levelaccessor.getBlockState(blockpos_mutableblockpos).isSolidRender(levelaccessor, blockpos_mutableblockpos)) {
                     BlockState blockstate = hugemushroomfeatureconfiguration.capProvider.getState(randomsource, blockpos);
                     if (blockstate.hasProperty(HugeMushroomBlock.WEST) && blockstate.hasProperty(HugeMushroomBlock.EAST) && blockstate.hasProperty(HugeMushroomBlock.NORTH) && blockstate.hasProperty(HugeMushroomBlock.SOUTH) && blockstate.hasProperty(HugeMushroomBlock.UP)) {
                        blockstate = blockstate.setValue(HugeMushroomBlock.UP, Boolean.valueOf(j >= i - 1)).setValue(HugeMushroomBlock.WEST, Boolean.valueOf(i1 < -l)).setValue(HugeMushroomBlock.EAST, Boolean.valueOf(i1 > l)).setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(j1 < -l)).setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(j1 > l));
                     }

                     this.setBlock(levelaccessor, blockpos_mutableblockpos, blockstate);
                  }
               }
            }
         }
      }

   }

   protected int getTreeRadiusForHeight(int i, int j, int k, int l) {
      int i1 = 0;
      if (l < j && l >= j - 3) {
         i1 = k;
      } else if (l == j) {
         i1 = k;
      }

      return i1;
   }
}
