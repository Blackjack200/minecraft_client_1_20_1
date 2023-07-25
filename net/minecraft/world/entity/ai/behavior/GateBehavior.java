package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity> implements BehaviorControl<E> {
   private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
   private final Set<MemoryModuleType<?>> exitErasedMemories;
   private final GateBehavior.OrderPolicy orderPolicy;
   private final GateBehavior.RunningPolicy runningPolicy;
   private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();
   private Behavior.Status status = Behavior.Status.STOPPED;

   public GateBehavior(Map<MemoryModuleType<?>, MemoryStatus> map, Set<MemoryModuleType<?>> set, GateBehavior.OrderPolicy gatebehavior_orderpolicy, GateBehavior.RunningPolicy gatebehavior_runningpolicy, List<Pair<? extends BehaviorControl<? super E>, Integer>> list) {
      this.entryCondition = map;
      this.exitErasedMemories = set;
      this.orderPolicy = gatebehavior_orderpolicy;
      this.runningPolicy = gatebehavior_runningpolicy;
      list.forEach((pair) -> this.behaviors.add(pair.getFirst(), pair.getSecond()));
   }

   public Behavior.Status getStatus() {
      return this.status;
   }

   private boolean hasRequiredMemories(E livingentity) {
      for(Map.Entry<MemoryModuleType<?>, MemoryStatus> map_entry : this.entryCondition.entrySet()) {
         MemoryModuleType<?> memorymoduletype = map_entry.getKey();
         MemoryStatus memorystatus = map_entry.getValue();
         if (!livingentity.getBrain().checkMemory(memorymoduletype, memorystatus)) {
            return false;
         }
      }

      return true;
   }

   public final boolean tryStart(ServerLevel serverlevel, E livingentity, long i) {
      if (this.hasRequiredMemories(livingentity)) {
         this.status = Behavior.Status.RUNNING;
         this.orderPolicy.apply(this.behaviors);
         this.runningPolicy.apply(this.behaviors.stream(), serverlevel, livingentity, i);
         return true;
      } else {
         return false;
      }
   }

   public final void tickOrStop(ServerLevel serverlevel, E livingentity, long i) {
      this.behaviors.stream().filter((behaviorcontrol2) -> behaviorcontrol2.getStatus() == Behavior.Status.RUNNING).forEach((behaviorcontrol1) -> behaviorcontrol1.tickOrStop(serverlevel, livingentity, i));
      if (this.behaviors.stream().noneMatch((behaviorcontrol) -> behaviorcontrol.getStatus() == Behavior.Status.RUNNING)) {
         this.doStop(serverlevel, livingentity, i);
      }

   }

   public final void doStop(ServerLevel serverlevel, E livingentity, long i) {
      this.status = Behavior.Status.STOPPED;
      this.behaviors.stream().filter((behaviorcontrol1) -> behaviorcontrol1.getStatus() == Behavior.Status.RUNNING).forEach((behaviorcontrol) -> behaviorcontrol.doStop(serverlevel, livingentity, i));
      this.exitErasedMemories.forEach(livingentity.getBrain()::eraseMemory);
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }

   public String toString() {
      Set<? extends BehaviorControl<? super E>> set = this.behaviors.stream().filter((behaviorcontrol) -> behaviorcontrol.getStatus() == Behavior.Status.RUNNING).collect(Collectors.toSet());
      return "(" + this.getClass().getSimpleName() + "): " + set;
   }

   public static enum OrderPolicy {
      ORDERED((shufflinglist) -> {
      }),
      SHUFFLED(ShufflingList::shuffle);

      private final Consumer<ShufflingList<?>> consumer;

      private OrderPolicy(Consumer<ShufflingList<?>> consumer) {
         this.consumer = consumer;
      }

      public void apply(ShufflingList<?> shufflinglist) {
         this.consumer.accept(shufflinglist);
      }
   }

   public static enum RunningPolicy {
      RUN_ONE {
         public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverlevel, E livingentity, long i) {
            stream.filter((behaviorcontrol1) -> behaviorcontrol1.getStatus() == Behavior.Status.STOPPED).filter((behaviorcontrol) -> behaviorcontrol.tryStart(serverlevel, livingentity, i)).findFirst();
         }
      },
      TRY_ALL {
         public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverlevel, E livingentity, long i) {
            stream.filter((behaviorcontrol1) -> behaviorcontrol1.getStatus() == Behavior.Status.STOPPED).forEach((behaviorcontrol) -> behaviorcontrol.tryStart(serverlevel, livingentity, i));
         }
      };

      public abstract <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverlevel, E livingentity, long i);
   }
}
