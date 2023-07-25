package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StartCelebratingIfTargetDead {
   public static BehaviorControl<LivingEntity> create(int i, BiPredicate<LivingEntity, LivingEntity> bipredicate) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.ANGRY_AT), behaviorbuilder_instance.absent(MemoryModuleType.CELEBRATE_LOCATION), behaviorbuilder_instance.registered(MemoryModuleType.DANCING)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, livingentity, i1) -> {
               LivingEntity livingentity1 = behaviorbuilder_instance.get(memoryaccessor);
               if (!livingentity1.isDeadOrDying()) {
                  return false;
               } else {
                  if (bipredicate.test(livingentity, livingentity1)) {
                     memoryaccessor3.setWithExpiry(true, (long)i);
                  }

                  memoryaccessor2.setWithExpiry(livingentity1.blockPosition(), (long)i);
                  if (livingentity1.getType() != EntityType.PLAYER || serverlevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
                     memoryaccessor.erase();
                     memoryaccessor1.erase();
                  }

                  return true;
               }
            }));
   }
}
