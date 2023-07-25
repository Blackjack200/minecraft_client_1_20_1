package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public abstract class Behavior<E extends LivingEntity> implements BehaviorControl<E> {
   public static final int DEFAULT_DURATION = 60;
   protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
   private Behavior.Status status = Behavior.Status.STOPPED;
   private long endTimestamp;
   private final int minDuration;
   private final int maxDuration;

   public Behavior(Map<MemoryModuleType<?>, MemoryStatus> map) {
      this(map, 60);
   }

   public Behavior(Map<MemoryModuleType<?>, MemoryStatus> map, int i) {
      this(map, i, i);
   }

   public Behavior(Map<MemoryModuleType<?>, MemoryStatus> map, int i, int j) {
      this.minDuration = i;
      this.maxDuration = j;
      this.entryCondition = map;
   }

   public Behavior.Status getStatus() {
      return this.status;
   }

   public final boolean tryStart(ServerLevel serverlevel, E livingentity, long i) {
      if (this.hasRequiredMemories(livingentity) && this.checkExtraStartConditions(serverlevel, livingentity)) {
         this.status = Behavior.Status.RUNNING;
         int j = this.minDuration + serverlevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
         this.endTimestamp = i + (long)j;
         this.start(serverlevel, livingentity, i);
         return true;
      } else {
         return false;
      }
   }

   protected void start(ServerLevel serverlevel, E livingentity, long i) {
   }

   public final void tickOrStop(ServerLevel serverlevel, E livingentity, long i) {
      if (!this.timedOut(i) && this.canStillUse(serverlevel, livingentity, i)) {
         this.tick(serverlevel, livingentity, i);
      } else {
         this.doStop(serverlevel, livingentity, i);
      }

   }

   protected void tick(ServerLevel serverlevel, E livingentity, long i) {
   }

   public final void doStop(ServerLevel serverlevel, E livingentity, long i) {
      this.status = Behavior.Status.STOPPED;
      this.stop(serverlevel, livingentity, i);
   }

   protected void stop(ServerLevel serverlevel, E livingentity, long i) {
   }

   protected boolean canStillUse(ServerLevel serverlevel, E livingentity, long i) {
      return false;
   }

   protected boolean timedOut(long i) {
      return i > this.endTimestamp;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, E livingentity) {
      return true;
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }

   protected boolean hasRequiredMemories(E livingentity) {
      for(Map.Entry<MemoryModuleType<?>, MemoryStatus> map_entry : this.entryCondition.entrySet()) {
         MemoryModuleType<?> memorymoduletype = map_entry.getKey();
         MemoryStatus memorystatus = map_entry.getValue();
         if (!livingentity.getBrain().checkMemory(memorymoduletype, memorystatus)) {
            return false;
         }
      }

      return true;
   }

   public static enum Status {
      STOPPED,
      RUNNING;
   }
}
