package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;

public class WorkAtPoi extends Behavior<Villager> {
   private static final int CHECK_COOLDOWN = 300;
   private static final double DISTANCE = 1.73D;
   private long lastCheck;

   public WorkAtPoi() {
      super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      if (serverlevel.getGameTime() - this.lastCheck < 300L) {
         return false;
      } else if (serverlevel.random.nextInt(2) != 0) {
         return false;
      } else {
         this.lastCheck = serverlevel.getGameTime();
         GlobalPos globalpos = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
         return globalpos.dimension() == serverlevel.dimension() && globalpos.pos().closerToCenterThan(villager.position(), 1.73D);
      }
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      Brain<Villager> brain = villager.getBrain();
      brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, i);
      brain.getMemory(MemoryModuleType.JOB_SITE).ifPresent((globalpos) -> brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(globalpos.pos())));
      villager.playWorkSound();
      this.useWorkstation(serverlevel, villager);
      if (villager.shouldRestock()) {
         villager.restock();
      }

   }

   protected void useWorkstation(ServerLevel serverlevel, Villager villager) {
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      if (!optional.isPresent()) {
         return false;
      } else {
         GlobalPos globalpos = optional.get();
         return globalpos.dimension() == serverlevel.dimension() && globalpos.pos().closerToCenterThan(villager.position(), 1.73D);
      }
   }
}
