package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class MoveToSkySeeingSpot {
   public static OneShot<LivingEntity> create(float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i) -> {
               if (serverlevel.canSeeSky(livingentity.blockPosition())) {
                  return false;
               } else {
                  Optional<Vec3> optional = Optional.ofNullable(getOutdoorPosition(serverlevel, livingentity));
                  optional.ifPresent((vec3) -> memoryaccessor.set(new WalkTarget(vec3, f, 0)));
                  return true;
               }
            }));
   }

   @Nullable
   private static Vec3 getOutdoorPosition(ServerLevel serverlevel, LivingEntity livingentity) {
      RandomSource randomsource = livingentity.getRandom();
      BlockPos blockpos = livingentity.blockPosition();

      for(int i = 0; i < 10; ++i) {
         BlockPos blockpos1 = blockpos.offset(randomsource.nextInt(20) - 10, randomsource.nextInt(6) - 3, randomsource.nextInt(20) - 10);
         if (hasNoBlocksAbove(serverlevel, livingentity, blockpos1)) {
            return Vec3.atBottomCenterOf(blockpos1);
         }
      }

      return null;
   }

   public static boolean hasNoBlocksAbove(ServerLevel serverlevel, LivingEntity livingentity, BlockPos blockpos) {
      return serverlevel.canSeeSky(blockpos) && (double)serverlevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() <= livingentity.getY();
   }
}
