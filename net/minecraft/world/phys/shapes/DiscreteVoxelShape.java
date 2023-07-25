package net.minecraft.world.phys.shapes;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;

public abstract class DiscreteVoxelShape {
   private static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
   protected final int xSize;
   protected final int ySize;
   protected final int zSize;

   protected DiscreteVoxelShape(int i, int j, int k) {
      if (i >= 0 && j >= 0 && k >= 0) {
         this.xSize = i;
         this.ySize = j;
         this.zSize = k;
      } else {
         throw new IllegalArgumentException("Need all positive sizes: x: " + i + ", y: " + j + ", z: " + k);
      }
   }

   public boolean isFullWide(AxisCycle axiscycle, int i, int j, int k) {
      return this.isFullWide(axiscycle.cycle(i, j, k, Direction.Axis.X), axiscycle.cycle(i, j, k, Direction.Axis.Y), axiscycle.cycle(i, j, k, Direction.Axis.Z));
   }

   public boolean isFullWide(int i, int j, int k) {
      if (i >= 0 && j >= 0 && k >= 0) {
         return i < this.xSize && j < this.ySize && k < this.zSize ? this.isFull(i, j, k) : false;
      } else {
         return false;
      }
   }

   public boolean isFull(AxisCycle axiscycle, int i, int j, int k) {
      return this.isFull(axiscycle.cycle(i, j, k, Direction.Axis.X), axiscycle.cycle(i, j, k, Direction.Axis.Y), axiscycle.cycle(i, j, k, Direction.Axis.Z));
   }

   public abstract boolean isFull(int i, int j, int k);

   public abstract void fill(int i, int j, int k);

   public boolean isEmpty() {
      for(Direction.Axis direction_axis : AXIS_VALUES) {
         if (this.firstFull(direction_axis) >= this.lastFull(direction_axis)) {
            return true;
         }
      }

      return false;
   }

   public abstract int firstFull(Direction.Axis direction_axis);

   public abstract int lastFull(Direction.Axis direction_axis);

   public int firstFull(Direction.Axis direction_axis, int i, int j) {
      int k = this.getSize(direction_axis);
      if (i >= 0 && j >= 0) {
         Direction.Axis direction_axis1 = AxisCycle.FORWARD.cycle(direction_axis);
         Direction.Axis direction_axis2 = AxisCycle.BACKWARD.cycle(direction_axis);
         if (i < this.getSize(direction_axis1) && j < this.getSize(direction_axis2)) {
            AxisCycle axiscycle = AxisCycle.between(Direction.Axis.X, direction_axis);

            for(int l = 0; l < k; ++l) {
               if (this.isFull(axiscycle, l, i, j)) {
                  return l;
               }
            }

            return k;
         } else {
            return k;
         }
      } else {
         return k;
      }
   }

