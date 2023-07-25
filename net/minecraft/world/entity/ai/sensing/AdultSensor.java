package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class AdultSensor extends Sensor<AgeableMob> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }

   protected void doTick(ServerLevel serverlevel, AgeableMob ageablemob) {
      ageablemob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((nearestvisiblelivingentities) -> this.setNearestVisibleAdult(ageablemob, nearestvisiblelivingentities));
   }

   private void setNearestVisibleAdult(AgeableMob ageablemob, NearestVisibleLivingEntities nearestvisiblelivingentities) {
      Optional<AgeableMob> optional = nearestvisiblelivingentities.findClosest((livingentity) -> livingentity.getType() == ageablemob.getType() && !livingentity.isBaby()).map(AgeableMob.class::cast);
      ageablemob.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
   }
}
