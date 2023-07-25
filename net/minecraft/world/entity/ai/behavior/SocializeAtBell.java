package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell {
   private static final float SPEED_MODIFIER = 0.3F;

   public static OneShot<LivingEntity> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.MEETING_POINT), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES), behaviorbuilder_instance.absent(MemoryModuleType.INTERACTION_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3, memoryaccessor4) -> (serverlevel, livingentity, i) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor2);
               NearestVisibleLivingEntities nearestvisiblelivingentities = behaviorbuilder_instance.get(memoryaccessor3);
               if (serverlevel.getRandom().nextInt(100) == 0 && serverlevel.dimension() == globalpos.dimension() && globalpos.pos().closerToCenterThan(livingentity.position(), 4.0D) && nearestvisiblelivingentities.contains((livingentity4) -> EntityType.VILLAGER.equals(livingentity4.getType()))) {
                  nearestvisiblelivingentities.findClosest((livingentity3) -> EntityType.VILLAGER.equals(livingentity3.getType()) && livingentity3.distanceToSqr(livingentity) <= 32.0D).ifPresent((livingentity1) -> {
                     memoryaccessor4.set(livingentity1);
                     memoryaccessor1.set(new EntityTracker(livingentity1, true));
                     memoryaccessor.set(new WalkTarget(new EntityTracker(livingentity1, false), 0.3F, 1));
                  });
                  return true;
               } else {
                  return false;
               }
            }));
   }
}
