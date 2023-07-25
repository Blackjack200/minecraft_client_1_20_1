package net.minecraft.world.entity.ai.sensing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 10.0F;

   protected boolean isMatchingEntity(LivingEntity livingentity, LivingEntity livingentity1) {
      return !livingentity.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && Sensor.isEntityAttackable(livingentity, livingentity1) && Frog.canEat(livingentity1) && !this.isUnreachableAttackTarget(livingentity, livingentity1) ? livingentity1.closerThan(livingentity, 10.0D) : false;
   }

   private boolean isUnreachableAttackTarget(LivingEntity livingentity, LivingEntity livingentity1) {
      List<UUID> list = livingentity.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      return list.contains(livingentity1.getUUID());
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}
