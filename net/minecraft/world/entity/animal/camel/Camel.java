package net.minecraft.world.entity.animal.camel;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RiderShieldingMount;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Camel extends AbstractHorse implements PlayerRideableJumping, RiderShieldingMount, Saddleable {
   public static final Ingredient TEMPTATION_ITEM = Ingredient.of(Items.CACTUS);
   public static final int DASH_COOLDOWN_TICKS = 55;
   public static final int MAX_HEAD_Y_ROT = 30;
   private static final float RUNNING_SPEED_BONUS = 0.1F;
   private static final float DASH_VERTICAL_MOMENTUM = 1.4285F;
   private static final float DASH_HORIZONTAL_MOMENTUM = 22.2222F;
   private static final int DASH_MINIMUM_DURATION_TICKS = 5;
   private static final int SITDOWN_DURATION_TICKS = 40;
   private static final int STANDUP_DURATION_TICKS = 52;
   private static final int IDLE_MINIMAL_DURATION_TICKS = 80;
   private static final float SITTING_HEIGHT_DIFFERENCE = 1.43F;
   public static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.LONG);
   public final AnimationState sitAnimationState = new AnimationState();
   public final AnimationState sitPoseAnimationState = new AnimationState();
   public final AnimationState sitUpAnimationState = new AnimationState();
   public final AnimationState idleAnimationState = new AnimationState();
   public final AnimationState dashAnimationState = new AnimationState();
   private static final EntityDimensions SITTING_DIMENSIONS = EntityDimensions.scalable(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43F);
   private int dashCooldown = 0;
   private int idleAnimationTimeout = 0;

   public Camel(EntityType<? extends Camel> entitytype, Level level) {
      super(entitytype, level);
      this.setMaxUpStep(1.5F);
      this.moveControl = new Camel.CamelMoveControl();
      GroundPathNavigation groundpathnavigation = (GroundPathNavigation)this.getNavigation();
      groundpathnavigation.setCanFloat(true);
      groundpathnavigation.setCanWalkOverFences(true);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      long i = compoundtag.getLong("LastPoseTick");
      if (i < 0L) {
         this.setPose(Pose.SITTING);
      }

      this.resetLastPoseChangeTick(i);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 32.0D).add(Attributes.MOVEMENT_SPEED, (double)0.09F).add(Attributes.JUMP_STRENGTH, (double)0.42F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DASH, false);
      this.entityData.define(LAST_POSE_CHANGE_TICK, 0L);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      CamelAi.initMemories(this, serverlevelaccessor.getRandom());
      this.resetLastPoseChangeTickToFullStand(serverlevelaccessor.getLevel().getGameTime());
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   protected Brain.Provider<Camel> brainProvider() {
      return CamelAi.brainProvider();
   }

   protected void registerGoals() {
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return CamelAi.makeBrain(this.brainProvider().makeBrain(dynamic));
   }

   public EntityDimensions getDimensions(Pose pose) {
      return pose == Pose.SITTING ? SITTING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(pose);
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return entitydimensions.height - 0.1F;
   }

   public double getRiderShieldingHeight() {
      return 0.5D;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("camelBrain");
      Brain<?> brain = this.getBrain();
      brain.tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("camelActivityUpdate");
      CamelAi.updateActivity(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   public void tick() {
      super.tick();
      if (this.isDashing() && this.dashCooldown < 50 && (this.onGround() || this.isInWater() || this.isPassenger())) {
         this.setDashing(false);
      }

      if (this.dashCooldown > 0) {
         --this.dashCooldown;
         if (this.dashCooldown == 0) {
            this.level().playSound((Player)null, this.blockPosition(), SoundEvents.CAMEL_DASH_READY, SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      }

      if (this.level().isClientSide()) {
         this.setupAnimationStates();
      }

      if (this.refuseToMove()) {
         this.clampHeadRotationToBody(this, 30.0F);
      }

      if (this.isCamelSitting() && this.isInWater()) {
         this.standUpInstantly();
      }

   }

   private void setupAnimationStates() {
      if (this.idleAnimationTimeout <= 0) {
         this.idleAnimationTimeout = this.random.nextInt(40) + 80;
         this.idleAnimationState.start(this.tickCount);
      } else {
         --this.idleAnimationTimeout;
      }

      if (this.isCamelVisuallySitting()) {
         this.sitUpAnimationState.stop();
         this.dashAnimationState.stop();
         if (this.isVisuallySittingDown()) {
            this.sitAnimationState.startIfStopped(this.tickCount);
            this.sitPoseAnimationState.stop();
         } else {
            this.sitAnimationState.stop();
            this.sitPoseAnimationState.startIfStopped(this.tickCount);
         }
      } else {
         this.sitAnimationState.stop();
         this.sitPoseAnimationState.stop();
         this.dashAnimationState.animateWhen(this.isDashing(), this.tickCount);
         this.sitUpAnimationState.animateWhen(this.isInPoseTransition() && this.getPoseTime() >= 0L, this.tickCount);
      }

   }

   protected void updateWalkAnimation(float f) {
      float f1;
      if (this.getPose() == Pose.STANDING && !this.dashAnimationState.isStarted()) {
         f1 = Math.min(f * 6.0F, 1.0F);
      } else {
         f1 = 0.0F;
      }

      this.walkAnimation.update(f1, 0.2F);
   }

   public void travel(Vec3 vec3) {
      if (this.refuseToMove() && this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
         vec3 = vec3.multiply(0.0D, 1.0D, 0.0D);
      }

      super.travel(vec3);
   }

   protected void tickRidden(Player player, Vec3 vec3) {
      super.tickRidden(player, vec3);
      if (player.zza > 0.0F && this.isCamelSitting() && !this.isInPoseTransition()) {
         this.standUp();
      }

   }

   public boolean refuseToMove() {
      return this.isCamelSitting() || this.isInPoseTransition();
   }

   protected float getRiddenSpeed(Player player) {
      float f = player.isSprinting() && this.getJumpCooldown() == 0 ? 0.1F : 0.0F;
      return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
   }

   protected Vec2 getRiddenRotation(LivingEntity livingentity) {
      return this.refuseToMove() ? new Vec2(this.getXRot(), this.getYRot()) : super.getRiddenRotation(livingentity);
   }

   protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
      return this.refuseToMove() ? Vec3.ZERO : super.getRiddenInput(player, vec3);
   }

   public boolean canJump() {
      return !this.refuseToMove() && super.canJump();
   }

   public void onPlayerJump(int i) {
      if (this.isSaddled() && this.dashCooldown <= 0 && this.onGround()) {
         super.onPlayerJump(i);
      }
   }

   public boolean canSprint() {
      return true;
   }

   protected void executeRidersJump(float f, Vec3 vec3) {
      double d0 = this.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.getBlockJumpFactor() + (double)this.getJumpBoostPower();
      this.addDeltaMovement(this.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)(22.2222F * f) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0D, (double)(1.4285F * f) * d0, 0.0D));
      this.dashCooldown = 55;
      this.setDashing(true);
      this.hasImpulse = true;
   }

   public boolean isDashing() {
      return this.entityData.get(DASH);
   }

   public void setDashing(boolean flag) {
      this.entityData.set(DASH, flag);
   }

   public boolean isPanicking() {
      return this.getBrain().checkMemory(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_PRESENT);
   }

   public void handleStartJump(int i) {
      this.playSound(SoundEvents.CAMEL_DASH, 1.0F, 1.0F);
      this.setDashing(true);
   }

   public void handleStopJump() {
   }

   public int getJumpCooldown() {
      return this.dashCooldown;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.CAMEL_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.CAMEL_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.CAMEL_HURT;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      if (blockstate.getSoundType() == SoundType.SAND) {
         this.playSound(SoundEvents.CAMEL_STEP_SAND, 1.0F, 1.0F);
      } else {
         this.playSound(SoundEvents.CAMEL_STEP, 1.0F, 1.0F);
      }

   }

   public boolean isFood(ItemStack itemstack) {
      return TEMPTATION_ITEM.test(itemstack);
   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (player.isSecondaryUseActive() && !this.isBaby()) {
         this.openCustomInventoryScreen(player);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         InteractionResult interactionresult = itemstack.interactLivingEntity(player, this, interactionhand);
         if (interactionresult.consumesAction()) {
            return interactionresult;
         } else if (this.isFood(itemstack)) {
            return this.fedFood(player, itemstack);
         } else {
            if (this.getPassengers().size() < 2 && !this.isBaby()) {
               this.doPlayerRide(player);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      }
   }

   protected void onLeashDistance(float f) {
      if (f > 6.0F && this.isCamelSitting() && !this.isInPoseTransition()) {
         this.standUp();
      }

   }

   protected boolean handleEating(Player player, ItemStack itemstack) {
      if (!this.isFood(itemstack)) {
         return false;
      } else {
         boolean flag = this.getHealth() < this.getMaxHealth();
         if (flag) {
            this.heal(2.0F);
         }

         boolean flag1 = this.isTamed() && this.getAge() == 0 && this.canFallInLove();
         if (flag1) {
            this.setInLove(player);
         }

         boolean flag2 = this.isBaby();
         if (flag2) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            if (!this.level().isClientSide) {
               this.ageUp(10);
            }
         }

         if (!flag && !flag1 && !flag2) {
            return false;
         } else {
            if (!this.isSilent()) {
               SoundEvent soundevent = this.getEatingSound();
               if (soundevent != null) {
                  this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
               }
            }

            return true;
         }
      }
   }

   protected boolean canPerformRearing() {
      return false;
   }

   public boolean canMate(Animal animal) {
      if (animal != this && animal instanceof Camel camel) {
         if (this.canParent() && camel.canParent()) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public Camel getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      return EntityType.CAMEL.create(serverlevel);
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.CAMEL_EAT;
   }

   protected void actuallyHurt(DamageSource damagesource, float f) {
      this.standUpInstantly();
      super.actuallyHurt(damagesource, f);
   }

   protected void positionRider(Entity entity, Entity.MoveFunction entity_movefunction) {
      int i = this.getPassengers().indexOf(entity);
      if (i >= 0) {
         boolean flag = i == 0;
         float f = 0.5F;
         float f1 = (float)(this.isRemoved() ? (double)0.01F : this.getBodyAnchorAnimationYOffset(flag, 0.0F) + entity.getMyRidingOffset());
         if (this.getPassengers().size() > 1) {
            if (!flag) {
               f = -0.7F;
            }

            if (entity instanceof Animal) {
               f += 0.2F;
            }
         }

         Vec3 vec3 = (new Vec3(0.0D, 0.0D, (double)f)).yRot(-this.yBodyRot * ((float)Math.PI / 180F));
         entity_movefunction.accept(entity, this.getX() + vec3.x, this.getY() + (double)f1, this.getZ() + vec3.z);
         this.clampRotation(entity);
      }
   }

   private double getBodyAnchorAnimationYOffset(boolean flag, float f) {
      double d0 = this.getPassengersRidingOffset();
      float f1 = this.getScale() * 1.43F;
      float f2 = f1 - this.getScale() * 0.2F;
      float f3 = f1 - f2;
      boolean flag1 = this.isInPoseTransition();
      boolean flag2 = this.isCamelSitting();
      if (flag1) {
         int i = flag2 ? 40 : 52;
         int j;
         float f4;
         if (flag2) {
            j = 28;
            f4 = flag ? 0.5F : 0.1F;
         } else {
            j = flag ? 24 : 32;
            f4 = flag ? 0.6F : 0.35F;
         }

         float f6 = Mth.clamp((float)this.getPoseTime() + f, 0.0F, (float)i);
         boolean flag3 = f6 < (float)j;
         float f7 = flag3 ? f6 / (float)j : (f6 - (float)j) / (float)(i - j);
         float f8 = f1 - f4 * f2;
         d0 += flag2 ? (double)Mth.lerp(f7, flag3 ? f1 : f8, flag3 ? f8 : f3) : (double)Mth.lerp(f7, flag3 ? f3 - f1 : f3 - f8, flag3 ? f3 - f8 : 0.0F);
      }

      if (flag2 && !flag1) {
         d0 += (double)f3;
      }

      return d0;
   }

   public Vec3 getLeashOffset(float f) {
      return new Vec3(0.0D, this.getBodyAnchorAnimationYOffset(true, f) - (double)(0.2F * this.getScale()), (double)(this.getBbWidth() * 0.56F));
   }

   public double getPassengersRidingOffset() {
      return (double)(this.getDimensions(this.isCamelSitting() ? Pose.SITTING : Pose.STANDING).height - (this.isBaby() ? 0.35F : 0.6F));
   }

   public void onPassengerTurned(Entity entity) {
      if (this.getControllingPassenger() != entity) {
         this.clampRotation(entity);
      }

   }

   private void clampRotation(Entity entity) {
      entity.setYBodyRot(this.getYRot());
      float f = entity.getYRot();
      float f1 = Mth.wrapDegrees(f - this.getYRot());
      float f2 = Mth.clamp(f1, -160.0F, 160.0F);
      entity.yRotO += f2 - f1;
      float f3 = f + f2 - f1;
      entity.setYRot(f3);
      entity.setYHeadRot(f3);
   }

   private void clampHeadRotationToBody(Entity entity, float f) {
      float f1 = entity.getYHeadRot();
      float f2 = Mth.wrapDegrees(this.yBodyRot - f1);
      float f3 = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - f1), -f, f);
      float f4 = f1 + f2 - f3;
      entity.setYHeadRot(f4);
   }

   public int getMaxHeadYRot() {
      return 30;
   }

   protected boolean canAddPassenger(Entity entity) {
      return this.getPassengers().size() <= 2;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (!this.getPassengers().isEmpty() && this.isSaddled()) {
         Entity entity = this.getPassengers().get(0);
         if (entity instanceof LivingEntity) {
            return (LivingEntity)entity;
         }
      }

      return null;
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public boolean isCamelSitting() {
      return this.entityData.get(LAST_POSE_CHANGE_TICK) < 0L;
   }

   public boolean isCamelVisuallySitting() {
      return this.getPoseTime() < 0L != this.isCamelSitting();
   }

   public boolean isInPoseTransition() {
      long i = this.getPoseTime();
      return i < (long)(this.isCamelSitting() ? 40 : 52);
   }

   private boolean isVisuallySittingDown() {
      return this.isCamelSitting() && this.getPoseTime() < 40L && this.getPoseTime() >= 0L;
   }

   public void sitDown() {
      if (!this.isCamelSitting()) {
         this.playSound(SoundEvents.CAMEL_SIT, 1.0F, 1.0F);
         this.setPose(Pose.SITTING);
         this.resetLastPoseChangeTick(-this.level().getGameTime());
      }
   }

   public void standUp() {
      if (this.isCamelSitting()) {
         this.playSound(SoundEvents.CAMEL_STAND, 1.0F, 1.0F);
         this.setPose(Pose.STANDING);
         this.resetLastPoseChangeTick(this.level().getGameTime());
      }
   }

   public void standUpInstantly() {
      this.setPose(Pose.STANDING);
      this.resetLastPoseChangeTickToFullStand(this.level().getGameTime());
   }

   @VisibleForTesting
   public void resetLastPoseChangeTick(long i) {
      this.entityData.set(LAST_POSE_CHANGE_TICK, i);
   }

   private void resetLastPoseChangeTickToFullStand(long i) {
      this.resetLastPoseChangeTick(Math.max(0L, i - 52L - 1L));
   }

   public long getPoseTime() {
      return this.level().getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
   }

   public SoundEvent getSaddleSoundEvent() {
      return SoundEvents.CAMEL_SADDLE;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (!this.firstTick && DASH.equals(entitydataaccessor)) {
         this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   protected BodyRotationControl createBodyControl() {
      return new Camel.CamelBodyRotationControl(this);
   }

   public boolean isTamed() {
      return true;
   }

   public void openCustomInventoryScreen(Player player) {
      if (!this.level().isClientSide) {
         player.openHorseInventory(this, this.inventory);
      }

   }

   class CamelBodyRotationControl extends BodyRotationControl {
      public CamelBodyRotationControl(Camel camel) {
         super(camel);
      }

      public void clientTick() {
         if (!Camel.this.refuseToMove()) {
            super.clientTick();
         }

      }
   }

   class CamelMoveControl extends MoveControl {
      public CamelMoveControl() {
         super(Camel.this);
      }

      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO && !Camel.this.isLeashed() && Camel.this.isCamelSitting() && !Camel.this.isInPoseTransition()) {
            Camel.this.standUp();
         }

         super.tick();
      }
   }
}
