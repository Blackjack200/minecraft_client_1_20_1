package net.minecraft.world.entity.ai.behavior.warden;

import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TryToSniff {
   private static final IntProvider SNIFF_COOLDOWN = UniformInt.of(100, 200);

   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.IS_SNIFFING), behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.SNIFF_COOLDOWN), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_ATTACKABLE), behaviorbuilder_instance.absent(MemoryModuleType.DISTURBANCE_LOCATION)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3, memoryaccessor4) -> (serverlevel, livingentity, i) -> {
               memoryaccessor.set(Unit.INSTANCE);
               memoryaccessor2.setWithExpiry(Unit.INSTANCE, (long)SNIFF_COOLDOWN.sample(serverlevel.getRandom()));
               memoryaccessor1.erase();
               livingentity.setPose(Pose.SNIFFING);
               return true;
            }));
   }
}
