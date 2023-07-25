package net.minecraft.world.entity.ai.navigation;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class WallClimberNavigation extends GroundPathNavigation {
   @Nullable
   private BlockPos pathToPosition;

   public WallClimberNavigation(Mob mob, Level level) {
      super(mob, level);
   }

   public Path createPath(BlockPos blockpos, int i) {
      this.pathToPosition = blockpos;
      return super.createPath(blockpos, i);
   }

   public Path createPath(Entity entity, int i) {
      this.pathToPosition = entity.blockPosition();
      return super.createPath(entity, i);
   }

   public boolean moveTo(Entity entity, double d0) {
      Path path = this.createPath(entity, 0);
      if (path != null) {
         return this.moveTo(path, d0);
      } else {
         this.pathToPosition = entity.blockPosition();
         this.speedModifier = d0;
         return true;
      }
   }

   public void tick() {
      if (!this.isDone()) {
         super.tick();
      } else {
         if (this.pathToPosition != null) {
            if (!this.pathToPosition.closerToCenterThan(this.mob.position(), (double)this.mob.getBbWidth()) && (!(this.mob.getY() > (double)this.pathToPosition.getY()) || !BlockPos.containing((double)this.pathToPosition.getX(), this.mob.getY(), (double)this.pathToPosition.getZ()).closerToCenterThan(this.mob.position(), (double)this.mob.getBbWidth()))) {
               this.mob.getMoveControl().setWantedPosition((double)this.pathToPosition.getX(), (double)this.pathToPosition.getY(), (double)this.pathToPosition.getZ(), this.speedModifier);
            } else {
               this.pathToPosition = null;
            }
         }

      }
   }
}
