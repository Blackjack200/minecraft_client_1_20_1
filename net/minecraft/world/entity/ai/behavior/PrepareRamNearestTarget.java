package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class PrepareRamNearestTarget<E extends PathfinderMob> extends Behavior<E> {
   public static final int TIME_OUT_DURATION = 160;
   private final ToIntFunction<E> getCooldownOnFail;
   private final int minRamDistance;
   private final int maxRamDistance;
   private final float walkSpeed;
   private final TargetingConditions ramTargeting;
   private final int ramPrepareTime;
   private final Function<E, SoundEvent> getPrepareRamSound;
   private Optional<Long> reachedRamPositionTimestamp = Optional.empty();
   private Optional<PrepareRamNearestTarget.RamCandidate> ramCandidate = Optional.empty();

   public PrepareRamNearestTarget(ToIntFunction<E> tointfunction, int i, int j, float f, TargetingConditions targetingconditions, int k, Function<E, SoundEvent> function) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT), 160);
      this.getCooldownOnFail = tointfunction;
      this.minRamDistance = i;
      this.maxRamDistance = j;
      this.walkSpeed = f;
      this.ramTargeting = targetingconditions;
      this.ramPrepareTime = k;
      this.getPrepareRamSound = function;
   }

   protected void start(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      Brain<?> brain = pathfindermob.getBrain();
      brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap((nearestvisiblelivingentities) -> nearestvisiblelivingentities.findClosest((livingentity1) -> this.ramTargeting.test(pathfindermob, livingentity1))).ifPresent((livingentity) -> this.chooseRamPosition(pathfindermob, livingentity));
   }

   protected void stop(ServerLevel serverlevel, E pathfindermob, long i) {
      Brain<?> brain = pathfindermob.getBrain();
      if (!brain.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
         serverlevel.broadcastEntityEvent(pathfindermob, (byte)59);
         brain.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getCooldownOnFail.applyAsInt(pathfindermob));
      }

   }

   protected boolean canStillUse(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      return this.ramCandidate.isPresent() && this.ramCandidate.get().getTarget().isAlive();
   }

   protected void tick(ServerLevel serverlevel, E pathfindermob, long i) {
      if (!this.ramCandidate.isEmpty()) {
         pathfindermob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ramCandidate.get().getStartPosition(), this.walkSpeed, 0));
         pathfindermob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.ramCandidate.get().getTarget(), true));
         boolean flag = !this.ramCandidate.get().getTarget().blockPosition().equals(this.ramCandidate.get().getTargetPosition());
         if (flag) {
            serverlevel.broadcastEntityEvent(pathfindermob, (byte)59);
            pathfindermob.getNavigation().stop();
            this.chooseRamPosition(pathfindermob, (this.ramCandidate.get()).target);
         } else {
            BlockPos blockpos = pathfindermob.blockPosition();
            if (blockpos.equals(this.ramCandidate.get().getStartPosition())) {
               serverlevel.broadcastEntityEvent(pathfindermob, (byte)58);
               if (this.reachedRamPositionTimestamp.isEmpty()) {
                  this.reachedRamPositionTimestamp = Optional.of(i);
               }

               if (i - this.reachedRamPositionTimestamp.get() >= (long)this.ramPrepareTime) {
                  pathfindermob.getBrain().setMemory(MemoryModuleType.RAM_TARGET, this.getEdgeOfBlock(blockpos, this.ramCandidate.get().getTargetPosition()));
                  serverlevel.playSound((Player)null, pathfindermob, this.getPrepareRamSound.apply(pathfindermob), SoundSource.NEUTRAL, 1.0F, pathfindermob.getVoicePitch());
                  this.ramCandidate = Optional.empty();
               }
            }
         }

      }
   }

   private Vec3 getEdgeOfBlock(BlockPos blockpos, BlockPos blockpos1) {
      double d0 = 0.5D;
      double d1 = 0.5D * (double)Mth.sign((double)(blockpos1.getX() - blockpos.getX()));
      double d2 = 0.5D * (double)Mth.sign((double)(blockpos1.getZ() - blockpos.getZ()));
      return Vec3.atBottomCenterOf(blockpos1).add(d1, 0.0D, d2);
   }

   private Optional<BlockPos> calculateRammingStartPosition(PathfinderMob pathfindermob, LivingEntity livingentity) {
      BlockPos blockpos = livingentity.blockPosition();
      if (!this.isWalkableBlock(pathfindermob, blockpos)) {
         return Optional.empty();
      } else {
         List<BlockPos> list = Lists.newArrayList();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            blockpos_mutableblockpos.set(blockpos);

            for(int i = 0; i < this.maxRamDistance; ++i) {
               if (!this.isWalkableBlock(pathfindermob, blockpos_mutableblockpos.move(direction))) {
                  blockpos_mutableblockpos.move(direction.getOpposite());
                  break;
               }
            }

            if (blockpos_mutableblockpos.distManhattan(blockpos) >= this.minRamDistance) {
               list.add(blockpos_mutableblockpos.immutable());
            }
         }

         PathNavigation pathnavigation = pathfindermob.getNavigation();
         return list.stream().sorted(Comparator.comparingDouble(pathfindermob.blockPosition()::distSqr)).filter((blockpos1) -> {
            Path path = pathnavigation.createPath(blockpos1, 0);
            return path != null && path.canReach();
         }).findFirst();
      }
   }

   private boolean isWalkableBlock(PathfinderMob pathfindermob, BlockPos blockpos) {
      return pathfindermob.getNavigation().isStableDestination(blockpos) && pathfindermob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pathfindermob.level(), blockpos.mutable())) == 0.0F;
   }

   private void chooseRamPosition(PathfinderMob pathfindermob, LivingEntity livingentity) {
      this.reachedRamPositionTimestamp = Optional.empty();
      this.ramCandidate = this.calculateRammingStartPosition(pathfindermob, livingentity).map((blockpos) -> new PrepareRamNearestTarget.RamCandidate(blockpos, livingentity.blockPosition(), livingentity));
   }

   public static class RamCandidate {
      private final BlockPos startPosition;
      private final BlockPos targetPosition;
      final LivingEntity target;

      public RamCandidate(BlockPos blockpos, BlockPos blockpos1, LivingEntity livingentity) {
         this.startPosition = blockpos;
         this.targetPosition = blockpos1;
         this.target = livingentity;
      }

      public BlockPos getStartPosition() {
         return this.startPosition;
      }

      public BlockPos getTargetPosition() {
         return this.targetPosition;
      }

      public LivingEntity getTarget() {
         return this.target;
      }
   }
}
