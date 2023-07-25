package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class HugeFungusFeature extends Feature<HugeFungusConfiguration> {
   private static final float HUGE_PROBABILITY = 0.06F;

   public HugeFungusFeature(Codec<HugeFungusConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<HugeFungusConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      ChunkGenerator chunkgenerator = featureplacecontext.chunkGenerator();
      HugeFungusConfiguration hugefungusconfiguration = featureplacecontext.config();
      Block block = hugefungusconfiguration.validBaseState.getBlock();
      BlockPos blockpos1 = null;
      BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
      if (blockstate.is(block)) {
         blockpos1 = blockpos;
      }

      if (blockpos1 == null) {
         return false;
      } else {
         int i = Mth.nextInt(randomsource, 4, 13);
         if (randomsource.nextInt(12) == 0) {
            i *= 2;
         }

         if (!hugefungusconfiguration.planted) {
            int j = chunkgenerator.getGenDepth();
            if (blockpos1.getY() + i + 1 >= j) {
               return false;
            }
         }

         boolean flag = !hugefungusconfiguration.planted && randomsource.nextFloat() < 0.06F;
         worldgenlevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 4);
         this.placeStem(worldgenlevel, randomsource, hugefungusconfiguration, blockpos1, i, flag);
         this.placeHat(worldgenlevel, randomsource, hugefungusconfiguration, blockpos1, i, flag);
         return true;
      }
   }

   private static boolean isReplaceable(WorldGenLevel worldgenlevel, BlockPos blockpos, HugeFungusConfiguration hugefungusconfiguration, boolean flag) {
      if (worldgenlevel.isStateAtPosition(blockpos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
         return true;
      } else {
         return flag ? hugefungusconfiguration.replaceableBlocks.test(worldgenlevel, blockpos) : false;
      }
   }

   private void placeStem(WorldGenLevel worldgenlevel, RandomSource randomsource, HugeFungusConfiguration hugefungusconfiguration, BlockPos blockpos, int i, boolean flag) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockState blockstate = hugefungusconfiguration.stemState;
      int j = flag ? 1 : 0;

      for(int k = -j; k <= j; ++k) {
         for(int l = -j; l <= j; ++l) {
            boolean flag1 = flag && Mth.abs(k) == j && Mth.abs(l) == j;

            for(int i1 = 0; i1 < i; ++i1) {
               blockpos_mutableblockpos.setWithOffset(blockpos, k, i1, l);
               if (isReplaceable(worldgenlevel, blockpos_mutableblockpos, hugefungusconfiguration, true)) {
                  if (hugefungusconfiguration.planted) {
                     if (!worldgenlevel.getBlockState(blockpos_mutableblockpos.below()).isAir()) {
                        worldgenlevel.destroyBlock(blockpos_mutableblockpos, true);
                     }

                     worldgenlevel.setBlock(blockpos_mutableblockpos, blockstate, 3);
                  } else if (flag1) {
                     if (randomsource.nextFloat() < 0.1F) {
                        this.setBlock(worldgenlevel, blockpos_mutableblockpos, blockstate);
                     }
                  } else {
                     this.setBlock(worldgenlevel, blockpos_mutableblockpos, blockstate);
                  }
               }
            }
         }
      }

   }

   private void placeHat(WorldGenLevel worldgenlevel, RandomSource randomsource, HugeFungusConfiguration hugefungusconfiguration, BlockPos blockpos, int i, boolean flag) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      boolean flag1 = hugefungusconfiguration.hatState.is(Blocks.NETHER_WART_BLOCK);
      int j = Math.min(randomsource.nextInt(1 + i / 3) + 5, i);
      int k = i - j;

      for(int l = k; l <= i; ++l) {
         int i1 = l < i - randomsource.nextInt(3) ? 2 : 1;
         if (j > 8 && l < k + 4) {
            i1 = 3;
         }

         if (flag) {
            ++i1;
         }

         for(int j1 = -i1; j1 <= i1; ++j1) {
            for(int k1 = -i1; k1 <= i1; ++k1) {
               boolean flag2 = j1 == -i1 || j1 == i1;
               boolean flag3 = k1 == -i1 || k1 == i1;
               boolean flag4 = !flag2 && !flag3 && l != i;
               boolean flag5 = flag2 && flag3;
               boolean flag6 = l < k + 3;
               blockpos_mutableblockpos.setWithOffset(blockpos, j1, l, k1);
               if (isReplaceable(worldgenlevel, blockpos_mutableblockpos, hugefungusconfiguration, false)) {
                  if (hugefungusconfiguration.planted && !worldgenlevel.getBlockState(blockpos_mutableblockpos.below()).isAir()) {
                     worldgenlevel.destroyBlock(blockpos_mutableblockpos, true);
                  }

                  if (flag6) {
                     if (!flag4) {
                        this.placeHatDropBlock(worldgenlevel, randomsource, blockpos_mutableblockpos, hugefungusconfiguration.hatState, flag1);
                     }
                  } else if (flag4) {
                     this.placeHatBlock(worldgenlevel, randomsource, hugefungusconfiguration, blockpos_mutableblockpos, 0.1F, 0.2F, flag1 ? 0.1F : 0.0F);
                  } else if (flag5) {
                     this.placeHatBlock(worldgenlevel, randomsource, hugefungusconfiguration, blockpos_mutableblockpos, 0.01F, 0.7F, flag1 ? 0.083F : 0.0F);
                  } else {
                     this.placeHatBlock(worldgenlevel, randomsource, hugefungusconfiguration, blockpos_mutableblockpos, 5.0E-4F, 0.98F, flag1 ? 0.07F : 0.0F);
                  }
               }
            }
         }
      }

   }

   private void placeHatBlock(LevelAccessor levelaccessor, RandomSource randomsource, HugeFungusConfiguration hugefungusconfiguration, BlockPos.MutableBlockPos blockpos_mutableblockpos, float f, float f1, float f2) {
      if (randomsource.nextFloat() < f) {
         this.setBlock(levelaccessor, blockpos_mutableblockpos, hugefungusconfiguration.decorState);
      } else if (randomsource.nextFloat() < f1) {
         this.setBlock(levelaccessor, blockpos_mutableblockpos, hugefungusconfiguration.hatState);
         if (randomsource.nextFloat() < f2) {
            tryPlaceWeepingVines(blockpos_mutableblockpos, levelaccessor, randomsource);
         }
      }

   }

   private void placeHatDropBlock(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, BlockState blockstate, boolean flag) {
      if (levelaccessor.getBlockState(blockpos.below()).is(blockstate.getBlock())) {
         this.setBlock(levelaccessor, blockpos, blockstate);
      } else if ((double)randomsource.nextFloat() < 0.15D) {
         this.setBlock(levelaccessor, blockpos, blockstate);
         if (flag && randomsource.nextInt(11) == 0) {
            tryPlaceWeepingVines(blockpos, levelaccessor, randomsource);
         }
      }

   }

   private static void tryPlaceWeepingVines(BlockPos blockpos, LevelAccessor levelaccessor, RandomSource randomsource) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable().move(Direction.DOWN);
      if (levelaccessor.isEmptyBlock(blockpos_mutableblockpos)) {
         int i = Mth.nextInt(randomsource, 1, 5);
         if (randomsource.nextInt(7) == 0) {
            i *= 2;
         }

         int j = 23;
         int k = 25;
         WeepingVinesFeature.placeWeepingVinesColumn(levelaccessor, randomsource, blockpos_mutableblockpos, i, 23, 25);
      }
   }
}
