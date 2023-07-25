package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Items;

public class StopHoldingItemIfNoLongerAdmiring {
   public static BehaviorControl<Piglin> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.ADMIRING_ITEM)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, piglin, i) -> {
               if (!piglin.getOffhandItem().isEmpty() && !piglin.getOffhandItem().is(Items.SHIELD)) {
                  PiglinAi.stopHoldingOffHandItem(piglin, true);
                  return true;
               } else {
                  return false;
               }
            }));
   }
}
