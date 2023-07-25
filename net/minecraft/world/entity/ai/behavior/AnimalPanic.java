package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic extends Behavior<PathfinderMob> {
   private static final int PANIC_MIN_DURATION = 100;
   private static final int PANIC_MAX_DURATION = 120;
   private static final int PANIC_DISTANCE_HORIZONTAL = 5;
   private static final int PANIC_DISTANCE_VERTICAL = 4;
   private static final Predicate<PathfinderMob> DEFAULT_SHOULD_PANIC_PREDICATE = (pathfindermob) -> pathfindermob.getLastHurtByMob() != null || pathfindermob.isFreezing() || pathfindermob.isOnFire();
   private final float speedMultiplier;
   private final Predicate<PathfinderMob> shouldPanic;

   public AnimalPanic(float f) {
      this(f, DEFAULT_SHOULD_PANIC_PREDICATE);
   }

   public AnimalPanic(float f, Predicate<PathfinderMob> predicate) {
      super(ImmutableMap.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
      this.speedMultiplier = f;
      this.shouldPanic = predicate;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, PathfinderMob pathfindermob) {
      return this.shouldPanic.test(pathfindermob);
   }

   protected boolean canStillUse(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      return true;
   }

   protected void start(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      pathfindermob.getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
      pathfindermob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
   }

   protected void stop(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      Brain<?> brain = pathfindermob.getBrain();
      brain.eraseMemory(MemoryModuleType.IS_PANICKING);
   }

   protected void tick(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      if (pathfindermob.getNavigation().isDone()) {
         Vec3 vec3 = this.getPanicPos(pathfindermob, serverlevel);
         if (vec3 != null) {
            pathfindermob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
         }
      }

   }

   @Nullable
   private Vec3 getPanicPos(PathfinderMob pathfindermob, ServerLevel serverlevel) {
      if (pathfindermob.isOnFire()) {
         Optional<Vec3> optional = this.lookForWater(serverlevel, pathfindermob).map(Vec3::atBottomCenterOf);
         if (optional.isPresent()) {
            return optional.get();
         }
      }

      return LandRandomPos.getPos(pathfindermob, 5, 4);
   }

   private Optional<BlockPos> lookForWater(BlockGetter blockgetter, Entity entity) {
      BlockPos blockpos = entity.blockPosition();
      if (!blockgetter.getBlockState(blockpos).getCollisionShape(blockgetter, blockpos).isEmpty()) {
         return Optional.empty();
      } else {
         Predicate<BlockPos> predicate;
         if (Mth.ceil(entity.getBbWidth()) == 2) {
            predicate = (blockpos2) -> BlockPos.squareOutSouthEast(blockpos2).allMatch((blockpos3) -> blockgetter.getFluidState(blockpos3).is(FluidTags.WATER));
         } else {
            predicate = (blockpos1) -> blockgetter.getFluidState(blockpos1).is(FluidTags.WATER);
         }

         return BlockPos.findClosestMatch(blockpos, 5, 1, predicate);
      }
   }
}
