package net.minecraft.world.entity.ai.sensing;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 8.0F;

   protected boolean isMatchingEntity(LivingEntity livingentity, LivingEntity livingentity1) {
      return this.isClose(livingentity, livingentity1) && livingentity1.isInWaterOrBubble() && (this.isHostileTarget(livingentity1) || this.isHuntTarget(livingentity, livingentity1)) && Sensor.isEntityAttackable(livingentity, livingentity1);
   }

   private boolean isHuntTarget(LivingEntity livingentity, LivingEntity livingentity1) {
      return !livingentity.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && livingentity1.getType().is(EntityTypeTags.AXOLOTL_HUNT_TARGETS);
   }

   private boolean isHostileTarget(LivingEntity livingentity) {
      return livingentity.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES);
   }

   private boolean isClose(LivingEntity livingentity, LivingEntity livingentity1) {
      return livingentity1.distanceToSqr(livingentity) <= 64.0D;
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}
