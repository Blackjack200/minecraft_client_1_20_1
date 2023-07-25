package net.minecraft.world.entity.ai.targeting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetingConditions {
   public static final TargetingConditions DEFAULT = forCombat();
   private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0D;
   private final boolean isCombat;
   private double range = -1.0D;
   private boolean checkLineOfSight = true;
   private boolean testInvisible = true;
   @Nullable
   private Predicate<LivingEntity> selector;

   private TargetingConditions(boolean flag) {
      this.isCombat = flag;
   }

   public static TargetingConditions forCombat() {
      return new TargetingConditions(true);
   }

   public static TargetingConditions forNonCombat() {
      return new TargetingConditions(false);
   }

   public TargetingConditions copy() {
      TargetingConditions targetingconditions = this.isCombat ? forCombat() : forNonCombat();
      targetingconditions.range = this.range;
      targetingconditions.checkLineOfSight = this.checkLineOfSight;
      targetingconditions.testInvisible = this.testInvisible;
      targetingconditions.selector = this.selector;
      return targetingconditions;
   }

   public TargetingConditions range(double d0) {
      this.range = d0;
      return this;
   }

   public TargetingConditions ignoreLineOfSight() {
      this.checkLineOfSight = false;
      return this;
   }

   public TargetingConditions ignoreInvisibilityTesting() {
      this.testInvisible = false;
      return this;
   }

   public TargetingConditions selector(@Nullable Predicate<LivingEntity> predicate) {
      this.selector = predicate;
      return this;
   }

   public boolean test(@Nullable LivingEntity livingentity, LivingEntity livingentity1) {
      if (livingentity == livingentity1) {
         return false;
      } else if (!livingentity1.canBeSeenByAnyone()) {
         return false;
      } else if (this.selector != null && !this.selector.test(livingentity1)) {
         return false;
      } else {
         if (livingentity == null) {
            if (this.isCombat && (!livingentity1.canBeSeenAsEnemy() || livingentity1.level().getDifficulty() == Difficulty.PEACEFUL)) {
               return false;
            }
         } else {
            if (this.isCombat && (!livingentity.canAttack(livingentity1) || !livingentity.canAttackType(livingentity1.getType()) || livingentity.isAlliedTo(livingentity1))) {
               return false;
            }

            if (this.range > 0.0D) {
               double d0 = this.testInvisible ? livingentity1.getVisibilityPercent(livingentity) : 1.0D;
               double d1 = Math.max(this.range * d0, 2.0D);
               double d2 = livingentity.distanceToSqr(livingentity1.getX(), livingentity1.getY(), livingentity1.getZ());
               if (d2 > d1 * d1) {
                  return false;
               }
            }

            if (this.checkLineOfSight && livingentity instanceof Mob) {
               Mob mob = (Mob)livingentity;
               if (!mob.getSensing().hasLineOfSight(livingentity1)) {
                  return false;
               }
            }
         }

         return true;
      }
   }
}
