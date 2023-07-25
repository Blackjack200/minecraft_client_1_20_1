package net.minecraft.world.phys.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class VoxelShape {
   protected final DiscreteVoxelShape shape;
   @Nullable
   private VoxelShape[] faces;

   VoxelShape(DiscreteVoxelShape discretevoxelshape) {
      this.shape = discretevoxelshape;
   }

   public double min(Direction.Axis direction_axis) {
      int i = this.shape.firstFull(direction_axis);
      return i >= this.shape.getSize(direction_axis) ? Double.POSITIVE_INFINITY : this.get(direction_axis, i);
   }

   public double max(Direction.Axis direction_axis) {
      int i = this.shape.lastFull(direction_axis);
      return i <= 0 ? Double.NEGATIVE_INFINITY : this.get(direction_axis, i);
   }

   public AABB bounds() {
      if (this.isEmpty()) {
         throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
      } else {
         return new AABB(this.min(Direction.Axis.X), this.min(Direction.Axis.Y), this.min(Direction.Axis.Z), this.max(Direction.Axis.X), this.max(Direction.Axis.Y), this.max(Direction.Axis.Z));
      }
   }

   protected double get(Direction.Axis direction_axis, int i) {
      return this.getCoords(direction_axis).getDouble(i);
   }

   protected abstract DoubleList getCoords(Direction.Axis direction_axis);

   public boolean isEmpty() {
      return this.shape.isEmpty();
   }

   public VoxelShape move(double d0, double d1, double d2) {
      return (VoxelShape)(this.isEmpty() ? Shapes.empty() : new ArrayVoxelShape(this.shape, (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.X), d0)), (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Y), d1)), (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Z), d2))));
   }

   public VoxelShape optimize() {
      VoxelShape[] avoxelshape = new VoxelShape[]{Shapes.empty()};
      this.forAllBoxes((d0, d1, d2, d3, d4, d5) -> avoxelshape[0] = Shapes.joinUnoptimized(avoxelshape[0], Shapes.box(d0, d1, d2, d3, d4, d5), BooleanOp.OR));
      return avoxelshape[0];
   }

   public void forAllEdges(Shapes.DoubleLineConsumer shapes_doublelineconsumer) {
      this.shape.forAllEdges((i, j, k, l, i1, j1) -> shapes_doublelineconsumer.consume(this.get(Direction.Axis.X, i), this.get(Direction.Axis.Y, j), this.get(Direction.Axis.Z, k), this.get(Direction.Axis.X, l), this.get(Direction.Axis.Y, i1), this.get(Direction.Axis.Z, j1)), true);
   }

   public void forAllBoxes(Shapes.DoubleLineConsumer shapes_doublelineconsumer) {
      DoubleList doublelist = this.getCoords(Direction.Axis.X);
      DoubleList doublelist1 = this.getCoords(Direction.Axis.Y);
      DoubleList doublelist2 = this.getCoords(Direction.Axis.Z);
      this.shape.forAllBoxes((i, j, k, l, i1, j1) -> shapes_doublelineconsumer.consume(doublelist.getDouble(i), doublelist1.getDouble(j), doublelist2.getDouble(k), doublelist.getDouble(l), doublelist1.getDouble(i1), doublelist2.getDouble(j1)), true);
   }

   public List<AABB> toAabbs() {
      List<AABB> list = Lists.newArrayList();
      this.forAllBoxes((d0, d1, d2, d3, d4, d5) -> list.add(new AABB(d0, d1, d2, d3, d4, d5)));
      return list;
   }

   public double min(Direction.Axis direction_axis, double d0, double d1) {
      Direction.Axis direction_axis1 = AxisCycle.FORWARD.cycle(direction_axis);
      Direction.Axis direction_axis2 = AxisCycle.BACKWARD.cycle(direction_axis);
      int i = this.findIndex(direction_axis1, d0);
      int j = this.findIndex(direction_axis2, d1);
      int k = this.shape.firstFull(direction_axis, i, j);
      return k >= this.shape.getSize(direction_axis) ? Double.POSITIVE_INFINITY : this.get(direction_axis, k);
   }

   public double max(Direction.Axis direction_axis, double d0, double d1) {
      Direction.Axis direction_axis1 = AxisCycle.FORWARD.cycle(direction_axis);
      Direction.Axis direction_axis2 = AxisCycle.BACKWARD.cycle(direction_axis);
      int i = this.findIndex(direction_axis1, d0);
      int j = this.findIndex(direction_axis2, d1);
      int k = this.shape.lastFull(direction_axis, i, j);
      return k <= 0 ? Double.NEGATIVE_INFINITY : this.get(direction_axis, k);
   }

   protected int findIndex(Direction.Axis direction_axis, double d0) {
      return Mth.binarySearch(0, this.shape.getSize(direction_axis) + 1, (i) -> d0 < this.get(direction_axis, i)) - 1;
   }

   @Nullable
   public BlockHitResult clip(Vec3 vec3, Vec3 vec31, BlockPos blockpos) {
      if (this.isEmpty()) {
         return null;
      } else {
         Vec3 vec32 = vec31.subtract(vec3);
         if (vec32.lengthSqr() < 1.0E-7D) {
            return null;
         } else {
            Vec3 vec33 = vec3.add(vec32.scale(0.001D));
            return this.shape.isFullWide(this.findIndex(Direction.Axis.X, vec33.x - (double)blockpos.getX()), this.findIndex(Direction.Axis.Y, vec33.y - (double)blockpos.getY()), this.findIndex(Direction.Axis.Z, vec33.z - (double)blockpos.getZ())) ? new BlockHitResult(vec33, Direction.getNearest(vec32.x, vec32.y, vec32.z).getOpposite(), blockpos, true) : AABB.clip(this.toAabbs(), vec3, vec31, blockpos);
         }
      }
   }

   public Optional<Vec3> closestPointTo(Vec3 vec3) {
      if (this.isEmpty()) {
         return Optional.empty();
      } else {
         Vec3[] avec3 = new Vec3[1];
         this.forAllBoxes((d0, d1, d2, d3, d4, d5) -> {
            double d6 = Mth.clamp(vec3.x(), d0, d3);
            double d7 = Mth.clamp(vec3.y(), d1, d4);
            double d8 = Mth.clamp(vec3.z(), d2, d5);
            if (avec3[0] == null || vec3.distanceToSqr(d6, d7, d8) < vec3.distanceToSqr(avec3[0])) {
               avec3[0] = new Vec3(d6, d7, d8);
            }

         });
         return Optional.of(avec3[0]);
      }
   }

   public VoxelShape getFaceShape(Direction direction) {
      if (!this.isEmpty() && this != Shapes.block()) {
         if (this.faces != null) {
            VoxelShape voxelshape = this.faces[direction.ordinal()];
            if (voxelshape != null) {
               return voxelshape;
            }
         } else {
            this.faces = new VoxelShape[6];
         }

         VoxelShape voxelshape1 = this.calculateFace(direction);
         this.faces[direction.ordinal()] = voxelshape1;
         return voxelshape1;
      } else {
         return this;
      }
   }

   private VoxelShape calculateFace(Direction direction) {
      Direction.Axis direction_axis = direction.getAxis();
      DoubleList doublelist = this.getCoords(direction_axis);
      if (doublelist.size() == 2 && DoubleMath.fuzzyEquals(doublelist.getDouble(0), 0.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(doublelist.getDouble(1), 1.0D, 1.0E-7D)) {
         return this;
      } else {
         Direction.AxisDirection direction_axisdirection = direction.getAxisDirection();
         int i = this.findIndex(direction_axis, direction_axisdirection == Direction.AxisDirection.POSITIVE ? 0.9999999D : 1.0E-7D);
         return new SliceShape(this, direction_axis, i);
      }
   }

   public double collide(Direction.Axis direction_axis, AABB aabb, double d0) {
      return this.collideX(AxisCycle.between(direction_axis, Direction.Axis.X), aabb, d0);
   }

   protected double collideX(AxisCycle axiscycle, AABB aabb, double d0) {
      if (this.isEmpty()) {
         return d0;
      } else if (Math.abs(d0) < 1.0E-7D) {
         return 0.0D;
      } else {
         AxisCycle axiscycle1 = axiscycle.inverse();
         Direction.Axis direction_axis = axiscycle1.cycle(Direction.Axis.X);
         Direction.Axis direction_axis1 = axiscycle1.cycle(Direction.Axis.Y);
         Direction.Axis direction_axis2 = axiscycle1.cycle(Direction.Axis.Z);
         double d1 = aabb.max(direction_axis);
         double d2 = aabb.min(direction_axis);
         int i = this.findIndex(direction_axis, d2 + 1.0E-7D);
         int j = this.findIndex(direction_axis, d1 - 1.0E-7D);
         int k = Math.max(0, this.findIndex(direction_axis1, aabb.min(direction_axis1) + 1.0E-7D));
         int l = Math.min(this.shape.getSize(direction_axis1), this.findIndex(direction_axis1, aabb.max(direction_axis1) - 1.0E-7D) + 1);
         int i1 = Math.max(0, this.findIndex(direction_axis2, aabb.min(direction_axis2) + 1.0E-7D));
         int j1 = Math.min(this.shape.getSize(direction_axis2), this.findIndex(direction_axis2, aabb.max(direction_axis2) - 1.0E-7D) + 1);
         int k1 = this.shape.getSize(direction_axis);
         if (d0 > 0.0D) {
            for(int l1 = j + 1; l1 < k1; ++l1) {
               for(int i2 = k; i2 < l; ++i2) {
                  for(int j2 = i1; j2 < j1; ++j2) {
                     if (this.shape.isFullWide(axiscycle1, l1, i2, j2)) {
                        double d3 = this.get(direction_axis, l1) - d1;
                        if (d3 >= -1.0E-7D) {
                           d0 = Math.min(d0, d3);
                        }

                        return d0;
                     }
                  }
               }
            }
         } else if (d0 < 0.0D) {
            for(int k2 = i - 1; k2 >= 0; --k2) {
               for(int l2 = k; l2 < l; ++l2) {
                  for(int i3 = i1; i3 < j1; ++i3) {
                     if (this.shape.isFullWide(axiscycle1, k2, l2, i3)) {
                        double d4 = this.get(direction_axis, k2 + 1) - d2;
                        if (d4 <= 1.0E-7D) {
                           d0 = Math.max(d0, d4);
                        }

                        return d0;
                     }
                  }
               }
            }
         }

         return d0;
      }
   }

   public String toString() {
      return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]";
   }
}
