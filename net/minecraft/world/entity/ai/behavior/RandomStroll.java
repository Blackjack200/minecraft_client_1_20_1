package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll {
   private static final int MAX_XZ_DIST = 10;
   private static final int MAX_Y_DIST = 7;
   private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

   public static OneShot<PathfinderMob> stroll(float f) {
      return stroll(f, true);
   }

   public static OneShot<PathfinderMob> stroll(float f, boolean flag) {
      return strollFlyOrSwim(f, (pathfindermob2) -> LandRandomPos.getPos(pathfindermob2, 10, 7), flag ? (pathfindermob1) -> true : (pathfindermob) -> !pathfindermob.isInWaterOrBubble());
   }

   public static BehaviorControl<PathfinderMob> stroll(float f, int i, int j) {
      return strollFlyOrSwim(f, (pathfindermob1) -> LandRandomPos.getPos(pathfindermob1, i, j), (pathfindermob) -> true);
   }

   public static BehaviorControl<PathfinderMob> fly(float f) {
      return strollFlyOrSwim(f, (pathfindermob1) -> getTargetFlyPos(pathfindermob1, 10, 7), (pathfindermob) -> true);
   }

   public static BehaviorControl<PathfinderMob> swim(float f) {
      return strollFlyOrSwim(f, RandomStroll::getTargetSwimPos, Entity::isInWaterOrBubble);
   }

   private static OneShot<PathfinderMob> strollFlyOrSwim(float f, Function<PathfinderMob, Vec3> function, Predicate<PathfinderMob> predicate) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, pathfindermob, i) -> {
               if (!predicate.test(pathfindermob)) {
                  return false;
               } else {
                  Optional<Vec3> optional = Optional.ofNullable(function.apply(pathfindermob));
                  memoryaccessor.setOrErase(optional.map((vec3) -> new WalkTarget(vec3, f, 0)));
                  return true;
               }
            }));
   }

   @Nullable
   private static Vec3 getTargetSwimPos(PathfinderMob pathfindermob) {
      Vec3 vec3 = null;
      Vec3 vec31 = null;

      for(int[] aint : SWIM_XY_DISTANCE_TIERS) {
         if (vec3 == null) {
            vec31 = BehaviorUtils.getRandomSwimmablePos(pathfindermob, aint[0], aint[1]);
         } else {
            vec31 = pathfindermob.position().add(pathfindermob.position().vectorTo(vec3).normalize().multiply((double)aint[0], (double)aint[1], (double)aint[0]));
         }

         if (vec31 == null || pathfindermob.level().getFluidState(BlockPos.containing(vec31)).isEmpty()) {
            return vec3;
         }

         vec3 = vec31;
      }

      return vec31;
   }

   @Nullable
   private static Vec3 getTargetFlyPos(PathfinderMob pathfindermob, int i, int j) {
      Vec3 vec3 = pathfindermob.getViewVector(0.0F);
      return AirAndWaterRandomPos.getPos(pathfindermob, i, j, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
   }
}
