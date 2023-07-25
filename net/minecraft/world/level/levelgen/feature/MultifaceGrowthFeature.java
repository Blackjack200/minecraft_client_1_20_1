package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;

public class MultifaceGrowthFeature extends Feature<MultifaceGrowthConfiguration> {
   public MultifaceGrowthFeature(Codec<MultifaceGrowthConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<MultifaceGrowthConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      MultifaceGrowthConfiguration multifacegrowthconfiguration = featureplacecontext.config();
      if (!isAirOrWater(worldgenlevel.getBlockState(blockpos))) {
         return false;
      } else {
         List<Direction> list = multifacegrowthconfiguration.getShuffledDirections(randomsource);
         if (placeGrowthIfPossible(worldgenlevel, blockpos, worldgenlevel.getBlockState(blockpos), multifacegrowthconfiguration, randomsource, list)) {
            return true;
         } else {
            BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

            for(Direction direction : list) {
               blockpos_mutableblockpos.set(blockpos);
               List<Direction> list1 = multifacegrowthconfiguration.getShuffledDirectionsExcept(randomsource, direction.getOpposite());

               for(int i = 0; i < multifacegrowthconfiguration.searchRange; ++i) {
                  blockpos_mutableblockpos.setWithOffset(blockpos, direction);
                  BlockState blockstate = worldgenlevel.getBlockState(blockpos_mutableblockpos);
                  if (!isAirOrWater(blockstate) && !blockstate.is(multifacegrowthconfiguration.placeBlock)) {
                     break;
                  }

                  if (placeGrowthIfPossible(worldgenlevel, blockpos_mutableblockpos, blockstate, multifacegrowthconfiguration, randomsource, list1)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   public static boolean placeGrowthIfPossible(WorldGenLevel worldgenlevel, BlockPos blockpos, BlockState blockstate, MultifaceGrowthConfiguration multifacegrowthconfiguration, RandomSource randomsource, List<Direction> list) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(Direction direction : list) {
         BlockState blockstate1 = worldgenlevel.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos, direction));
         if (blockstate1.is(multifacegrowthconfiguration.canBePlacedOn)) {
            BlockState blockstate2 = multifacegrowthconfiguration.placeBlock.getStateForPlacement(blockstate, worldgenlevel, blockpos, direction);
            if (blockstate2 == null) {
               return false;
            }

            worldgenlevel.setBlock(blockpos, blockstate2, 3);
            worldgenlevel.getChunk(blockpos).markPosForPostprocessing(blockpos);
            if (randomsource.nextFloat() < multifacegrowthconfiguration.chanceOfSpreading) {
               multifacegrowthconfiguration.placeBlock.getSpreader().spreadFromFaceTowardRandomDirection(blockstate2, worldgenlevel, blockpos, direction, randomsource, true);
            }

            return true;
         }
      }

      return false;
   }

   private static boolean isAirOrWater(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(Blocks.WATER);
   }
}
