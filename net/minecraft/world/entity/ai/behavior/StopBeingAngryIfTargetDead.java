package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StopBeingAngryIfTargetDead {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.ANGRY_AT)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i) -> {
               Optional.ofNullable(serverlevel.getEntity(behaviorbuilder_instance.get(memoryaccessor))).map((entity) -> {
                  LivingEntity var10000;
                  if (entity instanceof LivingEntity livingentity3) {
                     var10000 = livingentity3;
                  } else {
                     var10000 = null;
                  }

                  return var10000;
               }).filter(LivingEntity::isDeadOrDying).filter((livingentity2) -> livingentity2.getType() != EntityType.PLAYER || serverlevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)).ifPresent((livingentity1) -> memoryaccessor.erase());
               return true;
            }));
   }
}
