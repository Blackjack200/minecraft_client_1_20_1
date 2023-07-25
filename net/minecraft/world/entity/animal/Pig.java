package net.minecraft.world.entity.animal;

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
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Pig extends Animal implements ItemSteerable, Saddleable {
   private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
   private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);

   public Pig(EntityType<? extends Pig> entitytype, Level level) {
      super(entitytype, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
      this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.2D, Ingredient.of(Items.CARROT_ON_A_STICK), false));
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.2D, FOOD_ITEMS, false));
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.25D);
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (this.isSaddled()) {
         Entity var2 = this.getFirstPassenger();
         if (var2 instanceof Player) {
            Player player = (Player)var2;
            if (player.getMainHandItem().is(Items.CARROT_ON_A_STICK) || player.getOffhandItem().is(Items.CARROT_ON_A_STICK)) {
               return player;
            }
         }
      }

      return null;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_BOOST_TIME.equals(entitydataaccessor) && this.level().isClientSide) {
         this.steering.onSynced();
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SADDLE_ID, false);
      this.entityData.define(DATA_BOOST_TIME, 0);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      this.steering.addAdditionalSaveData(compoundtag);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.steering.readAdditionalSaveData(compoundtag);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.PIG_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.PIG_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PIG_DEATH;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(SoundEvents.PIG_STEP, 0.15F, 1.0F);
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
            return interactionresult;
         }
      }
   }

   public boolean isSaddleable() {
      return this.isAlive() && !this.isBaby();
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.isSaddled()) {
         this.spawnAtLocation(Items.SADDLE);
      }

   }

   public boolean isSaddled() {
      return this.steering.hasSaddle();
   }

   public void equipSaddle(@Nullable SoundSource soundsource) {
      this.steering.setSaddle(true);
      if (soundsource != null) {
         this.level().playSound((Player)null, this, SoundEvents.PIG_SADDLE, soundsource, 0.5F, 1.0F);
      }

   }

   public Vec3 getDismountLocationForPassenger(LivingEntity livingentity) {
      Direction direction = this.getMotionDirection();
      if (direction.getAxis() == Direction.Axis.Y) {
         return super.getDismountLocationForPassenger(livingentity);
      } else {
         int[][] aint = DismountHelper.offsetsForDirection(direction);
         BlockPos blockpos = this.blockPosition();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(Pose pose : livingentity.getDismountPoses()) {
            AABB aabb = livingentity.getLocalBoundsForPose(pose);

            for(int[] aint1 : aint) {
               blockpos_mutableblockpos.set(blockpos.getX() + aint1[0], blockpos.getY(), blockpos.getZ() + aint1[1]);
               double d0 = this.level().getBlockFloorHeight(blockpos_mutableblockpos);
               if (DismountHelper.isBlockFloorValid(d0)) {
                  Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos_mutableblockpos, d0);
                  if (DismountHelper.canDismountTo(this.level(), livingentity, aabb.move(vec3))) {
                     livingentity.setPose(pose);
                     return vec3;
                  }
               }
            }
         }

         return super.getDismountLocationForPassenger(livingentity);
      }
   }

   public void thunderHit(ServerLevel serverlevel, LightningBolt lightningbolt) {
      if (serverlevel.getDifficulty() != Difficulty.PEACEFUL) {
         ZombifiedPiglin zombifiedpiglin = EntityType.ZOMBIFIED_PIGLIN.create(serverlevel);
         if (zombifiedpiglin != null) {
            zombifiedpiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            zombifiedpiglin.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            zombifiedpiglin.setNoAi(this.isNoAi());
            zombifiedpiglin.setBaby(this.isBaby());
            if (this.hasCustomName()) {
               zombifiedpiglin.setCustomName(this.getCustomName());
               zombifiedpiglin.setCustomNameVisible(this.isCustomNameVisible());
            }

            zombifiedpiglin.setPersistenceRequired();
            serverlevel.addFreshEntity(zombifiedpiglin);
            this.discard();
         } else {
            super.thunderHit(serverlevel, lightningbolt);
         }
      } else {
         super.thunderHit(serverlevel, lightningbolt);
      }

   }

   protected void tickRidden(Player player, Vec3 vec3) {
      super.tickRidden(player, vec3);
      this.setRot(player.getYRot(), player.getXRot() * 0.5F);
      this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
      this.steering.tickBoost();
   }

   protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
      return new Vec3(0.0D, 0.0D, 1.0D);
   }

   protected float getRiddenSpeed(Player player) {
      return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225D * (double)this.steering.boostFactor());
   }

   public boolean boost() {
      return this.steering.boost(this.getRandom());
   }

   @Nullable
   public Pig getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      return EntityType.PIG.create(serverlevel);
   }

   public boolean isFood(ItemStack itemstack) {
      return FOOD_ITEMS.test(itemstack);
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }
}
