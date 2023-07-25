package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> {
   public static BehaviorControl<LivingEntity> create(int i) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.ADMIRING_ITEM), behaviorbuilder_instance.registered(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i1) -> {
               if (!livingentity.getOffhandItem().isEmpty()) {
                  return false;
               } else {
                  Optional<ItemEntity> optional = behaviorbuilder_instance.tryGet(memoryaccessor1);
                  if (optional.isPresent() && optional.get().closerThan(livingentity, (double)i)) {
                     return false;
                  } else {
                     memoryaccessor.erase();
                     return true;
                  }
               }
            }));
   }
}
