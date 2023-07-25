package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<LivingEntity> {
   private static final int GOLEM_SCAN_RATE = 200;
   private static final int MEMORY_TIME_TO_LIVE = 600;

   public GolemSensor() {
      this(200);
   }

   public GolemSensor(int i) {
      super(i);
   }

   protected void doTick(ServerLevel serverlevel, LivingEntity livingentity) {
      checkForNearbyGolem(livingentity);
   }

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
   }

   public static void checkForNearbyGolem(LivingEntity livingentity) {
      Optional<List<LivingEntity>> optional = livingentity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
      if (optional.isPresent()) {
         boolean flag = optional.get().stream().anyMatch((livingentity1) -> livingentity1.getType().equals(EntityType.IRON_GOLEM));
         if (flag) {
            golemDetected(livingentity);
         }

      }
   }

   public static void golemDetected(LivingEntity livingentity) {
      livingentity.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 600L);
   }
}
