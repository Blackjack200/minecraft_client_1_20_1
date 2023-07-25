package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
   public static <T extends LivingEntity> BehaviorControl<LivingEntity> of(EntityType<? extends T> entitytype, int i, MemoryModuleType<T> memorymoduletype, float f, int j) {
      return of(entitytype, i, (livingentity1) -> true, (livingentity) -> true, memorymoduletype, f, j);
   }

   public static <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> of(EntityType<? extends T> entitytype, int i, Predicate<E> predicate, Predicate<T> predicate1, MemoryModuleType<T> memorymoduletype, float f, int j) {
      int k = i * i;
      Predicate<LivingEntity> predicate2 = (livingentity4) -> entitytype.equals(livingentity4.getType()) && predicate1.test((T)livingentity4);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(memorymoduletype), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, livingentity, j2) -> {
               NearestVisibleLivingEntities nearestvisiblelivingentities = behaviorbuilder_instance.get(memoryaccessor3);
               if (predicate.test(livingentity) && nearestvisiblelivingentities.contains(predicate2)) {
                  Optional<LivingEntity> optional = nearestvisiblelivingentities.findClosest((livingentity3) -> livingentity3.distanceToSqr(livingentity) <= (double)k && predicate2.test(livingentity3));
                  optional.ifPresent((livingentity1) -> {
                     memoryaccessor.set(livingentity1);
                     memoryaccessor1.set(new EntityTracker(livingentity1, true));
                     memoryaccessor2.set(new WalkTarget(new EntityTracker(livingentity1, false), f, j));
                  });
                  return true;
               } else {
                  return false;
               }
            }));
   }
}
