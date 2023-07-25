package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Piglin extends AbstractPiglin implements CrossbowAttackMob, InventoryCarrier {
   private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
   private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
   private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", (double)0.2F, AttributeModifier.Operation.MULTIPLY_BASE);
   private static final int MAX_HEALTH = 16;
   private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
   private static final int ATTACK_DAMAGE = 5;
   private static final float CROSSBOW_POWER = 1.6F;
   private static final float CHANCE_OF_WEARING_EACH_ARMOUR_ITEM = 0.1F;
   private static final int MAX_PASSENGERS_ON_ONE_HOGLIN = 3;
   private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2F;
   private static final float BABY_EYE_HEIGHT_ADJUSTMENT = 0.82F;
   private static final double PROBABILITY_OF_SPAWNING_WITH_CROSSBOW_INSTEAD_OF_SWORD = 0.5D;
   private final SimpleContainer inventory = new SimpleContainer(8);
   private boolean cannotHunt;
   protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT);

   public Piglin(EntityType<? extends AbstractPiglin> entitytype, Level level) {
      super(entitytype, level);
      this.xpReward = 5;
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      if (this.isBaby()) {
         compoundtag.putBoolean("IsBaby", true);
      }

      if (this.cannotHunt) {
         compoundtag.putBoolean("CannotHunt", true);
      }

      this.writeInventoryToTag(compoundtag);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setBaby(compoundtag.getBoolean("IsBaby"));
      this.setCannotHunt(compoundtag.getBoolean("CannotHunt"));
      this.readInventoryFromTag(compoundtag);
   }

   @VisibleForDebug
   public SimpleContainer getInventory() {
      return this.inventory;
   }

   protected void dropCustomDeathLoot(DamageSource damagesource, int i, boolean flag) {
      super.dropCustomDeathLoot(damagesource, i, flag);
      Entity entity = damagesource.getEntity();
      if (entity instanceof Creeper creeper) {
         if (creeper.canDropMobsSkull()) {
            ItemStack itemstack = new ItemStack(Items.PIGLIN_HEAD);
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(itemstack);
         }
      }

      this.inventory.removeAllItems().forEach(this::spawnAtLocation);
   }

   protected ItemStack addToInventory(ItemStack itemstack) {
      return this.inventory.addItem(itemstack);
   }

   protected boolean canAddToInventory(ItemStack itemstack) {
      return this.inventory.canAddItem(itemstack);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
      this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
      this.entityData.define(DATA_IS_DANCING, false);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      super.onSyncedDataUpdated(entitydataaccessor);
      if (DATA_BABY_ID.equals(entitydataaccessor)) {
         this.refreshDimensions();
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.MOVEMENT_SPEED, (double)0.35F).add(Attributes.ATTACK_DAMAGE, 5.0D);
   }

   public static boolean checkPiglinSpawnRules(EntityType<Piglin> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      return !levelaccessor.getBlockState(blockpos.below()).is(Blocks.NETHER_WART_BLOCK);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      RandomSource randomsource = serverlevelaccessor.getRandom();
      if (mobspawntype != MobSpawnType.STRUCTURE) {
         if (randomsource.nextFloat() < 0.2F) {
            this.setBaby(true);
         } else if (this.isAdult()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
         }
      }

      PiglinAi.initMemories(this, serverlevelaccessor.getRandom());
      this.populateDefaultEquipmentSlots(randomsource, difficultyinstance);
      this.populateDefaultEquipmentEnchantments(randomsource, difficultyinstance);
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   public boolean removeWhenFarAway(double d0) {
      return !this.isPersistenceRequired();
   }

   protected void populateDefaultEquipmentSlots(RandomSource randomsource, DifficultyInstance difficultyinstance) {
      if (this.isAdult()) {
         this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), randomsource);
         this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), randomsource);
         this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), randomsource);
         this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), randomsource);
      }

   }

   private void maybeWearArmor(EquipmentSlot equipmentslot, ItemStack itemstack, RandomSource randomsource) {
      if (randomsource.nextFloat() < 0.1F) {
         this.setItemSlot(equipmentslot, itemstack);
      }

   }

   protected Brain.Provider<Piglin> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return PiglinAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
   }

   public Brain<Piglin> getBrain() {
      return super.getBrain();
   }

   public InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      InteractionResult interactionresult = super.mobInteract(player, interactionhand);
      if (interactionresult.consumesAction()) {
         return interactionresult;
      } else if (!this.level().isClientSide) {
         return PiglinAi.mobInteract(this, player, interactionhand);
      } else {
         boolean flag = PiglinAi.canAdmire(this, player.getItemInHand(interactionhand)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
         return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
      }
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      float f = super.getStandingEyeHeight(pose, entitydimensions);
      return this.isBaby() ? f - 0.82F : f;
   }

   public double getPassengersRidingOffset() {
      return (double)this.getBbHeight() * 0.92D;
   }

   public void setBaby(boolean flag) {
      this.getEntityData().set(DATA_BABY_ID, flag);
      if (!this.level().isClientSide) {
         AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
         attributeinstance.removeModifier(SPEED_MODIFIER_BABY);
         if (flag) {
            attributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
         }
      }

   }

   public boolean isBaby() {
      return this.getEntityData().get(DATA_BABY_ID);
   }

   private void setCannotHunt(boolean flag) {
      this.cannotHunt = flag;
   }

   protected boolean canHunt() {
      return !this.cannotHunt;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("piglinBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      PiglinAi.updateActivity(this);
      super.customServerAiStep();
   }

   public int getExperienceReward() {
      return this.xpReward;
   }

   protected void finishConversion(ServerLevel serverlevel) {
      PiglinAi.cancelAdmiring(this);
      this.inventory.removeAllItems().forEach(this::spawnAtLocation);
      super.finishConversion(serverlevel);
   }

   private ItemStack createSpawnWeapon() {
      return (double)this.random.nextFloat() < 0.5D ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
   }

   private boolean isChargingCrossbow() {
      return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
   }

   public void setChargingCrossbow(boolean flag) {
      this.entityData.set(DATA_IS_CHARGING_CROSSBOW, flag);
   }

   public void onCrossbowAttackPerformed() {
      this.noActionTime = 0;
   }

   public PiglinArmPose getArmPose() {
      if (this.isDancing()) {
         return PiglinArmPose.DANCING;
      } else if (PiglinAi.isLovedItem(this.getOffhandItem())) {
         return PiglinArmPose.ADMIRING_ITEM;
      } else if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
         return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
      } else if (this.isChargingCrossbow()) {
         return PiglinArmPose.CROSSBOW_CHARGE;
      } else {
         return this.isAggressive() && this.isHolding(Items.CROSSBOW) ? PiglinArmPose.CROSSBOW_HOLD : PiglinArmPose.DEFAULT;
      }
   }

   public boolean isDancing() {
      return this.entityData.get(DATA_IS_DANCING);
   }

   public void setDancing(boolean flag) {
      this.entityData.set(DATA_IS_DANCING, flag);
   }

   public boolean hurt(DamageSource damagesource, float f) {
      boolean flag = super.hurt(damagesource, f);
      if (this.level().isClientSide) {
         return false;
      } else {
         if (flag && damagesource.getEntity() instanceof LivingEntity) {
            PiglinAi.wasHurtBy(this, (LivingEntity)damagesource.getEntity());
         }

         return flag;
      }
   }

   public void performRangedAttack(LivingEntity livingentity, float f) {
      this.performCrossbowAttack(this, 1.6F);
   }

   public void shootCrossbowProjectile(LivingEntity livingentity, ItemStack itemstack, Projectile projectile, float f) {
      this.shootCrossbowProjectile(this, livingentity, projectile, f, 1.6F);
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileweaponitem) {
      return projectileweaponitem == Items.CROSSBOW;
   }

   protected void holdInMainHand(ItemStack itemstack) {
      this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemstack);
   }

   protected void holdInOffHand(ItemStack itemstack) {
      if (itemstack.is(PiglinAi.BARTERING_ITEM)) {
         this.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
         this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
      } else {
         this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, itemstack);
      }

   }

   public boolean wantsToPickUp(ItemStack itemstack) {
      return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.canPickUpLoot() && PiglinAi.wantsToPickup(this, itemstack);
   }

   protected boolean canReplaceCurrentItem(ItemStack itemstack) {
      EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
      ItemStack itemstack1 = this.getItemBySlot(equipmentslot);
      return this.canReplaceCurrentItem(itemstack, itemstack1);
   }

   protected boolean canReplaceCurrentItem(ItemStack itemstack, ItemStack itemstack1) {
      if (EnchantmentHelper.hasBindingCurse(itemstack1)) {
         return false;
      } else {
         boolean flag = PiglinAi.isLovedItem(itemstack) || itemstack.is(Items.CROSSBOW);
         boolean flag1 = PiglinAi.isLovedItem(itemstack1) || itemstack1.is(Items.CROSSBOW);
         if (flag && !flag1) {
            return true;
         } else if (!flag && flag1) {
            return false;
         } else {
            return this.isAdult() && !itemstack.is(Items.CROSSBOW) && itemstack1.is(Items.CROSSBOW) ? false : super.canReplaceCurrentItem(itemstack, itemstack1);
         }
      }
   }

   protected void pickUpItem(ItemEntity itementity) {
      this.onItemPickup(itementity);
      PiglinAi.pickUpItem(this, itementity);
   }

   public boolean startRiding(Entity entity, boolean flag) {
      if (this.isBaby() && entity.getType() == EntityType.HOGLIN) {
         entity = this.getTopPassenger(entity, 3);
      }

      return super.startRiding(entity, flag);
   }

   private Entity getTopPassenger(Entity entity, int i) {
      List<Entity> list = entity.getPassengers();
      return i != 1 && !list.isEmpty() ? this.getTopPassenger(list.get(0), i - 1) : entity;
   }

   protected SoundEvent getAmbientSound() {
      return this.level().isClientSide ? null : PiglinAi.getSoundForCurrentActivity(this).orElse((SoundEvent)null);
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.PIGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PIGLIN_DEATH;
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playSoundEvent(SoundEvent soundevent) {
      this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
   }

   protected void playConvertedSound() {
      this.playSoundEvent(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
   }
}
