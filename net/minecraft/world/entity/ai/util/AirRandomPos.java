package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirRandomPos {
   @Nullable
   public static Vec3 getPosTowards(PathfinderMob pathfindermob, int i, int j, int k, Vec3 vec3, double d0) {
      Vec3 vec31 = vec3.subtract(pathfindermob.getX(), pathfindermob.getY(), pathfindermob.getZ());
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(pathfindermob, () -> {
         BlockPos blockpos = AirAndWaterRandomPos.generateRandomPos(pathfindermob, i, j, k, vec31.x, vec31.z, d0, flag);
         return blockpos != null && !GoalUtils.isWater(pathfindermob, blockpos) ? blockpos : null;
      });
   }
}
