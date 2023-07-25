package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class VillagerPanicTrigger extends Behavior<Villager> {
   public VillagerPanicTrigger() {
      super(ImmutableMap.of());
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return isHurt(villager) || hasHostile(villager);
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      if (isHurt(villager) || hasHostile(villager)) {
         Brain<?> brain = villager.getBrain();
         if (!brain.isActive(Activity.PANIC)) {
            brain.eraseMemory(MemoryModuleType.PATH);
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            brain.eraseMemory(MemoryModuleType.BREED_TARGET);
            brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
         }

         brain.setActiveActivityIfPossible(Activity.PANIC);
      }

   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      if (i % 100L == 0L) {
         villager.spawnGolemIfNeeded(serverlevel, i, 3);
      }

   }

   public static boolean hasHostile(LivingEntity livingentity) {
      return livingentity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);
   }

   public static boolean isHurt(LivingEntity livingentity) {
      return livingentity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
   }
}
