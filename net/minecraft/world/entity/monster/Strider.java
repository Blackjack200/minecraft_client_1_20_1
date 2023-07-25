package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Strider extends Animal implements ItemSteerable, Saddleable {
   private static final UUID SUFFOCATING_MODIFIER_UUID = UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
   private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(SUFFOCATING_MODIFIER_UUID, "Strider suffocating modifier", (double)-0.34F, AttributeModifier.Operation.MULTIPLY_BASE);
   private static final float SUFFOCATE_STEERING_MODIFIER = 0.35F;
   private static final float STEERING_MODIFIER = 0.55F;
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WARPED_FUNGUS);
   private static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
   private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
   private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
   @Nullable
   private TemptGoal temptGoal;
   @Nullable
   private PanicGoal panicGoal;

   public Strider(EntityType<? extends Strider> entitytype, Level level) {
      super(entitytype, level);
      this.blocksBuilding = true;
      this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
   }

   public static boolean checkStriderSpawnRules(EntityType<Strider> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      do {
         blockpos_mutableblockpos.move(Direction.UP);
      } while(levelaccessor.getFluidState(blockpos_mutableblockpos).is(FluidTags.LAVA));

      return levelaccessor.getBlockState(blockpos_mutableblockpos).isAir();
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_BOOST_TIME.equals(entitydataaccessor) && this.level().isClientSide) {
         this.steering.onSynced();
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BOOST_TIME, 0);
      this.entityData.define(DATA_SUFFOCATING, false);
      this.entityData.define(DATA_SADDLE_ID, false);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      this.steering.addAdditionalSaveData(compoundtag);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.steering.readAdditionalSaveData(compoundtag);
   }

   public boolean isSaddled() {
      return this.steering.hasSaddle();
   }

   public boolean isSaddleable() {
      return this.isAlive() && !this.isBaby();
   }

   public void equipSaddle(@Nullable SoundSource soundsource) {
      this.steering.setSaddle(true);
      if (soundsource != null) {
         this.level().playSound((Player)null, this, SoundEvents.STRIDER_SADDLE, soundsource, 0.5F, 1.0F);
      }

   }

   protected void registerGoals() {
      this.panicGoal = new PanicGoal(this, 1.65D);
      this.goalSelector.addGoal(1, this.panicGoal);
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
      this.temptGoal = new TemptGoal(this, 1.4D, TEMPT_ITEMS, false);
      this.goalSelector.addGoal(3, this.temptGoal);
      this.goalSelector.addGoal(4, new Strider.StriderGoToLavaGoal(this, 1.0D));
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D, 60));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
   }

   public void setSuffocating(boolean flag) {
      this.entityData.set(DATA_SUFFOCATING, flag);
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attributeinstance != null) {
         attributeinstance.removeModifier(SUFFOCATING_MODIFIER_UUID);
         if (flag) {
            attributeinstance.addTransientModifier(SUFFOCATING_MODIFIER);
         }
      }

   }

   public boolean isSuffocating() {
      return this.entityData.get(DATA_SUFFOCATING);
   }

   public boolean canStandOnFluid(FluidState fluidstate) {
      return fluidstate.is(FluidTags.LAVA);
   }

   public double getPassengersRidingOffset() {
      float f = Math.min(0.25F, this.walkAnimation.speed());
      float f1 = this.walkAnimation.position();
      return (double)this.getBbHeight() - 0.19D + (double)(0.12F * Mth.cos(f1 * 1.5F) * 2.0F * f);
   }

   public boolean checkSpawnObstruction(LevelReader levelreader) {
      return levelreader.isUnobstructed(this);
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity var2 = this.getFirstPassenger();
      if (var2 instanceof Player player) {
         if (player.getMainHandItem().is(Items.WARPED_FUNGUS_ON_A_STICK) || player.getOffhandItem().is(Items.WARPED_FUNGUS_ON_A_STICK)) {
            return player;
         }
      }

      return null;
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity livingentity) {
      Vec3[] avec3 = new Vec3[]{getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), livingentity.getYRot()), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), livingentity.getYRot() - 22.5F), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), livingentity.getYRot() + 22.5F), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), livingentity.getYRot() - 45.0F), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), livingentity.getYRot() + 45.0F)};
      Set<BlockPos> set = Sets.newLinkedHashSet();
      double d0 = this.getBoundingBox().maxY;
      double d1 = this.getBoundingBox().minY - 0.5D;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Vec3 vec3 : avec3) {
         blockpos_mutableblockpos.set(this.getX() + vec3.x, d0, this.getZ() + vec3.z);

         for(double d2 = d0; d2 > d1; --d2) {
            set.add(blockpos_mutableblockpos.immutable());
            blockpos_mutableblockpos.move(Direction.DOWN);
         }
      }

      for(BlockPos blockpos : set) {
         if (!this.level().getFluidState(blockpos).is(FluidTags.LAVA)) {
            double d3 = this.level().getBlockFloorHeight(blockpos);
            if (DismountHelper.isBlockFloorValid(d3)) {
               Vec3 vec31 = Vec3.upFromBottomCenterOf(blockpos, d3);

               for(Pose pose : livingentity.getDismountPoses()) {
                  AABB aabb = livingentity.getLocalBoundsForPose(pose);
                  if (DismountHelper.canDismountTo(this.level(), livingentity, aabb.move(vec31))) {
                     livingentity.setPose(pose);
                     return vec31;
                  }
               }
            }
         }
      }

      return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   protected void tickRidden(Player player, Vec3 vec3) {
      this.setRot(player.getYRot(), player.getXRot() * 0.5F);
      this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
      this.steering.tickBoost();
      super.tickRidden(player, vec3);
   }

   protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
      return new Vec3(0.0D, 0.0D, 1.0D);
   }

   protected float getRiddenSpeed(Player player) {
      return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)(this.isSuffocating() ? 0.35F : 0.55F) * (double)this.steering.boostFactor());
   }

   protected float nextStep() {
      return this.moveDist + 0.6F;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0F, 1.0F);
   }

   public boolean boost() {
      return this.steering.boost(this.getRandom());
   }

   protected void checkFallDamage(double d0, boolean flag, BlockState blockstate, BlockPos blockpos) {
      this.checkInsideBlocks();
      if (this.isInLava()) {
         this.resetFallDistance();
      } else {
         super.checkFallDamage(d0, flag, blockstate, blockpos);
      }
   }

   public void tick() {
      if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
         this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, this.getVoicePitch());
      } else if (this.isPanicking() && this.random.nextInt(60) == 0) {
         this.playSound(SoundEvents.STRIDER_RETREAT, 1.0F, this.getVoicePitch());
      }

      if (!this.isNoAi()) {
         boolean flag;
         boolean var10000;
         label36: {
            BlockState blockstate = this.level().getBlockState(this.blockPosition());
            BlockState blockstate1 = this.getBlockStateOnLegacy();
            flag = blockstate.is(BlockTags.STRIDER_WARM_BLOCKS) || blockstate1.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0D;
            Entity var6 = this.getVehicle();
            if (var6 instanceof Strider) {
               Strider strider = (Strider)var6;
               if (strider.isSuffocating()) {
                  var10000 = true;
                  break label36;
               }
            }

            var10000 = false;
         }

         boolean flag1 = var10000;
         this.setSuffocating(!flag || flag1);
      }

      super.tick();
      this.floatStrider();
      this.checkInsideBlocks();
   }

   private boolean isPanicking() {
      return this.panicGoal != null && this.panicGoal.isRunning();
   }

   private boolean isBeingTempted() {
      return this.temptGoal != null && this.temptGoal.isRunning();
   }

   protected boolean shouldPassengersInheritMalus() {
      return true;
   }

   private void floatStrider() {
      if (this.isInLava()) {
         CollisionContext collisioncontext = CollisionContext.of(this);
         if (collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) && !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
            this.setOnGround(true);
         } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(0.0D, 0.05D, 0.0D));
         }
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.175F).add(Attributes.FOLLOW_RANGE, 16.0D);
   }

   protected SoundEvent getAmbientSound() {
      return !this.isPanicking() && !this.isBeingTempted() ? SoundEvents.STRIDER_AMBIENT : null;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.STRIDER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.STRIDER_DEATH;
   }

   protected boolean canAddPassenger(Entity entity) {
      return !this.isVehicle() && !this.isEyeInFluid(FluidTags.LAVA);
   }

   public boolean isSensitiveToWater() {
      return true;
   }

   public boolean isOnFire() {
      return false;
   }

   protected PathNavigation createNavigation(Level level) {
      return new Strider.StriderPathNavigation(this, level);
   }

   public float getWalkTargetValue(BlockPos blockpos, LevelReader levelreader) {
      if (levelreader.getBlockState(blockpos).getFluidState().is(FluidTags.LAVA)) {
         return 10.0F;
      } else {
         return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
      }
   }

   @Nullable
   public Strider getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      return EntityType.STRIDER.create(serverlevel);
   }

   public boolean isFood(ItemStack itemstack) {
      return FOOD_ITEMS.test(itemstack);
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.isSaddled()) {
         this.spawnAtLocation(Items.SADDLE);
      }

   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      boolean flag = this.isFood(player.getItemInHand(interactionhand));
      if (!flag && this.isSaddled() && !this.isVehicle() && !player.isSecondaryUseActive()) {
         if (!this.level().isClientSide) {
            player.startRiding(this);
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         InteractionResult interactionresult = super.mobInteract(player, interactionhand);
         if (!interactionresult.consumesAction()) {
            ItemStack itemstack = player.getItemInHand(interactionhand);
            return itemstack.is(Items.SADDLE) ? itemstack.interactLivingEntity(player, this, interactionhand) : InteractionResult.PASS;
         } else {
            if (flag && !this.isSilent()) {
               this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.STRIDER_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            }

            return interactionresult;
         }
      }
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      if (this.isBaby()) {
         return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
      } else {
         RandomSource randomsource = serverlevelaccessor.getRandom();
         if (randomsource.nextInt(30) == 0) {
            Mob mob = EntityType.ZOMBIFIED_PIGLIN.create(serverlevelaccessor.getLevel());
            if (mob != null) {
               spawngroupdata = this.spawnJockey(serverlevelaccessor, difficultyinstance, mob, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(randomsource), false));
               mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
               this.equipSaddle((SoundSource)null);
            }
         } else if (randomsource.nextInt(10) == 0) {
            AgeableMob ageablemob = EntityType.STRIDER.create(serverlevelaccessor.getLevel());
            if (ageablemob != null) {
               ageablemob.setAge(-24000);
               spawngroupdata = this.spawnJockey(serverlevelaccessor, difficultyinstance, ageablemob, (SpawnGroupData)null);
            }
         } else {
            spawngroupdata = new AgeableMob.AgeableMobGroupData(0.5F);
         }

         return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
      }
   }

   private SpawnGroupData spawnJockey(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, Mob mob, @Nullable SpawnGroupData spawngroupdata) {
      mob.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
      mob.finalizeSpawn(serverlevelaccessor, difficultyinstance, MobSpawnType.JOCKEY, spawngroupdata, (CompoundTag)null);
      mob.startRiding(this, true);
      return new AgeableMob.AgeableMobGroupData(0.0F);
   }

   static class StriderGoToLavaGoal extends MoveToBlockGoal {
      private final Strider strider;

      StriderGoToLavaGoal(Strider strider, double d0) {
         super(strider, d0, 8, 2);
         this.strider = strider;
      }

      public BlockPos getMoveToTarget() {
         return this.blockPos;
      }

      public boolean canContinueToUse() {
         return !this.strider.isInLava() && this.isValidTarget(this.strider.level(), this.blockPos);
      }

      public boolean canUse() {
         return !this.strider.isInLava() && super.canUse();
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 20 == 0;
      }

      protected boolean isValidTarget(LevelReader levelreader, BlockPos blockpos) {
         return levelreader.getBlockState(blockpos).is(Blocks.LAVA) && levelreader.getBlockState(blockpos.above()).isPathfindable(levelreader, blockpos, PathComputationType.LAND);
      }
   }

   static class StriderPathNavigation extends GroundPathNavigation {
      StriderPathNavigation(Strider strider, Level level) {
         super(strider, level);
      }

      protected PathFinder createPathFinder(int i) {
         this.nodeEvaluator = new WalkNodeEvaluator();
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, i);
      }

      protected boolean hasValidPathType(BlockPathTypes blockpathtypes) {
         return blockpathtypes != BlockPathTypes.LAVA && blockpathtypes != BlockPathTypes.DAMAGE_FIRE && blockpathtypes != BlockPathTypes.DANGER_FIRE ? super.hasValidPathType(blockpathtypes) : true;
      }

      public boolean isStableDestination(BlockPos blockpos) {
         return this.level.getBlockState(blockpos).is(Blocks.LAVA) || super.isStableDestination(blockpos);
      }
   }
}
