package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter extends BlockGetter {
   WorldBorder getWorldBorder();

   @Nullable
   BlockGetter getChunkForCollisions(int i, int j);

   default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelshape) {
      return true;
   }

   default boolean isUnobstructed(BlockState blockstate, BlockPos blockpos, CollisionContext collisioncontext) {
      VoxelShape voxelshape = blockstate.getCollisionShape(this, blockpos, collisioncontext);
      return voxelshape.isEmpty() || this.isUnobstructed((Entity)null, voxelshape.move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()));
   }

   default boolean isUnobstructed(Entity entity) {
      return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
   }

   default boolean noCollision(AABB aabb) {
      return this.noCollision((Entity)null, aabb);
   }

   default boolean noCollision(Entity entity) {
      return this.noCollision(entity, entity.getBoundingBox());
   }

   default boolean noCollision(@Nullable Entity entity, AABB aabb) {
      for(VoxelShape voxelshape : this.getBlockCollisions(entity, aabb)) {
         if (!voxelshape.isEmpty()) {
            return false;
         }
      }

      if (!this.getEntityCollisions(entity, aabb).isEmpty()) {
         return false;
      } else if (entity == null) {
         return true;
      } else {
         VoxelShape voxelshape1 = this.borderCollision(entity, aabb);
         return voxelshape1 == null || !Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(aabb), BooleanOp.AND);
      }
   }

   List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb);

   default Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB aabb) {
      List<VoxelShape> list = this.getEntityCollisions(entity, aabb);
      Iterable<VoxelShape> iterable = this.getBlockCollisions(entity, aabb);
      return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
   }

   default Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aabb) {
      return () -> new BlockCollisions<>(this, entity, aabb, false, (blockpos_mutableblockpos, voxelshape) -> voxelshape);
   }

   @Nullable
   private VoxelShape borderCollision(Entity entity, AABB aabb) {
      WorldBorder worldborder = this.getWorldBorder();
      return worldborder.isInsideCloseToBorder(entity, aabb) ? worldborder.getCollisionShape() : null;
   }

   default boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB aabb) {
      BlockCollisions<VoxelShape> blockcollisions = new BlockCollisions<>(this, entity, aabb, true, (blockpos_mutableblockpos, voxelshape) -> voxelshape);

      while(blockcollisions.hasNext()) {
         if (!blockcollisions.next().isEmpty()) {
            return true;
         }
      }

      return false;
   }

   default Optional<BlockPos> findSupportingBlock(Entity entity, AABB aabb) {
      BlockPos blockpos = null;
      double d0 = Double.MAX_VALUE;
      BlockCollisions<BlockPos> blockcollisions = new BlockCollisions<>(this, entity, aabb, false, (blockpos_mutableblockpos, voxelshape) -> blockpos_mutableblockpos);

      while(blockcollisions.hasNext()) {
         BlockPos blockpos1 = blockcollisions.next();
         double d1 = blockpos1.distToCenterSqr(entity.position());
         if (d1 < d0 || d1 == d0 && (blockpos == null || blockpos.compareTo(blockpos1) < 0)) {
            blockpos = blockpos1.immutable();
            d0 = d1;
         }
      }

      return Optional.ofNullable(blockpos);
   }

   default Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape voxelshape, Vec3 vec3, double d0, double d1, double d2) {
      if (voxelshape.isEmpty()) {
         return Optional.empty();
      } else {
         AABB aabb = voxelshape.bounds().inflate(d0, d1, d2);
         VoxelShape voxelshape1 = StreamSupport.stream(this.getBlockCollisions(entity, aabb).spliterator(), false).filter((voxelshape4) -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(voxelshape4.bounds())).flatMap((voxelshape3) -> voxelshape3.toAabbs().stream()).map((aabb1) -> aabb1.inflate(d0 / 2.0D, d1 / 2.0D, d2 / 2.0D)).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
         VoxelShape voxelshape2 = Shapes.join(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);
         return voxelshape2.closestPointTo(vec3);
      }
   }
}
