package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherrackBlock extends Block implements BonemealableBlock {
   public NetherrackBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      if (!levelreader.getBlockState(blockpos.above()).propagatesSkylightDown(levelreader, blockpos)) {
         return false;
      } else {
         for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-1, -1, -1), blockpos.offset(1, 1, 1))) {
            if (levelreader.getBlockState(blockpos1).is(BlockTags.NYLIUM)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      boolean flag = false;
      boolean flag1 = false;

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-1, -1, -1), blockpos.offset(1, 1, 1))) {
         BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
         if (blockstate1.is(Blocks.WARPED_NYLIUM)) {
            flag1 = true;
         }

         if (blockstate1.is(Blocks.CRIMSON_NYLIUM)) {
            flag = true;
         }

         if (flag1 && flag) {
            break;
         }
      }

      if (flag1 && flag) {
         serverlevel.setBlock(blockpos, randomsource.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
      } else if (flag1) {
         serverlevel.setBlock(blockpos, Blocks.WARPED_NYLIUM.defaultBlockState(), 3);
      } else if (flag) {
         serverlevel.setBlock(blockpos, Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
      }

   }
}
