package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.core.Direction;

public class ArrayVoxelShape extends VoxelShape {
   private final DoubleList xs;
   private final DoubleList ys;
   private final DoubleList zs;

   protected ArrayVoxelShape(DiscreteVoxelShape discretevoxelshape, double[] adouble, double[] adouble1, double[] adouble2) {
      this(discretevoxelshape, (DoubleList)DoubleArrayList.wrap(Arrays.copyOf(adouble, discretevoxelshape.getXSize() + 1)), (DoubleList)DoubleArrayList.wrap(Arrays.copyOf(adouble1, discretevoxelshape.getYSize() + 1)), (DoubleList)DoubleArrayList.wrap(Arrays.copyOf(adouble2, discretevoxelshape.getZSize() + 1)));
   }

   ArrayVoxelShape(DiscreteVoxelShape discretevoxelshape, DoubleList doublelist, DoubleList doublelist1, DoubleList doublelist2) {
      super(discretevoxelshape);
      int i = discretevoxelshape.getXSize() + 1;
      int j = discretevoxelshape.getYSize() + 1;
      int k = discretevoxelshape.getZSize() + 1;
      if (i == doublelist.size() && j == doublelist1.size() && k == doublelist2.size()) {
         this.xs = doublelist;
         this.ys = doublelist1;
         this.zs = doublelist2;
      } else {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape."));
      }
   }

   protected DoubleList getCoords(Direction.Axis direction_axis) {
      switch (direction_axis) {
         case X:
            return this.xs;
         case Y:
            return this.ys;
         case Z:
            return this.zs;
         default:
            throw new IllegalArgumentException();
      }
   }
}
