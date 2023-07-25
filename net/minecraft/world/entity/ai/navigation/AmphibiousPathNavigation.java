package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class AmphibiousPathNavigation extends PathNavigation {
   public AmphibiousPathNavigation(Mob mob, Level level) {
      super(mob, level);
   }

   protected PathFinder createPathFinder(int i) {
      this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
      this.nodeEvaluator.setCanPassDoors(true);
      return new PathFinder(this.nodeEvaluator, i);
   }

   protected boolean canUpdatePath() {
      return true;
   }

   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), this.mob.getY(0.5D), this.mob.getZ());
   }

   protected double getGroundY(Vec3 vec3) {
      return vec3.y;
   }

   protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec31) {
      return this.isInLiquid() ? isClearForMovementBetween(this.mob, vec3, vec31, false) : false;
   }

   public boolean isStableDestination(BlockPos blockpos) {
      return !this.level.getBlockState(blockpos.below()).isAir();
   }

   public void setCanFloat(boolean flag) {
   }
}
