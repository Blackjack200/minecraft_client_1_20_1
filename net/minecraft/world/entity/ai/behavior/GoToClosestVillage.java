package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage {
   public static BehaviorControl<Villager> create(float f, int i) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, villager, i1) -> {
               if (serverlevel.isVillage(villager.blockPosition())) {
                  return false;
               } else {
                  PoiManager poimanager = serverlevel.getPoiManager();
                  int j1 = poimanager.sectionsToVillage(SectionPos.of(villager.blockPosition()));
                  Vec3 vec3 = null;

                  for(int k1 = 0; k1 < 5; ++k1) {
                     Vec3 vec31 = LandRandomPos.getPos(villager, 15, 7, (blockpos) -> (double)(-poimanager.sectionsToVillage(SectionPos.of(blockpos))));
                     if (vec31 != null) {
                        int l1 = poimanager.sectionsToVillage(SectionPos.of(BlockPos.containing(vec31)));
                        if (l1 < j1) {
                           vec3 = vec31;
                           break;
                        }

                        if (l1 == j1) {
                           vec3 = vec31;
                        }
                     }
                  }

                  if (vec3 != null) {
                     memoryaccessor.set(new WalkTarget(vec3, f, i));
                  }

                  return true;
               }
            }));
   }
}
