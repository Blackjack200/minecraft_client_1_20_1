package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class EraseMemoryIf {
   public static <E extends LivingEntity> BehaviorControl<E> create(Predicate<E> predicate, MemoryModuleType<?> memorymoduletype) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i) -> {
               if (predicate.test(livingentity)) {
                  memoryaccessor.erase();
                  return true;
               } else {
                  return false;
               }
            }));
   }
}
