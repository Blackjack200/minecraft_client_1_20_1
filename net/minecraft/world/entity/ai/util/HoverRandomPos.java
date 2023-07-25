package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class HoverRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pathfindermob, int i, int j, double d0, double d1, float f, int k, int l) {
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(pathfindermob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pathfindermob.getRandom(), i, j, 0, d0, d1, (double)f);
         if (blockpos == null) {
            return null;
         } else {
            BlockPos blockpos1 = LandRandomPos.generateRandomPosTowardDirection(pathfindermob, i, flag, blockpos);
            if (blockpos1 == null) {
               return null;
            } else {
               blockpos1 = RandomPos.moveUpToAboveSolid(blockpos1, pathfindermob.getRandom().nextInt(k - l + 1) + l, pathfindermob.level().getMaxBuildHeight(), (blockpos2) -> GoalUtils.isSolid(pathfindermob, blockpos2));
               return !GoalUtils.isWater(pathfindermob, blockpos1) && !GoalUtils.hasMalus(pathfindermob, blockpos1) ? blockpos1 : null;
            }
         }
      });
   }
}
