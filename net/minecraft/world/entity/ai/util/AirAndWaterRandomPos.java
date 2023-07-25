package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirAndWaterRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pathfindermob, int i, int j, int k, double d0, double d1, double d2) {
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(pathfindermob, () -> generateRandomPos(pathfindermob, i, j, k, d0, d1, d2, flag));
   }

   @Nullable
   public static BlockPos generateRandomPos(PathfinderMob pathfindermob, int i, int j, int k, double d0, double d1, double d2, boolean flag) {
      BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pathfindermob.getRandom(), i, j, k, d0, d1, d2);
      if (blockpos == null) {
         return null;
      } else {
         BlockPos blockpos1 = RandomPos.generateRandomPosTowardDirection(pathfindermob, i, pathfindermob.getRandom(), blockpos);
         if (!GoalUtils.isOutsideLimits(blockpos1, pathfindermob) && !GoalUtils.isRestricted(flag, pathfindermob, blockpos1)) {
            blockpos1 = RandomPos.moveUpOutOfSolid(blockpos1, pathfindermob.level().getMaxBuildHeight(), (blockpos2) -> GoalUtils.isSolid(pathfindermob, blockpos2));
            return GoalUtils.hasMalus(pathfindermob, blockpos1) ? null : blockpos1;
         } else {
            return null;
         }
      }
   }
}
