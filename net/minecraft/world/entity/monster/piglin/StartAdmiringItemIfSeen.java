package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StartAdmiringItemIfSeen {
   public static BehaviorControl<LivingEntity> create(int i) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), behaviorbuilder_instance.absent(MemoryModuleType.ADMIRING_ITEM), behaviorbuilder_instance.absent(MemoryModuleType.ADMIRING_DISABLED), behaviorbuilder_instance.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, livingentity, i1) -> {
               ItemEntity itementity = behaviorbuilder_instance.get(memoryaccessor);
               if (!PiglinAi.isLovedItem(itementity.getItem())) {
                  return false;
               } else {
                  memoryaccessor1.setWithExpiry(true, (long)i);
                  return true;
               }
            }));
   }
}
