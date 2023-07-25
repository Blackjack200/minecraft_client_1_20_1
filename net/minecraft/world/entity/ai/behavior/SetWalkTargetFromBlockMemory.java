package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory {
   public static OneShot<Villager> create(MemoryModuleType<GlobalPos> memorymoduletype, float f, int i, int j, int k) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, villager, i3) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor2);
               Optional<Long> optional = behaviorbuilder_instance.tryGet(memoryaccessor);
               if (globalpos.dimension() == serverlevel.dimension() && (!optional.isPresent() || serverlevel.getGameTime() - optional.get() <= (long)k)) {
                  if (globalpos.pos().distManhattan(villager.blockPosition()) > j) {
                     Vec3 vec3 = null;
                     int j3 = 0;
                     int k3 = 1000;

                     while(vec3 == null || BlockPos.containing(vec3).distManhattan(villager.blockPosition()) > j) {
                        vec3 = DefaultRandomPos.getPosTowards(villager, 15, 7, Vec3.atBottomCenterOf(globalpos.pos()), (double)((float)Math.PI / 2F));
                        ++j3;
                        if (j3 == 1000) {
                           villager.releasePoi(memorymoduletype);
                           memoryaccessor2.erase();
                           memoryaccessor.set(i3);
                           return true;
                        }
                     }

                     memoryaccessor1.set(new WalkTarget(vec3, f, i));
                  } else if (globalpos.pos().distManhattan(villager.blockPosition()) > i) {
                     memoryaccessor1.set(new WalkTarget(globalpos.pos(), f, i));
                  }
               } else {
                  villager.releasePoi(memorymoduletype);
                  memoryaccessor2.erase();
                  memoryaccessor.set(i3);
               }

               return true;
            }));
   }
}
