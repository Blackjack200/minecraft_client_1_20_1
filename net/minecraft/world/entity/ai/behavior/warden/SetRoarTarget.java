package net.minecraft.world.entity.ai.behavior.warden;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class SetRoarTarget {
   public static <E extends Warden> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> function) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.ROAR_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, warden, i) -> {
               Optional<? extends LivingEntity> optional = function.apply(warden);
               if (optional.filter(warden::canTargetEntity).isEmpty()) {
                  return false;
               } else {
                  memoryaccessor.set(optional.get());
                  memoryaccessor2.erase();
                  return true;
               }
            }));
   }
}
