package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RamTarget extends Behavior<Goat> {
   public static final int TIME_OUT_DURATION = 200;
   public static final float RAM_SPEED_FORCE_FACTOR = 1.65F;
   private final Function<Goat, UniformInt> getTimeBetweenRams;
   private final TargetingConditions ramTargeting;
   private final float speed;
   private final ToDoubleFunction<Goat> getKnockbackForce;
   private Vec3 ramDirection;
   private final Function<Goat, SoundEvent> getImpactSound;
   private final Function<Goat, SoundEvent> getHornBreakSound;

   public RamTarget(Function<Goat, UniformInt> function, TargetingConditions targetingconditions, float f, ToDoubleFunction<Goat> todoublefunction, Function<Goat, SoundEvent> function1, Function<Goat, SoundEvent> function2) {
      super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_PRESENT), 200);
      this.getTimeBetweenRams = function;
      this.ramTargeting = targetingconditions;
      this.speed = f;
      this.getKnockbackForce = todoublefunction;
      this.getImpactSound = function1;
      this.getHornBreakSound = function2;
      this.ramDirection = Vec3.ZERO;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Goat goat) {
      return goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Goat goat, long i) {
      return goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
   }

   protected void start(ServerLevel serverlevel, Goat goat, long i) {
      BlockPos blockpos = goat.blockPosition();
      Brain<?> brain = goat.getBrain();
      Vec3 vec3 = brain.getMemory(MemoryModuleType.RAM_TARGET).get();
      this.ramDirection = (new Vec3((double)blockpos.getX() - vec3.x(), 0.0D, (double)blockpos.getZ() - vec3.z())).normalize();
      brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speed, 0));
   }

   protected void tick(ServerLevel serverlevel, Goat goat, long i) {
      List<LivingEntity> list = serverlevel.getNearbyEntities(LivingEntity.class, this.ramTargeting, goat, goat.getBoundingBox());
      Brain<?> brain = goat.getBrain();
      if (!list.isEmpty()) {
         LivingEntity livingentity = list.get(0);
         livingentity.hurt(serverlevel.damageSources().noAggroMobAttack(goat), (float)goat.getAttributeValue(Attributes.ATTACK_DAMAGE));
         int j = goat.hasEffect(MobEffects.MOVEMENT_SPEED) ? goat.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() + 1 : 0;
         int k = goat.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) ? goat.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() + 1 : 0;
         float f = 0.25F * (float)(j - k);
         float f1 = Mth.clamp(goat.getSpeed() * 1.65F, 0.2F, 3.0F) + f;
         float f2 = livingentity.isDamageSourceBlocked(serverlevel.damageSources().mobAttack(goat)) ? 0.5F : 1.0F;
         livingentity.knockback((double)(f2 * f1) * this.getKnockbackForce.applyAsDouble(goat), this.ramDirection.x(), this.ramDirection.z());
         this.finishRam(serverlevel, goat);
         serverlevel.playSound((Player)null, goat, this.getImpactSound.apply(goat), SoundSource.NEUTRAL, 1.0F, 1.0F);
      } else if (this.hasRammedHornBreakingBlock(serverlevel, goat)) {
         serverlevel.playSound((Player)null, goat, this.getImpactSound.apply(goat), SoundSource.NEUTRAL, 1.0F, 1.0F);
         boolean flag = goat.dropHorn();
         if (flag) {
            serverlevel.playSound((Player)null, goat, this.getHornBreakSound.apply(goat), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }

         this.finishRam(serverlevel, goat);
      } else {
         Optional<WalkTarget> optional = brain.getMemory(MemoryModuleType.WALK_TARGET);
         Optional<Vec3> optional1 = brain.getMemory(MemoryModuleType.RAM_TARGET);
         boolean flag1 = optional.isEmpty() || optional1.isEmpty() || optional.get().getTarget().currentPosition().closerThan(optional1.get(), 0.25D);
         if (flag1) {
            this.finishRam(serverlevel, goat);
         }
      }

   }

   private boolean hasRammedHornBreakingBlock(ServerLevel serverlevel, Goat goat) {
      Vec3 vec3 = goat.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize();
      BlockPos blockpos = BlockPos.containing(goat.position().add(vec3));
      return serverlevel.getBlockState(blockpos).is(BlockTags.SNAPS_GOAT_HORN) || serverlevel.getBlockState(blockpos.above()).is(BlockTags.SNAPS_GOAT_HORN);
   }

   protected void finishRam(ServerLevel serverlevel, Goat goat) {
      serverlevel.broadcastEntityEvent(goat, (byte)59);
      goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getTimeBetweenRams.apply(goat).sample(serverlevel.random));
      goat.getBrain().eraseMemory(MemoryModuleType.RAM_TARGET);
   }
}
