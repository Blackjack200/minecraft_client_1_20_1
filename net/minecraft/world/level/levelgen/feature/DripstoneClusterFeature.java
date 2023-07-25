package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature extends Feature<DripstoneClusterConfiguration> {
   public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      DripstoneClusterConfiguration dripstoneclusterconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      if (!DripstoneUtils.isEmptyOrWater(worldgenlevel, blockpos)) {
         return false;
      } else {
         int i = dripstoneclusterconfiguration.height.sample(randomsource);
         float f = dripstoneclusterconfiguration.wetness.sample(randomsource);
         float f1 = dripstoneclusterconfiguration.density.sample(randomsource);
         int j = dripstoneclusterconfiguration.radius.sample(randomsource);
         int k = dripstoneclusterconfiguration.radius.sample(randomsource);

         for(int l = -j; l <= j; ++l) {
            for(int i1 = -k; i1 <= k; ++i1) {
               double d0 = this.getChanceOfStalagmiteOrStalactite(j, k, l, i1, dripstoneclusterconfiguration);
               BlockPos blockpos1 = blockpos.offset(l, 0, i1);
               this.placeColumn(worldgenlevel, randomsource, blockpos1, l, i1, f, d0, i, f1, dripstoneclusterconfiguration);
            }
         }

         return true;
      }
   }

   private void placeColumn(WorldGenLevel worldgenlevel, RandomSource randomsource, BlockPos blockpos, int i, int j, float f, double d0, int k, float f1, DripstoneClusterConfiguration dripstoneclusterconfiguration) {
      Optional<Column> optional = Column.scan(worldgenlevel, blockpos, dripstoneclusterconfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
      if (optional.isPresent()) {
         OptionalInt optionalint = optional.get().getCeiling();
         OptionalInt optionalint1 = optional.get().getFloor();
         if (optionalint.isPresent() || optionalint1.isPresent()) {
            boolean flag = randomsource.nextFloat() < f;
            Column column;
            if (flag && optionalint1.isPresent() && this.canPlacePool(worldgenlevel, blockpos.atY(optionalint1.getAsInt()))) {
               int l = optionalint1.getAsInt();
               column = optional.get().withFloor(OptionalInt.of(l - 1));
               worldgenlevel.setBlock(blockpos.atY(l), Blocks.WATER.defaultBlockState(), 2);
            } else {
               column = optional.get();
            }

            OptionalInt optionalint2 = column.getFloor();
            boolean flag1 = randomsource.nextDouble() < d0;
            int l1;
            if (optionalint.isPresent() && flag1 && !this.isLava(worldgenlevel, blockpos.atY(optionalint.getAsInt()))) {
               int i1 = dripstoneclusterconfiguration.dripstoneBlockLayerThickness.sample(randomsource);
               this.replaceBlocksWithDripstoneBlocks(worldgenlevel, blockpos.atY(optionalint.getAsInt()), i1, Direction.UP);
               int j1;
               if (optionalint2.isPresent()) {
                  j1 = Math.min(k, optionalint.getAsInt() - optionalint2.getAsInt());
               } else {
                  j1 = k;
               }

               l1 = this.getDripstoneHeight(randomsource, i, j, f1, j1, dripstoneclusterconfiguration);
            } else {
               l1 = 0;
            }

            boolean flag2 = randomsource.nextDouble() < d0;
            int k2;
            if (optionalint2.isPresent() && flag2 && !this.isLava(worldgenlevel, blockpos.atY(optionalint2.getAsInt()))) {
               int j2 = dripstoneclusterconfiguration.dripstoneBlockLayerThickness.sample(randomsource);
               this.replaceBlocksWithDripstoneBlocks(worldgenlevel, blockpos.atY(optionalint2.getAsInt()), j2, Direction.DOWN);
               if (optionalint.isPresent()) {
                  k2 = Math.max(0, l1 + Mth.randomBetweenInclusive(randomsource, -dripstoneclusterconfiguration.maxStalagmiteStalactiteHeightDiff, dripstoneclusterconfiguration.maxStalagmiteStalactiteHeightDiff));
               } else {
                  k2 = this.getDripstoneHeight(randomsource, i, j, f1, k, dripstoneclusterconfiguration);
               }
            } else {
               k2 = 0;
            }

            int i5;
            int l4;
            if (optionalint.isPresent() && optionalint2.isPresent() && optionalint.getAsInt() - l1 <= optionalint2.getAsInt() + k2) {
               int j3 = optionalint2.getAsInt();
               int k3 = optionalint.getAsInt();
               int l3 = Math.max(k3 - l1, j3 + 1);
               int i4 = Math.min(j3 + k2, k3 - 1);
               int j4 = Mth.randomBetweenInclusive(randomsource, l3, i4 + 1);
               int k4 = j4 - 1;
               l4 = k3 - j4;
               i5 = k4 - j3;
            } else {
               l4 = l1;
               i5 = k2;
            }

            boolean flag3 = randomsource.nextBoolean() && l4 > 0 && i5 > 0 && column.getHeight().isPresent() && l4 + i5 == column.getHeight().getAsInt();
            if (optionalint.isPresent()) {
               DripstoneUtils.growPointedDripstone(worldgenlevel, blockpos.atY(optionalint.getAsInt() - 1), Direction.DOWN, l4, flag3);
            }

            if (optionalint2.isPresent()) {
               DripstoneUtils.growPointedDripstone(worldgenlevel, blockpos.atY(optionalint2.getAsInt() + 1), Direction.UP, i5, flag3);
            }

         }
      }
   }

   private boolean isLava(LevelReader levelreader, BlockPos blockpos) {
      return levelreader.getBlockState(blockpos).is(Blocks.LAVA);
   }

   private int getDripstoneHeight(RandomSource randomsource, int i, int j, float f, int k, DripstoneClusterConfiguration dripstoneclusterconfiguration) {
      if (randomsource.nextFloat() > f) {
         return 0;
      } else {
         int l = Math.abs(i) + Math.abs(j);
         float f1 = (float)Mth.clampedMap((double)l, 0.0D, (double)dripstoneclusterconfiguration.maxDistanceFromCenterAffectingHeightBias, (double)k / 2.0D, 0.0D);
         return (int)randomBetweenBiased(randomsource, 0.0F, (float)k, f1, (float)dripstoneclusterconfiguration.heightDeviation);
      }
   }

   private boolean canPlacePool(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      BlockState blockstate = worldgenlevel.getBlockState(blockpos);
      if (!blockstate.is(Blocks.WATER) && !blockstate.is(Blocks.DRIPSTONE_BLOCK) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
         if (worldgenlevel.getBlockState(blockpos.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
         } else {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
               if (!this.canBeAdjacentToWater(worldgenlevel, blockpos.relative(direction))) {
                  return false;
               }
            }

            return this.canBeAdjacentToWater(worldgenlevel, blockpos.below());
         }
      } else {
         return false;
      }
   }

   private boolean canBeAdjacentToWater(LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      return blockstate.is(BlockTags.BASE_STONE_OVERWORLD) || blockstate.getFluidState().is(FluidTags.WATER);
   }

   private void replaceBlocksWithDripstoneBlocks(WorldGenLevel worldgenlevel, BlockPos blockpos, int i, Direction direction) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int j = 0; j < i; ++j) {
         if (!DripstoneUtils.placeDripstoneBlockIfPossible(worldgenlevel, blockpos_mutableblockpos)) {
            return;
         }

         blockpos_mutableblockpos.move(direction);
      }

   }

   private double getChanceOfStalagmiteOrStalactite(int i, int j, int k, int l, DripstoneClusterConfiguration dripstoneclusterconfiguration) {
      int i1 = i - Math.abs(k);
      int j1 = j - Math.abs(l);
      int k1 = Math.min(i1, j1);
      return (double)Mth.clampedMap((float)k1, 0.0F, (float)dripstoneclusterconfiguration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, dripstoneclusterconfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F);
   }

   private static float randomBetweenBiased(RandomSource randomsource, float f, float f1, float f2, float f3) {
      return ClampedNormalFloat.sample(randomsource, f2, f3, f, f1);
   }
}
