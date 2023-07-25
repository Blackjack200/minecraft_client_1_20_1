package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAdmiringIfTiredOfTryingToReachItem {
   public static BehaviorControl<LivingEntity> create(int i, int j) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.ADMIRING_ITEM), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), behaviorbuilder_instance.registered(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM), behaviorbuilder_instance.registered(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, livingentity, i2) -> {
               if (!livingentity.getOffhandItem().isEmpty()) {
                  return false;
               } else {
                  Optional<Integer> optional = behaviorbuilder_instance.tryGet(memoryaccessor2);
                  if (optional.isEmpty()) {
                     memoryaccessor2.set(0);
                  } else {
                     int j2 = optional.get();
                     if (j2 > i) {
                        memoryaccessor.erase();
                        memoryaccessor2.erase();
                        memoryaccessor3.setWithExpiry(true, (long)j);
                     } else {
                        memoryaccessor2.set(j2 + 1);
                     }
                  }

                  return true;
               }
            }));
   }
}
