package net.minecraft.world.level.block.piston;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class PistonMath {
   public static AABB getMovementArea(AABB aabb, Direction direction, double d0) {
      double d1 = d0 * (double)direction.getAxisDirection().getStep();
      double d2 = Math.min(d1, 0.0D);
      double d3 = Math.max(d1, 0.0D);
      switch (direction) {
         case WEST:
            return new AABB(aabb.minX + d2, aabb.minY, aabb.minZ, aabb.minX + d3, aabb.maxY, aabb.maxZ);
         case EAST:
            return new AABB(aabb.maxX + d2, aabb.minY, aabb.minZ, aabb.maxX + d3, aabb.maxY, aabb.maxZ);
         case DOWN:
            return new AABB(aabb.minX, aabb.minY + d2, aabb.minZ, aabb.maxX, aabb.minY + d3, aabb.maxZ);
         case UP:
         default:
            return new AABB(aabb.minX, aabb.maxY + d2, aabb.minZ, aabb.maxX, aabb.maxY + d3, aabb.maxZ);
         case NORTH:
            return new AABB(aabb.minX, aabb.minY, aabb.minZ + d2, aabb.maxX, aabb.maxY, aabb.minZ + d3);
         case SOUTH:
            return new AABB(aabb.minX, aabb.minY, aabb.maxZ + d2, aabb.maxX, aabb.maxY, aabb.maxZ + d3);
      }
   }
}
