package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class Player extends LivingEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int MAX_NAME_LENGTH = 16;
   public static final int MAX_HEALTH = 20;
   public static final int SLEEP_DURATION = 100;
   public static final int WAKE_UP_DURATION = 10;
   public static final int ENDER_SLOT_OFFSET = 200;
   public static final float CROUCH_BB_HEIGHT = 1.5F;
   public static final float SWIMMING_BB_WIDTH = 0.6F;
   public static final float SWIMMING_BB_HEIGHT = 0.6F;
   public static final float DEFAULT_EYE_HEIGHT = 1.62F;
   public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F);
   private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder().put(Pose.STANDING, STANDING_DIMENSIONS).put(Pose.SLEEPING, SLEEPING_DIMENSIONS).put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.CROUCHING, EntityDimensions.scalable(0.6F, 1.5F)).put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F)).build();
   private static final int FLY_ACHIEVEMENT_SPEED = 25;
   private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_SCORE_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
   protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
   private long timeEntitySatOnShoulder;
   private final Inventory inventory = new Inventory(this);
   protected PlayerEnderChestContainer enderChestInventory = new PlayerEnderChestContainer();
   public final InventoryMenu inventoryMenu;
   public AbstractContainerMenu containerMenu;
   protected FoodData foodData = new FoodData();
   protected int jumpTriggerTime;
   public float oBob;
   public float bob;
   public int takeXpDelay;
   public double xCloakO;
   public double yCloakO;
   public double zCloakO;
   public double xCloak;
   public double yCloak;
   public double zCloak;
   private int sleepCounter;
   protected boolean wasUnderwater;
   private final Abilities abilities = new Abilities();
   public int experienceLevel;
   public int totalExperience;
   public float experienceProgress;
   protected int enchantmentSeed;
   protected final float defaultFlySpeed = 0.02F;
   private int lastLevelUpTime;
   private final GameProfile gameProfile;
   private boolean reducedDebugInfo;
   private ItemStack lastItemInMainHand = ItemStack.EMPTY;
   private final ItemCooldowns cooldowns = this.createItemCooldowns();
   private Optional<GlobalPos> lastDeathLocation = Optional.empty();
   @Nullable
   public FishingHook fishing;
   protected float hurtDir;

   public Player(Level level, BlockPos blockpos, float f, GameProfile gameprofile) {
      super(EntityType.PLAYER, level);
      this.setUUID(UUIDUtil.getOrCreatePlayerUUID(gameprofile));
      this.gameProfile = gameprofile;
      this.inventoryMenu = new InventoryMenu(this.inventory, !level.isClientSide, this);
      this.containerMenu = this.inventoryMenu;
      this.moveTo((double)blockpos.getX() + 0.5D, (double)(blockpos.getY() + 1), (double)blockpos.getZ() + 0.5D, f, 0.0F);
      this.rotOffs = 180.0F;
   }

   public boolean blockActionRestricted(Level level, BlockPos blockpos, GameType gametype) {
      if (!gametype.isBlockPlacingRestricted()) {
         return false;
      } else if (gametype == GameType.SPECTATOR) {
         return true;
      } else if (this.mayBuild()) {
         return false;
      } else {
         ItemStack itemstack = this.getMainHandItem();
         return itemstack.isEmpty() || !itemstack.hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(level, blockpos, false));
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.MOVEMENT_SPEED, (double)0.1F).add(Attributes.ATTACK_SPEED).add(Attributes.LUCK);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_PLAYER_ABSORPTION_ID, 0.0F);
      this.entityData.define(DATA_SCORE_ID, 0);
      this.entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
      this.entityData.define(DATA_PLAYER_MAIN_HAND, (byte)1);
      this.entityData.define(DATA_SHOULDER_LEFT, new CompoundTag());
      this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundTag());
   }

   public void tick() {
      this.noPhysics = this.isSpectator();
      if (this.isSpectator()) {
         this.setOnGround(false);
      }

      if (this.takeXpDelay > 0) {
         --this.takeXpDelay;
      }

      if (this.isSleeping()) {
         ++this.sleepCounter;
         if (this.sleepCounter > 100) {
            this.sleepCounter = 100;
         }

         if (!this.level().isClientSide && this.level().isDay()) {
            this.stopSleepInBed(false, true);
         }
      } else if (this.sleepCounter > 0) {
         ++this.sleepCounter;
         if (this.sleepCounter >= 110) {
            this.sleepCounter = 0;
         }
      }

      this.updateIsUnderwater();
      super.tick();
      if (!this.level().isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
         this.closeContainer();
         this.containerMenu = this.inventoryMenu;
      }

      this.moveCloak();
      if (!this.level().isClientSide) {
         this.foodData.tick(this);
         this.awardStat(Stats.PLAY_TIME);
         this.awardStat(Stats.TOTAL_WORLD_TIME);
         if (this.isAlive()) {
            this.awardStat(Stats.TIME_SINCE_DEATH);
         }

         if (this.isDiscrete()) {
            this.awardStat(Stats.CROUCH_TIME);
         }

         if (!this.isSleeping()) {
            this.awardStat(Stats.TIME_SINCE_REST);
         }
      }

      int i = 29999999;
      double d0 = Mth.clamp(this.getX(), -2.9999999E7D, 2.9999999E7D);
      double d1 = Mth.clamp(this.getZ(), -2.9999999E7D, 2.9999999E7D);
      if (d0 != this.getX() || d1 != this.getZ()) {
         this.setPos(d0, this.getY(), d1);
      }

      ++this.attackStrengthTicker;
      ItemStack itemstack = this.getMainHandItem();
      if (!ItemStack.matches(this.lastItemInMainHand, itemstack)) {
         if (!ItemStack.isSameItem(this.lastItemInMainHand, itemstack)) {
            this.resetAttackStrengthTicker();
         }

         this.lastItemInMainHand = itemstack.copy();
      }

      this.turtleHelmetTick();
      this.cooldowns.tick();
      this.updatePlayerPose();
   }

   public boolean isSecondaryUseActive() {
      return this.isShiftKeyDown();
   }

   protected boolean wantsToStopRiding() {
      return this.isShiftKeyDown();
   }

   protected boolean isStayingOnGroundSurface() {
      return this.isShiftKeyDown();
   }

   protected boolean updateIsUnderwater() {
      this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
      return this.wasUnderwater;
   }

   private void turtleHelmetTick() {
      ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
      if (itemstack.is(Items.TURTLE_HELMET) && !this.isEyeInFluid(FluidTags.WATER)) {
         this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
      }

   }

   protected ItemCooldowns createItemCooldowns() {
      return new ItemCooldowns();
   }

   private void moveCloak() {
      this.xCloakO = this.xCloak;
      this.yCloakO = this.yCloak;
      this.zCloakO = this.zCloak;
      double d0 = this.getX() - this.xCloak;
      double d1 = this.getY() - this.yCloak;
      double d2 = this.getZ() - this.zCloak;
      double d3 = 10.0D;
      if (d0 > 10.0D) {
         this.xCloak = this.getX();
         this.xCloakO = this.xCloak;
      }

      if (d2 > 10.0D) {
         this.zCloak = this.getZ();
         this.zCloakO = this.zCloak;
      }

      if (d1 > 10.0D) {
         this.yCloak = this.getY();
         this.yCloakO = this.yCloak;
      }

      if (d0 < -10.0D) {
         this.xCloak = this.getX();
         this.xCloakO = this.xCloak;
      }

      if (d2 < -10.0D) {
         this.zCloak = this.getZ();
         this.zCloakO = this.zCloak;
      }

      if (d1 < -10.0D) {
         this.yCloak = this.getY();
         this.yCloakO = this.yCloak;
      }

      this.xCloak += d0 * 0.25D;
      this.zCloak += d2 * 0.25D;
      this.yCloak += d1 * 0.25D;
   }

   protected void updatePlayerPose() {
      if (this.canEnterPose(Pose.SWIMMING)) {
         Pose pose;
         if (this.isFallFlying()) {
            pose = Pose.FALL_FLYING;
         } else if (this.isSleeping()) {
            pose = Pose.SLEEPING;
         } else if (this.isSwimming()) {
            pose = Pose.SWIMMING;
         } else if (this.isAutoSpinAttack()) {
            pose = Pose.SPIN_ATTACK;
         } else if (this.isShiftKeyDown() && !this.abilities.flying) {
            pose = Pose.CROUCHING;
         } else {
            pose = Pose.STANDING;
         }

         Pose pose7;
         if (!this.isSpectator() && !this.isPassenger() && !this.canEnterPose(pose)) {
            if (this.canEnterPose(Pose.CROUCHING)) {
               pose7 = Pose.CROUCHING;
            } else {
               pose7 = Pose.SWIMMING;
            }
         } else {
            pose7 = pose;
         }

         this.setPose(pose7);
      }
   }

   public int getPortalWaitTime() {
      return this.abilities.invulnerable ? 1 : 80;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.PLAYER_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.PLAYER_SPLASH;
   }

   protected SoundEvent getSwimHighSpeedSplashSound() {
      return SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
   }

   public int getDimensionChangingDelay() {
      return 10;
   }

   public void playSound(SoundEvent soundevent, float f, float f1) {
      this.level().playSound(this, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), f, f1);
   }

   public void playNotifySound(SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
   }

   public SoundSource getSoundSource() {
      return SoundSource.PLAYERS;
   }

   protected int getFireImmuneTicks() {
      return 20;
   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 9) {
         this.completeUsingItem();
      } else if (b0 == 23) {
         this.reducedDebugInfo = false;
      } else if (b0 == 22) {
         this.reducedDebugInfo = true;
      } else if (b0 == 43) {
         this.addParticlesAroundSelf(ParticleTypes.CLOUD);
      } else {
         super.handleEntityEvent(b0);
      }

   }

   private void addParticlesAroundSelf(ParticleOptions particleoptions) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   protected void closeContainer() {
      this.containerMenu = this.inventoryMenu;
   }

   protected void doCloseContainer() {
   }

   public void rideTick() {
      if (!this.level().isClientSide && this.wantsToStopRiding() && this.isPassenger()) {
         this.stopRiding();
         this.setShiftKeyDown(false);
      } else {
         double d0 = this.getX();
         double d1 = this.getY();
         double d2 = this.getZ();
         super.rideTick();
         this.oBob = this.bob;
         this.bob = 0.0F;
         this.checkRidingStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
      }
   }

   protected void serverAiStep() {
      super.serverAiStep();
      this.updateSwingTime();
      this.yHeadRot = this.getYRot();
   }

   public void aiStep() {
      if (this.jumpTriggerTime > 0) {
         --this.jumpTriggerTime;
      }

      if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
         if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
            this.heal(1.0F);
         }

         if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
            this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
         }
      }

      this.inventory.tick();
      this.oBob = this.bob;
      super.aiStep();
      this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
      float f1;
      if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming()) {
         f1 = Math.min(0.1F, (float)this.getDeltaMovement().horizontalDistance());
      } else {
         f1 = 0.0F;
      }

      this.bob += (f1 - this.bob) * 0.4F;
      if (this.getHealth() > 0.0F && !this.isSpectator()) {
         AABB aabb;
         if (this.isPassenger() && !this.getVehicle().isRemoved()) {
            aabb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0D, 0.0D, 1.0D);
         } else {
            aabb = this.getBoundingBox().inflate(1.0D, 0.5D, 1.0D);
         }

         List<Entity> list = this.level().getEntities(this, aabb);
         List<Entity> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if (entity.getType() == EntityType.EXPERIENCE_ORB) {
               list1.add(entity);
            } else if (!entity.isRemoved()) {
               this.touch(entity);
            }
         }

         if (!list1.isEmpty()) {
            this.touch(Util.getRandom(list1, this.random));
         }
      }

      this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
      this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
      if (!this.level().isClientSide && (this.fallDistance > 0.5F || this.isInWater()) || this.abilities.flying || this.isSleeping() || this.isInPowderSnow) {
         this.removeEntitiesOnShoulder();
      }

   }

   private void playShoulderEntityAmbientSound(@Nullable CompoundTag compoundtag) {
      if (compoundtag != null && (!compoundtag.contains("Silent") || !compoundtag.getBoolean("Silent")) && this.level().random.nextInt(200) == 0) {
         String s = compoundtag.getString("id");
         EntityType.byString(s).filter((entitytype1) -> entitytype1 == EntityType.PARROT).ifPresent((entitytype) -> {
            if (!Parrot.imitateNearbyMobs(this.level(), this)) {
               this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), Parrot.getAmbient(this.level(), this.level().random), this.getSoundSource(), 1.0F, Parrot.getPitch(this.level().random));
            }

         });
      }

   }

   private void touch(Entity entity) {
      entity.playerTouch(this);
   }

   public int getScore() {
      return this.entityData.get(DATA_SCORE_ID);
   }

   public void setScore(int i) {
      this.entityData.set(DATA_SCORE_ID, i);
   }

   public void increaseScore(int i) {
      int j = this.getScore();
      this.entityData.set(DATA_SCORE_ID, j + i);
   }

   public void startAutoSpinAttack(int i) {
      this.autoSpinAttackTicks = i;
      if (!this.level().isClientSide) {
         this.removeEntitiesOnShoulder();
         this.setLivingEntityFlag(4, true);
      }

   }

   public void die(DamageSource damagesource) {
      super.die(damagesource);
      this.reapplyPosition();
      if (!this.isSpectator()) {
         this.dropAllDeathLoot(damagesource);
      }

      if (damagesource != null) {
         this.setDeltaMovement((double)(-Mth.cos((this.getHurtDir() + this.getYRot()) * ((float)Math.PI / 180F)) * 0.1F), (double)0.1F, (double)(-Mth.sin((this.getHurtDir() + this.getYRot()) * ((float)Math.PI / 180F)) * 0.1F));
      } else {
         this.setDeltaMovement(0.0D, 0.1D, 0.0D);
      }

      this.awardStat(Stats.DEATHS);
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
      this.clearFire();
      this.setSharedFlagOnFire(false);
      this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (!this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
         this.destroyVanishingCursedItems();
         this.inventory.dropAll();
      }

   }

   protected void destroyVanishingCursedItems() {
      for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
         ItemStack itemstack = this.inventory.getItem(i);
         if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
            this.inventory.removeItemNoUpdate(i);
         }
      }

   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return damagesource.type().effects().sound();
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PLAYER_DEATH;
   }

   @Nullable
   public ItemEntity drop(ItemStack itemstack, boolean flag) {
      return this.drop(itemstack, false, flag);
   }

   @Nullable
   public ItemEntity drop(ItemStack itemstack, boolean flag, boolean flag1) {
      if (itemstack.isEmpty()) {
         return null;
      } else {
         if (this.level().isClientSide) {
            this.swing(InteractionHand.MAIN_HAND);
         }

         double d0 = this.getEyeY() - (double)0.3F;
         ItemEntity itementity = new ItemEntity(this.level(), this.getX(), d0, this.getZ(), itemstack);
         itementity.setPickUpDelay(40);
         if (flag1) {
            itementity.setThrower(this.getUUID());
         }

         if (flag) {
            float f = this.random.nextFloat() * 0.5F;
            float f1 = this.random.nextFloat() * ((float)Math.PI * 2F);
            itementity.setDeltaMovement((double)(-Mth.sin(f1) * f), (double)0.2F, (double)(Mth.cos(f1) * f));
         } else {
            float f2 = 0.3F;
            float f3 = Mth.sin(this.getXRot() * ((float)Math.PI / 180F));
            float f4 = Mth.cos(this.getXRot() * ((float)Math.PI / 180F));
            float f5 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
            float f6 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
            float f7 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f8 = 0.02F * this.random.nextFloat();
            itementity.setDeltaMovement((double)(-f5 * f4 * 0.3F) + Math.cos((double)f7) * (double)f8, (double)(-f3 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(f6 * f4 * 0.3F) + Math.sin((double)f7) * (double)f8);
         }

         return itementity;
      }
   }

   public float getDestroySpeed(BlockState blockstate) {
      float f = this.inventory.getDestroySpeed(blockstate);
      if (f > 1.0F) {
         int i = EnchantmentHelper.getBlockEfficiency(this);
         ItemStack itemstack = this.getMainHandItem();
         if (i > 0 && !itemstack.isEmpty()) {
            f += (float)(i * i + 1);
         }
      }

      if (MobEffectUtil.hasDigSpeed(this)) {
         f *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2F;
      }

      if (this.hasEffect(MobEffects.DIG_SLOWDOWN)) {
         float f1;
         switch (this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
            case 0:
               f1 = 0.3F;
               break;
            case 1:
               f1 = 0.09F;
               break;
            case 2:
               f1 = 0.0027F;
               break;
            case 3:
            default:
               f1 = 8.1E-4F;
         }

         f *= f1;
      }

      if (this.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
         f /= 5.0F;
      }

      if (!this.onGround()) {
         f /= 5.0F;
      }

      return f;
   }

   public boolean hasCorrectToolForDrops(BlockState blockstate) {
      return !blockstate.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(blockstate);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setUUID(UUIDUtil.getOrCreatePlayerUUID(this.gameProfile));
      ListTag listtag = compoundtag.getList("Inventory", 10);
      this.inventory.load(listtag);
      this.inventory.selected = compoundtag.getInt("SelectedItemSlot");
      this.sleepCounter = compoundtag.getShort("SleepTimer");
      this.experienceProgress = compoundtag.getFloat("XpP");
      this.experienceLevel = compoundtag.getInt("XpLevel");
      this.totalExperience = compoundtag.getInt("XpTotal");
      this.enchantmentSeed = compoundtag.getInt("XpSeed");
      if (this.enchantmentSeed == 0) {
         this.enchantmentSeed = this.random.nextInt();
      }

      this.setScore(compoundtag.getInt("Score"));
      this.foodData.readAdditionalSaveData(compoundtag);
      this.abilities.loadSaveData(compoundtag);
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)this.abilities.getWalkingSpeed());
      if (compoundtag.contains("EnderItems", 9)) {
         this.enderChestInventory.fromTag(compoundtag.getList("EnderItems", 10));
      }

      if (compoundtag.contains("ShoulderEntityLeft", 10)) {
         this.setShoulderEntityLeft(compoundtag.getCompound("ShoulderEntityLeft"));
      }

      if (compoundtag.contains("ShoulderEntityRight", 10)) {
         this.setShoulderEntityRight(compoundtag.getCompound("ShoulderEntityRight"));
      }

      if (compoundtag.contains("LastDeathLocation", 10)) {
         this.setLastDeathLocation(GlobalPos.CODEC.parse(NbtOps.INSTANCE, compoundtag.get("LastDeathLocation")).resultOrPartial(LOGGER::error));
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      NbtUtils.addCurrentDataVersion(compoundtag);
      compoundtag.put("Inventory", this.inventory.save(new ListTag()));
      compoundtag.putInt("SelectedItemSlot", this.inventory.selected);
      compoundtag.putShort("SleepTimer", (short)this.sleepCounter);
      compoundtag.putFloat("XpP", this.experienceProgress);
      compoundtag.putInt("XpLevel", this.experienceLevel);
      compoundtag.putInt("XpTotal", this.totalExperience);
      compoundtag.putInt("XpSeed", this.enchantmentSeed);
      compoundtag.putInt("Score", this.getScore());
      this.foodData.addAdditionalSaveData(compoundtag);
      this.abilities.addSaveData(compoundtag);
      compoundtag.put("EnderItems", this.enderChestInventory.createTag());
      if (!this.getShoulderEntityLeft().isEmpty()) {
         compoundtag.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
      }

      if (!this.getShoulderEntityRight().isEmpty()) {
         compoundtag.put("ShoulderEntityRight", this.getShoulderEntityRight());
      }

      this.getLastDeathLocation().flatMap((globalpos) -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalpos).resultOrPartial(LOGGER::error)).ifPresent((tag) -> compoundtag.put("LastDeathLocation", tag));
   }

   public boolean isInvulnerableTo(DamageSource damagesource) {
      if (super.isInvulnerableTo(damagesource)) {
         return true;
      } else if (damagesource.is(DamageTypeTags.IS_DROWNING)) {
         return !this.level().getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE);
      } else if (damagesource.is(DamageTypeTags.IS_FALL)) {
         return !this.level().getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE);
      } else if (damagesource.is(DamageTypeTags.IS_FIRE)) {
         return !this.level().getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE);
      } else if (damagesource.is(DamageTypeTags.IS_FREEZING)) {
         return !this.level().getGameRules().getBoolean(GameRules.RULE_FREEZE_DAMAGE);
      } else {
         return false;
      }
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (this.abilities.invulnerable && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      } else {
         this.noActionTime = 0;
         if (this.isDeadOrDying()) {
            return false;
         } else {
            if (!this.level().isClientSide) {
               this.removeEntitiesOnShoulder();
            }

            if (damagesource.scalesWithDifficulty()) {
               if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
                  f = 0.0F;
               }

               if (this.level().getDifficulty() == Difficulty.EASY) {
                  f = Math.min(f / 2.0F + 1.0F, f);
               }

               if (this.level().getDifficulty() == Difficulty.HARD) {
                  f = f * 3.0F / 2.0F;
               }
            }

            return f == 0.0F ? false : super.hurt(damagesource, f);
         }
      }
   }

   protected void blockUsingShield(LivingEntity livingentity) {
      super.blockUsingShield(livingentity);
      if (livingentity.canDisableShield()) {
         this.disableShield(true);
      }

   }

   public boolean canBeSeenAsEnemy() {
      return !this.getAbilities().invulnerable && super.canBeSeenAsEnemy();
   }

   public boolean canHarmPlayer(Player player) {
      Team team = this.getTeam();
      Team team1 = player.getTeam();
      if (team == null) {
         return true;
      } else {
         return !team.isAlliedTo(team1) ? true : team.isAllowFriendlyFire();
      }
   }

   protected void hurtArmor(DamageSource damagesource, float f) {
      this.inventory.hurtArmor(damagesource, f, Inventory.ALL_ARMOR_SLOTS);
   }

   protected void hurtHelmet(DamageSource damagesource, float f) {
      this.inventory.hurtArmor(damagesource, f, Inventory.HELMET_SLOT_ONLY);
   }

   protected void hurtCurrentlyUsedShield(float f) {
      if (this.useItem.is(Items.SHIELD)) {
         if (!this.level().isClientSide) {
            this.awardStat(Stats.ITEM_USED.get(this.useItem.getItem()));
         }

         if (f >= 3.0F) {
            int i = 1 + Mth.floor(f);
            InteractionHand interactionhand = this.getUsedItemHand();
            this.useItem.hurtAndBreak(i, this, (player) -> player.broadcastBreakEvent(interactionhand));
            if (this.useItem.isEmpty()) {
               if (interactionhand == InteractionHand.MAIN_HAND) {
                  this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
               } else {
                  this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
               }

               this.useItem = ItemStack.EMPTY;
               this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
            }
         }

      }
   }

   protected void actuallyHurt(DamageSource damagesource, float f) {
      if (!this.isInvulnerableTo(damagesource)) {
         f = this.getDamageAfterArmorAbsorb(damagesource, f);
         f = this.getDamageAfterMagicAbsorb(damagesource, f);
         float var7 = Math.max(f - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - var7));
         float f2 = f - var7;
         if (f2 > 0.0F && f2 < 3.4028235E37F) {
            this.awardStat(Stats.DAMAGE_ABSORBED, Math.round(f2 * 10.0F));
         }

         if (var7 != 0.0F) {
            this.causeFoodExhaustion(damagesource.getFoodExhaustion());
            this.getCombatTracker().recordDamage(damagesource, var7);
            this.setHealth(this.getHealth() - var7);
            if (var7 < 3.4028235E37F) {
               this.awardStat(Stats.DAMAGE_TAKEN, Math.round(var7 * 10.0F));
            }

            this.gameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   protected boolean onSoulSpeedBlock() {
      return !this.abilities.flying && super.onSoulSpeedBlock();
   }

   public boolean isTextFilteringEnabled() {
      return false;
   }

   public void openTextEdit(SignBlockEntity signblockentity, boolean flag) {
   }

   public void openMinecartCommandBlock(BaseCommandBlock basecommandblock) {
   }

   public void openCommandBlock(CommandBlockEntity commandblockentity) {
   }

   public void openStructureBlock(StructureBlockEntity structureblockentity) {
   }

   public void openJigsawBlock(JigsawBlockEntity jigsawblockentity) {
   }

   public void openHorseInventory(AbstractHorse abstracthorse, Container container) {
   }

   public OptionalInt openMenu(@Nullable MenuProvider menuprovider) {
      return OptionalInt.empty();
   }

   public void sendMerchantOffers(int i, MerchantOffers merchantoffers, int j, int k, boolean flag, boolean flag1) {
   }

   public void openItemGui(ItemStack itemstack, InteractionHand interactionhand) {
   }

   public InteractionResult interactOn(Entity entity, InteractionHand interactionhand) {
      if (this.isSpectator()) {
         if (entity instanceof MenuProvider) {
            this.openMenu((MenuProvider)entity);
         }

         return InteractionResult.PASS;
      } else {
         ItemStack itemstack = this.getItemInHand(interactionhand);
         ItemStack itemstack1 = itemstack.copy();
         InteractionResult interactionresult = entity.interact(this, interactionhand);
         if (interactionresult.consumesAction()) {
            if (this.abilities.instabuild && itemstack == this.getItemInHand(interactionhand) && itemstack.getCount() < itemstack1.getCount()) {
               itemstack.setCount(itemstack1.getCount());
            }

            return interactionresult;
         } else {
            if (!itemstack.isEmpty() && entity instanceof LivingEntity) {
               if (this.abilities.instabuild) {
                  itemstack = itemstack1;
               }

               InteractionResult interactionresult1 = itemstack.interactLivingEntity(this, (LivingEntity)entity, interactionhand);
               if (interactionresult1.consumesAction()) {
                  this.level().gameEvent(GameEvent.ENTITY_INTERACT, entity.position(), GameEvent.Context.of(this));
                  if (itemstack.isEmpty() && !this.abilities.instabuild) {
                     this.setItemInHand(interactionhand, ItemStack.EMPTY);
                  }

                  return interactionresult1;
               }
            }

            return InteractionResult.PASS;
         }
      }
   }

   public double getMyRidingOffset() {
      return -0.35D;
   }

   public void removeVehicle() {
      super.removeVehicle();
      this.boardingCooldown = 0;
   }

   protected boolean isImmobile() {
      return super.isImmobile() || this.isSleeping();
   }

   public boolean isAffectedByFluids() {
      return !this.abilities.flying;
   }

   protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType movertype) {
      if (!this.abilities.flying && vec3.y <= 0.0D && (movertype == MoverType.SELF || movertype == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround()) {
         double d0 = vec3.x;
         double d1 = vec3.z;
         double d2 = 0.05D;

         while(d0 != 0.0D && this.level().noCollision(this, this.getBoundingBox().move(d0, (double)(-this.maxUpStep()), 0.0D))) {
            if (d0 < 0.05D && d0 >= -0.05D) {
               d0 = 0.0D;
            } else if (d0 > 0.0D) {
               d0 -= 0.05D;
            } else {
               d0 += 0.05D;
            }
         }

         while(d1 != 0.0D && this.level().noCollision(this, this.getBoundingBox().move(0.0D, (double)(-this.maxUpStep()), d1))) {
            if (d1 < 0.05D && d1 >= -0.05D) {
               d1 = 0.0D;
            } else if (d1 > 0.0D) {
               d1 -= 0.05D;
            } else {
               d1 += 0.05D;
            }
         }

         while(d0 != 0.0D && d1 != 0.0D && this.level().noCollision(this, this.getBoundingBox().move(d0, (double)(-this.maxUpStep()), d1))) {
            if (d0 < 0.05D && d0 >= -0.05D) {
               d0 = 0.0D;
            } else if (d0 > 0.0D) {
               d0 -= 0.05D;
            } else {
               d0 += 0.05D;
            }

            if (d1 < 0.05D && d1 >= -0.05D) {
               d1 = 0.0D;
            } else if (d1 > 0.0D) {
               d1 -= 0.05D;
            } else {
               d1 += 0.05D;
            }
         }

         vec3 = new Vec3(d0, vec3.y, d1);
      }

      return vec3;
   }

   private boolean isAboveGround() {
      return this.onGround() || this.fallDistance < this.maxUpStep() && !this.level().noCollision(this, this.getBoundingBox().move(0.0D, (double)(this.fallDistance - this.maxUpStep()), 0.0D));
   }

   public void attack(Entity entity) {
      if (entity.isAttackable()) {
         if (!entity.skipAttackInteraction(this)) {
            float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float f1;
            if (entity instanceof LivingEntity) {
               f1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entity).getMobType());
            } else {
               f1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), MobType.UNDEFINED);
            }

            float f3 = this.getAttackStrengthScale(0.5F);
            f *= 0.2F + f3 * f3 * 0.8F;
            f1 *= f3;
            this.resetAttackStrengthTicker();
            if (f > 0.0F || f1 > 0.0F) {
               boolean flag = f3 > 0.9F;
               boolean flag1 = false;
               int i = 0;
               i += EnchantmentHelper.getKnockbackBonus(this);
               if (this.isSprinting() && flag) {
                  this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
                  ++i;
                  flag1 = true;
               }

               boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround() && !this.onClimbable() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof LivingEntity;
               flag2 = flag2 && !this.isSprinting();
               if (flag2) {
                  f *= 1.5F;
               }

               f += f1;
               boolean flag3 = false;
               double d0 = (double)(this.walkDist - this.walkDistO);
               if (flag && !flag2 && !flag1 && this.onGround() && d0 < (double)this.getSpeed()) {
                  ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
                  if (itemstack.getItem() instanceof SwordItem) {
                     flag3 = true;
                  }
               }

               float f4 = 0.0F;
               boolean flag4 = false;
               int j = EnchantmentHelper.getFireAspect(this);
               if (entity instanceof LivingEntity) {
                  f4 = ((LivingEntity)entity).getHealth();
                  if (j > 0 && !entity.isOnFire()) {
                     flag4 = true;
                     entity.setSecondsOnFire(1);
                  }
               }

               Vec3 vec3 = entity.getDeltaMovement();
               boolean flag5 = entity.hurt(this.damageSources().playerAttack(this), f);
               if (flag5) {
                  if (i > 0) {
                     if (entity instanceof LivingEntity) {
                        ((LivingEntity)entity).knockback((double)((float)i * 0.5F), (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
                     } else {
                        entity.push((double)(-Mth.sin(this.getYRot() * ((float)Math.PI / 180F)) * (float)i * 0.5F), 0.1D, (double)(Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * (float)i * 0.5F));
                     }

                     this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                     this.setSprinting(false);
                  }

                  if (flag3) {
                     float f5 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this) * f;

                     for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D))) {
                        if (livingentity != this && livingentity != entity && !this.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand)livingentity).isMarker()) && this.distanceToSqr(livingentity) < 9.0D) {
                           livingentity.knockback((double)0.4F, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
                           livingentity.hurt(this.damageSources().playerAttack(this), f5);
                        }
                     }

                     this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
                     this.sweepAttack();
                  }

                  if (entity instanceof ServerPlayer && entity.hurtMarked) {
                     ((ServerPlayer)entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
                     entity.hurtMarked = false;
                     entity.setDeltaMovement(vec3);
                  }

                  if (flag2) {
                     this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
                     this.crit(entity);
                  }

                  if (!flag2 && !flag3) {
                     if (flag) {
                        this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
                     } else {
                        this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
                     }
                  }

                  if (f1 > 0.0F) {
                     this.magicCrit(entity);
                  }

                  this.setLastHurtMob(entity);
                  if (entity instanceof LivingEntity) {
                     EnchantmentHelper.doPostHurtEffects((LivingEntity)entity, this);
                  }

                  EnchantmentHelper.doPostDamageEffects(this, entity);
                  ItemStack itemstack1 = this.getMainHandItem();
                  Entity entity1 = entity;
                  if (entity instanceof EnderDragonPart) {
                     entity1 = ((EnderDragonPart)entity).parentMob;
                  }

                  if (!this.level().isClientSide && !itemstack1.isEmpty() && entity1 instanceof LivingEntity) {
                     itemstack1.hurtEnemy((LivingEntity)entity1, this);
                     if (itemstack1.isEmpty()) {
                        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                     }
                  }

                  if (entity instanceof LivingEntity) {
                     float f6 = f4 - ((LivingEntity)entity).getHealth();
                     this.awardStat(Stats.DAMAGE_DEALT, Math.round(f6 * 10.0F));
                     if (j > 0) {
                        entity.setSecondsOnFire(j * 4);
                     }

                     if (this.level() instanceof ServerLevel && f6 > 2.0F) {
                        int k = (int)((double)f6 * 0.5D);
                        ((ServerLevel)this.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5D), entity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                     }
                  }

                  this.causeFoodExhaustion(0.1F);
               } else {
                  this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
                  if (flag4) {
                     entity.clearFire();
                  }
               }
            }

         }
      }
   }

   protected void doAutoAttackOnTouch(LivingEntity livingentity) {
      this.attack(livingentity);
   }

   public void disableShield(boolean flag) {
      float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
      if (flag) {
         f += 0.75F;
      }

      if (this.random.nextFloat() < f) {
         this.getCooldowns().addCooldown(Items.SHIELD, 100);
         this.stopUsingItem();
         this.level().broadcastEntityEvent(this, (byte)30);
      }

   }

   public void crit(Entity entity) {
   }

   public void magicCrit(Entity entity) {
   }

   public void sweepAttack() {
      double d0 = (double)(-Mth.sin(this.getYRot() * ((float)Math.PI / 180F)));
      double d1 = (double)Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
      if (this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d0, this.getY(0.5D), this.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
      }

   }

   public void respawn() {
   }

   public void remove(Entity.RemovalReason entity_removalreason) {
      super.remove(entity_removalreason);
      this.inventoryMenu.removed(this);
      if (this.containerMenu != null && this.hasContainerOpen()) {
         this.doCloseContainer();
      }

   }

   public boolean isLocalPlayer() {
      return false;
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public Abilities getAbilities() {
      return this.abilities;
   }

   public void updateTutorialInventoryAction(ItemStack itemstack, ItemStack itemstack1, ClickAction clickaction) {
   }

   public boolean hasContainerOpen() {
      return this.containerMenu != this.inventoryMenu;
   }

   public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockpos) {
      this.startSleeping(blockpos);
      this.sleepCounter = 0;
      return Either.right(Unit.INSTANCE);
   }

   public void stopSleepInBed(boolean flag, boolean flag1) {
      super.stopSleeping();
      if (this.level() instanceof ServerLevel && flag1) {
         ((ServerLevel)this.level()).updateSleepingPlayerList();
      }

      this.sleepCounter = flag ? 0 : 100;
   }

   public void stopSleeping() {
      this.stopSleepInBed(true, true);
   }

   public static Optional<Vec3> findRespawnPositionAndUseSpawnBlock(ServerLevel serverlevel, BlockPos blockpos, float f, boolean flag, boolean flag1) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (block instanceof RespawnAnchorBlock && (flag || blockstate.getValue(RespawnAnchorBlock.CHARGE) > 0) && RespawnAnchorBlock.canSetSpawn(serverlevel)) {
         Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, serverlevel, blockpos);
         if (!flag && !flag1 && optional.isPresent()) {
            serverlevel.setBlock(blockpos, blockstate.setValue(RespawnAnchorBlock.CHARGE, Integer.valueOf(blockstate.getValue(RespawnAnchorBlock.CHARGE) - 1)), 3);
         }

         return optional;
      } else if (block instanceof BedBlock && BedBlock.canSetSpawn(serverlevel)) {
         return BedBlock.findStandUpPosition(EntityType.PLAYER, serverlevel, blockpos, blockstate.getValue(BedBlock.FACING), f);
      } else if (!flag) {
         return Optional.empty();
      } else {
         boolean flag2 = block.isPossibleToRespawnInThis(blockstate);
         BlockState blockstate1 = serverlevel.getBlockState(blockpos.above());
         boolean flag3 = blockstate1.getBlock().isPossibleToRespawnInThis(blockstate1);
         return flag2 && flag3 ? Optional.of(new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.1D, (double)blockpos.getZ() + 0.5D)) : Optional.empty();
      }
   }

   public boolean isSleepingLongEnough() {
      return this.isSleeping() && this.sleepCounter >= 100;
   }

   public int getSleepTimer() {
      return this.sleepCounter;
   }

   public void displayClientMessage(Component component, boolean flag) {
   }

   public void awardStat(ResourceLocation resourcelocation) {
      this.awardStat(Stats.CUSTOM.get(resourcelocation));
   }

   public void awardStat(ResourceLocation resourcelocation, int i) {
      this.awardStat(Stats.CUSTOM.get(resourcelocation), i);
   }

   public void awardStat(Stat<?> stat) {
      this.awardStat(stat, 1);
   }

   public void awardStat(Stat<?> stat, int i) {
   }

   public void resetStat(Stat<?> stat) {
   }

   public int awardRecipes(Collection<Recipe<?>> collection) {
      return 0;
   }

   public void triggerRecipeCrafted(Recipe<?> recipe, List<ItemStack> list) {
   }

   public void awardRecipesByKey(ResourceLocation[] aresourcelocation) {
   }

   public int resetRecipes(Collection<Recipe<?>> collection) {
      return 0;
   }

   public void jumpFromGround() {
      super.jumpFromGround();
      this.awardStat(Stats.JUMP);
      if (this.isSprinting()) {
         this.causeFoodExhaustion(0.2F);
      } else {
         this.causeFoodExhaustion(0.05F);
      }

   }

   public void travel(Vec3 vec3) {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      if (this.isSwimming() && !this.isPassenger()) {
         double d3 = this.getLookAngle().y;
         double d4 = d3 < -0.2D ? 0.085D : 0.06D;
         if (d3 <= 0.0D || this.jumping || !this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() + 1.0D - 0.1D, this.getZ())).getFluidState().isEmpty()) {
            Vec3 vec31 = this.getDeltaMovement();
            this.setDeltaMovement(vec31.add(0.0D, (d3 - vec31.y) * d4, 0.0D));
         }
      }

      if (this.abilities.flying && !this.isPassenger()) {
         double d5 = this.getDeltaMovement().y;
         super.travel(vec3);
         Vec3 vec32 = this.getDeltaMovement();
         this.setDeltaMovement(vec32.x, d5 * 0.6D, vec32.z);
         this.resetFallDistance();
         this.setSharedFlag(7, false);
      } else {
         super.travel(vec3);
      }

      this.checkMovementStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
   }

   public void updateSwimming() {
      if (this.abilities.flying) {
         this.setSwimming(false);
      } else {
         super.updateSwimming();
      }

   }

   protected boolean freeAt(BlockPos blockpos) {
      return !this.level().getBlockState(blockpos).isSuffocating(this.level(), blockpos);
   }

   public float getSpeed() {
      return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
   }

   public void checkMovementStatistics(double d0, double d1, double d2) {
      if (!this.isPassenger()) {
         if (this.isSwimming()) {
            int i = Math.round((float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
            if (i > 0) {
               this.awardStat(Stats.SWIM_ONE_CM, i);
               this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
            }
         } else if (this.isEyeInFluid(FluidTags.WATER)) {
            int j = Math.round((float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
            if (j > 0) {
               this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, j);
               this.causeFoodExhaustion(0.01F * (float)j * 0.01F);
            }
         } else if (this.isInWater()) {
            int k = Math.round((float)Math.sqrt(d0 * d0 + d2 * d2) * 100.0F);
            if (k > 0) {
               this.awardStat(Stats.WALK_ON_WATER_ONE_CM, k);
               this.causeFoodExhaustion(0.01F * (float)k * 0.01F);
            }
         } else if (this.onClimbable()) {
            if (d1 > 0.0D) {
               this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(d1 * 100.0D));
            }
         } else if (this.onGround()) {
            int l = Math.round((float)Math.sqrt(d0 * d0 + d2 * d2) * 100.0F);
            if (l > 0) {
               if (this.isSprinting()) {
                  this.awardStat(Stats.SPRINT_ONE_CM, l);
                  this.causeFoodExhaustion(0.1F * (float)l * 0.01F);
               } else if (this.isCrouching()) {
                  this.awardStat(Stats.CROUCH_ONE_CM, l);
                  this.causeFoodExhaustion(0.0F * (float)l * 0.01F);
               } else {
                  this.awardStat(Stats.WALK_ONE_CM, l);
                  this.causeFoodExhaustion(0.0F * (float)l * 0.01F);
               }
            }
         } else if (this.isFallFlying()) {
            int i1 = Math.round((float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
            this.awardStat(Stats.AVIATE_ONE_CM, i1);
         } else {
            int j1 = Math.round((float)Math.sqrt(d0 * d0 + d2 * d2) * 100.0F);
            if (j1 > 25) {
               this.awardStat(Stats.FLY_ONE_CM, j1);
            }
         }

      }
   }

   private void checkRidingStatistics(double d0, double d1, double d2) {
      if (this.isPassenger()) {
         int i = Math.round((float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
         if (i > 0) {
            Entity entity = this.getVehicle();
            if (entity instanceof AbstractMinecart) {
               this.awardStat(Stats.MINECART_ONE_CM, i);
            } else if (entity instanceof Boat) {
               this.awardStat(Stats.BOAT_ONE_CM, i);
            } else if (entity instanceof Pig) {
               this.awardStat(Stats.PIG_ONE_CM, i);
            } else if (entity instanceof AbstractHorse) {
               this.awardStat(Stats.HORSE_ONE_CM, i);
            } else if (entity instanceof Strider) {
               this.awardStat(Stats.STRIDER_ONE_CM, i);
            }
         }
      }

   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      if (this.abilities.mayfly) {
         return false;
      } else {
         if (f >= 2.0F) {
            this.awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)f * 100.0D));
         }

         return super.causeFallDamage(f, f1, damagesource);
      }
   }

   public boolean tryToStartFallFlying() {
      if (!this.onGround() && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION)) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
         if (itemstack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemstack)) {
            this.startFallFlying();
            return true;
         }
      }

      return false;
   }

   public void startFallFlying() {
      this.setSharedFlag(7, true);
   }

   public void stopFallFlying() {
      this.setSharedFlag(7, true);
      this.setSharedFlag(7, false);
   }

   protected void doWaterSplashEffect() {
      if (!this.isSpectator()) {
         super.doWaterSplashEffect();
      }

   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      if (this.isInWater()) {
         this.waterSwimSound();
         this.playMuffledStepSound(blockstate);
      } else {
         BlockPos blockpos1 = this.getPrimaryStepSoundBlockPos(blockpos);
         if (!blockpos.equals(blockpos1)) {
            BlockState blockstate1 = this.level().getBlockState(blockpos1);
            if (blockstate1.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
               this.playCombinationStepSounds(blockstate1, blockstate);
            } else {
               super.playStepSound(blockpos1, blockstate1);
            }
         } else {
            super.playStepSound(blockpos, blockstate);
         }
      }

   }

   public LivingEntity.Fallsounds getFallSounds() {
      return new LivingEntity.Fallsounds(SoundEvents.PLAYER_SMALL_FALL, SoundEvents.PLAYER_BIG_FALL);
   }

   public boolean killedEntity(ServerLevel serverlevel, LivingEntity livingentity) {
      this.awardStat(Stats.ENTITY_KILLED.get(livingentity.getType()));
      return true;
   }

   public void makeStuckInBlock(BlockState blockstate, Vec3 vec3) {
      if (!this.abilities.flying) {
         super.makeStuckInBlock(blockstate, vec3);
      }

   }

   public void giveExperiencePoints(int i) {
      this.increaseScore(i);
      this.experienceProgress += (float)i / (float)this.getXpNeededForNextLevel();
      this.totalExperience = Mth.clamp(this.totalExperience + i, 0, Integer.MAX_VALUE);

      while(this.experienceProgress < 0.0F) {
         float f = this.experienceProgress * (float)this.getXpNeededForNextLevel();
         if (this.experienceLevel > 0) {
            this.giveExperienceLevels(-1);
            this.experienceProgress = 1.0F + f / (float)this.getXpNeededForNextLevel();
         } else {
            this.giveExperienceLevels(-1);
            this.experienceProgress = 0.0F;
         }
      }

      while(this.experienceProgress >= 1.0F) {
         this.experienceProgress = (this.experienceProgress - 1.0F) * (float)this.getXpNeededForNextLevel();
         this.giveExperienceLevels(1);
         this.experienceProgress /= (float)this.getXpNeededForNextLevel();
      }

   }

   public int getEnchantmentSeed() {
      return this.enchantmentSeed;
   }

   public void onEnchantmentPerformed(ItemStack itemstack, int i) {
      this.experienceLevel -= i;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experienceProgress = 0.0F;
         this.totalExperience = 0;
      }

      this.enchantmentSeed = this.random.nextInt();
   }

   public void giveExperienceLevels(int i) {
      this.experienceLevel += i;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experienceProgress = 0.0F;
         this.totalExperience = 0;
      }

      if (i > 0 && this.experienceLevel % 5 == 0 && (float)this.lastLevelUpTime < (float)this.tickCount - 100.0F) {
         float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), f * 0.75F, 1.0F);
         this.lastLevelUpTime = this.tickCount;
      }

   }

   public int getXpNeededForNextLevel() {
      if (this.experienceLevel >= 30) {
         return 112 + (this.experienceLevel - 30) * 9;
      } else {
         return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
      }
   }

   public void causeFoodExhaustion(float f) {
      if (!this.abilities.invulnerable) {
         if (!this.level().isClientSide) {
            this.foodData.addExhaustion(f);
         }

      }
   }

   public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
      return Optional.empty();
   }

   public FoodData getFoodData() {
      return this.foodData;
   }

   public boolean canEat(boolean flag) {
      return this.abilities.invulnerable || flag || this.foodData.needsFood();
   }

   public boolean isHurt() {
      return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
   }

   public boolean mayBuild() {
      return this.abilities.mayBuild;
   }

   public boolean mayUseItemAt(BlockPos blockpos, Direction direction, ItemStack itemstack) {
      if (this.abilities.mayBuild) {
         return true;
      } else {
         BlockPos blockpos1 = blockpos.relative(direction.getOpposite());
         BlockInWorld blockinworld = new BlockInWorld(this.level(), blockpos1, false);
         return itemstack.hasAdventureModePlaceTagForBlock(this.level().registryAccess().registryOrThrow(Registries.BLOCK), blockinworld);
      }
   }

   public int getExperienceReward() {
      if (!this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !this.isSpectator()) {
         int i = this.experienceLevel * 7;
         return i > 100 ? 100 : i;
      } else {
         return 0;
      }
   }

   protected boolean isAlwaysExperienceDropper() {
      return true;
   }

   public boolean shouldShowName() {
      return true;
   }

   protected Entity.MovementEmission getMovementEmission() {
      return this.abilities.flying || this.onGround() && this.isDiscrete() ? Entity.MovementEmission.NONE : Entity.MovementEmission.ALL;
   }

   public void onUpdateAbilities() {
   }

   public Component getName() {
      return Component.literal(this.gameProfile.getName());
   }

   public PlayerEnderChestContainer getEnderChestInventory() {
      return this.enderChestInventory;
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentslot) {
      if (equipmentslot == EquipmentSlot.MAINHAND) {
         return this.inventory.getSelected();
      } else if (equipmentslot == EquipmentSlot.OFFHAND) {
         return this.inventory.offhand.get(0);
      } else {
         return equipmentslot.getType() == EquipmentSlot.Type.ARMOR ? this.inventory.armor.get(equipmentslot.getIndex()) : ItemStack.EMPTY;
      }
   }

   protected boolean doesEmitEquipEvent(EquipmentSlot equipmentslot) {
      return equipmentslot.getType() == EquipmentSlot.Type.ARMOR;
   }

   public void setItemSlot(EquipmentSlot equipmentslot, ItemStack itemstack) {
      this.verifyEquippedItem(itemstack);
      if (equipmentslot == EquipmentSlot.MAINHAND) {
         this.onEquipItem(equipmentslot, this.inventory.items.set(this.inventory.selected, itemstack), itemstack);
      } else if (equipmentslot == EquipmentSlot.OFFHAND) {
         this.onEquipItem(equipmentslot, this.inventory.offhand.set(0, itemstack), itemstack);
      } else if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
         this.onEquipItem(equipmentslot, this.inventory.armor.set(equipmentslot.getIndex(), itemstack), itemstack);
      }

   }

   public boolean addItem(ItemStack itemstack) {
      return this.inventory.add(itemstack);
   }

   public Iterable<ItemStack> getHandSlots() {
      return Lists.newArrayList(this.getMainHandItem(), this.getOffhandItem());
   }

   public Iterable<ItemStack> getArmorSlots() {
      return this.inventory.armor;
   }

   public boolean setEntityOnShoulder(CompoundTag compoundtag) {
      if (!this.isPassenger() && this.onGround() && !this.isInWater() && !this.isInPowderSnow) {
         if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(compoundtag);
            this.timeEntitySatOnShoulder = this.level().getGameTime();
            return true;
         } else if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(compoundtag);
            this.timeEntitySatOnShoulder = this.level().getGameTime();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected void removeEntitiesOnShoulder() {
      if (this.timeEntitySatOnShoulder + 20L < this.level().getGameTime()) {
         this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
         this.setShoulderEntityLeft(new CompoundTag());
         this.respawnEntityOnShoulder(this.getShoulderEntityRight());
         this.setShoulderEntityRight(new CompoundTag());
      }

   }

   private void respawnEntityOnShoulder(CompoundTag compoundtag) {
      if (!this.level().isClientSide && !compoundtag.isEmpty()) {
         EntityType.create(compoundtag, this.level()).ifPresent((entity) -> {
            if (entity instanceof TamableAnimal) {
               ((TamableAnimal)entity).setOwnerUUID(this.uuid);
            }

            entity.setPos(this.getX(), this.getY() + (double)0.7F, this.getZ());
            ((ServerLevel)this.level()).addWithUUID(entity);
         });
      }

   }

   public abstract boolean isSpectator();

   public boolean canBeHitByProjectile() {
      return !this.isSpectator() && super.canBeHitByProjectile();
   }

   public boolean isSwimming() {
      return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
   }

   public abstract boolean isCreative();

   public boolean isPushedByFluid() {
      return !this.abilities.flying;
   }

   public Scoreboard getScoreboard() {
      return this.level().getScoreboard();
   }

   public Component getDisplayName() {
      MutableComponent mutablecomponent = PlayerTeam.formatNameForTeam(this.getTeam(), this.getName());
      return this.decorateDisplayNameComponent(mutablecomponent);
   }

   private MutableComponent decorateDisplayNameComponent(MutableComponent mutablecomponent) {
      String s = this.getGameProfile().getName();
      return mutablecomponent.withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + s + " ")).withHoverEvent(this.createHoverEvent()).withInsertion(s));
   }

   public String getScoreboardName() {
      return this.getGameProfile().getName();
   }

   public float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      switch (pose) {
         case SWIMMING:
         case FALL_FLYING:
         case SPIN_ATTACK:
            return 0.4F;
         case CROUCHING:
            return 1.27F;
         default:
            return 1.62F;
      }
   }

   public void setAbsorptionAmount(float f) {
      if (f < 0.0F) {
         f = 0.0F;
      }

      this.getEntityData().set(DATA_PLAYER_ABSORPTION_ID, f);
   }

   public float getAbsorptionAmount() {
      return this.getEntityData().get(DATA_PLAYER_ABSORPTION_ID);
   }

   public boolean isModelPartShown(PlayerModelPart playermodelpart) {
      return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & playermodelpart.getMask()) == playermodelpart.getMask();
   }

   public SlotAccess getSlot(int i) {
      if (i >= 0 && i < this.inventory.items.size()) {
         return SlotAccess.forContainer(this.inventory, i);
      } else {
         int j = i - 200;
         return j >= 0 && j < this.enderChestInventory.getContainerSize() ? SlotAccess.forContainer(this.enderChestInventory, j) : super.getSlot(i);
      }
   }

   public boolean isReducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public void setReducedDebugInfo(boolean flag) {
      this.reducedDebugInfo = flag;
   }

   public void setRemainingFireTicks(int i) {
      super.setRemainingFireTicks(this.abilities.invulnerable ? Math.min(i, 1) : i);
   }

   public HumanoidArm getMainArm() {
      return this.entityData.get(DATA_PLAYER_MAIN_HAND) == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
   }

   public void setMainArm(HumanoidArm humanoidarm) {
      this.entityData.set(DATA_PLAYER_MAIN_HAND, (byte)(humanoidarm == HumanoidArm.LEFT ? 0 : 1));
   }

   public CompoundTag getShoulderEntityLeft() {
      return this.entityData.get(DATA_SHOULDER_LEFT);
   }

   protected void setShoulderEntityLeft(CompoundTag compoundtag) {
      this.entityData.set(DATA_SHOULDER_LEFT, compoundtag);
   }

   public CompoundTag getShoulderEntityRight() {
      return this.entityData.get(DATA_SHOULDER_RIGHT);
   }

   protected void setShoulderEntityRight(CompoundTag compoundtag) {
      this.entityData.set(DATA_SHOULDER_RIGHT, compoundtag);
   }

   public float getCurrentItemAttackStrengthDelay() {
      return (float)(1.0D / this.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0D);
   }

   public float getAttackStrengthScale(float f) {
      return Mth.clamp(((float)this.attackStrengthTicker + f) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
   }

   public void resetAttackStrengthTicker() {
      this.attackStrengthTicker = 0;
   }

   public ItemCooldowns getCooldowns() {
      return this.cooldowns;
   }

   protected float getBlockSpeedFactor() {
      return !this.abilities.flying && !this.isFallFlying() ? super.getBlockSpeedFactor() : 1.0F;
   }

   public float getLuck() {
      return (float)this.getAttributeValue(Attributes.LUCK);
   }

   public boolean canUseGameMasterBlocks() {
      return this.abilities.instabuild && this.getPermissionLevel() >= 2;
   }

   public boolean canTakeItem(ItemStack itemstack) {
      EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
      return this.getItemBySlot(equipmentslot).isEmpty();
   }

   public EntityDimensions getDimensions(Pose pose) {
      return POSES.getOrDefault(pose, STANDING_DIMENSIONS);
   }

   public ImmutableList<Pose> getDismountPoses() {
      return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
   }

   public ItemStack getProjectile(ItemStack itemstack) {
      if (!(itemstack.getItem() instanceof ProjectileWeaponItem)) {
         return ItemStack.EMPTY;
      } else {
         Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemstack.getItem()).getSupportedHeldProjectiles();
         ItemStack itemstack1 = ProjectileWeaponItem.getHeldProjectile(this, predicate);
         if (!itemstack1.isEmpty()) {
            return itemstack1;
         } else {
            predicate = ((ProjectileWeaponItem)itemstack.getItem()).getAllSupportedProjectiles();

            for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
               ItemStack itemstack2 = this.inventory.getItem(i);
               if (predicate.test(itemstack2)) {
                  return itemstack2;
               }
            }

            return this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
         }
      }
   }

   public ItemStack eat(Level level, ItemStack itemstack) {
      this.getFoodData().eat(itemstack.getItem(), itemstack);
      this.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
      level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
      if (this instanceof ServerPlayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)this, itemstack);
      }

      return super.eat(level, itemstack);
   }

   protected boolean shouldRemoveSoulSpeed(BlockState blockstate) {
      return this.abilities.flying || super.shouldRemoveSoulSpeed(blockstate);
   }

   public Vec3 getRopeHoldPosition(float f) {
      double d0 = 0.22D * (this.getMainArm() == HumanoidArm.RIGHT ? -1.0D : 1.0D);
      float f1 = Mth.lerp(f * 0.5F, this.getXRot(), this.xRotO) * ((float)Math.PI / 180F);
      float f2 = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180F);
      if (!this.isFallFlying() && !this.isAutoSpinAttack()) {
         if (this.isVisuallySwimming()) {
            return this.getPosition(f).add((new Vec3(d0, 0.2D, -0.15D)).xRot(-f1).yRot(-f2));
         } else {
            double d5 = this.getBoundingBox().getYsize() - 1.0D;
            double d6 = this.isCrouching() ? -0.2D : 0.07D;
            return this.getPosition(f).add((new Vec3(d0, d5, d6)).yRot(-f2));
         }
      } else {
         Vec3 vec3 = this.getViewVector(f);
         Vec3 vec31 = this.getDeltaMovement();
         double d1 = vec31.horizontalDistanceSqr();
         double d2 = vec3.horizontalDistanceSqr();
         float f3;
         if (d1 > 0.0D && d2 > 0.0D) {
            double d3 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d1 * d2);
            double d4 = vec31.x * vec3.z - vec31.z * vec3.x;
            f3 = (float)(Math.signum(d4) * Math.acos(d3));
         } else {
            f3 = 0.0F;
         }

         return this.getPosition(f).add((new Vec3(d0, -0.11D, 0.85D)).zRot(-f3).xRot(-f1).yRot(-f2));
      }
   }

   public boolean isAlwaysTicking() {
      return true;
   }

   public boolean isScoping() {
      return this.isUsingItem() && this.getUseItem().is(Items.SPYGLASS);
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public Optional<GlobalPos> getLastDeathLocation() {
      return this.lastDeathLocation;
   }

   public void setLastDeathLocation(Optional<GlobalPos> optional) {
      this.lastDeathLocation = optional;
   }

   public float getHurtDir() {
      return this.hurtDir;
   }

   public void animateHurt(float f) {
      super.animateHurt(f);
      this.hurtDir = f;
   }

   public boolean canSprint() {
      return true;
   }

   protected float getFlyingSpeed() {
      if (this.abilities.flying && !this.isPassenger()) {
         return this.isSprinting() ? this.abilities.getFlyingSpeed() * 2.0F : this.abilities.getFlyingSpeed();
      } else {
         return this.isSprinting() ? 0.025999999F : 0.02F;
      }
   }

   public static enum BedSleepingProblem {
      NOT_POSSIBLE_HERE,
      NOT_POSSIBLE_NOW(Component.translatable("block.minecraft.bed.no_sleep")),
      TOO_FAR_AWAY(Component.translatable("block.minecraft.bed.too_far_away")),
      OBSTRUCTED(Component.translatable("block.minecraft.bed.obstructed")),
      OTHER_PROBLEM,
      NOT_SAFE(Component.translatable("block.minecraft.bed.not_safe"));

      @Nullable
      private final Component message;

      private BedSleepingProblem() {
         this.message = null;
      }

      private BedSleepingProblem(Component component) {
         this.message = component;
      }

      @Nullable
      public Component getMessage() {
         return this.message;
      }
   }
}
