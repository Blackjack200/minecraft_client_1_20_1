package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearWater {
   public static BehaviorControl<PathfinderMob> create(int i, float f) {
      MutableLong mutablelong = new MutableLong(0L);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, pathfindermob, i1) -> {
               if (serverlevel.getFluidState(pathfindermob.blockPosition()).is(FluidTags.WATER)) {
                  return false;
               } else if (i1 < mutablelong.getValue()) {
                  mutablelong.setValue(i1 + 40L);
                  return true;
               } else {
                  CollisionContext collisioncontext = CollisionContext.of(pathfindermob);
                  BlockPos blockpos = pathfindermob.blockPosition();
                  BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

                  label45:
                  for(BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, i, i, i)) {
                     if ((blockpos1.getX() != blockpos.getX() || blockpos1.getZ() != blockpos.getZ()) && serverlevel.getBlockState(blockpos1).getCollisionShape(serverlevel, blockpos1, collisioncontext).isEmpty() && !serverlevel.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos1, Direction.DOWN)).getCollisionShape(serverlevel, blockpos1, collisioncontext).isEmpty()) {
                        for(Direction direction : Direction.Plane.HORIZONTAL) {
                           blockpos_mutableblockpos.setWithOffset(blockpos1, direction);
                           if (serverlevel.getBlockState(blockpos_mutableblockpos).isAir() && serverlevel.getBlockState(blockpos_mutableblockpos.move(Direction.DOWN)).is(Blocks.WATER)) {
                              memoryaccessor2.set(new BlockPosTracker(blockpos1));
                              memoryaccessor1.set(new WalkTarget(new BlockPosTracker(blockpos1), f, 0));
                              break label45;
                           }
                        }
                     }
                  }

                  mutablelong.setValue(i1 + 40L);
                  return true;
               }
            }));
   }
}
