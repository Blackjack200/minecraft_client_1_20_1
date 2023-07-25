package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class AvoidEntityGoal<T extends LivingEntity> extends Goal {
   protected final PathfinderMob mob;
   private final double walkSpeedModifier;
   private final double sprintSpeedModifier;
   @Nullable
   protected T toAvoid;
   protected final float maxDist;
   @Nullable
   protected Path path;
   protected final PathNavigation pathNav;
   protected final Class<T> avoidClass;
   protected final Predicate<LivingEntity> avoidPredicate;
   protected final Predicate<LivingEntity> predicateOnAvoidEntity;
   private final TargetingConditions avoidEntityTargeting;

   public AvoidEntityGoal(PathfinderMob pathfindermob, Class<T> oclass, float f, double d0, double d1) {
      this(pathfindermob, oclass, (livingentity) -> true, f, d0, d1, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
   }

   public AvoidEntityGoal(PathfinderMob pathfindermob, Class<T> oclass, Predicate<LivingEntity> predicate, float f, double d0, double d1, Predicate<LivingEntity> predicate1) {
      this.mob = pathfindermob;
      this.avoidClass = oclass;
      this.avoidPredicate = predicate;
      this.maxDist = f;
      this.walkSpeedModifier = d0;
      this.sprintSpeedModifier = d1;
      this.predicateOnAvoidEntity = predicate1;
      this.pathNav = pathfindermob.getNavigation();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      this.avoidEntityTargeting = TargetingConditions.forCombat().range((double)f).selector(predicate1.and(predicate));
   }

   public AvoidEntityGoal(PathfinderMob pathfindermob, Class<T> oclass, float f, double d0, double d1, Predicate<LivingEntity> predicate) {
      this(pathfindermob, oclass, (livingentity) -> true, f, d0, d1, predicate);
   }

   public boolean canUse() {
      this.toAvoid = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate((double)this.maxDist, 3.0D, (double)this.maxDist), (livingentity) -> true), this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
      if (this.toAvoid == null) {
         return false;
      } else {
         Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
         if (vec3 == null) {
            return false;
         } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
            return false;
         } else {
            this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
            return this.path != null;
         }
      }
   }

   public boolean canContinueToUse() {
      return !this.pathNav.isDone();
   }

   public void start() {
      this.pathNav.moveTo(this.path, this.walkSpeedModifier);
   }

   public void stop() {
      this.toAvoid = null;
   }

   public void tick() {
      if (this.mob.distanceToSqr(this.toAvoid) < 49.0D) {
         this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
      } else {
         this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
      }

   }
}
