package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TrappedChestBlockEntity extends ChestBlockEntity {
   public TrappedChestBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.TRAPPED_CHEST, blockpos, blockstate);
   }

   protected void signalOpenCount(Level level, BlockPos blockpos, BlockState blockstate, int i, int j) {
      super.signalOpenCount(level, blockpos, blockstate, i, j);
      if (i != j) {
         Block block = blockstate.getBlock();
         level.updateNeighborsAt(blockpos, block);
         level.updateNeighborsAt(blockpos.below(), block);
      }

   }
}
