package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class Mount {
   private static final int CLOSE_ENOUGH_TO_START_RIDING_DIST = 1;

   public static BehaviorControl<LivingEntity> create(float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.RIDE_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, livingentity, i) -> {
               if (livingentity.isPassenger()) {
                  return false;
               } else {
                  Entity entity = behaviorbuilder_instance.get(memoryaccessor2);
                  if (entity.closerThan(livingentity, 1.0D)) {
                     livingentity.startRiding(entity);
                  } else {
                     memoryaccessor.set(new EntityTracker(entity, true));
                     memoryaccessor1.set(new WalkTarget(new EntityTracker(entity, false), f, 1));
                  }

                  return true;
               }
            }));
   }
}
