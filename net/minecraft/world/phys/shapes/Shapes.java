package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class Shapes {
   public static final double EPSILON = 1.0E-7D;
   public static final double BIG_EPSILON = 1.0E-6D;
   private static final VoxelShape BLOCK = Util.make(() -> {
      DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(1, 1, 1);
      discretevoxelshape.fill(0, 0, 0);
      return new CubeVoxelShape(discretevoxelshape);
   });
   public static final VoxelShape INFINITY = box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   private static final VoxelShape EMPTY = new ArrayVoxelShape(new BitSetDiscreteVoxelShape(0, 0, 0), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})));

   public static VoxelShape empty() {
      return EMPTY;
   }

   public static VoxelShape block() {
      return BLOCK;
   }

   public static VoxelShape box(double d0, double d1, double d2, double d3, double d4, double d5) {
      if (!(d0 > d3) && !(d1 > d4) && !(d2 > d5)) {
         return create(d0, d1, d2, d3, d4, d5);
      } else {
         throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
      }
   }

   public static VoxelShape create(double d0, double d1, double d2, double d3, double d4, double d5) {
      if (!(d3 - d0 < 1.0E-7D) && !(d4 - d1 < 1.0E-7D) && !(d5 - d2 < 1.0E-7D)) {
         int i = findBits(d0, d3);
         int j = findBits(d1, d4);
         int k = findBits(d2, d5);
         if (i >= 0 && j >= 0 && k >= 0) {
            if (i == 0 && j == 0 && k == 0) {
               return block();
            } else {
               int l = 1 << i;
               int i1 = 1 << j;
               int j1 = 1 << k;
               BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.withFilledBounds(l, i1, j1, (int)Math.round(d0 * (double)l), (int)Math.round(d1 * (double)i1), (int)Math.round(d2 * (double)j1), (int)Math.round(d3 * (double)l), (int)Math.round(d4 * (double)i1), (int)Math.round(d5 * (double)j1));
               return new CubeVoxelShape(bitsetdiscretevoxelshape);
            }
         } else {
            return new ArrayVoxelShape(BLOCK.shape, (DoubleList)DoubleArrayList.wrap(new double[]{d0, d3}), (DoubleList)DoubleArrayList.wrap(new double[]{d1, d4}), (DoubleList)DoubleArrayList.wrap(new double[]{d2, d5}));
         }
      } else {
         return empty();
      }
   }

   public static VoxelShape create(AABB aabb) {
      return create(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
   }

   @VisibleForTesting
   protected static int findBits(double d0, double d1) {
      if (!(d0 < -1.0E-7D) && !(d1 > 1.0000001D)) {
         for(int i = 0; i <= 3; ++i) {
            int j = 1 << i;
            double d2 = d0 * (double)j;
            double d3 = d1 * (double)j;
            boolean flag = Math.abs(d2 - (double)Math.round(d2)) < 1.0E-7D * (double)j;
            boolean flag1 = Math.abs(d3 - (double)Math.round(d3)) < 1.0E-7D * (double)j;
            if (flag && flag1) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   protected static long lcm(int i, int j) {
      return (long)i * (long)(j / IntMath.gcd(i, j));
   }

   public static VoxelShape or(VoxelShape voxelshape, VoxelShape voxelshape1) {
      return join(voxelshape, voxelshape1, BooleanOp.OR);
   }

   public static VoxelShape or(VoxelShape voxelshape, VoxelShape... avoxelshape) {
      return Arrays.stream(avoxelshape).reduce(voxelshape, Shapes::or);
   }

   public static VoxelShape join(VoxelShape voxelshape, VoxelShape voxelshape1, BooleanOp booleanop) {
      return joinUnoptimized(voxelshape, voxelshape1, booleanop).optimize();
   }

   public static VoxelShape joinUnoptimized(VoxelShape voxelshape, VoxelShape voxelshape1, BooleanOp booleanop) {
      if (booleanop.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if (voxelshape == voxelshape1) {
         return booleanop.apply(true, true) ? voxelshape : empty();
      } else {
         boolean flag = booleanop.apply(true, false);
         boolean flag1 = booleanop.apply(false, true);
         if (voxelshape.isEmpty()) {
            return flag1 ? voxelshape1 : empty();
         } else if (voxelshape1.isEmpty()) {
            return flag ? voxelshape : empty();
         } else {
            IndexMerger indexmerger = createIndexMerger(1, voxelshape.getCoords(Direction.Axis.X), voxelshape1.getCoords(Direction.Axis.X), flag, flag1);
            IndexMerger indexmerger1 = createIndexMerger(indexmerger.size() - 1, voxelshape.getCoords(Direction.Axis.Y), voxelshape1.getCoords(Direction.Axis.Y), flag, flag1);
            IndexMerger indexmerger2 = createIndexMerger((indexmerger.size() - 1) * (indexmerger1.size() - 1), voxelshape.getCoords(Direction.Axis.Z), voxelshape1.getCoords(Direction.Axis.Z), flag, flag1);
            BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.join(voxelshape.shape, voxelshape1.shape, indexmerger, indexmerger1, indexmerger2, booleanop);
            return (VoxelShape)(indexmerger instanceof DiscreteCubeMerger && indexmerger1 instanceof DiscreteCubeMerger && indexmerger2 instanceof DiscreteCubeMerger ? new CubeVoxelShape(bitsetdiscretevoxelshape) : new ArrayVoxelShape(bitsetdiscretevoxelshape, indexmerger.getList(), indexmerger1.getList(), indexmerger2.getList()));
         }
      }
   }

   public static boolean joinIsNotEmpty(VoxelShape voxelshape, VoxelShape voxelshape1, BooleanOp booleanop) {
      if (booleanop.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else {
         boolean flag = voxelshape.isEmpty();
         boolean flag1 = voxelshape1.isEmpty();
         if (!flag && !flag1) {
            if (voxelshape == voxelshape1) {
               return booleanop.apply(true, true);
            } else {
               boolean flag2 = booleanop.apply(true, false);
               boolean flag3 = booleanop.apply(false, true);

               for(Direction.Axis direction_axis : AxisCycle.AXIS_VALUES) {
                  if (voxelshape.max(direction_axis) < voxelshape1.min(direction_axis) - 1.0E-7D) {
                     return flag2 || flag3;
                  }

                  if (voxelshape1.max(direction_axis) < voxelshape.min(direction_axis) - 1.0E-7D) {
                     return flag2 || flag3;
                  }
               }

               IndexMerger indexmerger = createIndexMerger(1, voxelshape.getCoords(Direction.Axis.X), voxelshape1.getCoords(Direction.Axis.X), flag2, flag3);
               IndexMerger indexmerger1 = createIndexMerger(indexmerger.size() - 1, voxelshape.getCoords(Direction.Axis.Y), voxelshape1.getCoords(Direction.Axis.Y), flag2, flag3);
               IndexMerger indexmerger2 = createIndexMerger((indexmerger.size() - 1) * (indexmerger1.size() - 1), voxelshape.getCoords(Direction.Axis.Z), voxelshape1.getCoords(Direction.Axis.Z), flag2, flag3);
               return joinIsNotEmpty(indexmerger, indexmerger1, indexmerger2, voxelshape.shape, voxelshape1.shape, booleanop);
            }
         } else {
            return booleanop.apply(!flag, !flag1);
         }
      }
   }

   private static boolean joinIsNotEmpty(IndexMerger indexmerger, IndexMerger indexmerger1, IndexMerger indexmerger2, DiscreteVoxelShape discretevoxelshape, DiscreteVoxelShape discretevoxelshape1, BooleanOp booleanop) {
      return !indexmerger.forMergedIndexes((i, j, k) -> indexmerger1.forMergedIndexes((j1, k1, l1) -> indexmerger2.forMergedIndexes((i3, j3, k3) -> !booleanop.apply(discretevoxelshape.isFullWide(i, j1, i3), discretevoxelshape1.isFullWide(j, k1, j3)))));
   }

   public static double collide(Direction.Axis direction_axis, AABB aabb, Iterable<VoxelShape> iterable, double d0) {
      for(VoxelShape voxelshape : iterable) {
         if (Math.abs(d0) < 1.0E-7D) {
            return 0.0D;
         }

         d0 = voxelshape.collide(direction_axis, aabb, d0);
      }

      return d0;
   }

   public static boolean blockOccudes(VoxelShape voxelshape, VoxelShape voxelshape1, Direction direction) {
      if (voxelshape == block() && voxelshape1 == block()) {
         return true;
      } else if (voxelshape1.isEmpty()) {
         return false;
      } else {
         Direction.Axis direction_axis = direction.getAxis();
         Direction.AxisDirection direction_axisdirection = direction.getAxisDirection();
         VoxelShape voxelshape2 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? voxelshape : voxelshape1;
         VoxelShape voxelshape3 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? voxelshape1 : voxelshape;
         BooleanOp booleanop = direction_axisdirection == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
         return DoubleMath.fuzzyEquals(voxelshape2.max(direction_axis), 1.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(voxelshape3.min(direction_axis), 0.0D, 1.0E-7D) && !joinIsNotEmpty(new SliceShape(voxelshape2, direction_axis, voxelshape2.shape.getSize(direction_axis) - 1), new SliceShape(voxelshape3, direction_axis, 0), booleanop);
      }
   }

   public static VoxelShape getFaceShape(VoxelShape voxelshape, Direction direction) {
      if (voxelshape == block()) {
         return block();
      } else {
         Direction.Axis direction_axis = direction.getAxis();
         boolean flag;
         int i;
         if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            flag = DoubleMath.fuzzyEquals(voxelshape.max(direction_axis), 1.0D, 1.0E-7D);
            i = voxelshape.shape.getSize(direction_axis) - 1;
         } else {
            flag = DoubleMath.fuzzyEquals(voxelshape.min(direction_axis), 0.0D, 1.0E-7D);
            i = 0;
         }

         return (VoxelShape)(!flag ? empty() : new SliceShape(voxelshape, direction_axis, i));
      }
   }

   public static boolean mergedFaceOccludes(VoxelShape voxelshape, VoxelShape voxelshape1, Direction direction) {
      if (voxelshape != block() && voxelshape1 != block()) {
         Direction.Axis direction_axis = direction.getAxis();
         Direction.AxisDirection direction_axisdirection = direction.getAxisDirection();
         VoxelShape voxelshape2 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? voxelshape : voxelshape1;
         VoxelShape voxelshape3 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? voxelshape1 : voxelshape;
         if (!DoubleMath.fuzzyEquals(voxelshape2.max(direction_axis), 1.0D, 1.0E-7D)) {
            voxelshape2 = empty();
         }

         if (!DoubleMath.fuzzyEquals(voxelshape3.min(direction_axis), 0.0D, 1.0E-7D)) {
            voxelshape3 = empty();
         }

         return !joinIsNotEmpty(block(), joinUnoptimized(new SliceShape(voxelshape2, direction_axis, voxelshape2.shape.getSize(direction_axis) - 1), new SliceShape(voxelshape3, direction_axis, 0), BooleanOp.OR), BooleanOp.ONLY_FIRST);
      } else {
         return true;
      }
   }

   public static boolean faceShapeOccludes(VoxelShape voxelshape, VoxelShape voxelshape1) {
      if (voxelshape != block() && voxelshape1 != block()) {
         if (voxelshape.isEmpty() && voxelshape1.isEmpty()) {
            return false;
         } else {
            return !joinIsNotEmpty(block(), joinUnoptimized(voxelshape, voxelshape1, BooleanOp.OR), BooleanOp.ONLY_FIRST);
         }
      } else {
         return true;
      }
   }

   @VisibleForTesting
   protected static IndexMerger createIndexMerger(int i, DoubleList doublelist, DoubleList doublelist1, boolean flag, boolean flag1) {
      int j = doublelist.size() - 1;
      int k = doublelist1.size() - 1;
      if (doublelist instanceof CubePointRange && doublelist1 instanceof CubePointRange) {
         long l = lcm(j, k);
         if ((long)i * l <= 256L) {
            return new DiscreteCubeMerger(j, k);
         }
      }

      if (doublelist.getDouble(j) < doublelist1.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(doublelist, doublelist1, false);
      } else if (doublelist1.getDouble(k) < doublelist.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(doublelist1, doublelist, true);
      } else {
         return (IndexMerger)(j == k && Objects.equals(doublelist, doublelist1) ? new IdenticalMerger(doublelist) : new IndirectMerger(doublelist, doublelist1, flag, flag1));
      }
   }

   public interface DoubleLineConsumer {
      void consume(double d0, double d1, double d2, double d3, double d4, double d5);
   }
}
