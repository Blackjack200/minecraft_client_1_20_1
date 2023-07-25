package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
   private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

   public static <E extends Mob> BehaviorControl<E> create(BiConsumer<E, LivingEntity> biconsumer) {
      return create((livingentity) -> false, biconsumer, true);
   }

   public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> predicate) {
      return create(predicate, (mob, livingentity) -> {
      }, true);
   }

   public static <E extends Mob> BehaviorControl<E> create() {
      return create((livingentity1) -> false, (mob, livingentity) -> {
      }, true);
   }

   public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> predicate, BiConsumer<E, LivingEntity> biconsumer, boolean flag) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, mob, i) -> {
               LivingEntity livingentity = behaviorbuilder_instance.get(memoryaccessor);
               if (mob.canAttack(livingentity) && (!flag || !isTiredOfTryingToReachTarget(mob, behaviorbuilder_instance.tryGet(memoryaccessor1))) && livingentity.isAlive() && livingentity.level() == mob.level() && !predicate.test(livingentity)) {
                  return true;
               } else {
                  biconsumer.accept(mob, livingentity);
                  memoryaccessor.erase();
                  return true;
               }
            }));
   }

   private static boolean isTiredOfTryingToReachTarget(LivingEntity livingentity, Optional<Long> optional) {
      return optional.isPresent() && livingentity.level().getGameTime() - optional.get() > 200L;
   }
}
