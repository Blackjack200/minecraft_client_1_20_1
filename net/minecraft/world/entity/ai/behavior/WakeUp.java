package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.point((serverlevel, livingentity, i) -> {
            if (!livingentity.getBrain().isActive(Activity.REST) && livingentity.isSleeping()) {
               livingentity.stopSleeping();
               return true;
            } else {
               return false;
            }
         }));
   }
}
