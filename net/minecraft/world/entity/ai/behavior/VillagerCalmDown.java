package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerCalmDown {
   private static final int SAFE_DISTANCE_FROM_DANGER = 36;

   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.HURT_BY), behaviorbuilder_instance.registered(MemoryModuleType.HURT_BY_ENTITY), behaviorbuilder_instance.registered(MemoryModuleType.NEAREST_HOSTILE)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, livingentity, i) -> {
               boolean flag = behaviorbuilder_instance.tryGet(memoryaccessor).isPresent() || behaviorbuilder_instance.tryGet(memoryaccessor2).isPresent() || behaviorbuilder_instance.<LivingEntity>tryGet(memoryaccessor1).filter((livingentity2) -> livingentity2.distanceToSqr(livingentity) <= 36.0D).isPresent();
               if (!flag) {
                  memoryaccessor.erase();
                  memoryaccessor1.erase();
                  livingentity.getBrain().updateActivityFromSchedule(serverlevel.getDayTime(), serverlevel.getGameTime());
               }

               return true;
            }));
   }
}
