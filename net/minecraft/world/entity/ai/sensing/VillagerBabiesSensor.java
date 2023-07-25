package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class VillagerBabiesSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }

   protected void doTick(ServerLevel serverlevel, LivingEntity livingentity) {
      livingentity.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies(livingentity));
   }

   private List<LivingEntity> getNearestVillagerBabies(LivingEntity livingentity) {
      return ImmutableList.copyOf(this.getVisibleEntities(livingentity).findAll(this::isVillagerBaby));
   }

   private boolean isVillagerBaby(LivingEntity livingentity1) {
      return livingentity1.getType() == EntityType.VILLAGER && livingentity1.isBaby();
   }

   private NearestVisibleLivingEntities getVisibleEntities(LivingEntity livingentity) {
      return livingentity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
   }
}
