package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DismountOrSkipMounting {
   public static <E extends LivingEntity> BehaviorControl<E> create(int i, BiPredicate<E, Entity> bipredicate) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.RIDE_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i1) -> {
               Entity entity = livingentity.getVehicle();
               Entity entity1 = behaviorbuilder_instance.<Entity>tryGet(memoryaccessor).orElse((Entity)null);
               if (entity == null && entity1 == null) {
                  return false;
               } else {
                  Entity entity2 = entity == null ? entity1 : entity;
                  if (isVehicleValid(livingentity, entity2, i) && !bipredicate.test(livingentity, entity2)) {
                     return false;
                  } else {
                     livingentity.stopRiding();
                     memoryaccessor.erase();
                     return true;
                  }
               }
            }));
   }

   private static boolean isVehicleValid(LivingEntity livingentity, Entity entity, int i) {
      return entity.isAlive() && entity.closerThan(livingentity, (double)i) && entity.level() == livingentity.level();
   }
}
