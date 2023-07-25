package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class ForceUnmount extends Behavior<LivingEntity> {
   public ForceUnmount() {
      super(ImmutableMap.of());
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, LivingEntity livingentity) {
      return livingentity.isPassenger();
   }

   protected void start(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      livingentity.unRide();
   }
}
