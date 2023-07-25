package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class JumpOnBed extends Behavior<Mob> {
   private static final int MAX_TIME_TO_REACH_BED = 100;
   private static final int MIN_JUMPS = 3;
   private static final int MAX_JUMPS = 6;
   private static final int COOLDOWN_BETWEEN_JUMPS = 5;
   private final float speedModifier;
   @Nullable
   private BlockPos targetBed;
   private int remainingTimeToReachBed;
   private int remainingJumps;
   private int remainingCooldownUntilNextJump;

   public JumpOnBed(float f) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_BED, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
      this.speedModifier = f;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Mob mob) {
      return mob.isBaby() && this.nearBed(serverlevel, mob);
   }

   protected void start(ServerLevel serverlevel, Mob mob, long i) {
      super.start(serverlevel, mob, i);
      this.getNearestBed(mob).ifPresent((blockpos) -> {
         this.targetBed = blockpos;
         this.remainingTimeToReachBed = 100;
         this.remainingJumps = 3 + serverlevel.random.nextInt(4);
         this.remainingCooldownUntilNextJump = 0;
         this.startWalkingTowardsBed(mob, blockpos);
      });
   }

   protected void stop(ServerLevel serverlevel, Mob mob, long i) {
      super.stop(serverlevel, mob, i);
      this.targetBed = null;
      this.remainingTimeToReachBed = 0;
      this.remainingJumps = 0;
      this.remainingCooldownUntilNextJump = 0;
   }

   protected boolean canStillUse(ServerLevel serverlevel, Mob mob, long i) {
      return mob.isBaby() && this.targetBed != null && this.isBed(serverlevel, this.targetBed) && !this.tiredOfWalking(serverlevel, mob) && !this.tiredOfJumping(serverlevel, mob);
   }

   protected boolean timedOut(long i) {
      return false;
   }

   protected void tick(ServerLevel serverlevel, Mob mob, long i) {
      if (!this.onOrOverBed(serverlevel, mob)) {
         --this.remainingTimeToReachBed;
      } else if (this.remainingCooldownUntilNextJump > 0) {
         --this.remainingCooldownUntilNextJump;
      } else {
         if (this.onBedSurface(serverlevel, mob)) {
            mob.getJumpControl().jump();
            --this.remainingJumps;
            this.remainingCooldownUntilNextJump = 5;
         }

      }
   }

   private void startWalkingTowardsBed(Mob mob, BlockPos blockpos) {
      mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockpos, this.speedModifier, 0));
   }

   private boolean nearBed(ServerLevel serverlevel, Mob mob) {
      return this.onOrOverBed(serverlevel, mob) || this.getNearestBed(mob).isPresent();
   }

   private boolean onOrOverBed(ServerLevel serverlevel, Mob mob) {
      BlockPos blockpos = mob.blockPosition();
      BlockPos blockpos1 = blockpos.below();
      return this.isBed(serverlevel, blockpos) || this.isBed(serverlevel, blockpos1);
   }

   private boolean onBedSurface(ServerLevel serverlevel, Mob mob) {
      return this.isBed(serverlevel, mob.blockPosition());
   }

   private boolean isBed(ServerLevel serverlevel, BlockPos blockpos) {
      return serverlevel.getBlockState(blockpos).is(BlockTags.BEDS);
   }

   private Optional<BlockPos> getNearestBed(Mob mob) {
      return mob.getBrain().getMemory(MemoryModuleType.NEAREST_BED);
   }

   private boolean tiredOfWalking(ServerLevel serverlevel, Mob mob) {
      return !this.onOrOverBed(serverlevel, mob) && this.remainingTimeToReachBed <= 0;
   }

   private boolean tiredOfJumping(ServerLevel serverlevel, Mob mob) {
      return this.onOrOverBed(serverlevel, mob) && this.remainingJumps <= 0;
   }
}
