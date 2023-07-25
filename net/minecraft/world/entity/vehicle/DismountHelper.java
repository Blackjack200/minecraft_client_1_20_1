package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DismountHelper {
   public static int[][] offsetsForDirection(Direction direction) {
      Direction direction1 = direction.getClockWise();
      Direction direction2 = direction1.getOpposite();
      Direction direction3 = direction.getOpposite();
      return new int[][]{{direction1.getStepX(), direction1.getStepZ()}, {direction2.getStepX(), direction2.getStepZ()}, {direction3.getStepX() + direction1.getStepX(), direction3.getStepZ() + direction1.getStepZ()}, {direction3.getStepX() + direction2.getStepX(), direction3.getStepZ() + direction2.getStepZ()}, {direction.getStepX() + direction1.getStepX(), direction.getStepZ() + direction1.getStepZ()}, {direction.getStepX() + direction2.getStepX(), direction.getStepZ() + direction2.getStepZ()}, {direction3.getStepX(), direction3.getStepZ()}, {direction.getStepX(), direction.getStepZ()}};
   }

   public static boolean isBlockFloorValid(double d0) {
      return !Double.isInfinite(d0) && d0 < 1.0D;
   }

   public static boolean canDismountTo(CollisionGetter collisiongetter, LivingEntity livingentity, AABB aabb) {
      for(VoxelShape voxelshape : collisiongetter.getBlockCollisions(livingentity, aabb)) {
         if (!voxelshape.isEmpty()) {
            return false;
         }
      }

      return collisiongetter.getWorldBorder().isWithinBounds(aabb);
   }

   public static boolean canDismountTo(CollisionGetter collisiongetter, Vec3 vec3, LivingEntity livingentity, Pose pose) {
      return canDismountTo(collisiongetter, livingentity, livingentity.getLocalBoundsForPose(pose).move(vec3));
   }

   public static VoxelShape nonClimbableShape(BlockGetter blockgetter, BlockPos blockpos) {
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      return !blockstate.is(BlockTags.CLIMBABLE) && (!(blockstate.getBlock() instanceof TrapDoorBlock) || !blockstate.getValue(TrapDoorBlock.OPEN)) ? blockstate.getCollisionShape(blockgetter, blockpos) : Shapes.empty();
   }

   public static double findCeilingFrom(BlockPos blockpos, int i, Function<BlockPos, VoxelShape> function) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      int j = 0;

      while(j < i) {
         VoxelShape voxelshape = function.apply(blockpos_mutableblockpos);
         if (!voxelshape.isEmpty()) {
            return (double)(blockpos.getY() + j) + voxelshape.min(Direction.Axis.Y);
         }

         ++j;
         blockpos_mutableblockpos.move(Direction.UP);
      }

      return Double.POSITIVE_INFINITY;
   }

   @Nullable
   public static Vec3 findSafeDismountLocation(EntityType<?> entitytype, CollisionGetter collisiongetter, BlockPos blockpos, boolean flag) {
      if (flag && entitytype.isBlockDangerous(collisiongetter.getBlockState(blockpos))) {
         return null;
      } else {
         double d0 = collisiongetter.getBlockFloorHeight(nonClimbableShape(collisiongetter, blockpos), () -> nonClimbableShape(collisiongetter, blockpos.below()));
         if (!isBlockFloorValid(d0)) {
            return null;
         } else if (flag && d0 <= 0.0D && entitytype.isBlockDangerous(collisiongetter.getBlockState(blockpos.below()))) {
            return null;
         } else {
            Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos, d0);
            AABB aabb = entitytype.getDimensions().makeBoundingBox(vec3);

            for(VoxelShape voxelshape : collisiongetter.getBlockCollisions((Entity)null, aabb)) {
               if (!voxelshape.isEmpty()) {
                  return null;
               }
            }

            if (entitytype != EntityType.PLAYER || !collisiongetter.getBlockState(blockpos).is(BlockTags.INVALID_SPAWN_INSIDE) && !collisiongetter.getBlockState(blockpos.above()).is(BlockTags.INVALID_SPAWN_INSIDE)) {
               return !collisiongetter.getWorldBorder().isWithinBounds(aabb) ? null : vec3;
            } else {
               return null;
            }
         }
      }
   }
}
