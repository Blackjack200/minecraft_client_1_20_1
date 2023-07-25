package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerHostilesSensor extends NearestVisibleLivingEntitySensor {
   private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.<EntityType<?>, Float>builder().put(EntityType.DROWNED, 8.0F).put(EntityType.EVOKER, 12.0F).put(EntityType.HUSK, 8.0F).put(EntityType.ILLUSIONER, 12.0F).put(EntityType.PILLAGER, 15.0F).put(EntityType.RAVAGER, 12.0F).put(EntityType.VEX, 8.0F).put(EntityType.VINDICATOR, 10.0F).put(EntityType.ZOGLIN, 10.0F).put(EntityType.ZOMBIE, 8.0F).put(EntityType.ZOMBIE_VILLAGER, 8.0F).build();

   protected boolean isMatchingEntity(LivingEntity livingentity, LivingEntity livingentity1) {
      return this.isHostile(livingentity1) && this.isClose(livingentity, livingentity1);
   }

   private boolean isClose(LivingEntity livingentity, LivingEntity livingentity1) {
      float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingentity1.getType());
      return livingentity1.distanceToSqr(livingentity) <= (double)(f * f);
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_HOSTILE;
   }

   private boolean isHostile(LivingEntity livingentity) {
      return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingentity.getType());
   }
}
