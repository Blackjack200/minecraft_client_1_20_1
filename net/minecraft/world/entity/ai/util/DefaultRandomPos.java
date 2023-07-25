package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class DefaultRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pathfindermob, int i, int j) {
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(pathfindermob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirection(pathfindermob.getRandom(), i, j);
         return generateRandomPosTowardDirection(pathfindermob, i, flag, blockpos);
      });
   }

   @Nullable
   public static Vec3 getPosTowards(PathfinderMob pathfindermob, int i, int j, Vec3 vec3, double d0) {
      Vec3 vec31 = vec3.subtract(pathfindermob.getX(), pathfindermob.getY(), pathfindermob.getZ());
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(pathfindermob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pathfindermob.getRandom(), i, j, 0, vec31.x, vec31.z, d0);
         return blockpos == null ? null : generateRandomPosTowardDirection(pathfindermob, i, flag, blockpos);
      });
   }

   @Nullable
   public static Vec3 getPosAway(PathfinderMob pathfindermob, int i, int j, Vec3 vec3) {
      Vec3 vec31 = pathfindermob.position().subtract(vec3);
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(pathfindermob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pathfindermob.getRandom(), i, j, 0, vec31.x, vec31.z, (double)((float)Math.PI / 2F));
         return blockpos == null ? null : generateRandomPosTowardDirection(pathfindermob, i, flag, blockpos);
      });
   }

   @Nullable
   private static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfindermob, int i, boolean flag, BlockPos blockpos) {
      BlockPos blockpos1 = RandomPos.generateRandomPosTowardDirection(pathfindermob, i, pathfindermob.getRandom(), blockpos);
      return !GoalUtils.isOutsideLimits(blockpos1, pathfindermob) && !GoalUtils.isRestricted(flag, pathfindermob, blockpos1) && !GoalUtils.isNotStable(pathfindermob.getNavigation(), blockpos1) && !GoalUtils.hasMalus(pathfindermob, blockpos1) ? blockpos1 : null;
   }
}
