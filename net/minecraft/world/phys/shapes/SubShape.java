package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class SubShape extends DiscreteVoxelShape {
   private final DiscreteVoxelShape parent;
   private final int startX;
   private final int startY;
   private final int startZ;
   private final int endX;
   private final int endY;
   private final int endZ;

   protected SubShape(DiscreteVoxelShape discretevoxelshape, int i, int j, int k, int l, int i1, int j1) {
      super(l - i, i1 - j, j1 - k);
      this.parent = discretevoxelshape;
      this.startX = i;
      this.startY = j;
      this.startZ = k;
      this.endX = l;
      this.endY = i1;
      this.endZ = j1;
   }

   public boolean isFull(int i, int j, int k) {
      return this.parent.isFull(this.startX + i, this.startY + j, this.startZ + k);
   }

   public void fill(int i, int j, int k) {
      this.parent.fill(this.startX + i, this.startY + j, this.startZ + k);
   }

   public int firstFull(Direction.Axis direction_axis) {
      return this.clampToShape(direction_axis, this.parent.firstFull(direction_axis));
   }

   public int lastFull(Direction.Axis direction_axis) {
      return this.clampToShape(direction_axis, this.parent.lastFull(direction_axis));
   }

   private int clampToShape(Direction.Axis direction_axis, int i) {
      int j = direction_axis.choose(this.startX, this.startY, this.startZ);
      int k = direction_axis.choose(this.endX, this.endY, this.endZ);
      return Mth.clamp(i, j, k) - j;
   }
}
