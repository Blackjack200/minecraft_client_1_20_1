package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation {
   private boolean avoidSun;

   public GroundPathNavigation(Mob mob, Level level) {
      super(mob, level);
   }

   protected PathFinder createPathFinder(int i) {
      this.nodeEvaluator = new WalkNodeEvaluator();
      this.nodeEvaluator.setCanPassDoors(true);
      return new PathFinder(this.nodeEvaluator, i);
   }

   protected boolean canUpdatePath() {
      return this.mob.onGround() || this.isInLiquid() || this.mob.isPassenger();
   }

   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
   }

   public Path createPath(BlockPos blockpos, int i) {
      if (this.level.getBlockState(blockpos).isAir()) {
         BlockPos blockpos1;
         for(blockpos1 = blockpos.below(); blockpos1.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockpos1).isAir(); blockpos1 = blockpos1.below()) {
         }

         if (blockpos1.getY() > this.level.getMinBuildHeight()) {
            return super.createPath(blockpos1.above(), i);
         }

         while(blockpos1.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos1).isAir()) {
            blockpos1 = blockpos1.above();
         }

         blockpos = blockpos1;
      }

      if (!this.level.getBlockState(blockpos).isSolid()) {
         return super.createPath(blockpos, i);
      } else {
         BlockPos blockpos2;
         for(blockpos2 = blockpos.above(); blockpos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos2).isSolid(); blockpos2 = blockpos2.above()) {
         }

         return super.createPath(blockpos2, i);
      }
   }

   public Path createPath(Entity entity, int i) {
      return this.createPath(entity.blockPosition(), i);
   }

   private int getSurfaceY() {
      if (this.mob.isInWater() && this.canFloat()) {
         int i = this.mob.getBlockY();
         BlockState blockstate = this.level.getBlockState(BlockPos.containing(this.mob.getX(), (double)i, this.mob.getZ()));
         int j = 0;

         while(blockstate.is(Blocks.WATER)) {
            ++i;
            blockstate = this.level.getBlockState(BlockPos.containing(this.mob.getX(), (double)i, this.mob.getZ()));
            ++j;
            if (j > 16) {
               return this.mob.getBlockY();
            }
         }

         return i;
      } else {
         return Mth.floor(this.mob.getY() + 0.5D);
      }
   }

   protected void trimPath() {
      super.trimPath();
      if (this.avoidSun) {
         if (this.level.canSeeSky(BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()))) {
            return;
         }

         for(int i = 0; i < this.path.getNodeCount(); ++i) {
            Node node = this.path.getNode(i);
            if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
               this.path.truncateNodes(i);
               return;
            }
         }
      }

   }

   protected boolean hasValidPathType(BlockPathTypes blockpathtypes) {
      if (blockpathtypes == BlockPathTypes.WATER) {
         return false;
      } else if (blockpathtypes == BlockPathTypes.LAVA) {
         return false;
      } else {
         return blockpathtypes != BlockPathTypes.OPEN;
      }
   }

   public void setCanOpenDoors(boolean flag) {
      this.nodeEvaluator.setCanOpenDoors(flag);
   }

   public boolean canPassDoors() {
      return this.nodeEvaluator.canPassDoors();
   }

   public void setCanPassDoors(boolean flag) {
      this.nodeEvaluator.setCanPassDoors(flag);
   }

   public boolean canOpenDoors() {
      return this.nodeEvaluator.canPassDoors();
   }

   public void setAvoidSun(boolean flag) {
      this.avoidSun = flag;
   }

   public void setCanWalkOverFences(boolean flag) {
      this.nodeEvaluator.setCanWalkOverFences(flag);
   }
}
