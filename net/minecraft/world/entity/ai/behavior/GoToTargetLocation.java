package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GoToTargetLocation {
   private static BlockPos getNearbyPos(Mob mob, BlockPos blockpos) {
      RandomSource randomsource = mob.level().random;
      return blockpos.offset(getRandomOffset(randomsource), 0, getRandomOffset(randomsource));
   }

   private static int getRandomOffset(RandomSource randomsource) {
      return randomsource.nextInt(3) - 1;
   }

   public static <E extends Mob> OneShot<E> create(MemoryModuleType<BlockPos> memorymoduletype, int i, float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(memorymoduletype), behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, mob, i1) -> {
               BlockPos blockpos = behaviorbuilder_instance.get(memoryaccessor);
               boolean flag = blockpos.closerThan(mob.blockPosition(), (double)i);
               if (!flag) {
                  BehaviorUtils.setWalkAndLookTargetMemories(mob, getNearbyPos(mob, blockpos), f, i);
               }

               return true;
            }));
   }
}
