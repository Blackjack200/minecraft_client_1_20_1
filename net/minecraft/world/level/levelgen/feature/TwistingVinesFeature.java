package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;

public class TwistingVinesFeature extends Feature<TwistingVinesConfig> {
   public TwistingVinesFeature(Codec<TwistingVinesConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<TwistingVinesConfig> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      if (isInvalidPlacementLocation(worldgenlevel, blockpos)) {
         return false;
      } else {
         RandomSource randomsource = featureplacecontext.random();
         TwistingVinesConfig twistingvinesconfig = featureplacecontext.config();
         int i = twistingvinesconfig.spreadWidth();
         int j = twistingvinesconfig.spreadHeight();
         int k = twistingvinesconfig.maxHeight();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int l = 0; l < i * i; ++l) {
            blockpos_mutableblockpos.set(blockpos).move(Mth.nextInt(randomsource, -i, i), Mth.nextInt(randomsource, -j, j), Mth.nextInt(randomsource, -i, i));
            if (findFirstAirBlockAboveGround(worldgenlevel, blockpos_mutableblockpos) && !isInvalidPlacementLocation(worldgenlevel, blockpos_mutableblockpos)) {
               int i1 = Mth.nextInt(randomsource, 1, k);
               if (randomsource.nextInt(6) == 0) {
                  i1 *= 2;
               }

               if (randomsource.nextInt(5) == 0) {
                  i1 = 1;
               }

               int j1 = 17;
               int k1 = 25;
               placeWeepingVinesColumn(worldgenlevel, randomsource, blockpos_mutableblockpos, i1, 17, 25);
            }
         }

         return true;
      }
   }

   private static boolean findFirstAirBlockAboveGround(LevelAccessor levelaccessor, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      do {
         blockpos_mutableblockpos.move(0, -1, 0);
         if (levelaccessor.isOutsideBuildHeight(blockpos_mutableblockpos)) {
            return false;
         }
      } while(levelaccessor.getBlockState(blockpos_mutableblockpos).isAir());

      blockpos_mutableblockpos.move(0, 1, 0);
      return true;
   }

   public static void placeWeepingVinesColumn(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos.MutableBlockPos blockpos_mutableblockpos, int i, int j, int k) {
      for(int l = 1; l <= i; ++l) {
         if (levelaccessor.isEmptyBlock(blockpos_mutableblockpos)) {
            if (l == i || !levelaccessor.isEmptyBlock(blockpos_mutableblockpos.above())) {
               levelaccessor.setBlock(blockpos_mutableblockpos, Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(randomsource, j, k))), 2);
               break;
            }

            levelaccessor.setBlock(blockpos_mutableblockpos, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
         }

         blockpos_mutableblockpos.move(Direction.UP);
      }

   }

   private static boolean isInvalidPlacementLocation(LevelAccessor levelaccessor, BlockPos blockpos) {
      if (!levelaccessor.isEmptyBlock(blockpos)) {
         return true;
      } else {
         BlockState blockstate = levelaccessor.getBlockState(blockpos.below());
         return !blockstate.is(Blocks.NETHERRACK) && !blockstate.is(Blocks.WARPED_NYLIUM) && !blockstate.is(Blocks.WARPED_WART_BLOCK);
      }
   }
}
