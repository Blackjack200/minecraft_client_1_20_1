package net.minecraft.world.entity.ai.behavior;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk {
   public static BehaviorControl<PathfinderMob> create(float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, pathfindermob, i) -> {
               if (serverlevel.canSeeSky(pathfindermob.blockPosition())) {
                  return false;
               } else {
                  BlockPos blockpos = pathfindermob.blockPosition();
                  List<BlockPos> list = BlockPos.betweenClosedStream(blockpos.offset(-1, -1, -1), blockpos.offset(1, 1, 1)).map(BlockPos::immutable).collect(Collectors.toList());
                  Collections.shuffle(list);
                  list.stream().filter((blockpos4) -> !serverlevel.canSeeSky(blockpos4)).filter((blockpos3) -> serverlevel.loadedAndEntityCanStandOn(blockpos3, pathfindermob)).filter((blockpos2) -> serverlevel.noCollision(pathfindermob)).findFirst().ifPresent((blockpos1) -> memoryaccessor.set(new WalkTarget(blockpos1, f, 0)));
                  return true;
               }
            }));
   }
}
