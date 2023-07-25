package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter extends BlockGetter {
   Direction[] DIRECTIONS = Direction.values();

   default int getDirectSignal(BlockPos blockpos, Direction direction) {
      return this.getBlockState(blockpos).getDirectSignal(this, blockpos, direction);
   }

   default int getDirectSignalTo(BlockPos blockpos) {
      int i = 0;
      i = Math.max(i, this.getDirectSignal(blockpos.below(), Direction.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getDirectSignal(blockpos.above(), Direction.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getDirectSignal(blockpos.north(), Direction.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getDirectSignal(blockpos.south(), Direction.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getDirectSignal(blockpos.west(), Direction.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getDirectSignal(blockpos.east(), Direction.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   default int getControlInputSignal(BlockPos blockpos, Direction direction, boolean flag) {
      BlockState blockstate = this.getBlockState(blockpos);
      if (flag) {
         return DiodeBlock.isDiode(blockstate) ? this.getDirectSignal(blockpos, direction) : 0;
      } else if (blockstate.is(Blocks.REDSTONE_BLOCK)) {
         return 15;
      } else if (blockstate.is(Blocks.REDSTONE_WIRE)) {
         return blockstate.getValue(RedStoneWireBlock.POWER);
      } else {
         return blockstate.isSignalSource() ? this.getDirectSignal(blockpos, direction) : 0;
      }
   }

   default boolean hasSignal(BlockPos blockpos, Direction direction) {
      return this.getSignal(blockpos, direction) > 0;
   }

   default int getSignal(BlockPos blockpos, Direction direction) {
      BlockState blockstate = this.getBlockState(blockpos);
      int i = blockstate.getSignal(this, blockpos, direction);
      return blockstate.isRedstoneConductor(this, blockpos) ? Math.max(i, this.getDirectSignalTo(blockpos)) : i;
   }

   default boolean hasNeighborSignal(BlockPos blockpos) {
      if (this.getSignal(blockpos.below(), Direction.DOWN) > 0) {
         return true;
      } else if (this.getSignal(blockpos.above(), Direction.UP) > 0) {
         return true;
      } else if (this.getSignal(blockpos.north(), Direction.NORTH) > 0) {
         return true;
      } else if (this.getSignal(blockpos.south(), Direction.SOUTH) > 0) {
         return true;
      } else if (this.getSignal(blockpos.west(), Direction.WEST) > 0) {
         return true;
      } else {
         return this.getSignal(blockpos.east(), Direction.EAST) > 0;
      }
   }

   default int getBestNeighborSignal(BlockPos blockpos) {
      int i = 0;

      for(Direction direction : DIRECTIONS) {
         int j = this.getSignal(blockpos.relative(direction), direction);
         if (j >= 15) {
            return 15;
         }

         if (j > i) {
            i = j;
         }
      }

      return i;
   }
}
