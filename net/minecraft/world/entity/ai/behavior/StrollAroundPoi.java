package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollAroundPoi {
   private static final int MIN_TIME_BETWEEN_STROLLS = 180;
   private static final int STROLL_MAX_XZ_DIST = 8;
   private static final int STROLL_MAX_Y_DIST = 6;

   public static OneShot<PathfinderMob> create(MemoryModuleType<GlobalPos> memorymoduletype, float f, int i) {
      MutableLong mutablelong = new MutableLong(0L);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, pathfindermob, i1) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor1);
               if (serverlevel.dimension() == globalpos.dimension() && globalpos.pos().closerToCenterThan(pathfindermob.position(), (double)i)) {
                  if (i1 <= mutablelong.getValue()) {
                     return true;
                  } else {
                     Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.getPos(pathfindermob, 8, 6));
                     memoryaccessor.setOrErase(optional.map((vec3) -> new WalkTarget(vec3, f, 1)));
                     mutablelong.setValue(i1 + 180L);
                     return true;
                  }
               } else {
                  return false;
               }
            }));
   }
}
