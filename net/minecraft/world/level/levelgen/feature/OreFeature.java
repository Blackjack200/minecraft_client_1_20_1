package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature extends Feature<OreConfiguration> {
   public OreFeature(Codec<OreConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<OreConfiguration> featureplacecontext) {
      RandomSource randomsource = featureplacecontext.random();
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      OreConfiguration oreconfiguration = featureplacecontext.config();
      float f = randomsource.nextFloat() * (float)Math.PI;
      float f1 = (float)oreconfiguration.size / 8.0F;
      int i = Mth.ceil(((float)oreconfiguration.size / 16.0F * 2.0F + 1.0F) / 2.0F);
      double d0 = (double)blockpos.getX() + Math.sin((double)f) * (double)f1;
      double d1 = (double)blockpos.getX() - Math.sin((double)f) * (double)f1;
      double d2 = (double)blockpos.getZ() + Math.cos((double)f) * (double)f1;
      double d3 = (double)blockpos.getZ() - Math.cos((double)f) * (double)f1;
      int j = 2;
      double d4 = (double)(blockpos.getY() + randomsource.nextInt(3) - 2);
      double d5 = (double)(blockpos.getY() + randomsource.nextInt(3) - 2);
      int k = blockpos.getX() - Mth.ceil(f1) - i;
      int l = blockpos.getY() - 2 - i;
      int i1 = blockpos.getZ() - Mth.ceil(f1) - i;
      int j1 = 2 * (Mth.ceil(f1) + i);
      int k1 = 2 * (2 + i);

      for(int l1 = k; l1 <= k + j1; ++l1) {
         for(int i2 = i1; i2 <= i1 + j1; ++i2) {
            if (l <= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l1, i2)) {
               return this.doPlace(worldgenlevel, randomsource, oreconfiguration, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1);
            }
         }
      }

      return false;
   }

   protected boolean doPlace(WorldGenLevel worldgenlevel, RandomSource randomsource, OreConfiguration oreconfiguration, double d0, double d1, double d2, double d3, double d4, double d5, int i, int j, int k, int l, int i1) {
      int j1 = 0;
      BitSet bitset = new BitSet(l * i1 * l);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int k1 = oreconfiguration.size;
      double[] adouble = new double[k1 * 4];

      for(int l1 = 0; l1 < k1; ++l1) {
         float f = (float)l1 / (float)k1;
         double d6 = Mth.lerp((double)f, d0, d1);
         double d7 = Mth.lerp((double)f, d4, d5);
         double d8 = Mth.lerp((double)f, d2, d3);
         double d9 = randomsource.nextDouble() * (double)k1 / 16.0D;
         double d10 = ((double)(Mth.sin((float)Math.PI * f) + 1.0F) * d9 + 1.0D) / 2.0D;
         adouble[l1 * 4 + 0] = d6;
         adouble[l1 * 4 + 1] = d7;
         adouble[l1 * 4 + 2] = d8;
         adouble[l1 * 4 + 3] = d10;
      }

      for(int i2 = 0; i2 < k1 - 1; ++i2) {
         if (!(adouble[i2 * 4 + 3] <= 0.0D)) {
            for(int j2 = i2 + 1; j2 < k1; ++j2) {
               if (!(adouble[j2 * 4 + 3] <= 0.0D)) {
                  double d11 = adouble[i2 * 4 + 0] - adouble[j2 * 4 + 0];
                  double d12 = adouble[i2 * 4 + 1] - adouble[j2 * 4 + 1];
                  double d13 = adouble[i2 * 4 + 2] - adouble[j2 * 4 + 2];
                  double d14 = adouble[i2 * 4 + 3] - adouble[j2 * 4 + 3];
                  if (d14 * d14 > d11 * d11 + d12 * d12 + d13 * d13) {
                     if (d14 > 0.0D) {
                        adouble[j2 * 4 + 3] = -1.0D;
                     } else {
                        adouble[i2 * 4 + 3] = -1.0D;
                     }
                  }
               }
            }
         }
      }

      BulkSectionAccess bulksectionaccess = new BulkSectionAccess(worldgenlevel);

      try {
         for(int k2 = 0; k2 < k1; ++k2) {
            double d15 = adouble[k2 * 4 + 3];
            if (!(d15 < 0.0D)) {
               double d16 = adouble[k2 * 4 + 0];
               double d17 = adouble[k2 * 4 + 1];
               double d18 = adouble[k2 * 4 + 2];
               int l2 = Math.max(Mth.floor(d16 - d15), i);
               int i3 = Math.max(Mth.floor(d17 - d15), j);
               int j3 = Math.max(Mth.floor(d18 - d15), k);
               int k3 = Math.max(Mth.floor(d16 + d15), l2);
               int l3 = Math.max(Mth.floor(d17 + d15), i3);
               int i4 = Math.max(Mth.floor(d18 + d15), j3);

               for(int j4 = l2; j4 <= k3; ++j4) {
                  double d19 = ((double)j4 + 0.5D - d16) / d15;
                  if (d19 * d19 < 1.0D) {
                     for(int k4 = i3; k4 <= l3; ++k4) {
                        double d20 = ((double)k4 + 0.5D - d17) / d15;
                        if (d19 * d19 + d20 * d20 < 1.0D) {
                           for(int l4 = j3; l4 <= i4; ++l4) {
                              double d21 = ((double)l4 + 0.5D - d18) / d15;
                              if (d19 * d19 + d20 * d20 + d21 * d21 < 1.0D && !worldgenlevel.isOutsideBuildHeight(k4)) {
                                 int i5 = j4 - i + (k4 - j) * l + (l4 - k) * l * i1;
                                 if (!bitset.get(i5)) {
                                    bitset.set(i5);
                                    blockpos_mutableblockpos.set(j4, k4, l4);
                                    if (worldgenlevel.ensureCanWrite(blockpos_mutableblockpos)) {
                                       LevelChunkSection levelchunksection = bulksectionaccess.getSection(blockpos_mutableblockpos);
                                       if (levelchunksection != null) {
                                          int j5 = SectionPos.sectionRelative(j4);
                                          int k5 = SectionPos.sectionRelative(k4);
                                          int l5 = SectionPos.sectionRelative(l4);
                                          BlockState blockstate = levelchunksection.getBlockState(j5, k5, l5);

                                          for(OreConfiguration.TargetBlockState oreconfiguration_targetblockstate : oreconfiguration.targetStates) {
                                             if (canPlaceOre(blockstate, bulksectionaccess::getBlockState, randomsource, oreconfiguration, oreconfiguration_targetblockstate, blockpos_mutableblockpos)) {
                                                levelchunksection.setBlockState(j5, k5, l5, oreconfiguration_targetblockstate.state, false);
                                                ++j1;
                                                break;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var60) {
         try {
            bulksectionaccess.close();
         } catch (Throwable var59) {
            var60.addSuppressed(var59);
         }

         throw var60;
      }

      bulksectionaccess.close();
      return j1 > 0;
   }

   public static boolean canPlaceOre(BlockState blockstate, Function<BlockPos, BlockState> function, RandomSource randomsource, OreConfiguration oreconfiguration, OreConfiguration.TargetBlockState oreconfiguration_targetblockstate, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      if (!oreconfiguration_targetblockstate.target.test(blockstate, randomsource)) {
         return false;
      } else if (shouldSkipAirCheck(randomsource, oreconfiguration.discardChanceOnAirExposure)) {
         return true;
      } else {
         return !isAdjacentToAir(function, blockpos_mutableblockpos);
      }
   }

   protected static boolean shouldSkipAirCheck(RandomSource randomsource, float f) {
      if (f <= 0.0F) {
         return true;
      } else if (f >= 1.0F) {
         return false;
      } else {
         return randomsource.nextFloat() >= f;
      }
   }
}
