package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WitherBoss extends Monster implements PowerableMob, RangedAttackMob {
   private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
   private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
   private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
   private static final int INVULNERABLE_TICKS = 220;
   private final float[] xRotHeads = new float[2];
   private final float[] yRotHeads = new float[2];
   private final float[] xRotOHeads = new float[2];
   private final float[] yRotOHeads = new float[2];
   private final int[] nextHeadUpdate = new int[2];
   private final int[] idleHeadUpdates = new int[2];
   private int destroyBlocksTick;
   private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
   private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = (livingentity) -> livingentity.getMobType() != MobType.UNDEAD && livingentity.attackable();
   private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().range(20.0D).selector(LIVING_ENTITY_SELECTOR);

   public WitherBoss(EntityType<? extends WitherBoss> entitytype, Level level) {
      super(entitytype, level);
      this.moveControl = new FlyingMoveControl(this, 10, false);
      this.setHealth(this.getMaxHealth());
      this.xpReward = 50;
   }

   protected PathNavigation createNavigation(Level level) {
      FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
      flyingpathnavigation.setCanOpenDoors(false);
      flyingpathnavigation.setCanFloat(true);
      flyingpathnavigation.setCanPassDoors(true);
      return flyingpathnavigation;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new WitherBoss.WitherDoNothingGoal());
      this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 20.0F));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TARGET_A, 0);
      this.entityData.define(DATA_TARGET_B, 0);
      this.entityData.define(DATA_TARGET_C, 0);
      this.entityData.define(DATA_ID_INV, 0);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Invul", this.getInvulnerableTicks());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setInvulnerableTicks(compoundtag.getInt("Invul"));
      if (this.hasCustomName()) {
         this.bossEvent.setName(this.getDisplayName());
      }

   }

   public void setCustomName(@Nullable Component component) {
      super.setCustomName(component);
      this.bossEvent.setName(this.getDisplayName());
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.WITHER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.WITHER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_DEATH;
   }

   public void aiStep() {
      Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D);
      if (!this.level().isClientSide && this.getAlternativeTarget(0) > 0) {
         Entity entity = this.level().getEntity(this.getAlternativeTarget(0));
         if (entity != null) {
            double d0 = vec3.y;
            if (this.getY() < entity.getY() || !this.isPowered() && this.getY() < entity.getY() + 5.0D) {
               d0 = Math.max(0.0D, d0);
               d0 += 0.3D - d0 * (double)0.6F;
            }

            vec3 = new Vec3(vec3.x, d0, vec3.z);
            Vec3 vec31 = new Vec3(entity.getX() - this.getX(), 0.0D, entity.getZ() - this.getZ());
            if (vec31.horizontalDistanceSqr() > 9.0D) {
               Vec3 vec32 = vec31.normalize();
               vec3 = vec3.add(vec32.x * 0.3D - vec3.x * 0.6D, 0.0D, vec32.z * 0.3D - vec3.z * 0.6D);
            }
         }
      }

      this.setDeltaMovement(vec3);
      if (vec3.horizontalDistanceSqr() > 0.05D) {
         this.setYRot((float)Mth.atan2(vec3.z, vec3.x) * (180F / (float)Math.PI) - 90.0F);
      }

      super.aiStep();

      for(int i = 0; i < 2; ++i) {
         this.yRotOHeads[i] = this.yRotHeads[i];
         this.xRotOHeads[i] = this.xRotHeads[i];
      }

      for(int j = 0; j < 2; ++j) {
         int k = this.getAlternativeTarget(j + 1);
         Entity entity1 = null;
         if (k > 0) {
            entity1 = this.level().getEntity(k);
         }

         if (entity1 != null) {
            double d1 = this.getHeadX(j + 1);
            double d2 = this.getHeadY(j + 1);
            double d3 = this.getHeadZ(j + 1);
            double d4 = entity1.getX() - d1;
            double d5 = entity1.getEyeY() - d2;
            double d6 = entity1.getZ() - d3;
            double d7 = Math.sqrt(d4 * d4 + d6 * d6);
            float f = (float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)) - 90.0F;
            float f1 = (float)(-(Mth.atan2(d5, d7) * (double)(180F / (float)Math.PI)));
            this.xRotHeads[j] = this.rotlerp(this.xRotHeads[j], f1, 40.0F);
            this.yRotHeads[j] = this.rotlerp(this.yRotHeads[j], f, 10.0F);
         } else {
            this.yRotHeads[j] = this.rotlerp(this.yRotHeads[j], this.yBodyRot, 10.0F);
         }
      }

      boolean flag = this.isPowered();

      for(int l = 0; l < 3; ++l) {
         double d8 = this.getHeadX(l);
         double d9 = this.getHeadY(l);
         double d10 = this.getHeadZ(l);
         this.level().addParticle(ParticleTypes.SMOKE, d8 + this.random.nextGaussian() * (double)0.3F, d9 + this.random.nextGaussian() * (double)0.3F, d10 + this.random.nextGaussian() * (double)0.3F, 0.0D, 0.0D, 0.0D);
         if (flag && this.level().random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, d8 + this.random.nextGaussian() * (double)0.3F, d9 + this.random.nextGaussian() * (double)0.3F, d10 + this.random.nextGaussian() * (double)0.3F, (double)0.7F, (double)0.7F, 0.5D);
         }
      }

      if (this.getInvulnerableTicks() > 0) {
         for(int i1 = 0; i1 < 3; ++i1) {
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * 3.3F), this.getZ() + this.random.nextGaussian(), (double)0.7F, (double)0.7F, (double)0.9F);
         }
      }

   }

   protected void customServerAiStep() {
      if (this.getInvulnerableTicks() > 0) {
         int i = this.getInvulnerableTicks() - 1;
         this.bossEvent.setProgress(1.0F - (float)i / 220.0F);
         if (i <= 0) {
            this.level().explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, Level.ExplosionInteraction.MOB);
            if (!this.isSilent()) {
               this.level().globalLevelEvent(1023, this.blockPosition(), 0);
            }
         }

         this.setInvulnerableTicks(i);
         if (this.tickCount % 10 == 0) {
            this.heal(10.0F);
         }

      } else {
         super.customServerAiStep();

         for(int j = 1; j < 3; ++j) {
            if (this.tickCount >= this.nextHeadUpdate[j - 1]) {
               this.nextHeadUpdate[j - 1] = this.tickCount + 10 + this.random.nextInt(10);
               if (this.level().getDifficulty() == Difficulty.NORMAL || this.level().getDifficulty() == Difficulty.HARD) {
                  int var10001 = j - 1;
                  int var10003 = this.idleHeadUpdates[j - 1];
                  this.idleHeadUpdates[var10001] = this.idleHeadUpdates[j - 1] + 1;
                  if (var10003 > 15) {
                     float f = 10.0F;
                     float f1 = 5.0F;
                     double d0 = Mth.nextDouble(this.random, this.getX() - 10.0D, this.getX() + 10.0D);
                     double d1 = Mth.nextDouble(this.random, this.getY() - 5.0D, this.getY() + 5.0D);
                     double d2 = Mth.nextDouble(this.random, this.getZ() - 10.0D, this.getZ() + 10.0D);
                     this.performRangedAttack(j + 1, d0, d1, d2, true);
                     this.idleHeadUpdates[j - 1] = 0;
                  }
               }

               int k = this.getAlternativeTarget(j);
               if (k > 0) {
                  LivingEntity livingentity = (LivingEntity)this.level().getEntity(k);
                  if (livingentity != null && this.canAttack(livingentity) && !(this.distanceToSqr(livingentity) > 900.0D) && this.hasLineOfSight(livingentity)) {
                     this.performRangedAttack(j + 1, livingentity);
                     this.nextHeadUpdate[j - 1] = this.tickCount + 40 + this.random.nextInt(20);
                     this.idleHeadUpdates[j - 1] = 0;
                  } else {
                     this.setAlternativeTarget(j, 0);
                  }
               } else {
                  List<LivingEntity> list = this.level().getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0D, 8.0D, 20.0D));
                  if (!list.isEmpty()) {
                     LivingEntity livingentity1 = list.get(this.random.nextInt(list.size()));
                     this.setAlternativeTarget(j, livingentity1.getId());
                  }
               }
            }
         }

         if (this.getTarget() != null) {
            this.setAlternativeTarget(0, this.getTarget().getId());
         } else {
            this.setAlternativeTarget(0, 0);
         }

         if (this.destroyBlocksTick > 0) {
            --this.destroyBlocksTick;
            if (this.destroyBlocksTick == 0 && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
               int l = Mth.floor(this.getY());
               int i1 = Mth.floor(this.getX());
               int j1 = Mth.floor(this.getZ());
               boolean flag = false;

               for(int k1 = -1; k1 <= 1; ++k1) {
                  for(int l1 = -1; l1 <= 1; ++l1) {
                     for(int i2 = 0; i2 <= 3; ++i2) {
                        int j2 = i1 + k1;
                        int k2 = l + i2;
                        int l2 = j1 + l1;
                        BlockPos blockpos = new BlockPos(j2, k2, l2);
                        BlockState blockstate = this.level().getBlockState(blockpos);
                        if (canDestroy(blockstate)) {
                           flag = this.level().destroyBlock(blockpos, true, this) || flag;
                        }
                     }
                  }
               }

               if (flag) {
                  this.level().levelEvent((Player)null, 1022, this.blockPosition(), 0);
               }
            }
         }

         if (this.tickCount % 20 == 0) {
            this.heal(1.0F);
         }

         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
      }
   }

   public static boolean canDestroy(BlockState blockstate) {
      return !blockstate.isAir() && !blockstate.is(BlockTags.WITHER_IMMUNE);
   }

   public void makeInvulnerable() {
      this.setInvulnerableTicks(220);
      this.bossEvent.setProgress(0.0F);
      this.setHealth(this.getMaxHealth() / 3.0F);
   }

   public void makeStuckInBlock(BlockState blockstate, Vec3 vec3) {
   }

   public void startSeenByPlayer(ServerPlayer serverplayer) {
      super.startSeenByPlayer(serverplayer);
      this.bossEvent.addPlayer(serverplayer);
   }

   public void stopSeenByPlayer(ServerPlayer serverplayer) {
      super.stopSeenByPlayer(serverplayer);
      this.bossEvent.removePlayer(serverplayer);
   }

   private double getHeadX(int i) {
      if (i <= 0) {
         return this.getX();
      } else {
         float f = (this.yBodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180F);
         float f1 = Mth.cos(f);
         return this.getX() + (double)f1 * 1.3D;
      }
   }

   private double getHeadY(int i) {
      return i <= 0 ? this.getY() + 3.0D : this.getY() + 2.2D;
   }

   private double getHeadZ(int i) {
      if (i <= 0) {
         return this.getZ();
      } else {
         float f = (this.yBodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180F);
         float f1 = Mth.sin(f);
         return this.getZ() + (double)f1 * 1.3D;
      }
   }

   private float rotlerp(float f, float f1, float f2) {
      float f3 = Mth.wrapDegrees(f1 - f);
      if (f3 > f2) {
         f3 = f2;
      }

      if (f3 < -f2) {
         f3 = -f2;
      }

      return f + f3;
   }

   private void performRangedAttack(int i, LivingEntity livingentity) {
      this.performRangedAttack(i, livingentity.getX(), livingentity.getY() + (double)livingentity.getEyeHeight() * 0.5D, livingentity.getZ(), i == 0 && this.random.nextFloat() < 0.001F);
   }

   private void performRangedAttack(int i, double d0, double d1, double d2, boolean flag) {
      if (!this.isSilent()) {
         this.level().levelEvent((Player)null, 1024, this.blockPosition(), 0);
      }

      double d3 = this.getHeadX(i);
      double d4 = this.getHeadY(i);
      double d5 = this.getHeadZ(i);
      double d6 = d0 - d3;
      double d7 = d1 - d4;
      double d8 = d2 - d5;
      WitherSkull witherskull = new WitherSkull(this.level(), this, d6, d7, d8);
      witherskull.setOwner(this);
      if (flag) {
         witherskull.setDangerous(true);
      }

      witherskull.setPosRaw(d3, d4, d5);
      this.level().addFreshEntity(witherskull);
   }

   public void performRangedAttack(LivingEntity livingentity, float f) {
      this.performRangedAttack(0, livingentity);
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (!damagesource.is(DamageTypeTags.WITHER_IMMUNE_TO) && !(damagesource.getEntity() instanceof WitherBoss)) {
         if (this.getInvulnerableTicks() > 0 && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
         } else {
            if (this.isPowered()) {
               Entity entity = damagesource.getDirectEntity();
               if (entity instanceof AbstractArrow) {
                  return false;
               }
            }

            Entity entity1 = damagesource.getEntity();
            if (entity1 != null && !(entity1 instanceof Player) && entity1 instanceof LivingEntity && ((LivingEntity)entity1).getMobType() == this.getMobType()) {
               return false;
            } else {
               if (this.destroyBlocksTick <= 0) {
                  this.destroyBlocksTick = 20;
               }

               for(int i = 0; i < this.idleHeadUpdates.length; ++i) {
                  this.idleHeadUpdates[i] += 3;
               }

               return super.hurt(damagesource, f);
            }
         }
      } else {
         return false;
      }
   }

   protected void dropCustomDeathLoot(DamageSource damagesource, int i, boolean flag) {
      super.dropCustomDeathLoot(damagesource, i, flag);
      ItemEntity itementity = this.spawnAtLocation(Items.NETHER_STAR);
      if (itementity != null) {
         itementity.setExtendedLifetime();
      }

   }

   public void checkDespawn() {
      if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
         this.discard();
      } else {
         this.noActionTime = 0;
      }
   }

   public boolean addEffect(MobEffectInstance mobeffectinstance, @Nullable Entity entity) {
      return false;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0D).add(Attributes.MOVEMENT_SPEED, (double)0.6F).add(Attributes.FLYING_SPEED, (double)0.6F).add(Attributes.FOLLOW_RANGE, 40.0D).add(Attributes.ARMOR, 4.0D);
   }

   public float getHeadYRot(int i) {
      return this.yRotHeads[i];
   }

   public float getHeadXRot(int i) {
      return this.xRotHeads[i];
   }

   public int getInvulnerableTicks() {
      return this.entityData.get(DATA_ID_INV);
   }

   public void setInvulnerableTicks(int i) {
      this.entityData.set(DATA_ID_INV, i);
   }

   public int getAlternativeTarget(int i) {
      return this.entityData.get(DATA_TARGETS.get(i));
   }

   public void setAlternativeTarget(int i, int j) {
      this.entityData.set(DATA_TARGETS.get(i), j);
   }

   public boolean isPowered() {
      return this.getHealth() <= this.getMaxHealth() / 2.0F;
   }

   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   protected boolean canRide(Entity entity) {
      return false;
   }

   public boolean canChangeDimensions() {
      return false;
   }

   public boolean canBeAffected(MobEffectInstance mobeffectinstance) {
      return mobeffectinstance.getEffect() == MobEffects.WITHER ? false : super.canBeAffected(mobeffectinstance);
   }

   class WitherDoNothingGoal extends Goal {
      public WitherDoNothingGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
      }

      public boolean canUse() {
         return WitherBoss.this.getInvulnerableTicks() > 0;
      }
   }
}
