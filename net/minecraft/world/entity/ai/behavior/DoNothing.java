package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class DoNothing implements BehaviorControl<LivingEntity> {
   private final int minDuration;
   private final int maxDuration;
   private Behavior.Status status = Behavior.Status.STOPPED;
   private long endTimestamp;

   public DoNothing(int i, int j) {
      this.minDuration = i;
      this.maxDuration = j;
   }

   public Behavior.Status getStatus() {
      return this.status;
   }

   public final boolean tryStart(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      this.status = Behavior.Status.RUNNING;
      int j = this.minDuration + serverlevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
      this.endTimestamp = i + (long)j;
      return true;
   }

   public final void tickOrStop(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      if (i > this.endTimestamp) {
         this.doStop(serverlevel, livingentity, i);
      }

   }

   public final void doStop(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      this.status = Behavior.Status.STOPPED;
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }
}
