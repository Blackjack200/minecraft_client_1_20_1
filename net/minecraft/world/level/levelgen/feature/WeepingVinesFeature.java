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
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WeepingVinesFeature extends Feature<NoneFeatureConfiguration> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public WeepingVinesFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      if (!worldgenlevel.isEmptyBlock(blockpos)) {
         return false;
      } else {
         BlockState blockstate = worldgenlevel.getBlockState(blockpos.above());
         if (!blockstate.is(Blocks.NETHERRACK) && !blockstate.is(Blocks.NETHER_WART_BLOCK)) {
            return false;
         } else {
            this.placeRoofNetherWart(worldgenlevel, randomsource, blockpos);
            this.placeRoofWeepingVines(worldgenlevel, randomsource, blockpos);
            return true;
         }
      }
   }

   private void placeRoofNetherWart(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos) {
      levelaccessor.setBlock(blockpos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = new BlockPos.MutableBlockPos();

      for(int i = 0; i < 200; ++i) {
         blockpos_mutableblockpos.setWithOffset(blockpos, randomsource.nextInt(6) - randomsource.nextInt(6), randomsource.nextInt(2) - randomsource.nextInt(5), randomsource.nextInt(6) - randomsource.nextInt(6));
         if (levelaccessor.isEmptyBlock(blockpos_mutableblockpos)) {
            int j = 0;

            for(Direction direction : DIRECTIONS) {
               BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos1.setWithOffset(blockpos_mutableblockpos, direction));
               if (blockstate.is(Blocks.NETHERRACK) || blockstate.is(Blocks.NETHER_WART_BLOCK)) {
                  ++j;
               }

               if (j > 1) {
                  break;
               }
            }

            if (j == 1) {
               levelaccessor.setBlock(blockpos_mutableblockpos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
            }
         }
      }

   }

   private void placeRoofWeepingVines(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < 100; ++i) {
         blockpos_mutableblockpos.setWithOffset(blockpos, randomsource.nextInt(8) - randomsource.nextInt(8), randomsource.nextInt(2) - randomsource.nextInt(7), randomsource.nextInt(8) - randomsource.nextInt(8));
         if (levelaccessor.isEmptyBlock(blockpos_mutableblockpos)) {
            BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos.above());
            if (blockstate.is(Blocks.NETHERRACK) || blockstate.is(Blocks.NETHER_WART_BLOCK)) {
               int j = Mth.nextInt(randomsource, 1, 8);
               if (randomsource.nextInt(6) == 0) {
                  j *= 2;
               }

               if (randomsource.nextInt(5) == 0) {
                  j = 1;
               }

               int k = 17;
               int l = 25;
               placeWeepingVinesColumn(levelaccessor, randomsource, blockpos_mutableblockpos, j, 17, 25);
            }
         }
      }

   }

   public static void placeWeepingVinesColumn(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos.MutableBlockPos blockpos_mutableblockpos, int i, int j, int k) {
      for(int l = 0; l <= i; ++l) {
         if (levelaccessor.isEmptyBlock(blockpos_mutableblockpos)) {
            if (l == i || !levelaccessor.isEmptyBlock(blockpos_mutableblockpos.below())) {
               levelaccessor.setBlock(blockpos_mutableblockpos, Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(randomsource, j, k))), 2);
               break;
            }

            levelaccessor.setBlock(blockpos_mutableblockpos, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
         }

         blockpos_mutableblockpos.move(Direction.DOWN);
      }

   }
}
