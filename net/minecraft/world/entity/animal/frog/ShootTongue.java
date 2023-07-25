package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class ShootTongue extends Behavior<Frog> {
   public static final int TIME_OUT_DURATION = 100;
   public static final int CATCH_ANIMATION_DURATION = 6;
   public static final int TONGUE_ANIMATION_DURATION = 10;
   private static final float EATING_DISTANCE = 1.75F;
   private static final float EATING_MOVEMENT_FACTOR = 0.75F;
   public static final int UNREACHABLE_TONGUE_TARGETS_COOLDOWN_DURATION = 100;
   public static final int MAX_UNREACHBLE_TONGUE_TARGETS_IN_MEMORY = 5;
   private int eatAnimationTimer;
   private int calculatePathCounter;
   private final SoundEvent tongueSound;
   private final SoundEvent eatSound;
   private Vec3 itemSpawnPos;
   private ShootTongue.State state = ShootTongue.State.DONE;

   public ShootTongue(SoundEvent soundevent, SoundEvent soundevent1) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT), 100);
      this.tongueSound = soundevent;
      this.eatSound = soundevent1;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Frog frog) {
      LivingEntity livingentity = frog.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      boolean flag = this.canPathfindToTarget(frog, livingentity);
      if (!flag) {
         frog.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
         this.addUnreachableTargetToMemory(frog, livingentity);
      }

      return flag && frog.getPose() != Pose.CROAKING && Frog.canEat(livingentity);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Frog frog, long i) {
      return frog.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.state != ShootTongue.State.DONE && !frog.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
   }

   protected void start(ServerLevel serverlevel, Frog frog, long i) {
      LivingEntity livingentity = frog.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      BehaviorUtils.lookAtEntity(frog, livingentity);
      frog.setTongueTarget(livingentity);
      frog.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingentity.position(), 2.0F, 0));
      this.calculatePathCounter = 10;
      this.state = ShootTongue.State.MOVE_TO_TARGET;
   }

   protected void stop(ServerLevel serverlevel, Frog frog, long i) {
      frog.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      frog.eraseTongueTarget();
      frog.setPose(Pose.STANDING);
   }

   private void eatEntity(ServerLevel serverlevel, Frog frog) {
      serverlevel.playSound((Player)null, frog, this.eatSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
      Optional<Entity> optional = frog.getTongueTarget();
      if (optional.isPresent()) {
         Entity entity = optional.get();
         if (entity.isAlive()) {
            frog.doHurtTarget(entity);
            if (!entity.isAlive()) {
               entity.remove(Entity.RemovalReason.KILLED);
            }
         }
      }

   }

   protected void tick(ServerLevel serverlevel, Frog frog, long i) {
      LivingEntity livingentity = frog.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      frog.setTongueTarget(livingentity);
      switch (this.state) {
         case MOVE_TO_TARGET:
            if (livingentity.distanceTo(frog) < 1.75F) {
               serverlevel.playSound((Player)null, frog, this.tongueSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
               frog.setPose(Pose.USING_TONGUE);
               livingentity.setDeltaMovement(livingentity.position().vectorTo(frog.position()).normalize().scale(0.75D));
               this.itemSpawnPos = livingentity.position();
               this.eatAnimationTimer = 0;
               this.state = ShootTongue.State.CATCH_ANIMATION;
            } else if (this.calculatePathCounter <= 0) {
               frog.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingentity.position(), 2.0F, 0));
               this.calculatePathCounter = 10;
            } else {
               --this.calculatePathCounter;
            }
            break;
         case CATCH_ANIMATION:
            if (this.eatAnimationTimer++ >= 6) {
               this.state = ShootTongue.State.EAT_ANIMATION;
               this.eatEntity(serverlevel, frog);
            }
            break;
         case EAT_ANIMATION:
            if (this.eatAnimationTimer >= 10) {
               this.state = ShootTongue.State.DONE;
            } else {
               ++this.eatAnimationTimer;
            }
         case DONE:
      }

   }

   private boolean canPathfindToTarget(Frog frog, LivingEntity livingentity) {
      Path path = frog.getNavigation().createPath(livingentity, 0);
      return path != null && path.getDistToTarget() < 1.75F;
   }

   private void addUnreachableTargetToMemory(Frog frog, LivingEntity livingentity) {
      List<UUID> list = frog.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      boolean flag = !list.contains(livingentity.getUUID());
      if (list.size() == 5 && flag) {
         list.remove(0);
      }

      if (flag) {
         list.add(livingentity.getUUID());
      }

      frog.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, list, 100L);
   }

   static enum State {
      MOVE_TO_TARGET,
      CATCH_ANIMATION,
      EAT_ANIMATION,
      DONE;
   }
}
