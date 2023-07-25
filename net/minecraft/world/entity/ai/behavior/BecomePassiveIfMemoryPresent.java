package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Function3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BecomePassiveIfMemoryPresent {
   public static BehaviorControl<LivingEntity> create(MemoryModuleType<?> memorymoduletype, int i) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.group(behaviorbuilder_instance.registered(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.PACIFIED), behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, behaviorbuilder_instance.point(() -> "[BecomePassive if " + memorymoduletype + " present]", (Function3<MemoryAccessor, MemoryAccessor, MemoryAccessor, Trigger<LivingEntity>>)(memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, livingentity, i1) -> {
               memoryaccessor1.setWithExpiry(true, (long)i);
               memoryaccessor.erase();
               return true;
            })));
   }
}
