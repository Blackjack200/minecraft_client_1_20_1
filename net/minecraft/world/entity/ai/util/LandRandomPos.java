package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class LandRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pathfindermob, int i, int j) {
      return getPos(pathfindermob, i, j, pathfindermob::getWalkTargetValue);
   }

   @Nullable
   public static Vec3 getPos(PathfinderMob pathfindermob, int i, int j, ToDoubleFunction<BlockPos> todoublefunction) {
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return RandomPos.generateRandomPos(() -> {
         BlockPos blockpos = RandomPos.generateRandomDirection(pathfindermob.getRandom(), i, j);
         BlockPos blockpos1 = generateRandomPosTowardDirection(pathfindermob, i, flag, blockpos);
         return blockpos1 == null ? null : movePosUpOutOfSolid(pathfindermob, blockpos1);
      }, todoublefunction);
   }

   @Nullable
   public static Vec3 getPosTowards(PathfinderMob pathfindermob, int i, int j, Vec3 vec3) {
      Vec3 vec31 = vec3.subtract(pathfindermob.getX(), pathfindermob.getY(), pathfindermob.getZ());
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return getPosInDirection(pathfindermob, i, j, vec31, flag);
   }

   @Nullable
   public static Vec3 getPosAway(PathfinderMob pathfindermob, int i, int j, Vec3 vec3) {
      Vec3 vec31 = pathfindermob.position().subtract(vec3);
      boolean flag = GoalUtils.mobRestricted(pathfindermob, i);
      return getPosInDirection(pathfindermob, i, j, vec31, flag);
   }

   @Nullable
   private static Vec3 getPosInDirection(PathfinderMob pathfindermob, int i, int j, Vec3 vec3, boolean flag) {
      return RandomPos.generateRandomPos(pathfindermob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pathfindermob.getRandom(), i, j, 0, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
         if (blockpos == null) {
            return null;
         } else {
            BlockPos blockpos1 = generateRandomPosTowardDirection(pathfindermob, i, flag, blockpos);
            return blockpos1 == null ? null : movePosUpOutOfSolid(pathfindermob, blockpos1);
         }
      });
   }

   @Nullable
   public static BlockPos movePosUpOutOfSolid(PathfinderMob pathfindermob, BlockPos blockpos) {
      blockpos = RandomPos.moveUpOutOfSolid(blockpos, pathfindermob.level().getMaxBuildHeight(), (blockpos1) -> GoalUtils.isSolid(pathfindermob, blockpos1));
      return !GoalUtils.isWater(pathfindermob, blockpos) && !GoalUtils.hasMalus(pathfindermob, blockpos) ? blockpos : null;
   }

   @Nullable
   public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfindermob, int i, boolean flag, BlockPos blockpos) {
      BlockPos blockpos1 = RandomPos.generateRandomPosTowardDirection(pathfindermob, i, pathfindermob.getRandom(), blockpos);
      return !GoalUtils.isOutsideLimits(blockpos1, pathfindermob) && !GoalUtils.isRestricted(flag, pathfindermob, blockpos1) && !GoalUtils.isNotStable(pathfindermob.getNavigation(), blockpos1) ? blockpos1 : null;
   }
}
