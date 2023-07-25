package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum SupportType {
   FULL {
      public boolean isSupporting(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return Block.isFaceFull(blockstate.getBlockSupportShape(blockgetter, blockpos), direction);
      }
   },
   CENTER {
      private final int CENTER_SUPPORT_WIDTH = 1;
      private final VoxelShape CENTER_SUPPORT_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D);

      public boolean isSupporting(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return !Shapes.joinIsNotEmpty(blockstate.getBlockSupportShape(blockgetter, blockpos).getFaceShape(direction), this.CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
      }
   },
   RIGID {
      private final int RIGID_SUPPORT_WIDTH = 2;
      private final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);

      public boolean isSupporting(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return !Shapes.joinIsNotEmpty(blockstate.getBlockSupportShape(blockgetter, blockpos).getFaceShape(direction), this.RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
      }
   };

   public abstract boolean isSupporting(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction);
}
