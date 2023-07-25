package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll {
   private static final int MAX_XZ_DIST = 10;
   private static final int MAX_Y_DIST = 7;

   public static OneShot<PathfinderMob> create(float f) {
      return create(f, 10, 7);
   }

   public static OneShot<PathfinderMob> create(float f, int i, int j) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, pathfindermob, i2) -> {
               BlockPos blockpos = pathfindermob.blockPosition();
               Vec3 vec3;
               if (serverlevel.isVillage(blockpos)) {
                  vec3 = LandRandomPos.getPos(pathfindermob, i, j);
               } else {
                  SectionPos sectionpos = SectionPos.of(blockpos);
                  SectionPos sectionpos1 = BehaviorUtils.findSectionClosestToVillage(serverlevel, sectionpos, 2);
                  if (sectionpos1 != sectionpos) {
                     vec3 = DefaultRandomPos.getPosTowards(pathfindermob, i, j, Vec3.atBottomCenterOf(sectionpos1.center()), (double)((float)Math.PI / 2F));
                  } else {
                     vec3 = LandRandomPos.getPos(pathfindermob, i, j);
                  }
               }

               memoryaccessor.setOrErase(Optional.ofNullable(vec3).map((vec33) -> new WalkTarget(vec33, f, 0)));
               return true;
            }));
   }
}
