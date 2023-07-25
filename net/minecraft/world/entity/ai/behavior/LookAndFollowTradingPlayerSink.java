package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class LookAndFollowTradingPlayerSink extends Behavior<Villager> {
   private final float speedModifier;

   public LookAndFollowTradingPlayerSink(float f) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), Integer.MAX_VALUE);
      this.speedModifier = f;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      Player player = villager.getTradingPlayer();
      return villager.isAlive() && player != null && !villager.isInWater() && !villager.hurtMarked && villager.distanceToSqr(player) <= 16.0D && player.containerMenu != null;
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.checkExtraStartConditions(serverlevel, villager);
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      this.followPlayer(villager);
   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      Brain<?> brain = villager.getBrain();
      brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      this.followPlayer(villager);
   }

   protected boolean timedOut(long i) {
      return false;
   }

   private void followPlayer(Villager villager) {
      Brain<?> brain = villager.getBrain();
      brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(villager.getTradingPlayer(), false), this.speedModifier, 2));
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(villager.getTradingPlayer(), true));
   }
}
