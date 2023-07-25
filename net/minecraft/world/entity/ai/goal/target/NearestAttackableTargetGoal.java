package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
   private static final int DEFAULT_RANDOM_INTERVAL = 10;
   protected final Class<T> targetType;
   protected final int randomInterval;
   @Nullable
   protected LivingEntity target;
   protected TargetingConditions targetConditions;

   public NearestAttackableTargetGoal(Mob mob, Class<T> oclass, boolean flag) {
      this(mob, oclass, 10, flag, false, (Predicate<LivingEntity>)null);
   }

   public NearestAttackableTargetGoal(Mob mob, Class<T> oclass, boolean flag, Predicate<LivingEntity> predicate) {
      this(mob, oclass, 10, flag, false, predicate);
   }

   public NearestAttackableTargetGoal(Mob mob, Class<T> oclass, boolean flag, boolean flag1) {
      this(mob, oclass, 10, flag, flag1, (Predicate<LivingEntity>)null);
   }

   public NearestAttackableTargetGoal(Mob mob, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<LivingEntity> predicate) {
      super(mob, flag, flag1);
      this.targetType = oclass;
      this.randomInterval = reducedTickDelay(i);
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
      this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(predicate);
   }

   public boolean canUse() {
      if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
         return false;
      } else {
         this.findTarget();
         return this.target != null;
      }
   }

   protected AABB getTargetSearchArea(double d0) {
      return this.mob.getBoundingBox().inflate(d0, 4.0D, d0);
   }

   protected void findTarget() {
      if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
         this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (livingentity) -> true), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      } else {
         this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      }

   }

   public void start() {
      this.mob.setTarget(this.target);
      super.start();
   }

   public void setTarget(@Nullable LivingEntity livingentity) {
      this.target = livingentity;
   }
}
