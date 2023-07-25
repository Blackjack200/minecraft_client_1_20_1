package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeBrownMushroomFeature extends AbstractHugeMushroomFeature {
   public HugeBrownMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
      super(codec);
   }

   protected void makeCap(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos, HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration) {
      int j = hugemushroomfeatureconfiguration.foliageRadius;

      for(int k = -j; k <= j; ++k) {
         for(int l = -j; l <= j; ++l) {
            boolean flag = k == -j;
            boolean flag1 = k == j;
            boolean flag2 = l == -j;
            boolean flag3 = l == j;
            boolean flag4 = flag || flag1;
            boolean flag5 = flag2 || flag3;
            if (!flag4 || !flag5) {
               blockpos_mutableblockpos.setWithOffset(blockpos, k, i, l);
               if (!levelaccessor.getBlockState(blockpos_mutableblockpos).isSolidRender(levelaccessor, blockpos_mutableblockpos)) {
                  boolean flag6 = flag || flag5 && k == 1 - j;
                  boolean flag7 = flag1 || flag5 && k == j - 1;
                  boolean flag8 = flag2 || flag4 && l == 1 - j;
                  boolean flag9 = flag3 || flag4 && l == j - 1;
                  BlockState blockstate = hugemushroomfeatureconfiguration.capProvider.getState(randomsource, blockpos);
                  if (blockstate.hasProperty(HugeMushroomBlock.WEST) && blockstate.hasProperty(HugeMushroomBlock.EAST) && blockstate.hasProperty(HugeMushroomBlock.NORTH) && blockstate.hasProperty(HugeMushroomBlock.SOUTH)) {
                     blockstate = blockstate.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(flag6)).setValue(HugeMushroomBlock.EAST, Boolean.valueOf(flag7)).setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(flag8)).setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(flag9));
                  }

                  this.setBlock(levelaccessor, blockpos_mutableblockpos, blockstate);
               }
            }
         }
      }

   }

   protected int getTreeRadiusForHeight(int i, int j, int k, int l) {
      return l <= 3 ? 0 : k;
   }
}
