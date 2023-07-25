package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

public class TryLaySpawnOnWaterNearLand {
   public static BehaviorControl<LivingEntity> create(Block block) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.IS_PREGNANT)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, livingentity, i) -> {
               if (!livingentity.isInWater() && livingentity.onGround()) {
                  BlockPos blockpos = livingentity.blockPosition().below();

                  for(Direction direction : Direction.Plane.HORIZONTAL) {
                     BlockPos blockpos1 = blockpos.relative(direction);
                     if (serverlevel.getBlockState(blockpos1).getCollisionShape(serverlevel, blockpos1).getFaceShape(Direction.UP).isEmpty() && serverlevel.getFluidState(blockpos1).is(Fluids.WATER)) {
                        BlockPos blockpos2 = blockpos1.above();
                        if (serverlevel.getBlockState(blockpos2).isAir()) {
                           BlockState blockstate = block.defaultBlockState();
                           serverlevel.setBlock(blockpos2, blockstate, 3);
                           serverlevel.gameEvent(GameEvent.BLOCK_PLACE, blockpos2, GameEvent.Context.of(livingentity, blockstate));
                           serverlevel.playSound((Player)null, livingentity, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                           memoryaccessor2.erase();
                           return true;
                        }
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            }));
   }
}
