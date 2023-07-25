package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RingBell {
   private static final float BELL_RING_CHANCE = 0.95F;
   public static final int RING_BELL_FROM_DISTANCE = 3;

   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.MEETING_POINT)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i) -> {
               if (serverlevel.random.nextFloat() <= 0.95F) {
                  return false;
               } else {
                  BlockPos blockpos = behaviorbuilder_instance.<GlobalPos>get(memoryaccessor).pos();
                  if (blockpos.closerThan(livingentity.blockPosition(), 3.0D)) {
                     BlockState blockstate = serverlevel.getBlockState(blockpos);
                     if (blockstate.is(Blocks.BELL)) {
                        BellBlock bellblock = (BellBlock)blockstate.getBlock();
                        bellblock.attemptToRing(livingentity, serverlevel, blockpos, (Direction)null);
                     }
                  }

                  return true;
               }
            }));
   }
}
