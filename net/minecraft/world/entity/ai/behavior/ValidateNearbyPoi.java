package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ValidateNearbyPoi {
   private static final int MAX_DISTANCE = 16;

   public static BehaviorControl<LivingEntity> create(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memorymoduletype) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.present(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, livingentity, i) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor);
               BlockPos blockpos = globalpos.pos();
               if (serverlevel.dimension() == globalpos.dimension() && blockpos.closerToCenterThan(livingentity.position(), 16.0D)) {
                  ServerLevel serverlevel1 = serverlevel.getServer().getLevel(globalpos.dimension());
                  if (serverlevel1 != null && serverlevel1.getPoiManager().exists(blockpos, predicate)) {
                     if (bedIsOccupied(serverlevel1, blockpos, livingentity)) {
                        memoryaccessor.erase();
                        serverlevel.getPoiManager().release(blockpos);
                        DebugPackets.sendPoiTicketCountPacket(serverlevel, blockpos);
                     }
                  } else {
                     memoryaccessor.erase();
                  }

                  return true;
               } else {
                  return false;
               }
            }));
   }

   private static boolean bedIsOccupied(ServerLevel serverlevel, BlockPos blockpos, LivingEntity livingentity) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      return blockstate.is(BlockTags.BEDS) && blockstate.getValue(BedBlock.OCCUPIED) && !livingentity.isSleeping();
   }
}
