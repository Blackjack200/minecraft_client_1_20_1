package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

public class NearestAttackableWitchTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
   private boolean canAttack = true;

   public NearestAttackableWitchTargetGoal(Raider raider, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<LivingEntity> predicate) {
      super(raider, oclass, i, flag, flag1, predicate);
   }

   public void setCanAttack(boolean flag) {
      this.canAttack = flag;
   }

   public boolean canUse() {
      return this.canAttack && super.canUse();
   }
}
