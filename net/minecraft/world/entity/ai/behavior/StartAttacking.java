package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
   public static <E extends Mob> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> function) {
      return create((mob) -> true, function);
   }

   public static <E extends Mob> BehaviorControl<E> create(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, mob, i) -> {
               if (!predicate.test(mob)) {
                  return false;
               } else {
                  Optional<? extends LivingEntity> optional = function.apply(mob);
                  if (optional.isEmpty()) {
                     return false;
                  } else {
                     LivingEntity livingentity = optional.get();
                     if (!mob.canAttack(livingentity)) {
                        return false;
                     } else {
                        memoryaccessor.set(livingentity);
                        memoryaccessor1.erase();
                        return true;
                     }
                  }
               }
            }));
   }
}
