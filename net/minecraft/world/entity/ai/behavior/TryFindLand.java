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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLand {
   private static final int COOLDOWN_TICKS = 60;

   public static BehaviorControl<PathfinderMob> create(int i, float f) {
      MutableLong mutablelong = new MutableLong(0L);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, pathfindermob, i1) -> {
               if (!serverlevel.getFluidState(pathfindermob.blockPosition()).is(FluidTags.WATER)) {
                  return false;
               } else if (i1 < mutablelong.getValue()) {
                  mutablelong.setValue(i1 + 60L);
                  return true;
               } else {
                  BlockPos blockpos = pathfindermob.blockPosition();
                  BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
                  CollisionContext collisioncontext = CollisionContext.of(pathfindermob);

                  for(BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, i, i, i)) {
                     if (blockpos1.getX() != blockpos.getX() || blockpos1.getZ() != blockpos.getZ()) {
                        BlockState blockstate = serverlevel.getBlockState(blockpos1);
                        BlockState blockstate1 = serverlevel.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos1, Direction.DOWN));
                        if (!blockstate.is(Blocks.WATER) && serverlevel.getFluidState(blockpos1).isEmpty() && blockstate.getCollisionShape(serverlevel, blockpos1, collisioncontext).isEmpty() && blockstate1.isFaceSturdy(serverlevel, blockpos_mutableblockpos, Direction.UP)) {
                           BlockPos blockpos2 = blockpos1.immutable();
                           memoryaccessor2.set(new BlockPosTracker(blockpos2));
                           memoryaccessor1.set(new WalkTarget(new BlockPosTracker(blockpos2), f, 1));
                           break;
                        }
                     }
                  }

                  mutablelong.setValue(i1 + 60L);
                  return true;
               }
            }));
   }
}
