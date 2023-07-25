package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public abstract class NearestVisibleLivingEntitySensor extends Sensor<LivingEntity> {
   protected abstract boolean isMatchingEntity(LivingEntity livingentity, LivingEntity livingentity1);

   protected abstract MemoryModuleType<LivingEntity> getMemory();

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(this.getMemory());
   }

   protected void doTick(ServerLevel serverlevel, LivingEntity livingentity) {
      livingentity.getBrain().setMemory(this.getMemory(), this.getNearestEntity(livingentity));
   }

   private Optional<LivingEntity> getNearestEntity(LivingEntity livingentity) {
      return this.getVisibleEntities(livingentity).flatMap((nearestvisiblelivingentities) -> nearestvisiblelivingentities.findClosest((livingentity3) -> this.isMatchingEntity(livingentity, livingentity3)));
   }

   protected Optional<NearestVisibleLivingEntities> getVisibleEntities(LivingEntity livingentity) {
      return livingentity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }
}
