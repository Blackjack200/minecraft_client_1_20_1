package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
   public AbstractHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
      super(codec);
   }

   protected void placeTrunk(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      for(int j = 0; j < i; ++j) {
         blockpos_mutableblockpos.set(blockpos).move(Direction.UP, j);
         if (!levelaccessor.getBlockState(blockpos_mutableblockpos).isSolidRender(levelaccessor, blockpos_mutableblockpos)) {
            this.setBlock(levelaccessor, blockpos_mutableblockpos, hugemushroomfeatureconfiguration.stemProvider.getState(randomsource, blockpos));
         }
      }

   }

   protected int getTreeHeight(RandomSource randomsource) {
      int i = randomsource.nextInt(3) + 4;
      if (randomsource.nextInt(12) == 0) {
         i *= 2;
      }

      return i;
   }

   protected boolean isValidPosition(LevelAccessor levelaccessor, BlockPos blockpos, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos, HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration) {
      int j = blockpos.getY();
      if (j >= levelaccessor.getMinBuildHeight() + 1 && j + i + 1 < levelaccessor.getMaxBuildHeight()) {
         BlockState blockstate = levelaccessor.getBlockState(blockpos.below());
         if (!isDirt(blockstate) && !blockstate.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
         } else {
            for(int k = 0; k <= i; ++k) {
               int l = this.getTreeRadiusForHeight(-1, -1, hugemushroomfeatureconfiguration.foliageRadius, k);

               for(int i1 = -l; i1 <= l; ++i1) {
                  for(int j1 = -l; j1 <= l; ++j1) {
                     BlockState blockstate1 = levelaccessor.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos, i1, k, j1));
                     if (!blockstate1.isAir() && !blockstate1.is(BlockTags.LEAVES)) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration = featureplacecontext.config();
      int i = this.getTreeHeight(randomsource);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      if (!this.isValidPosition(worldgenlevel, blockpos, i, blockpos_mutableblockpos, hugemushroomfeatureconfiguration)) {
         return false;
      } else {
         this.makeCap(worldgenlevel, randomsource, blockpos, i, blockpos_mutableblockpos, hugemushroomfeatureconfiguration);
         this.placeTrunk(worldgenlevel, randomsource, blockpos, hugemushroomfeatureconfiguration, i, blockpos_mutableblockpos);
         return true;
      }
   }

   protected abstract int getTreeRadiusForHeight(int i, int j, int k, int l);

   protected abstract void makeCap(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos, HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration);
}
