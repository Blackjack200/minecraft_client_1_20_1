package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {
   static Aquifer create(NoiseChunk noisechunk, ChunkPos chunkpos, NoiseRouter noiserouter, PositionalRandomFactory positionalrandomfactory, int i, int j, Aquifer.FluidPicker aquifer_fluidpicker) {
      return new Aquifer.NoiseBasedAquifer(noisechunk, chunkpos, noiserouter, positionalrandomfactory, i, j, aquifer_fluidpicker);
   }

   static Aquifer createDisabled(final Aquifer.FluidPicker aquifer_fluidpicker) {
      return new Aquifer() {
         @Nullable
         public BlockState computeSubstance(DensityFunction.FunctionContext densityfunction_functioncontext, double d0) {
            return d0 > 0.0D ? null : aquifer_fluidpicker.computeFluid(densityfunction_functioncontext.blockX(), densityfunction_functioncontext.blockY(), densityfunction_functioncontext.blockZ()).at(densityfunction_functioncontext.blockY());
         }

         public boolean shouldScheduleFluidUpdate() {
            return false;
         }
      };
   }

   @Nullable
   BlockState computeSubstance(DensityFunction.FunctionContext densityfunction_functioncontext, double d0);

   boolean shouldScheduleFluidUpdate();

   public interface FluidPicker {
      Aquifer.FluidStatus computeFluid(int i, int j, int k);
   }

   public static final class FluidStatus {
      final int fluidLevel;
      final BlockState fluidType;

      public FluidStatus(int i, BlockState blockstate) {
         this.fluidLevel = i;
         this.fluidType = blockstate;
      }

      public BlockState at(int i) {
         return i < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
      }
   }

   public static class NoiseBasedAquifer implements Aquifer {
      private static final int X_RANGE = 10;
      private static final int Y_RANGE = 9;
      private static final int Z_RANGE = 10;
      private static final int X_SEPARATION = 6;
      private static final int Y_SEPARATION = 3;
      private static final int Z_SEPARATION = 6;
      private static final int X_SPACING = 16;
      private static final int Y_SPACING = 12;
      private static final int Z_SPACING = 16;
      private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
      private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
      private final NoiseChunk noiseChunk;
      private final DensityFunction barrierNoise;
      private final DensityFunction fluidLevelFloodednessNoise;
      private final DensityFunction fluidLevelSpreadNoise;
      private final DensityFunction lavaNoise;
      private final PositionalRandomFactory positionalRandomFactory;
      private final Aquifer.FluidStatus[] aquiferCache;
      private final long[] aquiferLocationCache;
      private final Aquifer.FluidPicker globalFluidPicker;
      private final DensityFunction erosion;
      private final DensityFunction depth;
      private boolean shouldScheduleFluidUpdate;
      private final int minGridX;
      private final int minGridY;
      private final int minGridZ;
      private final int gridSizeX;
      private final int gridSizeZ;
      private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};

      NoiseBasedAquifer(NoiseChunk noisechunk, ChunkPos chunkpos, NoiseRouter noiserouter, PositionalRandomFactory positionalrandomfactory, int i, int j, Aquifer.FluidPicker aquifer_fluidpicker) {
         this.noiseChunk = noisechunk;
         this.barrierNoise = noiserouter.barrierNoise();
         this.fluidLevelFloodednessNoise = noiserouter.fluidLevelFloodednessNoise();
         this.fluidLevelSpreadNoise = noiserouter.fluidLevelSpreadNoise();
         this.lavaNoise = noiserouter.lavaNoise();
         this.erosion = noiserouter.erosion();
         this.depth = noiserouter.depth();
         this.positionalRandomFactory = positionalrandomfactory;
         this.minGridX = this.gridX(chunkpos.getMinBlockX()) - 1;
         this.globalFluidPicker = aquifer_fluidpicker;
         int k = this.gridX(chunkpos.getMaxBlockX()) + 1;
         this.gridSizeX = k - this.minGridX + 1;
         this.minGridY = this.gridY(i) - 1;
         int l = this.gridY(i + j) + 1;
         int i1 = l - this.minGridY + 1;
         this.minGridZ = this.gridZ(chunkpos.getMinBlockZ()) - 1;
         int j1 = this.gridZ(chunkpos.getMaxBlockZ()) + 1;
         this.gridSizeZ = j1 - this.minGridZ + 1;
         int k1 = this.gridSizeX * i1 * this.gridSizeZ;
         this.aquiferCache = new Aquifer.FluidStatus[k1];
         this.aquiferLocationCache = new long[k1];
         Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
      }

      private int getIndex(int i, int j, int k) {
         int l = i - this.minGridX;
         int i1 = j - this.minGridY;
         int j1 = k - this.minGridZ;
         return (i1 * this.gridSizeZ + j1) * this.gridSizeX + l;
      }

      @Nullable
      public BlockState computeSubstance(DensityFunction.FunctionContext densityfunction_functioncontext, double d0) {
         int i = densityfunction_functioncontext.blockX();
         int j = densityfunction_functioncontext.blockY();
         int k = densityfunction_functioncontext.blockZ();
         if (d0 > 0.0D) {
            this.shouldScheduleFluidUpdate = false;
            return null;
         } else {
            Aquifer.FluidStatus aquifer_fluidstatus = this.globalFluidPicker.computeFluid(i, j, k);
            if (aquifer_fluidstatus.at(j).is(Blocks.LAVA)) {
               this.shouldScheduleFluidUpdate = false;
               return Blocks.LAVA.defaultBlockState();
            } else {
               int l = Math.floorDiv(i - 5, 16);
               int i1 = Math.floorDiv(j + 1, 12);
               int j1 = Math.floorDiv(k - 5, 16);
               int k1 = Integer.MAX_VALUE;
               int l1 = Integer.MAX_VALUE;
               int i2 = Integer.MAX_VALUE;
               long j2 = 0L;
               long k2 = 0L;
               long l2 = 0L;

               for(int i3 = 0; i3 <= 1; ++i3) {
                  for(int j3 = -1; j3 <= 1; ++j3) {
                     for(int k3 = 0; k3 <= 1; ++k3) {
                        int l3 = l + i3;
                        int i4 = i1 + j3;
                        int j4 = j1 + k3;
                        int k4 = this.getIndex(l3, i4, j4);
                        long l4 = this.aquiferLocationCache[k4];
                        long i5;
                        if (l4 != Long.MAX_VALUE) {
                           i5 = l4;
                        } else {
                           RandomSource randomsource = this.positionalRandomFactory.at(l3, i4, j4);
                           i5 = BlockPos.asLong(l3 * 16 + randomsource.nextInt(10), i4 * 12 + randomsource.nextInt(9), j4 * 16 + randomsource.nextInt(10));
                           this.aquiferLocationCache[k4] = i5;
                        }

                        int k5 = BlockPos.getX(i5) - i;
                        int l5 = BlockPos.getY(i5) - j;
                        int i6 = BlockPos.getZ(i5) - k;
                        int j6 = k5 * k5 + l5 * l5 + i6 * i6;
                        if (k1 >= j6) {
                           l2 = k2;
                           k2 = j2;
                           j2 = i5;
                           i2 = l1;
                           l1 = k1;
                           k1 = j6;
                        } else if (l1 >= j6) {
                           l2 = k2;
                           k2 = i5;
                           i2 = l1;
                           l1 = j6;
                        } else if (i2 >= j6) {
                           l2 = i5;
                           i2 = j6;
                        }
                     }
                  }
               }

               Aquifer.FluidStatus aquifer_fluidstatus1 = this.getAquiferStatus(j2);
               double d1 = similarity(k1, l1);
               BlockState blockstate = aquifer_fluidstatus1.at(j);
               if (d1 <= 0.0D) {
                  this.shouldScheduleFluidUpdate = d1 >= FLOWING_UPDATE_SIMULARITY;
                  return blockstate;
               } else if (blockstate.is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
                  this.shouldScheduleFluidUpdate = true;
                  return blockstate;
               } else {
                  MutableDouble mutabledouble = new MutableDouble(Double.NaN);
                  Aquifer.FluidStatus aquifer_fluidstatus2 = this.getAquiferStatus(k2);
                  double d2 = d1 * this.calculatePressure(densityfunction_functioncontext, mutabledouble, aquifer_fluidstatus1, aquifer_fluidstatus2);
                  if (d0 + d2 > 0.0D) {
                     this.shouldScheduleFluidUpdate = false;
                     return null;
                  } else {
                     Aquifer.FluidStatus aquifer_fluidstatus3 = this.getAquiferStatus(l2);
                     double d3 = similarity(k1, i2);
                     if (d3 > 0.0D) {
                        double d4 = d1 * d3 * this.calculatePressure(densityfunction_functioncontext, mutabledouble, aquifer_fluidstatus1, aquifer_fluidstatus3);
                        if (d0 + d4 > 0.0D) {
                           this.shouldScheduleFluidUpdate = false;
                           return null;
                        }
                     }

                     double d5 = similarity(l1, i2);
                     if (d5 > 0.0D) {
                        double d6 = d1 * d5 * this.calculatePressure(densityfunction_functioncontext, mutabledouble, aquifer_fluidstatus2, aquifer_fluidstatus3);
                        if (d0 + d6 > 0.0D) {
                           this.shouldScheduleFluidUpdate = false;
                           return null;
                        }
                     }

                     this.shouldScheduleFluidUpdate = true;
                     return blockstate;
                  }
               }
            }
         }
      }

      public boolean shouldScheduleFluidUpdate() {
         return this.shouldScheduleFluidUpdate;
      }

      private static double similarity(int i, int j) {
         double d0 = 25.0D;
         return 1.0D - (double)Math.abs(j - i) / 25.0D;
      }

      private double calculatePressure(DensityFunction.FunctionContext densityfunction_functioncontext, MutableDouble mutabledouble, Aquifer.FluidStatus aquifer_fluidstatus, Aquifer.FluidStatus aquifer_fluidstatus1) {
         int i = densityfunction_functioncontext.blockY();
         BlockState blockstate = aquifer_fluidstatus.at(i);
         BlockState blockstate1 = aquifer_fluidstatus1.at(i);
         if ((!blockstate.is(Blocks.LAVA) || !blockstate1.is(Blocks.WATER)) && (!blockstate.is(Blocks.WATER) || !blockstate1.is(Blocks.LAVA))) {
            int j = Math.abs(aquifer_fluidstatus.fluidLevel - aquifer_fluidstatus1.fluidLevel);
            if (j == 0) {
               return 0.0D;
            } else {
               double d0 = 0.5D * (double)(aquifer_fluidstatus.fluidLevel + aquifer_fluidstatus1.fluidLevel);
               double d1 = (double)i + 0.5D - d0;
               double d2 = (double)j / 2.0D;
               double d3 = 0.0D;
               double d4 = 2.5D;
               double d5 = 1.5D;
               double d6 = 3.0D;
               double d7 = 10.0D;
               double d8 = 3.0D;
               double d9 = d2 - Math.abs(d1);
               double d11;
               if (d1 > 0.0D) {
                  double d10 = 0.0D + d9;
                  if (d10 > 0.0D) {
                     d11 = d10 / 1.5D;
                  } else {
                     d11 = d10 / 2.5D;
                  }
               } else {
                  double d13 = 3.0D + d9;
                  if (d13 > 0.0D) {
                     d11 = d13 / 3.0D;
                  } else {
                     d11 = d13 / 10.0D;
                  }
               }

               double d16 = 2.0D;
               double d20;
               if (!(d11 < -2.0D) && !(d11 > 2.0D)) {
                  double d18 = mutabledouble.getValue();
                  if (Double.isNaN(d18)) {
                     double d19 = this.barrierNoise.compute(densityfunction_functioncontext);
                     mutabledouble.setValue(d19);
                     d20 = d19;
                  } else {
                     d20 = d18;
                  }
               } else {
                  d20 = 0.0D;
               }

               return 2.0D * (d20 + d11);
            }
         } else {
            return 2.0D;
         }
      }

      private int gridX(int i) {
         return Math.floorDiv(i, 16);
      }

      private int gridY(int i) {
         return Math.floorDiv(i, 12);
      }

      private int gridZ(int i) {
         return Math.floorDiv(i, 16);
      }

      private Aquifer.FluidStatus getAquiferStatus(long i) {
         int j = BlockPos.getX(i);
         int k = BlockPos.getY(i);
         int l = BlockPos.getZ(i);
         int i1 = this.gridX(j);
         int j1 = this.gridY(k);
         int k1 = this.gridZ(l);
         int l1 = this.getIndex(i1, j1, k1);
         Aquifer.FluidStatus aquifer_fluidstatus = this.aquiferCache[l1];
         if (aquifer_fluidstatus != null) {
            return aquifer_fluidstatus;
         } else {
            Aquifer.FluidStatus aquifer_fluidstatus1 = this.computeFluid(j, k, l);
            this.aquiferCache[l1] = aquifer_fluidstatus1;
            return aquifer_fluidstatus1;
         }
      }

      private Aquifer.FluidStatus computeFluid(int i, int j, int k) {
         Aquifer.FluidStatus aquifer_fluidstatus = this.globalFluidPicker.computeFluid(i, j, k);
         int l = Integer.MAX_VALUE;
         int i1 = j + 12;
         int j1 = j - 12;
         boolean flag = false;

         for(int[] aint : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
            int k1 = i + SectionPos.sectionToBlockCoord(aint[0]);
            int l1 = k + SectionPos.sectionToBlockCoord(aint[1]);
            int i2 = this.noiseChunk.preliminarySurfaceLevel(k1, l1);
            int j2 = i2 + 8;
            boolean flag1 = aint[0] == 0 && aint[1] == 0;
            if (flag1 && j1 > j2) {
               return aquifer_fluidstatus;
            }

            boolean flag2 = i1 > j2;
            if (flag2 || flag1) {
               Aquifer.FluidStatus aquifer_fluidstatus1 = this.globalFluidPicker.computeFluid(k1, j2, l1);
               if (!aquifer_fluidstatus1.at(j2).isAir()) {
                  if (flag1) {
                     flag = true;
                  }

                  if (flag2) {
                     return aquifer_fluidstatus1;
                  }
               }
            }

            l = Math.min(l, i2);
         }

         int k2 = this.computeSurfaceLevel(i, j, k, aquifer_fluidstatus, l, flag);
         return new Aquifer.FluidStatus(k2, this.computeFluidType(i, j, k, aquifer_fluidstatus, k2));
      }

      private int computeSurfaceLevel(int i, int j, int k, Aquifer.FluidStatus aquifer_fluidstatus, int l, boolean flag) {
         DensityFunction.SinglePointContext densityfunction_singlepointcontext = new DensityFunction.SinglePointContext(i, j, k);
         double d0;
         double d1;
         if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, densityfunction_singlepointcontext)) {
            d0 = -1.0D;
            d1 = -1.0D;
         } else {
            int i1 = l + 8 - j;
            int j1 = 64;
            double d2 = flag ? Mth.clampedMap((double)i1, 0.0D, 64.0D, 1.0D, 0.0D) : 0.0D;
            double d3 = Mth.clamp(this.fluidLevelFloodednessNoise.compute(densityfunction_singlepointcontext), -1.0D, 1.0D);
            double d4 = Mth.map(d2, 1.0D, 0.0D, -0.3D, 0.8D);
            double d5 = Mth.map(d2, 1.0D, 0.0D, -0.8D, 0.4D);
            d0 = d3 - d5;
            d1 = d3 - d4;
         }

         int k1;
         if (d1 > 0.0D) {
            k1 = aquifer_fluidstatus.fluidLevel;
         } else if (d0 > 0.0D) {
            k1 = this.computeRandomizedFluidSurfaceLevel(i, j, k, l);
         } else {
            k1 = DimensionType.WAY_BELOW_MIN_Y;
         }

         return k1;
      }

      private int computeRandomizedFluidSurfaceLevel(int i, int j, int k, int l) {
         int i1 = 16;
         int j1 = 40;
         int k1 = Math.floorDiv(i, 16);
         int l1 = Math.floorDiv(j, 40);
         int i2 = Math.floorDiv(k, 16);
         int j2 = l1 * 40 + 20;
         int k2 = 10;
         double d0 = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(k1, l1, i2)) * 10.0D;
         int l2 = Mth.quantize(d0, 3);
         int i3 = j2 + l2;
         return Math.min(l, i3);
      }

      private BlockState computeFluidType(int i, int j, int k, Aquifer.FluidStatus aquifer_fluidstatus, int l) {
         BlockState blockstate = aquifer_fluidstatus.fluidType;
         if (l <= -10 && l != DimensionType.WAY_BELOW_MIN_Y && aquifer_fluidstatus.fluidType != Blocks.LAVA.defaultBlockState()) {
            int i1 = 64;
            int j1 = 40;
            int k1 = Math.floorDiv(i, 64);
            int l1 = Math.floorDiv(j, 40);
            int i2 = Math.floorDiv(k, 64);
            double d0 = this.lavaNoise.compute(new DensityFunction.SinglePointContext(k1, l1, i2));
            if (Math.abs(d0) > 0.3D) {
               blockstate = Blocks.LAVA.defaultBlockState();
            }
         }

         return blockstate;
      }
   }
}
