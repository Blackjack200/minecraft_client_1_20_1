package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
   public static BehaviorControl<LivingEntity> create(Function<LivingEntity, Optional<PositionTracker>> function, Predicate<LivingEntity> predicate, int i, int j, float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i2) -> {
               Optional<PositionTracker> optional = function.apply(livingentity);
               if (!optional.isEmpty() && predicate.test(livingentity)) {
                  PositionTracker positiontracker = optional.get();
                  if (livingentity.position().closerThan(positiontracker.currentPosition(), (double)j)) {
                     return false;
                  } else {
                     PositionTracker positiontracker1 = optional.get();
                     memoryaccessor.set(positiontracker1);
                     memoryaccessor1.set(new WalkTarget(positiontracker1, f, i));
                     return true;
                  }
               } else {
                  return false;
               }
            }));
   }
}
