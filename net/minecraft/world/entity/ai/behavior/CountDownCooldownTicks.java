package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownCooldownTicks extends Behavior<LivingEntity> {
   private final MemoryModuleType<Integer> cooldownTicks;

   public CountDownCooldownTicks(MemoryModuleType<Integer> memorymoduletype) {
      super(ImmutableMap.of(memorymoduletype, MemoryStatus.VALUE_PRESENT));
      this.cooldownTicks = memorymoduletype;
   }

   private Optional<Integer> getCooldownTickMemory(LivingEntity livingentity) {
      return livingentity.getBrain().getMemory(this.cooldownTicks);
   }

   protected boolean timedOut(long i) {
      return false;
   }

   protected boolean canStillUse(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      Optional<Integer> optional = this.getCooldownTickMemory(livingentity);
      return optional.isPresent() && optional.get() > 0;
   }

   protected void tick(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      Optional<Integer> optional = this.getCooldownTickMemory(livingentity);
      livingentity.getBrain().setMemory(this.cooldownTicks, optional.get() - 1);
   }

   protected void stop(ServerLevel serverlevel, LivingEntity livingentity, long i) {
      livingentity.getBrain().eraseMemory(this.cooldownTicks);
   }
}
