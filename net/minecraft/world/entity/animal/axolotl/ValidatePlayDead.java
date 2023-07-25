package net.minecraft.world.entity.animal.axolotl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ValidatePlayDead {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.PLAY_DEAD_TICKS), behaviorbuilder_instance.registered(MemoryModuleType.HURT_BY_ENTITY)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i) -> {
               int j = behaviorbuilder_instance.<Integer>get(memoryaccessor);
               if (j <= 0) {
                  memoryaccessor.erase();
                  memoryaccessor1.erase();
                  livingentity.getBrain().useDefaultActivity();
               } else {
                  memoryaccessor.set(j - 1);
               }

               return true;
            }));
   }
}
