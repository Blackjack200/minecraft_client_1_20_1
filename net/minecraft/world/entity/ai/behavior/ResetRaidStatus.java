package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ResetRaidStatus {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.point((serverlevel, livingentity, i) -> {
            if (serverlevel.random.nextInt(20) != 0) {
               return false;
            } else {
               Brain<?> brain = livingentity.getBrain();
               Raid raid = serverlevel.getRaidAt(livingentity.blockPosition());
               if (raid == null || raid.isStopped() || raid.isLoss()) {
                  brain.setDefaultActivity(Activity.IDLE);
                  brain.updateActivityFromSchedule(serverlevel.getDayTime(), serverlevel.getGameTime());
               }

               return true;
            }
         }));
   }
}
