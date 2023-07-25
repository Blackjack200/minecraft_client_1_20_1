package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CopyMemoryWithExpiry {
   public static <E extends LivingEntity, T> BehaviorControl<E> create(Predicate<E> predicate, MemoryModuleType<? extends T> memorymoduletype, MemoryModuleType<T> memorymoduletype1, UniformInt uniformint) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(memorymoduletype), behaviorbuilder_instance.absent(memorymoduletype1)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i) -> {
               if (!predicate.test(livingentity)) {
                  return false;
               } else {
                  memoryaccessor1.setWithExpiry(behaviorbuilder_instance.get(memoryaccessor), (long)uniformint.sample(serverlevel.random));
                  return true;
               }
            }));
   }
}
