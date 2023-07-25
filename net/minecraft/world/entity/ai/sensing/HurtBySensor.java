package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class HurtBySensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
   }

   protected void doTick(ServerLevel serverlevel, LivingEntity livingentity) {
      Brain<?> brain = livingentity.getBrain();
      DamageSource damagesource = livingentity.getLastDamageSource();
      if (damagesource != null) {
         brain.setMemory(MemoryModuleType.HURT_BY, livingentity.getLastDamageSource());
         Entity entity = damagesource.getEntity();
         if (entity instanceof LivingEntity) {
            brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity)entity);
         }
      } else {
         brain.eraseMemory(MemoryModuleType.HURT_BY);
      }

      brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent((livingentity1) -> {
         if (!livingentity1.isAlive() || livingentity1.level() != serverlevel) {
            brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
         }

      });
   }
}
