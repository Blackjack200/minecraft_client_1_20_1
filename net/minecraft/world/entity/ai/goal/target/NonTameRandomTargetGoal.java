package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public class NonTameRandomTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
   private final TamableAnimal tamableMob;

   public NonTameRandomTargetGoal(TamableAnimal tamableanimal, Class<T> oclass, boolean flag, @Nullable Predicate<LivingEntity> predicate) {
      super(tamableanimal, oclass, 10, flag, false, predicate);
      this.tamableMob = tamableanimal;
   }

   public boolean canUse() {
      return !this.tamableMob.isTame() && super.canUse();
   }

   public boolean canContinueToUse() {
      return this.targetConditions != null ? this.targetConditions.test(this.mob, this.target) : super.canContinueToUse();
   }
}