   public int lastFull(Direction.Axis direction_axis, int i, int j) {
      if (i >= 0 && j >= 0) {
         Direction.Axis direction_axis1 = AxisCycle.FORWARD.cycle(direction_axis);
         Direction.Axis direction_axis2 = AxisCycle.BACKWARD.cycle(direction_axis);
         if (i < this.getSize(direction_axis1) && j < this.getSize(direction_axis2)) {
            int k = this.getSize(direction_axis);
            AxisCycle axiscycle = AxisCycle.between(Direction.Axis.X, direction_axis);

            for(int l = k - 1; l >= 0; --l) {
               if (this.isFull(axiscycle, l, i, j)) {
                  return l + 1;
               }
            }

            return 0;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public int getSize(Direction.Axis direction_axis) {
      return direction_axis.choose(this.xSize, this.ySize, this.zSize);
   }

   public int getXSize() {
      return this.getSize(Direction.Axis.X);
   }

   public int getYSize() {
      return this.getSize(Direction.Axis.Y);
   }

   public int getZSize() {
      return this.getSize(Direction.Axis.Z);
   }

   public void forAllEdges(DiscreteVoxelShape.IntLineConsumer discretevoxelshape_intlineconsumer, boolean flag) {
      this.forAllAxisEdges(discretevoxelshape_intlineconsumer, AxisCycle.NONE, flag);
      this.forAllAxisEdges(discretevoxelshape_intlineconsumer, AxisCycle.FORWARD, flag);
      this.forAllAxisEdges(discretevoxelshape_intlineconsumer, AxisCycle.BACKWARD, flag);
   }

   private void forAllAxisEdges(DiscreteVoxelShape.IntLineConsumer discretevoxelshape_intlineconsumer, AxisCycle axiscycle, boolean flag) {
      AxisCycle axiscycle1 = axiscycle.inverse();
      int i = this.getSize(axiscycle1.cycle(Direction.Axis.X));
      int j = this.getSize(axiscycle1.cycle(Direction.Axis.Y));
      int k = this.getSize(axiscycle1.cycle(Direction.Axis.Z));

      for(int l = 0; l <= i; ++l) {
         for(int i1 = 0; i1 <= j; ++i1) {
            int j1 = -1;

            for(int k1 = 0; k1 <= k; ++k1) {
               int l1 = 0;
               int i2 = 0;

               for(int j2 = 0; j2 <= 1; ++j2) {
                  for(int k2 = 0; k2 <= 1; ++k2) {
                     if (this.isFullWide(axiscycle1, l + j2 - 1, i1 + k2 - 1, k1)) {
                        ++l1;
                        i2 ^= j2 ^ k2;
                     }
                  }
               }

               if (l1 == 1 || l1 == 3 || l1 == 2 && (i2 & 1) == 0) {
                  if (flag) {
                     if (j1 == -1) {
                        j1 = k1;
                     }
                  } else {
                     discretevoxelshape_intlineconsumer.consume(axiscycle1.cycle(l, i1, k1, Direction.Axis.X), axiscycle1.cycle(l, i1, k1, Direction.Axis.Y), axiscycle1.cycle(l, i1, k1, Direction.Axis.Z), axiscycle1.cycle(l, i1, k1 + 1, Direction.Axis.X), axiscycle1.cycle(l, i1, k1 + 1, Direction.Axis.Y), axiscycle1.cycle(l, i1, k1 + 1, Direction.Axis.Z));
                  }
               } else if (j1 != -1) {
                  discretevoxelshape_intlineconsumer.consume(axiscycle1.cycle(l, i1, j1, Direction.Axis.X), axiscycle1.cycle(l, i1, j1, Direction.Axis.Y), axiscycle1.cycle(l, i1, j1, Direction.Axis.Z), axiscycle1.cycle(l, i1, k1, Direction.Axis.X), axiscycle1.cycle(l, i1, k1, Direction.Axis.Y), axiscycle1.cycle(l, i1, k1, Direction.Axis.Z));
                  j1 = -1;
               }
            }
         }
      }

   }

   public void forAllBoxes(DiscreteVoxelShape.IntLineConsumer discretevoxelshape_intlineconsumer, boolean flag) {
      BitSetDiscreteVoxelShape.forAllBoxes(this, discretevoxelshape_intlineconsumer, flag);
   }

   public void forAllFaces(DiscreteVoxelShape.IntFaceConsumer discretevoxelshape_intfaceconsumer) {
      this.forAllAxisFaces(discretevoxelshape_intfaceconsumer, AxisCycle.NONE);
      this.forAllAxisFaces(discretevoxelshape_intfaceconsumer, AxisCycle.FORWARD);
      this.forAllAxisFaces(discretevoxelshape_intfaceconsumer, AxisCycle.BACKWARD);
   }

   private void forAllAxisFaces(DiscreteVoxelShape.IntFaceConsumer discretevoxelshape_intfaceconsumer, AxisCycle axiscycle) {
      AxisCycle axiscycle1 = axiscycle.inverse();
      Direction.Axis direction_axis = axiscycle1.cycle(Direction.Axis.Z);
      int i = this.getSize(axiscycle1.cycle(Direction.Axis.X));
      int j = this.getSize(axiscycle1.cycle(Direction.Axis.Y));
      int k = this.getSize(direction_axis);
      Direction direction = Direction.fromAxisAndDirection(direction_axis, Direction.AxisDirection.NEGATIVE);
      Direction direction1 = Direction.fromAxisAndDirection(direction_axis, Direction.AxisDirection.POSITIVE);

      for(int l = 0; l < i; ++l) {
         for(int i1 = 0; i1 < j; ++i1) {
            boolean flag = false;

            for(int j1 = 0; j1 <= k; ++j1) {
               boolean flag1 = j1 != k && this.isFull(axiscycle1, l, i1, j1);
               if (!flag && flag1) {
                  discretevoxelshape_intfaceconsumer.consume(direction, axiscycle1.cycle(l, i1, j1, Direction.Axis.X), axiscycle1.cycle(l, i1, j1, Direction.Axis.Y), axiscycle1.cycle(l, i1, j1, Direction.Axis.Z));
               }

               if (flag && !flag1) {
                  discretevoxelshape_intfaceconsumer.consume(direction1, axiscycle1.cycle(l, i1, j1 - 1, Direction.Axis.X), axiscycle1.cycle(l, i1, j1 - 1, Direction.Axis.Y), axiscycle1.cycle(l, i1, j1 - 1, Direction.Axis.Z));
               }

               flag = flag1;
            }
         }
      }

   }

   public interface IntFaceConsumer {
      void consume(Direction direction, int i, int j, int k);
   }

   public interface IntLineConsumer {
      void consume(int i, int j, int k, int l, int i1, int j1);
   }
}
