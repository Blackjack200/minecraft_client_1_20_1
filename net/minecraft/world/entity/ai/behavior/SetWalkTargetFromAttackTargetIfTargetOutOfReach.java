package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach {
   private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;

   public static BehaviorControl<Mob> create(float f) {
      return create((livingentity) -> f);
   }

   public static BehaviorControl<Mob> create(Function<LivingEntity, Float> function) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, mob, i) -> {
               LivingEntity livingentity = behaviorbuilder_instance.get(memoryaccessor2);
               Optional<NearestVisibleLivingEntities> optional = behaviorbuilder_instance.tryGet(memoryaccessor3);
               if (optional.isPresent() && optional.get().contains(livingentity) && BehaviorUtils.isWithinAttackRange(mob, livingentity, 1)) {
                  memoryaccessor.erase();
               } else {
                  memoryaccessor1.set(new EntityTracker(livingentity, true));
                  memoryaccessor.set(new WalkTarget(new EntityTracker(livingentity, false), function.apply(mob), 0));
               }

               return true;
            }));
   }
}
