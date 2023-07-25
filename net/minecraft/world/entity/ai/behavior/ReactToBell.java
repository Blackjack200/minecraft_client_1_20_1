package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ReactToBell {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i) -> {
               Raid raid = serverlevel.getRaidAt(livingentity.blockPosition());
               if (raid == null) {
                  livingentity.getBrain().setActiveActivityIfPossible(Activity.HIDE);
               }

               return true;
            }));
   }
}
