package net.minecraft.world.entity.ai.behavior.warden;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class SetWardenLookTarget {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.DISTURBANCE_LOCATION), behaviorbuilder_instance.registered(MemoryModuleType.ROAR_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, livingentity, i) -> {
               Optional<BlockPos> optional = behaviorbuilder_instance.<LivingEntity>tryGet(memoryaccessor2).map(Entity::blockPosition).or(() -> behaviorbuilder_instance.tryGet(memoryaccessor1));
               if (optional.isEmpty()) {
                  return false;
               } else {
                  memoryaccessor.set(new BlockPosTracker(optional.get()));
                  return true;
               }
            }));
   }
}
