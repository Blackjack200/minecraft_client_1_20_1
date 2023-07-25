package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoiList {
   public static BehaviorControl<Villager> create(MemoryModuleType<List<GlobalPos>> memorymoduletype, float f, int i, int j, MemoryModuleType<GlobalPos> memorymoduletype1) {
      MutableLong mutablelong = new MutableLong(0L);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(memorymoduletype), behaviorbuilder_instance.present(memorymoduletype1)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, villager, i2) -> {
               List<GlobalPos> list = behaviorbuilder_instance.get(memoryaccessor1);
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor2);
               if (list.isEmpty()) {
                  return false;
               } else {
                  GlobalPos globalpos1 = list.get(serverlevel.getRandom().nextInt(list.size()));
                  if (globalpos1 != null && serverlevel.dimension() == globalpos1.dimension() && globalpos.pos().closerToCenterThan(villager.position(), (double)j)) {
                     if (i2 > mutablelong.getValue()) {
                        memoryaccessor.set(new WalkTarget(globalpos1.pos(), f, i));
                        mutablelong.setValue(i2 + 100L);
                     }

                     return true;
                  } else {
                     return false;
                  }
               }
            }));
   }
}
