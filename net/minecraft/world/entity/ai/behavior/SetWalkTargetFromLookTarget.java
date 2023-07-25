package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
   public static OneShot<LivingEntity> create(float f, int i) {
      return create((livingentity1) -> true, (livingentity) -> f, i);
   }

   public static OneShot<LivingEntity> create(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> function, int i) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.LOOK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i1) -> {
               if (!predicate.test(livingentity)) {
                  return false;
               } else {
                  memoryaccessor.set(new WalkTarget(behaviorbuilder_instance.get(memoryaccessor1), function.apply(livingentity), i));
                  return true;
               }
            }));
   }
}
