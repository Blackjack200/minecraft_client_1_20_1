package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
   public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> memorymoduletype, float f, int i, boolean flag) {
      return create(memorymoduletype, f, i, flag, Vec3::atBottomCenterOf);
   }

   public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> memorymoduletype, float f, int i, boolean flag) {
      return create(memorymoduletype, f, i, flag, Entity::position);
   }

   private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> memorymoduletype, float f, int i, boolean flag, Function<T, Vec3> function) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, pathfindermob, i1) -> {
               Optional<WalkTarget> optional = behaviorbuilder_instance.tryGet(memoryaccessor);
               if (optional.isPresent() && !flag) {
                  return false;
               } else {
                  Vec3 vec3 = pathfindermob.position();
                  Vec3 vec31 = function.apply(behaviorbuilder_instance.get(memoryaccessor1));
                  if (!vec3.closerThan(vec31, (double)i)) {
                     return false;
                  } else {
                     if (optional.isPresent() && optional.get().getSpeedModifier() == f) {
                        Vec3 vec32 = optional.get().getTarget().currentPosition().subtract(vec3);
                        Vec3 vec33 = vec31.subtract(vec3);
                        if (vec32.dot(vec33) < 0.0D) {
                           return false;
                        }
                     }

                     for(int j1 = 0; j1 < 10; ++j1) {
                        Vec3 vec34 = LandRandomPos.getPosAway(pathfindermob, 16, 7, vec31);
                        if (vec34 != null) {
                           memoryaccessor.set(new WalkTarget(vec34, f, 0));
                           break;
                        }
                     }

                     return true;
                  }
               }
            }));
   }
}
