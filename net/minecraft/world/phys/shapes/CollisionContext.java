package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
   static CollisionContext empty() {
      return EntityCollisionContext.EMPTY;
   }

   static CollisionContext of(Entity entity) {
      return new EntityCollisionContext(entity);
   }

   boolean isDescending();

   boolean isAbove(VoxelShape voxelshape, BlockPos blockpos, boolean flag);

   boolean isHoldingItem(Item item);

   boolean canStandOnFluid(FluidState fluidstate, FluidState fluidstate1);
}
