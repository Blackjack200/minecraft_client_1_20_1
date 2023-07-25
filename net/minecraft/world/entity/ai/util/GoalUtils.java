package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GoalUtils {
   public static boolean hasGroundPathNavigation(Mob mob) {
      return mob.getNavigation() instanceof GroundPathNavigation;
   }

   public static boolean mobRestricted(PathfinderMob pathfindermob, int i) {
      return pathfindermob.hasRestriction() && pathfindermob.getRestrictCenter().closerToCenterThan(pathfindermob.position(), (double)(pathfindermob.getRestrictRadius() + (float)i) + 1.0D);
   }

   public static boolean isOutsideLimits(BlockPos blockpos, PathfinderMob pathfindermob) {
      return blockpos.getY() < pathfindermob.level().getMinBuildHeight() || blockpos.getY() > pathfindermob.level().getMaxBuildHeight();
   }

   public static boolean isRestricted(boolean flag, PathfinderMob pathfindermob, BlockPos blockpos) {
      return flag && !pathfindermob.isWithinRestriction(blockpos);
   }

   public static boolean isNotStable(PathNavigation pathnavigation, BlockPos blockpos) {
      return !pathnavigation.isStableDestination(blockpos);
   }

   public static boolean isWater(PathfinderMob pathfindermob, BlockPos blockpos) {
      return pathfindermob.level().getFluidState(blockpos).is(FluidTags.WATER);
   }

   public static boolean hasMalus(PathfinderMob pathfindermob, BlockPos blockpos) {
      return pathfindermob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pathfindermob.level(), blockpos.mutable())) != 0.0F;
   }

   public static boolean isSolid(PathfinderMob pathfindermob, BlockPos blockpos) {
      return pathfindermob.level().getBlockState(blockpos).isSolid();
   }
}
