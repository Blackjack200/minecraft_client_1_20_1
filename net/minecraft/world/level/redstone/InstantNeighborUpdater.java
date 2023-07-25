package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
   private final Level level;

   public InstantNeighborUpdater(Level level) {
      this.level = level;
   }

   public void shapeUpdate(Direction direction, BlockState blockstate, BlockPos blockpos, BlockPos blockpos1, int i, int j) {
      NeighborUpdater.executeShapeUpdate(this.level, direction, blockstate, blockpos, blockpos1, i, j - 1);
   }

   public void neighborChanged(BlockPos blockpos, Block block, BlockPos blockpos1) {
      BlockState blockstate = this.level.getBlockState(blockpos);
      this.neighborChanged(blockstate, blockpos, block, blockpos1, false);
   }

   public void neighborChanged(BlockState blockstate, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      NeighborUpdater.executeUpdate(this.level, blockstate, blockpos, block, blockpos1, flag);
   }
}
