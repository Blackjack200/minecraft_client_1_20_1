package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {
   public static <E extends LivingEntity> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> list) {
      return triggerGate(list, GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE);
   }

   public static <E extends LivingEntity> OneShot<E> triggerGate(List<Pair<? extends Trigger<? super E>, Integer>> list, GateBehavior.OrderPolicy gatebehavior_orderpolicy, GateBehavior.RunningPolicy gatebehavior_runningpolicy) {
      ShufflingList<Trigger<? super E>> shufflinglist = new ShufflingList<>();
      list.forEach((pair) -> shufflinglist.add(pair.getFirst(), pair.getSecond()));
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.point((serverlevel, livingentity, i) -> {
            if (gatebehavior_orderpolicy == GateBehavior.OrderPolicy.SHUFFLED) {
               shufflinglist.shuffle();
            }

            for(Trigger<? super E> trigger : shufflinglist) {
               if (trigger.trigger(serverlevel, livingentity, i) && gatebehavior_runningpolicy == GateBehavior.RunningPolicy.RUN_ONE) {
                  break;
               }
            }

            return true;
         }));
   }
}
