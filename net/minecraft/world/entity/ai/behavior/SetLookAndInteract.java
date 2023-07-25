package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract {
   public static BehaviorControl<LivingEntity> create(EntityType<?> entitytype, int i) {
      int j = i * i;
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.INTERACTION_TARGET), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, livingentity, j1) -> {
               Optional<LivingEntity> optional = behaviorbuilder_instance.<NearestVisibleLivingEntities>get(memoryaccessor2).findClosest((livingentity3) -> livingentity3.distanceToSqr(livingentity) <= (double)j && entitytype.equals(livingentity3.getType()));
               if (optional.isEmpty()) {
                  return false;
               } else {
                  LivingEntity livingentity1 = optional.get();
                  memoryaccessor1.set(livingentity1);
                  memoryaccessor.set(new EntityTracker(livingentity1, true));
                  return true;
               }
            }));
   }
}
