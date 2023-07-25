package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
   int SCAN_DISTANCE = 4;

   Optional<BlockState> getNext(BlockState blockstate);

   float getChanceModifier();

   default void onRandomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      float f = 0.05688889F;
      if (randomsource.nextFloat() < 0.05688889F) {
         this.applyChangeOverTime(blockstate, serverlevel, blockpos, randomsource);
      }

   }

   T getAge();

   default void applyChangeOverTime(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      int i = this.getAge().ordinal();
      int j = 0;
      int k = 0;

      for(BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, 4, 4, 4)) {
         int l = blockpos1.distManhattan(blockpos);
         if (l > 4) {
            break;
         }

         if (!blockpos1.equals(blockpos)) {
            BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
            Block block = blockstate1.getBlock();
            if (block instanceof ChangeOverTimeBlock) {
               Enum<?> oenum = ((ChangeOverTimeBlock)block).getAge();
               if (this.getAge().getClass() == oenum.getClass()) {
                  int i1 = oenum.ordinal();
                  if (i1 < i) {
                     return;
                  }

                  if (i1 > i) {
                     ++k;
                  } else {
                     ++j;
                  }
               }
            }
         }
      }

      float f = (float)(k + 1) / (float)(k + j + 1);
      float f1 = f * f * this.getChanceModifier();
      if (randomsource.nextFloat() < f1) {
         this.getNext(blockstate).ifPresent((blockstate2) -> serverlevel.setBlockAndUpdate(blockpos, blockstate2));
      }

   }
}
