package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.point((serverlevel, livingentity, i) -> {
            livingentity.getBrain().updateActivityFromSchedule(serverlevel.getDayTime(), serverlevel.getGameTime());
            return true;
         }));
   }
}
