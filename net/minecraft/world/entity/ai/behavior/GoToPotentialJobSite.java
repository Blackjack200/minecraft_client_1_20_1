package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
   private static final int TICKS_UNTIL_TIMEOUT = 1200;
   final float speedModifier;

   public GoToPotentialJobSite(float f) {
      super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT), 1200);
      this.speedModifier = f;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      return villager.getBrain().getActiveNonCoreActivity().map((activity) -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY).orElse(true);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return villager.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      BehaviorUtils.setWalkAndLookTargetMemories(villager, villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
      optional.ifPresent((globalpos) -> {
         BlockPos blockpos = globalpos.pos();
         ServerLevel serverlevel2 = serverlevel.getServer().getLevel(globalpos.dimension());
         if (serverlevel2 != null) {
            PoiManager poimanager = serverlevel2.getPoiManager();
            if (poimanager.exists(blockpos, (holder) -> true)) {
               poimanager.release(blockpos);
            }

            DebugPackets.sendPoiTicketCountPacket(serverlevel, blockpos);
         }
      });
      villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
   }
}
