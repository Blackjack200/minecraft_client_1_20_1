package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHorse extends Animal implements ContainerListener, HasCustomInventoryScreen, OwnableEntity, PlayerRideableJumping, Saddleable {
   public static final int EQUIPMENT_SLOT_OFFSET = 400;
   public static final int CHEST_SLOT_OFFSET = 499;
   public static final int INVENTORY_SLOT_OFFSET = 500;
   public static final double BREEDING_CROSS_FACTOR = 0.15D;
   private static final float MIN_MOVEMENT_SPEED = (float)generateSpeed(() -> 0.0D);
   private static final float MAX_MOVEMENT_SPEED = (float)generateSpeed(() -> 1.0D);
   private static final float MIN_JUMP_STRENGTH = (float)generateJumpStrength(() -> 0.0D);
   private static final float MAX_JUMP_STRENGTH = (float)generateJumpStrength(() -> 1.0D);
   private static final float MIN_HEALTH = generateMaxHealth((i) -> 0);
   private static final float MAX_HEALTH = generateMaxHealth((i) -> i - 1);
   private static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25F;
   private static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5F;
   private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = (livingentity) -> livingentity instanceof AbstractHorse && ((AbstractHorse)livingentity).isBred();
   private static final TargetingConditions MOMMY_TARGETING = TargetingConditions.forNonCombat().range(16.0D).ignoreLineOfSight().selector(PARENT_HORSE_SELECTOR);
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
   private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
   private static final int FLAG_TAME = 2;
   private static final int FLAG_SADDLE = 4;
   private static final int FLAG_BRED = 8;
   private static final int FLAG_EATING = 16;
   private static final int FLAG_STANDING = 32;
   private static final int FLAG_OPEN_MOUTH = 64;
   public static final int INV_SLOT_SADDLE = 0;
   public static final int INV_SLOT_ARMOR = 1;
   public static final int INV_BASE_COUNT = 2;
   private int eatingCounter;
   private int mouthCounter;
   private int standCounter;
   public int tailCounter;
   public int sprintCounter;
   protected boolean isJumping;
   protected SimpleContainer inventory;
   protected int temper;
   protected float playerJumpPendingScale;
   protected boolean allowStandSliding;
   private float eatAnim;
   private float eatAnimO;
   private float standAnim;
   private float standAnimO;
   private float mouthAnim;
   private float mouthAnimO;
   protected boolean canGallop = true;
   protected int gallopSoundCounter;
   @Nullable
   private UUID owner;

   protected AbstractHorse(EntityType<? extends AbstractHorse> entitytype, Level level) {
      super(entitytype, level);
      this.setMaxUpStep(1.0F);
      this.createInventory();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
      this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D, AbstractHorse.class));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      if (this.canPerformRearing()) {
         this.goalSelector.addGoal(9, new RandomStandGoal(this));
      }

      this.addBehaviourGoals();
   }

   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE), false));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_FLAGS, (byte)0);
   }

   protected boolean getFlag(int i) {
      return (this.entityData.get(DATA_ID_FLAGS) & i) != 0;
   }

   protected void setFlag(int i, boolean flag) {
      byte b0 = this.entityData.get(DATA_ID_FLAGS);
      if (flag) {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | i));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & ~i));
      }

   }

   public boolean isTamed() {
      return this.getFlag(2);
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.owner;
   }

   public void setOwnerUUID(@Nullable UUID uuid) {
      this.owner = uuid;
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public void setTamed(boolean flag) {
      this.setFlag(2, flag);
   }

   public void setIsJumping(boolean flag) {
      this.isJumping = flag;
   }

   protected void onLeashDistance(float f) {
      if (f > 6.0F && this.isEating()) {
         this.setEating(false);
      }

   }

   public boolean isEating() {
      return this.getFlag(16);
   }

   public boolean isStanding() {
      return this.getFlag(32);
   }

   public boolean isBred() {
      return this.getFlag(8);
   }

   public void setBred(boolean flag) {
      this.setFlag(8, flag);
   }

   public boolean isSaddleable() {
      return this.isAlive() && !this.isBaby() && this.isTamed();
   }

   public void equipSaddle(@Nullable SoundSource soundsource) {
      this.inventory.setItem(0, new ItemStack(Items.SADDLE));
   }

   public void equipArmor(Player player, ItemStack itemstack) {
      if (this.isArmor(itemstack)) {
         this.inventory.setItem(1, itemstack.copyWithCount(1));
         if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
         }
      }

   }

   public boolean isSaddled() {
      return this.getFlag(4);
   }

   public int getTemper() {
      return this.temper;
   }

   public void setTemper(int i) {
      this.temper = i;
   }

   public int modifyTemper(int i) {
      int j = Mth.clamp(this.getTemper() + i, 0, this.getMaxTemper());
      this.setTemper(j);
      return j;
   }

   public boolean isPushable() {
      return !this.isVehicle();
   }

   private void eating() {
      this.openMouth();
      if (!this.isSilent()) {
         SoundEvent soundevent = this.getEatingSound();
         if (soundevent != null) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      if (f > 1.0F) {
         this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
      }

      int i = this.calculateFallDamage(f, f1);
      if (i <= 0) {
         return false;
      } else {
         this.hurt(damagesource, (float)i);
         if (this.isVehicle()) {
            for(Entity entity : this.getIndirectPassengers()) {
               entity.hurt(damagesource, (float)i);
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   protected int calculateFallDamage(float f, float f1) {
      return Mth.ceil((f * 0.5F - 3.0F) * f1);
   }

   protected int getInventorySize() {
      return 2;
   }

   protected void createInventory() {
      SimpleContainer simplecontainer = this.inventory;
      this.inventory = new SimpleContainer(this.getInventorySize());
      if (simplecontainer != null) {
         simplecontainer.removeListener(this);
         int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

         for(int j = 0; j < i; ++j) {
            ItemStack itemstack = simplecontainer.getItem(j);
            if (!itemstack.isEmpty()) {
               this.inventory.setItem(j, itemstack.copy());
            }
         }
      }

      this.inventory.addListener(this);
      this.updateContainerEquipment();
   }

   protected void updateContainerEquipment() {
      if (!this.level().isClientSide) {
         this.setFlag(4, !this.inventory.getItem(0).isEmpty());
      }
   }

   public void containerChanged(Container container) {
      boolean flag = this.isSaddled();
      this.updateContainerEquipment();
      if (this.tickCount > 20 && !flag && this.isSaddled()) {
         this.playSound(this.getSaddleSoundEvent(), 0.5F, 1.0F);
      }

   }

   public double getCustomJump() {
      return this.getAttributeValue(Attributes.JUMP_STRENGTH);
   }

   public boolean hurt(DamageSource damagesource, float f) {
      boolean flag = super.hurt(damagesource, f);
      if (flag && this.random.nextInt(3) == 0) {
         this.standIfPossible();
      }

      return flag;
   }

   protected boolean canPerformRearing() {
      return true;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getAngrySound() {
      return null;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      if (!blockstate.liquid()) {
         BlockState blockstate1 = this.level().getBlockState(blockpos.above());
         SoundType soundtype = blockstate.getSoundType();
         if (blockstate1.is(Blocks.SNOW)) {
            soundtype = blockstate1.getSoundType();
         }

         if (this.isVehicle() && this.canGallop) {
            ++this.gallopSoundCounter;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
               this.playGallopSound(soundtype);
            } else if (this.gallopSoundCounter <= 5) {
               this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.15F, soundtype.getPitch());
            }
         } else if (this.isWoodSoundType(soundtype)) {
            this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.15F, soundtype.getPitch());
         } else {
            this.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
         }

      }
   }

   private boolean isWoodSoundType(SoundType soundtype) {
      return soundtype == SoundType.WOOD || soundtype == SoundType.NETHER_WOOD || soundtype == SoundType.STEM || soundtype == SoundType.CHERRY_WOOD || soundtype == SoundType.BAMBOO_WOOD;
   }

   protected void playGallopSound(SoundType soundtype) {
      this.playSound(SoundEvents.HORSE_GALLOP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
   }

   public static AttributeSupplier.Builder createBaseHorseAttributes() {
      return Mob.createMobAttributes().add(Attributes.JUMP_STRENGTH).add(Attributes.MAX_HEALTH, 53.0D).add(Attributes.MOVEMENT_SPEED, (double)0.225F);
   }

   public int getMaxSpawnClusterSize() {
      return 6;
   }

   public int getMaxTemper() {
      return 100;
   }

   protected float getSoundVolume() {
      return 0.8F;
   }

   public int getAmbientSoundInterval() {
      return 400;
   }

   public void openCustomInventoryScreen(Player player) {
      if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(player)) && this.isTamed()) {
         player.openHorseInventory(this, this.inventory);
      }

   }

   public InteractionResult fedFood(Player player, ItemStack itemstack) {
      boolean flag = this.handleEating(player, itemstack);
      if (!player.getAbilities().instabuild) {
         itemstack.shrink(1);
      }

      if (this.level().isClientSide) {
         return InteractionResult.CONSUME;
      } else {
         return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
      }
   }

   protected boolean handleEating(Player player, ItemStack itemstack) {
      boolean flag = false;
      float f = 0.0F;
      int i = 0;
      int j = 0;
      if (itemstack.is(Items.WHEAT)) {
         f = 2.0F;
         i = 20;
         j = 3;
      } else if (itemstack.is(Items.SUGAR)) {
         f = 1.0F;
         i = 30;
         j = 3;
      } else if (itemstack.is(Blocks.HAY_BLOCK.asItem())) {
         f = 20.0F;
         i = 180;
      } else if (itemstack.is(Items.APPLE)) {
         f = 3.0F;
         i = 60;
         j = 3;
      } else if (itemstack.is(Items.GOLDEN_CARROT)) {
         f = 4.0F;
         i = 60;
         j = 5;
         if (!this.level().isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
            flag = true;
            this.setInLove(player);
         }
      } else if (itemstack.is(Items.GOLDEN_APPLE) || itemstack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
         f = 10.0F;
         i = 240;
         j = 10;
         if (!this.level().isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
            flag = true;
            this.setInLove(player);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
         this.heal(f);
         flag = true;
      }

      if (this.isBaby() && i > 0) {
         this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
         if (!this.level().isClientSide) {
            this.ageUp(i);
         }

         flag = true;
      }

      if (j > 0 && (flag || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
         flag = true;
         if (!this.level().isClientSide) {
            this.modifyTemper(j);
         }
      }

      if (flag) {
         this.eating();
         this.gameEvent(GameEvent.EAT);
      }

      return flag;
   }

   protected void doPlayerRide(Player player) {
      this.setEating(false);
      this.setStanding(false);
      if (!this.level().isClientSide) {
         player.setYRot(this.getYRot());
         player.setXRot(this.getXRot());
         player.startRiding(this);
      }

   }

   public boolean isImmobile() {
      return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
   }

   public boolean isFood(ItemStack itemstack) {
      return FOOD_ITEMS.test(itemstack);
   }

   private void moveTail() {
      this.tailCounter = 1;
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.inventory != null) {
         for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
               this.spawnAtLocation(itemstack);
            }
         }

      }
   }

   public void aiStep() {
      if (this.random.nextInt(200) == 0) {
         this.moveTail();
      }

      super.aiStep();
      if (!this.level().isClientSide && this.isAlive()) {
         if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0F);
         }

         if (this.canEatGrass()) {
            if (!this.isEating() && !this.isVehicle() && this.random.nextInt(300) == 0 && this.level().getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
               this.setEating(true);
            }

            if (this.isEating() && ++this.eatingCounter > 50) {
               this.eatingCounter = 0;
               this.setEating(false);
            }
         }

         this.followMommy();
      }
   }

   protected void followMommy() {
      if (this.isBred() && this.isBaby() && !this.isEating()) {
         LivingEntity livingentity = this.level().getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0D));
         if (livingentity != null && this.distanceToSqr(livingentity) > 4.0D) {
            this.navigation.createPath(livingentity, 0);
         }
      }

   }

   public boolean canEatGrass() {
      return true;
   }

   public void tick() {
      super.tick();
      if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
         this.mouthCounter = 0;
         this.setFlag(64, false);
      }

      if (this.isEffectiveAi() && this.standCounter > 0 && ++this.standCounter > 20) {
         this.standCounter = 0;
         this.setStanding(false);
      }

      if (this.tailCounter > 0 && ++this.tailCounter > 8) {
         this.tailCounter = 0;
      }

      if (this.sprintCounter > 0) {
         ++this.sprintCounter;
         if (this.sprintCounter > 300) {
            this.sprintCounter = 0;
         }
      }

      this.eatAnimO = this.eatAnim;
      if (this.isEating()) {
         this.eatAnim += (1.0F - this.eatAnim) * 0.4F + 0.05F;
         if (this.eatAnim > 1.0F) {
            this.eatAnim = 1.0F;
         }
      } else {
         this.eatAnim += (0.0F - this.eatAnim) * 0.4F - 0.05F;
         if (this.eatAnim < 0.0F) {
            this.eatAnim = 0.0F;
         }
      }

      this.standAnimO = this.standAnim;
      if (this.isStanding()) {
         this.eatAnim = 0.0F;
         this.eatAnimO = this.eatAnim;
         this.standAnim += (1.0F - this.standAnim) * 0.4F + 0.05F;
         if (this.standAnim > 1.0F) {
            this.standAnim = 1.0F;
         }
      } else {
         this.allowStandSliding = false;
         this.standAnim += (0.8F * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6F - 0.05F;
         if (this.standAnim < 0.0F) {
            this.standAnim = 0.0F;
         }
      }

      this.mouthAnimO = this.mouthAnim;
      if (this.getFlag(64)) {
         this.mouthAnim += (1.0F - this.mouthAnim) * 0.7F + 0.05F;
         if (this.mouthAnim > 1.0F) {
            this.mouthAnim = 1.0F;
         }
      } else {
         this.mouthAnim += (0.0F - this.mouthAnim) * 0.7F - 0.05F;
         if (this.mouthAnim < 0.0F) {
            this.mouthAnim = 0.0F;
         }
      }

   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      if (!this.isVehicle() && !this.isBaby()) {
         if (this.isTamed() && player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else {
            ItemStack itemstack = player.getItemInHand(interactionhand);
            if (!itemstack.isEmpty()) {
               InteractionResult interactionresult = itemstack.interactLivingEntity(player, this, interactionhand);
               if (interactionresult.consumesAction()) {
                  return interactionresult;
               }

               if (this.canWearArmor() && this.isArmor(itemstack) && !this.isWearingArmor()) {
                  this.equipArmor(player, itemstack);
                  return InteractionResult.sidedSuccess(this.level().isClientSide);
               }
            }

            this.doPlayerRide(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      } else {
         return super.mobInteract(player, interactionhand);
      }
   }

   private void openMouth() {
      if (!this.level().isClientSide) {
         this.mouthCounter = 1;
         this.setFlag(64, true);
      }

   }

   public void setEating(boolean flag) {
      this.setFlag(16, flag);
   }

   public void setStanding(boolean flag) {
      if (flag) {
         this.setEating(false);
      }

      this.setFlag(32, flag);
   }

   @Nullable
   public SoundEvent getAmbientStandSound() {
      return this.getAmbientSound();
   }

   public void standIfPossible() {
      if (this.canPerformRearing() && this.isEffectiveAi()) {
         this.standCounter = 1;
         this.setStanding(true);
      }

   }

   public void makeMad() {
      if (!this.isStanding()) {
         this.standIfPossible();
         SoundEvent soundevent = this.getAngrySound();
         if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
         }
      }

   }

   public boolean tameWithName(Player player) {
      this.setOwnerUUID(player.getUUID());
      this.setTamed(true);
      if (player instanceof ServerPlayer) {
         CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
      }

      this.level().broadcastEntityEvent(this, (byte)7);
      return true;
   }

   protected void tickRidden(Player player, Vec3 vec3) {
      super.tickRidden(player, vec3);
      Vec2 vec2 = this.getRiddenRotation(player);
      this.setRot(vec2.y, vec2.x);
      this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
      if (this.isControlledByLocalInstance()) {
         if (vec3.z <= 0.0D) {
            this.gallopSoundCounter = 0;
         }

         if (this.onGround()) {
            this.setIsJumping(false);
            if (this.playerJumpPendingScale > 0.0F && !this.isJumping()) {
               this.executeRidersJump(this.playerJumpPendingScale, vec3);
            }

            this.playerJumpPendingScale = 0.0F;
         }
      }

   }

   protected Vec2 getRiddenRotation(LivingEntity livingentity) {
      return new Vec2(livingentity.getXRot() * 0.5F, livingentity.getYRot());
   }

   protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
      if (this.onGround() && this.playerJumpPendingScale == 0.0F && this.isStanding() && !this.allowStandSliding) {
         return Vec3.ZERO;
      } else {
         float f = player.xxa * 0.5F;
         float f1 = player.zza;
         if (f1 <= 0.0F) {
            f1 *= 0.25F;
         }

         return new Vec3((double)f, 0.0D, (double)f1);
      }
   }

   protected float getRiddenSpeed(Player player) {
      return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
   }

   protected void executeRidersJump(float f, Vec3 vec3) {
      double d0 = this.getCustomJump() * (double)f * (double)this.getBlockJumpFactor();
      double d1 = d0 + (double)this.getJumpBoostPower();
      Vec3 vec31 = this.getDeltaMovement();
      this.setDeltaMovement(vec31.x, d1, vec31.z);
      this.setIsJumping(true);
      this.hasImpulse = true;
      if (vec3.z > 0.0D) {
         float f1 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
         float f2 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
         this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4F * f1 * f), 0.0D, (double)(0.4F * f2 * f)));
      }

   }

   protected void playJumpSound() {
      this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putBoolean("EatingHaystack", this.isEating());
      compoundtag.putBoolean("Bred", this.isBred());
      compoundtag.putInt("Temper", this.getTemper());
      compoundtag.putBoolean("Tame", this.isTamed());
      if (this.getOwnerUUID() != null) {
         compoundtag.putUUID("Owner", this.getOwnerUUID());
      }

      if (!this.inventory.getItem(0).isEmpty()) {
         compoundtag.put("SaddleItem", this.inventory.getItem(0).save(new CompoundTag()));
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setEating(compoundtag.getBoolean("EatingHaystack"));
      this.setBred(compoundtag.getBoolean("Bred"));
      this.setTemper(compoundtag.getInt("Temper"));
      this.setTamed(compoundtag.getBoolean("Tame"));
      UUID uuid;
      if (compoundtag.hasUUID("Owner")) {
         uuid = compoundtag.getUUID("Owner");
      } else {
         String s = compoundtag.getString("Owner");
         uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
      }

      if (uuid != null) {
         this.setOwnerUUID(uuid);
      }

      if (compoundtag.contains("SaddleItem", 10)) {
         ItemStack itemstack = ItemStack.of(compoundtag.getCompound("SaddleItem"));
         if (itemstack.is(Items.SADDLE)) {
            this.inventory.setItem(0, itemstack);
         }
      }

      this.updateContainerEquipment();
   }

   public boolean canMate(Animal animal) {
      return false;
   }

   protected boolean canParent() {
      return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      return null;
   }

   protected void setOffspringAttributes(AgeableMob ageablemob, AbstractHorse abstracthorse) {
      this.setOffspringAttribute(ageablemob, abstracthorse, Attributes.MAX_HEALTH, (double)MIN_HEALTH, (double)MAX_HEALTH);
      this.setOffspringAttribute(ageablemob, abstracthorse, Attributes.JUMP_STRENGTH, (double)MIN_JUMP_STRENGTH, (double)MAX_JUMP_STRENGTH);
      this.setOffspringAttribute(ageablemob, abstracthorse, Attributes.MOVEMENT_SPEED, (double)MIN_MOVEMENT_SPEED, (double)MAX_MOVEMENT_SPEED);
   }

   private void setOffspringAttribute(AgeableMob ageablemob, AbstractHorse abstracthorse, Attribute attribute, double d0, double d1) {
      double d2 = createOffspringAttribute(this.getAttributeBaseValue(attribute), ageablemob.getAttributeBaseValue(attribute), d0, d1, this.random);
      abstracthorse.getAttribute(attribute).setBaseValue(d2);
   }

   static double createOffspringAttribute(double d0, double d1, double d2, double d3, RandomSource randomsource) {
      if (d3 <= d2) {
         throw new IllegalArgumentException("Incorrect range for an attribute");
      } else {
         d0 = Mth.clamp(d0, d2, d3);
         d1 = Mth.clamp(d1, d2, d3);
         double d4 = 0.15D * (d3 - d2);
         double d5 = Math.abs(d0 - d1) + d4 * 2.0D;
         double d6 = (d0 + d1) / 2.0D;
         double d7 = (randomsource.nextDouble() + randomsource.nextDouble() + randomsource.nextDouble()) / 3.0D - 0.5D;
         double d8 = d6 + d5 * d7;
         if (d8 > d3) {
            double d9 = d8 - d3;
            return d3 - d9;
         } else if (d8 < d2) {
            double d10 = d2 - d8;
            return d2 + d10;
         } else {
            return d8;
         }
      }
   }

   public float getEatAnim(float f) {
      return Mth.lerp(f, this.eatAnimO, this.eatAnim);
   }

   public float getStandAnim(float f) {
      return Mth.lerp(f, this.standAnimO, this.standAnim);
   }

   public float getMouthAnim(float f) {
      return Mth.lerp(f, this.mouthAnimO, this.mouthAnim);
   }

   public void onPlayerJump(int i) {
      if (this.isSaddled()) {
         if (i < 0) {
            i = 0;
         } else {
            this.allowStandSliding = true;
            this.standIfPossible();
         }

         if (i >= 90) {
            this.playerJumpPendingScale = 1.0F;
         } else {
            this.playerJumpPendingScale = 0.4F + 0.4F * (float)i / 90.0F;
         }

      }
   }

   public boolean canJump() {
      return this.isSaddled();
   }

   public void handleStartJump(int i) {
      this.allowStandSliding = true;
      this.standIfPossible();
      this.playJumpSound();
   }

   public void handleStopJump() {
   }

   protected void spawnTamingParticles(boolean flag) {
      ParticleOptions particleoptions = flag ? ParticleTypes.HEART : ParticleTypes.SMOKE;

      for(int i = 0; i < 7; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 7) {
         this.spawnTamingParticles(true);
      } else if (b0 == 6) {
         this.spawnTamingParticles(false);
      } else {
         super.handleEntityEvent(b0);
      }

   }

   protected void positionRider(Entity entity, Entity.MoveFunction entity_movefunction) {
      super.positionRider(entity, entity_movefunction);
      if (this.standAnimO > 0.0F) {
         float f = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
         float f1 = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
         float f2 = 0.7F * this.standAnimO;
         float f3 = 0.15F * this.standAnimO;
         entity_movefunction.accept(entity, this.getX() + (double)(f2 * f), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset() + (double)f3, this.getZ() - (double)(f2 * f1));
         if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).yBodyRot = this.yBodyRot;
         }
      }

   }

   protected static float generateMaxHealth(IntUnaryOperator intunaryoperator) {
      return 15.0F + (float)intunaryoperator.applyAsInt(8) + (float)intunaryoperator.applyAsInt(9);
   }

   protected static double generateJumpStrength(DoubleSupplier doublesupplier) {
      return (double)0.4F + doublesupplier.getAsDouble() * 0.2D + doublesupplier.getAsDouble() * 0.2D + doublesupplier.getAsDouble() * 0.2D;
   }

   protected static double generateSpeed(DoubleSupplier doublesupplier) {
      return ((double)0.45F + doublesupplier.getAsDouble() * 0.3D + doublesupplier.getAsDouble() * 0.3D + doublesupplier.getAsDouble() * 0.3D) * 0.25D;
   }

   public boolean onClimbable() {
      return false;
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return entitydimensions.height * 0.95F;
   }

   public boolean canWearArmor() {
      return false;
   }

   public boolean isWearingArmor() {
      return !this.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
   }

   public boolean isArmor(ItemStack itemstack) {
      return false;
   }

   private SlotAccess createEquipmentSlotAccess(final int i, final Predicate<ItemStack> predicate) {
      return new SlotAccess() {
         public ItemStack get() {
            return AbstractHorse.this.inventory.getItem(i);
         }

         public boolean set(ItemStack itemstack) {
            if (!predicate.test(itemstack)) {
               return false;
            } else {
               AbstractHorse.this.inventory.setItem(i, itemstack);
               AbstractHorse.this.updateContainerEquipment();
               return true;
            }
         }
      };
   }

   public SlotAccess getSlot(int i) {
      int j = i - 400;
      if (j >= 0 && j < 2 && j < this.inventory.getContainerSize()) {
         if (j == 0) {
            return this.createEquipmentSlotAccess(j, (itemstack1) -> itemstack1.isEmpty() || itemstack1.is(Items.SADDLE));
         }

         if (j == 1) {
            if (!this.canWearArmor()) {
               return SlotAccess.NULL;
            }

            return this.createEquipmentSlotAccess(j, (itemstack) -> itemstack.isEmpty() || this.isArmor(itemstack));
         }
      }

      int k = i - 500 + 2;
      return k >= 2 && k < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, k) : super.getSlot(i);
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity var3 = this.getFirstPassenger();
      if (var3 instanceof Mob) {
         return (Mob)var3;
      } else {
         if (this.isSaddled()) {
            var3 = this.getFirstPassenger();
            if (var3 instanceof Player) {
               return (Player)var3;
            }
         }

         return null;
      }
   }

   @Nullable
   private Vec3 getDismountLocationInDirection(Vec3 vec3, LivingEntity livingentity) {
      double d0 = this.getX() + vec3.x;
      double d1 = this.getBoundingBox().minY;
      double d2 = this.getZ() + vec3.z;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Pose pose : livingentity.getDismountPoses()) {
         blockpos_mutableblockpos.set(d0, d1, d2);
         double d3 = this.getBoundingBox().maxY + 0.75D;

         while(true) {
            double d4 = this.level().getBlockFloorHeight(blockpos_mutableblockpos);
            if ((double)blockpos_mutableblockpos.getY() + d4 > d3) {
               break;
            }

            if (DismountHelper.isBlockFloorValid(d4)) {
               AABB aabb = livingentity.getLocalBoundsForPose(pose);
               Vec3 vec31 = new Vec3(d0, (double)blockpos_mutableblockpos.getY() + d4, d2);
               if (DismountHelper.canDismountTo(this.level(), livingentity, aabb.move(vec31))) {
                  livingentity.setPose(pose);
                  return vec31;
               }
            }

            blockpos_mutableblockpos.move(Direction.UP);
            if (!((double)blockpos_mutableblockpos.getY() < d3)) {
               break;
            }
         }
      }

      return null;
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity livingentity) {
      Vec3 vec3 = getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), this.getYRot() + (livingentity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F));
      Vec3 vec31 = this.getDismountLocationInDirection(vec3, livingentity);
      if (vec31 != null) {
         return vec31;
      } else {
         Vec3 vec32 = getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingentity.getBbWidth(), this.getYRot() + (livingentity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F));
         Vec3 vec33 = this.getDismountLocationInDirection(vec32, livingentity);
         return vec33 != null ? vec33 : this.position();
      }
   }

   protected void randomizeAttributes(RandomSource randomsource) {
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      if (spawngroupdata == null) {
         spawngroupdata = new AgeableMob.AgeableMobGroupData(0.2F);
      }

      this.randomizeAttributes(serverlevelaccessor.getRandom());
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   public boolean hasInventoryChanged(Container container) {
      return this.inventory != container;
   }

   public int getAmbientStandInterval() {
      return this.getAmbientSoundInterval();
   }
}
