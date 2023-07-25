package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

/** @deprecated */
@Deprecated
public class SetEntityLookTargetSometimes {
   public static BehaviorControl<LivingEntity> create(float f, UniformInt uniformint) {
      return create(f, uniformint, (livingentity) -> true);
   }

   public static BehaviorControl<LivingEntity> create(EntityType<?> entitytype, float f, UniformInt uniformint) {
      return create(f, uniformint, (livingentity) -> entitytype.equals(livingentity.getType()));
   }

   private static BehaviorControl<LivingEntity> create(float f, UniformInt uniformint, Predicate<LivingEntity> predicate) {
      float f1 = f * f;
      SetEntityLookTargetSometimes.Ticker setentitylooktargetsometimes_ticker = new SetEntityLookTargetSometimes.Ticker(uniformint);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, livingentity, i) -> {
               Optional<LivingEntity> optional = behaviorbuilder_instance.<NearestVisibleLivingEntities>get(memoryaccessor1).findClosest(predicate.and((livingentity2) -> livingentity2.distanceToSqr(livingentity) <= (double)f1));
               if (optional.isEmpty()) {
                  return false;
               } else if (!setentitylooktargetsometimes_ticker.tickDownAndCheck(serverlevel.random)) {
                  return false;
               } else {
                  memoryaccessor.set(new EntityTracker(optional.get(), true));
                  return true;
               }
            }));
   }

   public static final class Ticker {
      private final UniformInt interval;
      private int ticksUntilNextStart;

      public Ticker(UniformInt uniformint) {
         if (uniformint.getMinValue() <= 1) {
            throw new IllegalArgumentException();
         } else {
            this.interval = uniformint;
         }
      }

      public boolean tickDownAndCheck(RandomSource randomsource) {
         if (this.ticksUntilNextStart == 0) {
            this.ticksUntilNextStart = this.interval.sample(randomsource) - 1;
            return false;
         } else {
            return --this.ticksUntilNextStart == 0;
         }
      }
   }
}
