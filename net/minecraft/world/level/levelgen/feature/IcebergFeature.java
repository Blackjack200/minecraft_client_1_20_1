package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class IcebergFeature extends Feature<BlockStateConfiguration> {
   public IcebergFeature(Codec<BlockStateConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockStateConfiguration> featureplacecontext) {
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      blockpos = new BlockPos(blockpos.getX(), featureplacecontext.chunkGenerator().getSeaLevel(), blockpos.getZ());
      RandomSource randomsource = featureplacecontext.random();
      boolean flag = randomsource.nextDouble() > 0.7D;
      BlockState blockstate = (featureplacecontext.config()).state;
      double d0 = randomsource.nextDouble() * 2.0D * Math.PI;
      int i = 11 - randomsource.nextInt(5);
      int j = 3 + randomsource.nextInt(3);
      boolean flag1 = randomsource.nextDouble() > 0.7D;
      int k = 11;
      int l = flag1 ? randomsource.nextInt(6) + 6 : randomsource.nextInt(15) + 3;
      if (!flag1 && randomsource.nextDouble() > 0.9D) {
         l += randomsource.nextInt(19) + 7;
      }

      int i1 = Math.min(l + randomsource.nextInt(11), 18);
      int j1 = Math.min(l + randomsource.nextInt(7) - randomsource.nextInt(5), 11);
      int k1 = flag1 ? i : 11;

      for(int l1 = -k1; l1 < k1; ++l1) {
         for(int i2 = -k1; i2 < k1; ++i2) {
            for(int j2 = 0; j2 < l; ++j2) {
               int k2 = flag1 ? this.heightDependentRadiusEllipse(j2, l, j1) : this.heightDependentRadiusRound(randomsource, j2, l, j1);
               if (flag1 || l1 < k2) {
                  this.generateIcebergBlock(worldgenlevel, randomsource, blockpos, l, l1, j2, i2, k2, k1, flag1, j, d0, flag, blockstate);
               }
            }
         }
      }

      this.smooth(worldgenlevel, blockpos, j1, l, flag1, i);

      for(int l2 = -k1; l2 < k1; ++l2) {
         for(int i3 = -k1; i3 < k1; ++i3) {
            for(int j3 = -1; j3 > -i1; --j3) {
               int k3 = flag1 ? Mth.ceil((float)k1 * (1.0F - (float)Math.pow((double)j3, 2.0D) / ((float)i1 * 8.0F))) : k1;
               int l3 = this.heightDependentRadiusSteep(randomsource, -j3, i1, j1);
               if (l2 < l3) {
                  this.generateIcebergBlock(worldgenlevel, randomsource, blockpos, i1, l2, j3, i3, l3, k3, flag1, j, d0, flag, blockstate);
               }
            }
         }
      }

      boolean flag2 = flag1 ? randomsource.nextDouble() > 0.1D : randomsource.nextDouble() > 0.7D;
      if (flag2) {
         this.generateCutOut(randomsource, worldgenlevel, j1, l, blockpos, flag1, i, d0, j);
      }

      return true;
   }

   private void generateCutOut(RandomSource randomsource, LevelAccessor levelaccessor, int i, int j, BlockPos blockpos, boolean flag, int k, double d0, int l) {
      int i1 = randomsource.nextBoolean() ? -1 : 1;
      int j1 = randomsource.nextBoolean() ? -1 : 1;
      int k1 = randomsource.nextInt(Math.max(i / 2 - 2, 1));
      if (randomsource.nextBoolean()) {
         k1 = i / 2 + 1 - randomsource.nextInt(Math.max(i - i / 2 - 1, 1));
      }

      int l1 = randomsource.nextInt(Math.max(i / 2 - 2, 1));
      if (randomsource.nextBoolean()) {
         l1 = i / 2 + 1 - randomsource.nextInt(Math.max(i - i / 2 - 1, 1));
      }

      if (flag) {
         k1 = l1 = randomsource.nextInt(Math.max(k - 5, 1));
      }

      BlockPos blockpos1 = new BlockPos(i1 * k1, 0, j1 * l1);
      double d1 = flag ? d0 + (Math.PI / 2D) : randomsource.nextDouble() * 2.0D * Math.PI;

      for(int i2 = 0; i2 < j - 3; ++i2) {
         int j2 = this.heightDependentRadiusRound(randomsource, i2, j, i);
         this.carve(j2, i2, blockpos, levelaccessor, false, d1, blockpos1, k, l);
      }

      for(int k2 = -1; k2 > -j + randomsource.nextInt(5); --k2) {
         int l2 = this.heightDependentRadiusSteep(randomsource, -k2, j, i);
         this.carve(l2, k2, blockpos, levelaccessor, true, d1, blockpos1, k, l);
      }

   }

   private void carve(int i, int j, BlockPos blockpos, LevelAccessor levelaccessor, boolean flag, double d0, BlockPos blockpos1, int k, int l) {
      int i1 = i + 1 + k / 3;
      int j1 = Math.min(i - 3, 3) + l / 2 - 1;

      for(int k1 = -i1; k1 < i1; ++k1) {
         for(int l1 = -i1; l1 < i1; ++l1) {
            double d1 = this.signedDistanceEllipse(k1, l1, blockpos1, i1, j1, d0);
            if (d1 < 0.0D) {
               BlockPos blockpos2 = blockpos.offset(k1, j, l1);
               BlockState blockstate = levelaccessor.getBlockState(blockpos2);
               if (isIcebergState(blockstate) || blockstate.is(Blocks.SNOW_BLOCK)) {
                  if (flag) {
                     this.setBlock(levelaccessor, blockpos2, Blocks.WATER.defaultBlockState());
                  } else {
                     this.setBlock(levelaccessor, blockpos2, Blocks.AIR.defaultBlockState());
                     this.removeFloatingSnowLayer(levelaccessor, blockpos2);
                  }
               }
            }
         }
      }

   }

   private void removeFloatingSnowLayer(LevelAccessor levelaccessor, BlockPos blockpos) {
      if (levelaccessor.getBlockState(blockpos.above()).is(Blocks.SNOW)) {
         this.setBlock(levelaccessor, blockpos.above(), Blocks.AIR.defaultBlockState());
      }

   }

   private void generateIcebergBlock(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, int i, int j, int k, int l, int i1, int j1, boolean flag, int k1, double d0, boolean flag1, BlockState blockstate) {
      double d1 = flag ? this.signedDistanceEllipse(j, l, BlockPos.ZERO, j1, this.getEllipseC(k, i, k1), d0) : this.signedDistanceCircle(j, l, BlockPos.ZERO, i1, randomsource);
      if (d1 < 0.0D) {
         BlockPos blockpos1 = blockpos.offset(j, k, l);
         double d2 = flag ? -0.5D : (double)(-6 - randomsource.nextInt(3));
         if (d1 > d2 && randomsource.nextDouble() > 0.9D) {
            return;
         }

         this.setIcebergBlock(blockpos1, levelaccessor, randomsource, i - k, i, flag, flag1, blockstate);
      }

   }

   private void setIcebergBlock(BlockPos blockpos, LevelAccessor levelaccessor, RandomSource randomsource, int i, int j, boolean flag, boolean flag1, BlockState blockstate) {
      BlockState blockstate1 = levelaccessor.getBlockState(blockpos);
      if (blockstate1.isAir() || blockstate1.is(Blocks.SNOW_BLOCK) || blockstate1.is(Blocks.ICE) || blockstate1.is(Blocks.WATER)) {
         boolean flag2 = !flag || randomsource.nextDouble() > 0.05D;
         int k = flag ? 3 : 2;
         if (flag1 && !blockstate1.is(Blocks.WATER) && (double)i <= (double)randomsource.nextInt(Math.max(1, j / k)) + (double)j * 0.6D && flag2) {
            this.setBlock(levelaccessor, blockpos, Blocks.SNOW_BLOCK.defaultBlockState());
         } else {
            this.setBlock(levelaccessor, blockpos, blockstate);
         }
      }

   }

   private int getEllipseC(int i, int j, int k) {
      int l = k;
      if (i > 0 && j - i <= 3) {
         l = k - (4 - (j - i));
      }

      return l;
   }

   private double signedDistanceCircle(int i, int j, BlockPos blockpos, int k, RandomSource randomsource) {
      float f = 10.0F * Mth.clamp(randomsource.nextFloat(), 0.2F, 0.8F) / (float)k;
      return (double)f + Math.pow((double)(i - blockpos.getX()), 2.0D) + Math.pow((double)(j - blockpos.getZ()), 2.0D) - Math.pow((double)k, 2.0D);
   }

   private double signedDistanceEllipse(int i, int j, BlockPos blockpos, int k, int l, double d0) {
      return Math.pow(((double)(i - blockpos.getX()) * Math.cos(d0) - (double)(j - blockpos.getZ()) * Math.sin(d0)) / (double)k, 2.0D) + Math.pow(((double)(i - blockpos.getX()) * Math.sin(d0) + (double)(j - blockpos.getZ()) * Math.cos(d0)) / (double)l, 2.0D) - 1.0D;
   }

   private int heightDependentRadiusRound(RandomSource randomsource, int i, int j, int k) {
      float f = 3.5F - randomsource.nextFloat();
      float f1 = (1.0F - (float)Math.pow((double)i, 2.0D) / ((float)j * f)) * (float)k;
      if (j > 15 + randomsource.nextInt(5)) {
         int l = i < 3 + randomsource.nextInt(6) ? i / 2 : i;
         f1 = (1.0F - (float)l / ((float)j * f * 0.4F)) * (float)k;
      }

      return Mth.ceil(f1 / 2.0F);
   }

   private int heightDependentRadiusEllipse(int i, int j, int k) {
      float f = 1.0F;
      float f1 = (1.0F - (float)Math.pow((double)i, 2.0D) / ((float)j * 1.0F)) * (float)k;
      return Mth.ceil(f1 / 2.0F);
   }

   private int heightDependentRadiusSteep(RandomSource randomsource, int i, int j, int k) {
      float f = 1.0F + randomsource.nextFloat() / 2.0F;
      float f1 = (1.0F - (float)i / ((float)j * f)) * (float)k;
      return Mth.ceil(f1 / 2.0F);
   }

   private static boolean isIcebergState(BlockState blockstate) {
      return blockstate.is(Blocks.PACKED_ICE) || blockstate.is(Blocks.SNOW_BLOCK) || blockstate.is(Blocks.BLUE_ICE);
   }

   private boolean belowIsAir(BlockGetter blockgetter, BlockPos blockpos) {
      return blockgetter.getBlockState(blockpos.below()).isAir();
   }

   private void smooth(LevelAccessor levelaccessor, BlockPos blockpos, int i, int j, boolean flag, int k) {
      int l = flag ? k : i / 2;

      for(int i1 = -l; i1 <= l; ++i1) {
         for(int j1 = -l; j1 <= l; ++j1) {
            for(int k1 = 0; k1 <= j; ++k1) {
               BlockPos blockpos1 = blockpos.offset(i1, k1, j1);
               BlockState blockstate = levelaccessor.getBlockState(blockpos1);
               if (isIcebergState(blockstate) || blockstate.is(Blocks.SNOW)) {
                  if (this.belowIsAir(levelaccessor, blockpos1)) {
                     this.setBlock(levelaccessor, blockpos1, Blocks.AIR.defaultBlockState());
                     this.setBlock(levelaccessor, blockpos1.above(), Blocks.AIR.defaultBlockState());
                  } else if (isIcebergState(blockstate)) {
                     BlockState[] ablockstate = new BlockState[]{levelaccessor.getBlockState(blockpos1.west()), levelaccessor.getBlockState(blockpos1.east()), levelaccessor.getBlockState(blockpos1.north()), levelaccessor.getBlockState(blockpos1.south())};
                     int l1 = 0;

                     for(BlockState blockstate1 : ablockstate) {
                        if (!isIcebergState(blockstate1)) {
                           ++l1;
                        }
                     }

                     if (l1 >= 3) {
                        this.setBlock(levelaccessor, blockpos1, Blocks.AIR.defaultBlockState());
                     }
                  }
               }
            }
         }
      }

   }
}
