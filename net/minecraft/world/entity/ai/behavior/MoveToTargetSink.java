package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveToTargetSink extends Behavior<Mob> {
   private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
   private int remainingCooldown;
   @Nullable
   private Path path;
   @Nullable
   private BlockPos lastTargetPos;
   private float speedModifier;

   public MoveToTargetSink() {
      this(150, 250);
   }

   public MoveToTargetSink(int i, int j) {
      super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED, MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), i, j);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Mob mob) {
      if (this.remainingCooldown > 0) {
         --this.remainingCooldown;
         return false;
      } else {
         Brain<?> brain = mob.getBrain();
         WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
         boolean flag = this.reachedTarget(mob, walktarget);
         if (!flag && this.tryComputePath(mob, walktarget, serverlevel.getGameTime())) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            return true;
         } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            if (flag) {
               brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }

            return false;
         }
      }
   }

   protected boolean canStillUse(ServerLevel serverlevel, Mob mob, long i) {
      if (this.path != null && this.lastTargetPos != null) {
         Optional<WalkTarget> optional = mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
         boolean flag = optional.map(MoveToTargetSink::isWalkTargetSpectator).orElse(false);
         PathNavigation pathnavigation = mob.getNavigation();
         return !pathnavigation.isDone() && optional.isPresent() && !this.reachedTarget(mob, optional.get()) && !flag;
      } else {
         return false;
      }
   }

   protected void stop(ServerLevel serverlevel, Mob mob, long i) {
      if (mob.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(mob, mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get()) && mob.getNavigation().isStuck()) {
         this.remainingCooldown = serverlevel.getRandom().nextInt(40);
      }

      mob.getNavigation().stop();
      mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      mob.getBrain().eraseMemory(MemoryModuleType.PATH);
      this.path = null;
   }

   protected void start(ServerLevel serverlevel, Mob mob, long i) {
      mob.getBrain().setMemory(MemoryModuleType.PATH, this.path);
      mob.getNavigation().moveTo(this.path, (double)this.speedModifier);
   }

   protected void tick(ServerLevel serverlevel, Mob mob, long i) {
      Path path = mob.getNavigation().getPath();
      Brain<?> brain = mob.getBrain();
      if (this.path != path) {
         this.path = path;
         brain.setMemory(MemoryModuleType.PATH, path);
      }

      if (path != null && this.lastTargetPos != null) {
         WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
         if (walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D && this.tryComputePath(mob, walktarget, serverlevel.getGameTime())) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            this.start(serverlevel, mob, i);
         }

      }
   }

   private boolean tryComputePath(Mob mob, WalkTarget walktarget, long i) {
      BlockPos blockpos = walktarget.getTarget().currentBlockPosition();
      this.path = mob.getNavigation().createPath(blockpos, 0);
      this.speedModifier = walktarget.getSpeedModifier();
      Brain<?> brain = mob.getBrain();
      if (this.reachedTarget(mob, walktarget)) {
         brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      } else {
         boolean flag = this.path != null && this.path.canReach();
         if (flag) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, i);
         }

         if (this.path != null) {
            return true;
         }

         Vec3 vec3 = DefaultRandomPos.getPosTowards((PathfinderMob)mob, 10, 7, Vec3.atBottomCenterOf(blockpos), (double)((float)Math.PI / 2F));
         if (vec3 != null) {
            this.path = mob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
            return this.path != null;
         }
      }

      return false;
   }

   private boolean reachedTarget(Mob mob, WalkTarget walktarget) {
      return walktarget.getTarget().currentBlockPosition().distManhattan(mob.blockPosition()) <= walktarget.getCloseEnoughDist();
   }

   private static boolean isWalkTargetSpectator(WalkTarget walktarget) {
      PositionTracker positiontracker = walktarget.getTarget();
      if (positiontracker instanceof EntityTracker entitytracker) {
         return entitytracker.getEntity().isSpectator();
      } else {
         return false;
      }
   }
}
