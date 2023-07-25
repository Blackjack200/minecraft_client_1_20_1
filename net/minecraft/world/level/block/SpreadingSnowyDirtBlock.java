package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public abstract class SpreadingSnowyDirtBlock extends SnowyDirtBlock {
   protected SpreadingSnowyDirtBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   private static boolean canBeGrass(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.above();
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      if (blockstate1.is(Blocks.SNOW) && blockstate1.getValue(SnowLayerBlock.LAYERS) == 1) {
         return true;
      } else if (blockstate1.getFluidState().getAmount() == 8) {
         return false;
      } else {
         int i = LightEngine.getLightBlockInto(levelreader, blockstate, blockpos, blockstate1, blockpos1, Direction.UP, blockstate1.getLightBlock(levelreader, blockpos1));
         return i < levelreader.getMaxLightLevel();
      }
   }

   private static boolean canPropagate(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.above();
      return canBeGrass(blockstate, levelreader, blockpos) && !levelreader.getFluidState(blockpos1).is(FluidTags.WATER);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!canBeGrass(blockstate, serverlevel, blockpos)) {
         serverlevel.setBlockAndUpdate(blockpos, Blocks.DIRT.defaultBlockState());
      } else {
         if (serverlevel.getMaxLocalRawBrightness(blockpos.above()) >= 9) {
            BlockState blockstate1 = this.defaultBlockState();

            for(int i = 0; i < 4; ++i) {
               BlockPos blockpos1 = blockpos.offset(randomsource.nextInt(3) - 1, randomsource.nextInt(5) - 3, randomsource.nextInt(3) - 1);
               if (serverlevel.getBlockState(blockpos1).is(Blocks.DIRT) && canPropagate(blockstate1, serverlevel, blockpos1)) {
                  serverlevel.setBlockAndUpdate(blockpos1, blockstate1.setValue(SNOWY, Boolean.valueOf(serverlevel.getBlockState(blockpos1.above()).is(Blocks.SNOW))));
               }
            }
         }

      }
   }
}
