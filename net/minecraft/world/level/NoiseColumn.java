package net.minecraft.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public final class NoiseColumn implements BlockColumn {
   private final int minY;
   private final BlockState[] column;

   public NoiseColumn(int i, BlockState[] ablockstate) {
      this.minY = i;
      this.column = ablockstate;
   }

   public BlockState getBlock(int i) {
      int j = i - this.minY;
      return j >= 0 && j < this.column.length ? this.column[j] : Blocks.AIR.defaultBlockState();
   }

   public void setBlock(int i, BlockState blockstate) {
      int j = i - this.minY;
      if (j >= 0 && j < this.column.length) {
         this.column[j] = blockstate;
      } else {
         throw new IllegalArgumentException("Outside of column height: " + i);
      }
   }
}
