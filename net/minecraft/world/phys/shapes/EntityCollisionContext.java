package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
   protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, ItemStack.EMPTY, (fluidstate) -> false, (Entity)null) {
      public boolean isAbove(VoxelShape voxelshape, BlockPos blockpos, boolean flag) {
         return flag;
      }
   };
   private final boolean descending;
   private final double entityBottom;
   private final ItemStack heldItem;
   private final Predicate<FluidState> canStandOnFluid;
   @Nullable
   private final Entity entity;

   protected EntityCollisionContext(boolean flag, double d0, ItemStack itemstack, Predicate<FluidState> predicate, @Nullable Entity entity) {
      this.descending = flag;
      this.entityBottom = d0;
      this.heldItem = itemstack;
      this.canStandOnFluid = predicate;
      this.entity = entity;
   }

   /** @deprecated */
   @Deprecated
   protected EntityCollisionContext(Entity entity) {
      this(entity.isDescending(), entity.getY(), entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandItem() : ItemStack.EMPTY, entity instanceof LivingEntity ? ((LivingEntity)entity)::canStandOnFluid : (fluidstate) -> false, entity);
   }

   public boolean isHoldingItem(Item item) {
      return this.heldItem.is(item);
   }

   public boolean canStandOnFluid(FluidState fluidstate, FluidState fluidstate1) {
      return this.canStandOnFluid.test(fluidstate1) && !fluidstate.getType().isSame(fluidstate1.getType());
   }

   public boolean isDescending() {
      return this.descending;
   }

   public boolean isAbove(VoxelShape voxelshape, BlockPos blockpos, boolean flag) {
      return this.entityBottom > (double)blockpos.getY() + voxelshape.max(Direction.Axis.Y) - (double)1.0E-5F;
   }

   @Nullable
   public Entity getEntity() {
      return this.entity;
   }
}
