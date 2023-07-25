package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface BehaviorControl<E extends LivingEntity> {
   Behavior.Status getStatus();

   boolean tryStart(ServerLevel serverlevel, E livingentity, long i);

   void tickOrStop(ServerLevel serverlevel, E livingentity, long i);

   void doStop(ServerLevel serverlevel, E livingentity, long i);

   String debugString();
}
