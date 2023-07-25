package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public abstract class OneShot<E extends LivingEntity> implements BehaviorControl<E>, Trigger<E> {
   private Behavior.Status status = Behavior.Status.STOPPED;

   public final Behavior.Status getStatus() {
      return this.status;
   }

   public final boolean tryStart(ServerLevel serverlevel, E livingentity, long i) {
      if (this.trigger(serverlevel, livingentity, i)) {
         this.status = Behavior.Status.RUNNING;
         return true;
      } else {
         return false;
      }
   }

   public final void tickOrStop(ServerLevel serverlevel, E livingentity, long i) {
      this.doStop(serverlevel, livingentity, i);
   }

   public final void doStop(ServerLevel serverlevel, E livingentity, long i) {
      this.status = Behavior.Status.STOPPED;
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }
}
