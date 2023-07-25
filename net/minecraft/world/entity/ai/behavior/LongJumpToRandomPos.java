package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
   protected static final int FIND_JUMP_TRIES = 20;
   private static final int PREPARE_JUMP_DURATION = 40;
   protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
   private static final int TIME_OUT_DURATION = 200;
   private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
   private final UniformInt timeBetweenLongJumps;
   protected final int maxLongJumpHeight;
   protected final int maxLongJumpWidth;
   protected final float maxJumpVelocity;
   protected List<LongJumpToRandomPos.PossibleJump> jumpCandidates = Lists.newArrayList();
   protected Optional<Vec3> initialPosition = Optional.empty();
   @Nullable
   protected Vec3 chosenJump;
   protected int findJumpTries;
   protected long prepareJumpStart;
   private final Function<E, SoundEvent> getJumpSound;
   private final BiPredicate<E, BlockPos> acceptableLandingSpot;

   public LongJumpToRandomPos(UniformInt uniformint, int i, int j, float f, Function<E, SoundEvent> function) {
      this(uniformint, i, j, f, function, LongJumpToRandomPos::defaultAcceptableLandingSpot);
   }

   public static <E extends Mob> boolean defaultAcceptableLandingSpot(E mob, BlockPos blockpos) {
      Level level = mob.level();
      BlockPos blockpos1 = blockpos.below();
      return level.getBlockState(blockpos1).isSolidRender(level, blockpos1) && mob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(level, blockpos.mutable())) == 0.0F;
   }

   public LongJumpToRandomPos(UniformInt uniformint, int i, int j, float f, Function<E, SoundEvent> function, BiPredicate<E, BlockPos> bipredicate) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), 200);
      this.timeBetweenLongJumps = uniformint;
      this.maxLongJumpHeight = i;
      this.maxLongJumpWidth = j;
      this.maxJumpVelocity = f;
      this.getJumpSound = function;
      this.acceptableLandingSpot = bipredicate;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Mob mob) {
      boolean flag = mob.onGround() && !mob.isInWater() && !mob.isInLava() && !serverlevel.getBlockState(mob.blockPosition()).is(Blocks.HONEY_BLOCK);
      if (!flag) {
         mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverlevel.random) / 2);
      }

      return flag;
   }

   protected boolean canStillUse(ServerLevel serverlevel, Mob mob, long i) {
      boolean flag = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && this.findJumpTries > 0 && !mob.isInWaterOrBubble() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
      if (!flag && mob.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
         mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverlevel.random) / 2);
         mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      }

      return flag;
   }

   protected void start(ServerLevel serverlevel, E mob, long i) {
      this.chosenJump = null;
      this.findJumpTries = 20;
      this.initialPosition = Optional.of(mob.position());
      BlockPos blockpos = mob.blockPosition();
      int j = blockpos.getX();
      int k = blockpos.getY();
      int l = blockpos.getZ();
      this.jumpCandidates = BlockPos.betweenClosedStream(j - this.maxLongJumpWidth, k - this.maxLongJumpHeight, l - this.maxLongJumpWidth, j + this.maxLongJumpWidth, k + this.maxLongJumpHeight, l + this.maxLongJumpWidth).filter((blockpos4) -> !blockpos4.equals(blockpos)).map((blockpos2) -> new LongJumpToRandomPos.PossibleJump(blockpos2.immutable(), Mth.ceil(blockpos.distSqr(blockpos2)))).collect(Collectors.toCollection(Lists::newArrayList));
   }

   protected void tick(ServerLevel serverlevel, E mob, long i) {
      if (this.chosenJump != null) {
         if (i - this.prepareJumpStart >= 40L) {
            mob.setYRot(mob.yBodyRot);
            mob.setDiscardFriction(true);
            double d0 = this.chosenJump.length();
            double d1 = d0 + (double)mob.getJumpBoostPower();
            mob.setDeltaMovement(this.chosenJump.scale(d1 / d0));
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
            serverlevel.playSound((Player)null, mob, this.getJumpSound.apply(mob), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      } else {
         --this.findJumpTries;
         this.pickCandidate(serverlevel, mob, i);
      }

   }

   protected void pickCandidate(ServerLevel serverlevel, E mob, long i) {
      while(true) {
         if (!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = this.getJumpCandidate(serverlevel);
            if (optional.isEmpty()) {
               continue;
            }

            LongJumpToRandomPos.PossibleJump longjumptorandompos_possiblejump = optional.get();
            BlockPos blockpos = longjumptorandompos_possiblejump.getJumpTarget();
            if (!this.isAcceptableLandingPosition(serverlevel, mob, blockpos)) {
               continue;
            }

            Vec3 vec3 = Vec3.atCenterOf(blockpos);
            Vec3 vec31 = this.calculateOptimalJumpVector(mob, vec3);
            if (vec31 == null) {
               continue;
            }

            mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockpos));
            PathNavigation pathnavigation = mob.getNavigation();
            Path path = pathnavigation.createPath(blockpos, 0, 8);
            if (path != null && path.canReach()) {
               continue;
            }

            this.chosenJump = vec31;
            this.prepareJumpStart = i;
            return;
         }

         return;
      }
   }

   protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel serverlevel) {
      Optional<LongJumpToRandomPos.PossibleJump> optional = WeightedRandom.getRandomItem(serverlevel.random, this.jumpCandidates);
      optional.ifPresent(this.jumpCandidates::remove);
      return optional;
   }

   private boolean isAcceptableLandingPosition(ServerLevel serverlevel, E mob, BlockPos blockpos) {
      BlockPos blockpos1 = mob.blockPosition();
      int i = blockpos1.getX();
      int j = blockpos1.getZ();
      return i == blockpos.getX() && j == blockpos.getZ() ? false : this.acceptableLandingSpot.test(mob, blockpos);
   }

   @Nullable
   protected Vec3 calculateOptimalJumpVector(Mob mob, Vec3 vec3) {
      List<Integer> list = Lists.newArrayList(ALLOWED_ANGLES);
      Collections.shuffle(list);

      for(int i : list) {
         Vec3 vec31 = this.calculateJumpVectorForAngle(mob, vec3, i);
         if (vec31 != null) {
            return vec31;
         }
      }

      return null;
   }

   @Nullable
   private Vec3 calculateJumpVectorForAngle(Mob mob, Vec3 vec3, int i) {
      Vec3 vec31 = mob.position();
      Vec3 vec32 = (new Vec3(vec3.x - vec31.x, 0.0D, vec3.z - vec31.z)).normalize().scale(0.5D);
      vec3 = vec3.subtract(vec32);
      Vec3 vec33 = vec3.subtract(vec31);
      float f = (float)i * (float)Math.PI / 180.0F;
      double d0 = Math.atan2(vec33.z, vec33.x);
      double d1 = vec33.subtract(0.0D, vec33.y, 0.0D).lengthSqr();
      double d2 = Math.sqrt(d1);
      double d3 = vec33.y;
      double d4 = Math.sin((double)(2.0F * f));
      double d5 = 0.08D;
      double d6 = Math.pow(Math.cos((double)f), 2.0D);
      double d7 = Math.sin((double)f);
      double d8 = Math.cos((double)f);
      double d9 = Math.sin(d0);
      double d10 = Math.cos(d0);
      double d11 = d1 * 0.08D / (d2 * d4 - 2.0D * d3 * d6);
      if (d11 < 0.0D) {
         return null;
      } else {
         double d12 = Math.sqrt(d11);
         if (d12 > (double)this.maxJumpVelocity) {
            return null;
         } else {
            double d13 = d12 * d8;
            double d14 = d12 * d7;
            int j = Mth.ceil(d2 / d13) * 2;
            double d15 = 0.0D;
            Vec3 vec34 = null;
            EntityDimensions entitydimensions = mob.getDimensions(Pose.LONG_JUMPING);

            for(int k = 0; k < j - 1; ++k) {
               d15 += d2 / (double)j;
               double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * 0.08D / (2.0D * d11 * Math.pow(d8, 2.0D));
               double d17 = d15 * d10;
               double d18 = d15 * d9;
               Vec3 vec35 = new Vec3(vec31.x + d17, vec31.y + d16, vec31.z + d18);
               if (vec34 != null && !this.isClearTransition(mob, entitydimensions, vec34, vec35)) {
                  return null;
               }

               vec34 = vec35;
            }

            return (new Vec3(d13 * d10, d14, d13 * d9)).scale((double)0.95F);
         }
      }
   }

   private boolean isClearTransition(Mob mob, EntityDimensions entitydimensions, Vec3 vec3, Vec3 vec31) {
      Vec3 vec32 = vec31.subtract(vec3);
      double d0 = (double)Math.min(entitydimensions.width, entitydimensions.height);
      int i = Mth.ceil(vec32.length() / d0);
      Vec3 vec33 = vec32.normalize();
      Vec3 vec34 = vec3;

      for(int j = 0; j < i; ++j) {
         vec34 = j == i - 1 ? vec31 : vec34.add(vec33.scale(d0 * (double)0.9F));
         if (!mob.level().noCollision(mob, entitydimensions.makeBoundingBox(vec34))) {
            return false;
         }
      }

      return true;
   }

   public static class PossibleJump extends WeightedEntry.IntrusiveBase {
      private final BlockPos jumpTarget;

      public PossibleJump(BlockPos blockpos, int i) {
         super(i);
         this.jumpTarget = blockpos;
      }

      public BlockPos getJumpTarget() {
         return this.jumpTarget;
      }
   }
}
