package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class CubeVoxelShape extends VoxelShape {
   protected CubeVoxelShape(DiscreteVoxelShape discretevoxelshape) {
      super(discretevoxelshape);
   }

   protected DoubleList getCoords(Direction.Axis direction_axis) {
      return new CubePointRange(this.shape.getSize(direction_axis));
   }

   protected int findIndex(Direction.Axis direction_axis, double d0) {
      int i = this.shape.getSize(direction_axis);
      return Mth.floor(Mth.clamp(d0 * (double)i, -1.0D, (double)i));
   }
}
