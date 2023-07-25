package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TheEndPortalBlockEntity extends BlockEntity {
   protected TheEndPortalBlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate) {
      super(blockentitytype, blockpos, blockstate);
   }

   public TheEndPortalBlockEntity(BlockPos blockpos, BlockState blockstate) {
      this(BlockEntityType.END_PORTAL, blockpos, blockstate);
   }

   public boolean shouldRenderFace(Direction direction) {
      return direction.getAxis() == Direction.Axis.Y;
   }
}
