package net.minecraft.world.entity.animal;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PolarBear extends Animal implements NeutralMob {
   private static final EntityDataAccessor<Boolean> DATA_STANDING_ID = SynchedEntityData.defineId(PolarBear.class, EntityDataSerializers.BOOLEAN);
   private static final float STAND_ANIMATION_TICKS = 6.0F;
   private float clientSideStandAnimationO;
   private float clientSideStandAnimation;
   private int warningSoundTicks;
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   private int remainingPersistentAngerTime;
   @Nullable
   private UUID persistentAngerTarget;

   public PolarBear(EntityType<? extends PolarBear> entitytype, Level level) {
      super(entitytype, level);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      return EntityType.POLAR_BEAR.create(serverlevel);
   }

   public boolean isFood(ItemStack itemstack) {
      return false;
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PolarBear.PolarBearMeleeAttackGoal());
      this.goalSelector.addGoal(1, new PolarBear.PolarBearPanicGoal());
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new PolarBear.PolarBearHurtByTargetGoal());
      this.targetSelector.addGoal(2, new PolarBear.PolarBearAttackPlayersGoal());
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Fox.class, 10, true, true, (Predicate<LivingEntity>)null));
      this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 6.0D);
   }

   public static boolean checkPolarBearSpawnRules(EntityType<PolarBear> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      Holder<Biome> holder = levelaccessor.getBiome(blockpos);
      if (!holder.is(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)) {
         return checkAnimalSpawnRules(entitytype, levelaccessor, mobspawntype, blockpos, randomsource);
      } else {
         return isBrightEnoughToSpawn(levelaccessor, blockpos) && levelaccessor.getBlockState(blockpos.below()).is(BlockTags.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE);
      }
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.readPersistentAngerSaveData(this.level(), compoundtag);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      this.addPersistentAngerSaveData(compoundtag);
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
   }

   public void setRemainingPersistentAngerTime(int i) {
      this.remainingPersistentAngerTime = i;
   }

   public int getRemainingPersistentAngerTime() {
      return this.remainingPersistentAngerTime;
   }

   public void setPersistentAngerTarget(@Nullable UUID uuid) {
      this.persistentAngerTarget = uuid;
   }

   @Nullable
   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : SoundEvents.POLAR_BEAR_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.POLAR_BEAR_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.POLAR_BEAR_DEATH;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
   }

   protected void playWarningSound() {
      if (this.warningSoundTicks <= 0) {
         this.playSound(SoundEvents.POLAR_BEAR_WARNING, 1.0F, this.getVoicePitch());
         this.warningSoundTicks = 40;
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_STANDING_ID, false);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.clientSideStandAnimation != this.clientSideStandAnimationO) {
            this.refreshDimensions();
         }

         this.clientSideStandAnimationO = this.clientSideStandAnimation;
         if (this.isStanding()) {
            this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
         } else {
            this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
         }
      }

      if (this.warningSoundTicks > 0) {
         --this.warningSoundTicks;
      }

      if (!this.level().isClientSide) {
         this.updatePersistentAnger((ServerLevel)this.level(), true);
      }

   }

   public EntityDimensions getDimensions(Pose pose) {
      if (this.clientSideStandAnimation > 0.0F) {
         float f = this.clientSideStandAnimation / 6.0F;
         float f1 = 1.0F + f;
         return super.getDimensions(pose).scale(1.0F, f1);
      } else {
         return super.getDimensions(pose);
      }
   }

   public boolean doHurtTarget(Entity entity) {
      boolean flag = entity.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag) {
         this.doEnchantDamageEffects(this, entity);
      }

      return flag;
   }

   public boolean isStanding() {
      return this.entityData.get(DATA_STANDING_ID);
   }

   public void setStanding(boolean flag) {
      this.entityData.set(DATA_STANDING_ID, flag);
   }

   public float getStandingAnimationScale(float f) {
      return Mth.lerp(f, this.clientSideStandAnimationO, this.clientSideStandAnimation) / 6.0F;
   }

   protected float getWaterSlowDown() {
      return 0.98F;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      if (spawngroupdata == null) {
         spawngroupdata = new AgeableMob.AgeableMobGroupData(1.0F);
      }

      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   class PolarBearAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
      public PolarBearAttackPlayersGoal() {
         super(PolarBear.this, Player.class, 20, true, true, (Predicate<LivingEntity>)null);
      }

      public boolean canUse() {
         if (PolarBear.this.isBaby()) {
            return false;
         } else {
            if (super.canUse()) {
               for(PolarBear polarbear : PolarBear.this.level().getEntitiesOfClass(PolarBear.class, PolarBear.this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D))) {
                  if (polarbear.isBaby()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected double getFollowDistance() {
         return super.getFollowDistance() * 0.5D;
      }
   }

   class PolarBearHurtByTargetGoal extends HurtByTargetGoal {
      public PolarBearHurtByTargetGoal() {
         super(PolarBear.this);
      }

      public void start() {
         super.start();
         if (PolarBear.this.isBaby()) {
            this.alertOthers();
            this.stop();
         }

      }

      protected void alertOther(Mob mob, LivingEntity livingentity) {
         if (mob instanceof PolarBear && !mob.isBaby()) {
            super.alertOther(mob, livingentity);
         }

      }
   }

   class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
      public PolarBearMeleeAttackGoal() {
         super(PolarBear.this, 1.25D, true);
      }

      protected void checkAndPerformAttack(LivingEntity livingentity, double d0) {
         double d1 = this.getAttackReachSqr(livingentity);
         if (d0 <= d1 && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.doHurtTarget(livingentity);
            PolarBear.this.setStanding(false);
         } else if (d0 <= d1 * 2.0D) {
            if (this.isTimeToAttack()) {
               PolarBear.this.setStanding(false);
               this.resetAttackCooldown();
            }

            if (this.getTicksUntilNextAttack() <= 10) {
               PolarBear.this.setStanding(true);
               PolarBear.this.playWarningSound();
            }
         } else {
            this.resetAttackCooldown();
            PolarBear.this.setStanding(false);
         }

      }

      public void stop() {
         PolarBear.this.setStanding(false);
         super.stop();
      }

      protected double getAttackReachSqr(LivingEntity livingentity) {
         return (double)(4.0F + livingentity.getBbWidth());
      }
   }

   class PolarBearPanicGoal extends PanicGoal {
      public PolarBearPanicGoal() {
         super(PolarBear.this, 2.0D);
      }

      protected boolean shouldPanic() {
         return this.mob.getLastHurtByMob() != null && this.mob.isBaby() || this.mob.isOnFire();
      }
   }
}
