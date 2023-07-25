package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BabyFollowAdult {
   public static OneShot<AgeableMob> create(UniformInt uniformint, float f) {
      return create(uniformint, (livingentity) -> f);
   }

   public static OneShot<AgeableMob> create(UniformInt uniformint, Function<LivingEntity, Float> function) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_ADULT), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, ageablemob, i) -> {
               if (!ageablemob.isBaby()) {
                  return false;
               } else {
                  AgeableMob ageablemob1 = behaviorbuilder_instance.get(memoryaccessor);
                  if (ageablemob.closerThan(ageablemob1, (double)(uniformint.getMaxValue() + 1)) && !ageablemob.closerThan(ageablemob1, (double)uniformint.getMinValue())) {
                     WalkTarget walktarget = new WalkTarget(new EntityTracker(ageablemob1, false), function.apply(ageablemob), uniformint.getMinValue() - 1);
                     memoryaccessor1.set(new EntityTracker(ageablemob1, true));
                     memoryaccessor2.set(walktarget);
                     return true;
                  } else {
                     return false;
                  }
               }
            }));
   }
}
