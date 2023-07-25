package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;

public class SliceShape extends VoxelShape {
   private final VoxelShape delegate;
   private final Direction.Axis axis;
   private static final DoubleList SLICE_COORDS = new CubePointRange(1);

   public SliceShape(VoxelShape voxelshape, Direction.Axis direction_axis, int i) {
      super(makeSlice(voxelshape.shape, direction_axis, i));
      this.delegate = voxelshape;
      this.axis = direction_axis;
   }

   private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape discretevoxelshape, Direction.Axis direction_axis, int i) {
      return new SubShape(discretevoxelshape, direction_axis.choose(i, 0, 0), direction_axis.choose(0, i, 0), direction_axis.choose(0, 0, i), direction_axis.choose(i + 1, discretevoxelshape.xSize, discretevoxelshape.xSize), direction_axis.choose(discretevoxelshape.ySize, i + 1, discretevoxelshape.ySize), direction_axis.choose(discretevoxelshape.zSize, discretevoxelshape.zSize, i + 1));
   }

   protected DoubleList getCoords(Direction.Axis direction_axis) {
      return direction_axis == this.axis ? SLICE_COORDS : this.delegate.getCoords(direction_axis);
   }
}
