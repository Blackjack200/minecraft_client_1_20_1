package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;

public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration> {
   private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
   private static final int CLUSTERED_REACH = 5;
   private static final int CLUSTERED_SIZE = 50;
   private static final int UNCLUSTERED_REACH = 8;
   private static final int UNCLUSTERED_SIZE = 15;

   public BasaltColumnsFeature(Codec<ColumnFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<ColumnFeatureConfiguration> featureplacecontext) {
      int i = featureplacecontext.chunkGenerator().getSeaLevel();
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      ColumnFeatureConfiguration columnfeatureconfiguration = featureplacecontext.config();
      if (!canPlaceAt(worldgenlevel, i, blockpos.mutable())) {
         return false;
      } else {
         int j = columnfeatureconfiguration.height().sample(randomsource);
         boolean flag = randomsource.nextFloat() < 0.9F;
         int k = Math.min(j, flag ? 5 : 8);
         int l = flag ? 50 : 15;
         boolean flag1 = false;

         for(BlockPos blockpos1 : BlockPos.randomBetweenClosed(randomsource, l, blockpos.getX() - k, blockpos.getY(), blockpos.getZ() - k, blockpos.getX() + k, blockpos.getY(), blockpos.getZ() + k)) {
            int i1 = j - blockpos1.distManhattan(blockpos);
            if (i1 >= 0) {
               flag1 |= this.placeColumn(worldgenlevel, i, blockpos1, i1, columnfeatureconfiguration.reach().sample(randomsource));
            }
         }

         return flag1;
      }
   }

   private boolean placeColumn(LevelAccessor levelaccessor, int i, BlockPos blockpos, int j, int k) {
      boolean flag = false;

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.getX() - k, blockpos.getY(), blockpos.getZ() - k, blockpos.getX() + k, blockpos.getY(), blockpos.getZ() + k)) {
         int l = blockpos1.distManhattan(blockpos);
         BlockPos blockpos2 = isAirOrLavaOcean(levelaccessor, i, blockpos1) ? findSurface(levelaccessor, i, blockpos1.mutable(), l) : findAir(levelaccessor, blockpos1.mutable(), l);
         if (blockpos2 != null) {
            int i1 = j - l / 2;

            for(BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos2.mutable(); i1 >= 0; --i1) {
               if (isAirOrLavaOcean(levelaccessor, i, blockpos_mutableblockpos)) {
                  this.setBlock(levelaccessor, blockpos_mutableblockpos, Blocks.BASALT.defaultBlockState());
                  blockpos_mutableblockpos.move(Direction.UP);
                  flag = true;
               } else {
                  if (!levelaccessor.getBlockState(blockpos_mutableblockpos).is(Blocks.BASALT)) {
                     break;
                  }

                  blockpos_mutableblockpos.move(Direction.UP);
               }
            }
         }
      }

      return flag;
   }

   @Nullable
   private static BlockPos findSurface(LevelAccessor levelaccessor, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos, int j) {
      while(blockpos_mutableblockpos.getY() > levelaccessor.getMinBuildHeight() + 1 && j > 0) {
         --j;
         if (canPlaceAt(levelaccessor, i, blockpos_mutableblockpos)) {
            return blockpos_mutableblockpos;
         }

         blockpos_mutableblockpos.move(Direction.DOWN);
      }

      return null;
   }

   private static boolean canPlaceAt(LevelAccessor levelaccessor, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      if (!isAirOrLavaOcean(levelaccessor, i, blockpos_mutableblockpos)) {
         return false;
      } else {
         BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos.move(Direction.DOWN));
         blockpos_mutableblockpos.move(Direction.UP);
         return !blockstate.isAir() && !CANNOT_PLACE_ON.contains(blockstate.getBlock());
      }
   }

   @Nullable
   private static BlockPos findAir(LevelAccessor levelaccessor, BlockPos.MutableBlockPos blockpos_mutableblockpos, int i) {
      while(blockpos_mutableblockpos.getY() < levelaccessor.getMaxBuildHeight() && i > 0) {
         --i;
         BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos);
         if (CANNOT_PLACE_ON.contains(blockstate.getBlock())) {
            return null;
         }

         if (blockstate.isAir()) {
            return blockpos_mutableblockpos;
         }

         blockpos_mutableblockpos.move(Direction.UP);
      }

      return null;
   }

   private static boolean isAirOrLavaOcean(LevelAccessor levelaccessor, int i, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      return blockstate.isAir() || blockstate.is(Blocks.LAVA) && blockpos.getY() <= i;
   }
}
