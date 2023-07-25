package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoi {
   public static BehaviorControl<PathfinderMob> create(MemoryModuleType<GlobalPos> memorymoduletype, float f, int i, int j) {
      MutableLong mutablelong = new MutableLong(0L);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, pathfindermob, i2) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor1);
               if (serverlevel.dimension() == globalpos.dimension() && globalpos.pos().closerToCenterThan(pathfindermob.position(), (double)j)) {
                  if (i2 <= mutablelong.getValue()) {
                     return true;
                  } else {
                     memoryaccessor.set(new WalkTarget(globalpos.pos(), f, i));
                     mutablelong.setValue(i2 + 80L);
                     return true;
                  }
               } else {
                  return false;
               }
            }));
   }
}
