package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableInt;

public class SetHiddenState {
   private static final int HIDE_TIMEOUT = 300;

   public static BehaviorControl<LivingEntity> create(int i, int j) {
      int k = i * 20;
      MutableInt mutableint = new MutableInt(0);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.HIDING_PLACE), behaviorbuilder_instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, j2) -> {
               long k2 = behaviorbuilder_instance.<Long>get(memoryaccessor1);
               boolean flag = k2 + 300L <= j2;
               if (mutableint.getValue() <= k && !flag) {
                  BlockPos blockpos = behaviorbuilder_instance.<GlobalPos>get(memoryaccessor).pos();
                  if (blockpos.closerThan(livingentity.blockPosition(), (double)j)) {
                     mutableint.increment();
                  }

                  return true;
               } else {
                  memoryaccessor1.erase();
                  memoryaccessor.erase();
                  livingentity.getBrain().updateActivityFromSchedule(serverlevel.getDayTime(), serverlevel.getGameTime());
                  mutableint.setValue(0);
                  return true;
               }
            }));
   }
}
