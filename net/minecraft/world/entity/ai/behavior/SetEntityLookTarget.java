package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget {
   public static BehaviorControl<LivingEntity> create(MobCategory mobcategory, float f) {
      return create((livingentity) -> mobcategory.equals(livingentity.getType().getCategory()), f);
   }

   public static OneShot<LivingEntity> create(EntityType<?> entitytype, float f) {
      return create((livingentity) -> entitytype.equals(livingentity.getType()), f);
   }

   public static OneShot<LivingEntity> create(float f) {
      return create((livingentity) -> true, f);
   }

   public static OneShot<LivingEntity> create(Predicate<LivingEntity> predicate, float f) {
      float f1 = f * f;
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i) -> {
               Optional<LivingEntity> optional = behaviorbuilder_instance.<NearestVisibleLivingEntities>get(memoryaccessor1).findClosest(predicate.and((livingentity2) -> livingentity2.distanceToSqr(livingentity) <= (double)f1 && !livingentity.hasPassenger(livingentity2)));
               if (optional.isEmpty()) {
                  return false;
               } else {
                  memoryaccessor.set(new EntityTracker(optional.get(), true));
                  return true;
               }
            }));
   }
}
