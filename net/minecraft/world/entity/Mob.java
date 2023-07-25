package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class Mob extends LivingEntity implements Targeting {
   private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
   private static final int MOB_FLAG_NO_AI = 1;
   private static final int MOB_FLAG_LEFTHANDED = 2;
   private static final int MOB_FLAG_AGGRESSIVE = 4;
   protected static final int PICKUP_REACH = 1;
   private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
   public static final float MAX_WEARING_ARMOR_CHANCE = 0.15F;
   public static final float MAX_PICKUP_LOOT_CHANCE = 0.55F;
   public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5F;
   public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25F;
   public static final String LEASH_TAG = "Leash";
   public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
   public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
   public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
   public int ambientSoundTime;
   protected int xpReward;
   protected LookControl lookControl;
   protected MoveControl moveControl;
   protected JumpControl jumpControl;
   private final BodyRotationControl bodyRotationControl;
   protected PathNavigation navigation;
   protected final GoalSelector goalSelector;
   protected final GoalSelector targetSelector;
   @Nullable
   private LivingEntity target;
   private final Sensing sensing;
   private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
   protected final float[] handDropChances = new float[2];
   private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
   protected final float[] armorDropChances = new float[4];
   private boolean canPickUpLoot;
   private boolean persistenceRequired;
   private final Map<BlockPathTypes, Float> pathfindingMalus = Maps.newEnumMap(BlockPathTypes.class);
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;
   @Nullable
   private Entity leashHolder;
   private int delayedLeashHolderId;
   @Nullable
   private CompoundTag leashInfoTag;
   private BlockPos restrictCenter = BlockPos.ZERO;
   private float restrictRadius = -1.0F;

   protected Mob(EntityType<? extends Mob> entitytype, Level level) {
      super(entitytype, level);
      this.goalSelector = new GoalSelector(level.getProfilerSupplier());
      this.targetSelector = new GoalSelector(level.getProfilerSupplier());
      this.lookControl = new LookControl(this);
      this.moveControl = new MoveControl(this);
      this.jumpControl = new JumpControl(this);
      this.bodyRotationControl = this.createBodyControl();
      this.navigation = this.createNavigation(level);
      this.sensing = new Sensing(this);
      Arrays.fill(this.armorDropChances, 0.085F);
      Arrays.fill(this.handDropChances, 0.085F);
      if (level != null && !level.isClientSide) {
         this.registerGoals();
      }

   }

   protected void registerGoals() {
   }

   public static AttributeSupplier.Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK);
   }

   protected PathNavigation createNavigation(Level level) {
      return new GroundPathNavigation(this, level);
   }

   protected boolean shouldPassengersInheritMalus() {
      return false;
   }

   public float getPathfindingMalus(BlockPathTypes blockpathtypes) {
      Mob mob1;
      label17: {
         Entity var4 = this.getControlledVehicle();
         if (var4 instanceof Mob mob) {
            if (mob.shouldPassengersInheritMalus()) {
               mob1 = mob;
               break label17;
            }
         }

         mob1 = this;
      }

      Float ofloat = mob1.pathfindingMalus.get(blockpathtypes);
      return ofloat == null ? blockpathtypes.getMalus() : ofloat;
   }

   public void setPathfindingMalus(BlockPathTypes blockpathtypes, float f) {
      this.pathfindingMalus.put(blockpathtypes, f);
   }

   public void onPathfindingStart() {
   }

   public void onPathfindingDone() {
   }

   protected BodyRotationControl createBodyControl() {
      return new BodyRotationControl(this);
   }

   public LookControl getLookControl() {
      return this.lookControl;
   }

   public MoveControl getMoveControl() {
      Entity var2 = this.getControlledVehicle();
      if (var2 instanceof Mob mob) {
         return mob.getMoveControl();
      } else {
         return this.moveControl;
      }
   }

   public JumpControl getJumpControl() {
      return this.jumpControl;
   }

   public PathNavigation getNavigation() {
      Entity var2 = this.getControlledVehicle();
      if (var2 instanceof Mob mob) {
         return mob.getNavigation();
      } else {
         return this.navigation;
      }
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (!this.isNoAi()) {
         Entity var2 = this.getFirstPassenger();
         if (var2 instanceof Mob) {
            return (Mob)var2;
         }
      }

      return null;
   }

   public Sensing getSensing() {
      return this.sensing;
   }

   @Nullable
   public LivingEntity getTarget() {
      return this.target;
   }

   public void setTarget(@Nullable LivingEntity livingentity) {
      this.target = livingentity;
   }

   public boolean canAttackType(EntityType<?> entitytype) {
      return entitytype != EntityType.GHAST;
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileweaponitem) {
      return false;
   }

   public void ate() {
      this.gameEvent(GameEvent.EAT);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_MOB_FLAGS_ID, (byte)0);
   }

   public int getAmbientSoundInterval() {
      return 80;
   }

   public void playAmbientSound() {
      SoundEvent soundevent = this.getAmbientSound();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public void baseTick() {
      super.baseTick();
      this.level().getProfiler().push("mobBaseTick");
      if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
         this.resetAmbientSoundTime();
         this.playAmbientSound();
      }

      this.level().getProfiler().pop();
   }

   protected void playHurtSound(DamageSource damagesource) {
      this.resetAmbientSoundTime();
      super.playHurtSound(damagesource);
   }

   private void resetAmbientSoundTime() {
      this.ambientSoundTime = -this.getAmbientSoundInterval();
   }

   public int getExperienceReward() {
      if (this.xpReward > 0) {
         int i = this.xpReward;

         for(int j = 0; j < this.armorItems.size(); ++j) {
            if (!this.armorItems.get(j).isEmpty() && this.armorDropChances[j] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         for(int k = 0; k < this.handItems.size(); ++k) {
            if (!this.handItems.get(k).isEmpty() && this.handDropChances[k] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         return i;
      } else {
         return this.xpReward;
      }
   }

   public void spawnAnim() {
      if (this.level().isClientSide) {
         for(int i = 0; i < 20; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            double d3 = 10.0D;
            this.level().addParticle(ParticleTypes.POOF, this.getX(1.0D) - d0 * 10.0D, this.getRandomY() - d1 * 10.0D, this.getRandomZ(1.0D) - d2 * 10.0D, d0, d1, d2);
         }
      } else {
         this.level().broadcastEntityEvent(this, (byte)20);
      }

   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 20) {
         this.spawnAnim();
      } else {
         super.handleEntityEvent(b0);
      }

   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         this.tickLeash();
         if (this.tickCount % 5 == 0) {
            this.updateControlFlags();
         }
      }

   }

   protected void updateControlFlags() {
      boolean flag = !(this.getControllingPassenger() instanceof Mob);
      boolean flag1 = !(this.getVehicle() instanceof Boat);
      this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
      this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
      this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
   }

   protected float tickHeadTurn(float f, float f1) {
      this.bodyRotationControl.clientTick();
      return f1;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putBoolean("CanPickUpLoot", this.canPickUpLoot());
      compoundtag.putBoolean("PersistenceRequired", this.persistenceRequired);
      ListTag listtag = new ListTag();

      for(ItemStack itemstack : this.armorItems) {
         CompoundTag compoundtag1 = new CompoundTag();
         if (!itemstack.isEmpty()) {
            itemstack.save(compoundtag1);
         }

         listtag.add(compoundtag1);
      }

      compoundtag.put("ArmorItems", listtag);
      ListTag listtag1 = new ListTag();

      for(ItemStack itemstack1 : this.handItems) {
         CompoundTag compoundtag2 = new CompoundTag();
         if (!itemstack1.isEmpty()) {
            itemstack1.save(compoundtag2);
         }

         listtag1.add(compoundtag2);
      }

      compoundtag.put("HandItems", listtag1);
      ListTag listtag2 = new ListTag();

      for(float f : this.armorDropChances) {
         listtag2.add(FloatTag.valueOf(f));
      }

      compoundtag.put("ArmorDropChances", listtag2);
      ListTag listtag3 = new ListTag();

      for(float f1 : this.handDropChances) {
         listtag3.add(FloatTag.valueOf(f1));
      }

      compoundtag.put("HandDropChances", listtag3);
      if (this.leashHolder != null) {
         CompoundTag compoundtag3 = new CompoundTag();
         if (this.leashHolder instanceof LivingEntity) {
            UUID uuid = this.leashHolder.getUUID();
            compoundtag3.putUUID("UUID", uuid);
         } else if (this.leashHolder instanceof HangingEntity) {
            BlockPos blockpos = ((HangingEntity)this.leashHolder).getPos();
            compoundtag3.putInt("X", blockpos.getX());
            compoundtag3.putInt("Y", blockpos.getY());
            compoundtag3.putInt("Z", blockpos.getZ());
         }

         compoundtag.put("Leash", compoundtag3);
      } else if (this.leashInfoTag != null) {
         compoundtag.put("Leash", this.leashInfoTag.copy());
      }

      compoundtag.putBoolean("LeftHanded", this.isLeftHanded());
      if (this.lootTable != null) {
         compoundtag.putString("DeathLootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            compoundtag.putLong("DeathLootTableSeed", this.lootTableSeed);
         }
      }

      if (this.isNoAi()) {
         compoundtag.putBoolean("NoAI", this.isNoAi());
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("CanPickUpLoot", 1)) {
         this.setCanPickUpLoot(compoundtag.getBoolean("CanPickUpLoot"));
      }

      this.persistenceRequired = compoundtag.getBoolean("PersistenceRequired");
      if (compoundtag.contains("ArmorItems", 9)) {
         ListTag listtag = compoundtag.getList("ArmorItems", 10);

         for(int i = 0; i < this.armorItems.size(); ++i) {
            this.armorItems.set(i, ItemStack.of(listtag.getCompound(i)));
         }
      }

      if (compoundtag.contains("HandItems", 9)) {
         ListTag listtag1 = compoundtag.getList("HandItems", 10);

         for(int j = 0; j < this.handItems.size(); ++j) {
            this.handItems.set(j, ItemStack.of(listtag1.getCompound(j)));
         }
      }

      if (compoundtag.contains("ArmorDropChances", 9)) {
         ListTag listtag2 = compoundtag.getList("ArmorDropChances", 5);

         for(int k = 0; k < listtag2.size(); ++k) {
            this.armorDropChances[k] = listtag2.getFloat(k);
         }
      }

      if (compoundtag.contains("HandDropChances", 9)) {
         ListTag listtag3 = compoundtag.getList("HandDropChances", 5);

         for(int l = 0; l < listtag3.size(); ++l) {
            this.handDropChances[l] = listtag3.getFloat(l);
         }
      }

      if (compoundtag.contains("Leash", 10)) {
         this.leashInfoTag = compoundtag.getCompound("Leash");
      }

      this.setLeftHanded(compoundtag.getBoolean("LeftHanded"));
      if (compoundtag.contains("DeathLootTable", 8)) {
         this.lootTable = new ResourceLocation(compoundtag.getString("DeathLootTable"));
         this.lootTableSeed = compoundtag.getLong("DeathLootTableSeed");
      }

      this.setNoAi(compoundtag.getBoolean("NoAI"));
   }

   protected void dropFromLootTable(DamageSource damagesource, boolean flag) {
      super.dropFromLootTable(damagesource, flag);
      this.lootTable = null;
   }

   public final ResourceLocation getLootTable() {
      return this.lootTable == null ? this.getDefaultLootTable() : this.lootTable;
   }

   protected ResourceLocation getDefaultLootTable() {
      return super.getLootTable();
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setZza(float f) {
      this.zza = f;
   }

   public void setYya(float f) {
      this.yya = f;
   }

   public void setXxa(float f) {
      this.xxa = f;
   }

   public void setSpeed(float f) {
      super.setSpeed(f);
      this.setZza(f);
   }

   public void aiStep() {
      super.aiStep();
      this.level().getProfiler().push("looting");
      if (!this.level().isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         Vec3i vec3i = this.getPickupReach();

         for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ()))) {
            if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay() && this.wantsToPickUp(itementity.getItem())) {
               this.pickUpItem(itementity);
            }
         }
      }

      this.level().getProfiler().pop();
   }

   protected Vec3i getPickupReach() {
      return ITEM_PICKUP_REACH;
   }

   protected void pickUpItem(ItemEntity itementity) {
      ItemStack itemstack = itementity.getItem();
      ItemStack itemstack1 = this.equipItemIfPossible(itemstack.copy());
      if (!itemstack1.isEmpty()) {
         this.onItemPickup(itementity);
         this.take(itementity, itemstack1.getCount());
         itemstack.shrink(itemstack1.getCount());
         if (itemstack.isEmpty()) {
            itementity.discard();
         }
      }

   }

   public ItemStack equipItemIfPossible(ItemStack itemstack) {
      EquipmentSlot equipmentslot = getEquipmentSlotForItem(itemstack);
      ItemStack itemstack1 = this.getItemBySlot(equipmentslot);
      boolean flag = this.canReplaceCurrentItem(itemstack, itemstack1);
      if (equipmentslot.isArmor() && !flag) {
         equipmentslot = EquipmentSlot.MAINHAND;
         itemstack1 = this.getItemBySlot(equipmentslot);
         flag = itemstack1.isEmpty();
      }

      if (flag && this.canHoldItem(itemstack)) {
         double d0 = (double)this.getEquipmentDropChance(equipmentslot);
         if (!itemstack1.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
            this.spawnAtLocation(itemstack1);
         }

         if (equipmentslot.isArmor() && itemstack.getCount() > 1) {
            ItemStack itemstack2 = itemstack.copyWithCount(1);
            this.setItemSlotAndDropWhenKilled(equipmentslot, itemstack2);
            return itemstack2;
         } else {
            this.setItemSlotAndDropWhenKilled(equipmentslot, itemstack);
            return itemstack;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   protected void setItemSlotAndDropWhenKilled(EquipmentSlot equipmentslot, ItemStack itemstack) {
      this.setItemSlot(equipmentslot, itemstack);
      this.setGuaranteedDrop(equipmentslot);
      this.persistenceRequired = true;
   }

   public void setGuaranteedDrop(EquipmentSlot equipmentslot) {
      switch (equipmentslot.getType()) {
         case HAND:
            this.handDropChances[equipmentslot.getIndex()] = 2.0F;
            break;
         case ARMOR:
            this.armorDropChances[equipmentslot.getIndex()] = 2.0F;
      }

   }

   protected boolean canReplaceCurrentItem(ItemStack itemstack, ItemStack itemstack1) {
      if (itemstack1.isEmpty()) {
         return true;
      } else if (itemstack.getItem() instanceof SwordItem) {
         if (!(itemstack1.getItem() instanceof SwordItem)) {
            return true;
         } else {
            SwordItem sworditem = (SwordItem)itemstack.getItem();
            SwordItem sworditem1 = (SwordItem)itemstack1.getItem();
            if (sworditem.getDamage() != sworditem1.getDamage()) {
               return sworditem.getDamage() > sworditem1.getDamage();
            } else {
               return this.canReplaceEqualItem(itemstack, itemstack1);
            }
         }
      } else if (itemstack.getItem() instanceof BowItem && itemstack1.getItem() instanceof BowItem) {
         return this.canReplaceEqualItem(itemstack, itemstack1);
      } else if (itemstack.getItem() instanceof CrossbowItem && itemstack1.getItem() instanceof CrossbowItem) {
         return this.canReplaceEqualItem(itemstack, itemstack1);
      } else if (itemstack.getItem() instanceof ArmorItem) {
         if (EnchantmentHelper.hasBindingCurse(itemstack1)) {
            return false;
         } else if (!(itemstack1.getItem() instanceof ArmorItem)) {
            return true;
         } else {
            ArmorItem armoritem = (ArmorItem)itemstack.getItem();
            ArmorItem armoritem1 = (ArmorItem)itemstack1.getItem();
            if (armoritem.getDefense() != armoritem1.getDefense()) {
               return armoritem.getDefense() > armoritem1.getDefense();
            } else if (armoritem.getToughness() != armoritem1.getToughness()) {
               return armoritem.getToughness() > armoritem1.getToughness();
            } else {
               return this.canReplaceEqualItem(itemstack, itemstack1);
            }
         }
      } else {
         if (itemstack.getItem() instanceof DiggerItem) {
            if (itemstack1.getItem() instanceof BlockItem) {
               return true;
            }

            if (itemstack1.getItem() instanceof DiggerItem) {
               DiggerItem diggeritem = (DiggerItem)itemstack.getItem();
               DiggerItem diggeritem1 = (DiggerItem)itemstack1.getItem();
               if (diggeritem.getAttackDamage() != diggeritem1.getAttackDamage()) {
                  return diggeritem.getAttackDamage() > diggeritem1.getAttackDamage();
               }

               return this.canReplaceEqualItem(itemstack, itemstack1);
            }
         }

         return false;
      }
   }

   public boolean canReplaceEqualItem(ItemStack itemstack, ItemStack itemstack1) {
      if (itemstack.getDamageValue() >= itemstack1.getDamageValue() && (!itemstack.hasTag() || itemstack1.hasTag())) {
         if (itemstack.hasTag() && itemstack1.hasTag()) {
            return itemstack.getTag().getAllKeys().stream().anyMatch((s1) -> !s1.equals("Damage")) && !itemstack1.getTag().getAllKeys().stream().anyMatch((s) -> !s.equals("Damage"));
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean canHoldItem(ItemStack itemstack) {
      return true;
   }

   public boolean wantsToPickUp(ItemStack itemstack) {
      return this.canHoldItem(itemstack);
   }

   public boolean removeWhenFarAway(double d0) {
      return true;
   }

   public boolean requiresCustomPersistence() {
      return this.isPassenger();
   }

   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   public void checkDespawn() {
      if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
         this.discard();
      } else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
         Entity entity = this.level().getNearestPlayer(this, -1.0D);
         if (entity != null) {
            double d0 = entity.distanceToSqr(this);
            int i = this.getType().getCategory().getDespawnDistance();
            int j = i * i;
            if (d0 > (double)j && this.removeWhenFarAway(d0)) {
               this.discard();
            }

            int k = this.getType().getCategory().getNoDespawnDistance();
            int l = k * k;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.removeWhenFarAway(d0)) {
               this.discard();
            } else if (d0 < (double)l) {
               this.noActionTime = 0;
            }
         }

      } else {
         this.noActionTime = 0;
      }
   }

   protected final void serverAiStep() {
      ++this.noActionTime;
      this.level().getProfiler().push("sensing");
      this.sensing.tick();
      this.level().getProfiler().pop();
      int i = this.level().getServer().getTickCount() + this.getId();
      if (i % 2 != 0 && this.tickCount > 1) {
         this.level().getProfiler().push("targetSelector");
         this.targetSelector.tickRunningGoals(false);
         this.level().getProfiler().pop();
         this.level().getProfiler().push("goalSelector");
         this.goalSelector.tickRunningGoals(false);
         this.level().getProfiler().pop();
      } else {
         this.level().getProfiler().push("targetSelector");
         this.targetSelector.tick();
         this.level().getProfiler().pop();
         this.level().getProfiler().push("goalSelector");
         this.goalSelector.tick();
         this.level().getProfiler().pop();
      }

      this.level().getProfiler().push("navigation");
      this.navigation.tick();
      this.level().getProfiler().pop();
      this.level().getProfiler().push("mob tick");
      this.customServerAiStep();
      this.level().getProfiler().pop();
      this.level().getProfiler().push("controls");
      this.level().getProfiler().push("move");
      this.moveControl.tick();
      this.level().getProfiler().popPush("look");
      this.lookControl.tick();
      this.level().getProfiler().popPush("jump");
      this.jumpControl.tick();
      this.level().getProfiler().pop();
      this.level().getProfiler().pop();
      this.sendDebugPackets();
   }

   protected void sendDebugPackets() {
      DebugPackets.sendGoalSelector(this.level(), this, this.goalSelector);
   }

   protected void customServerAiStep() {
   }

   public int getMaxHeadXRot() {
      return 40;
   }

   public int getMaxHeadYRot() {
      return 75;
   }

   public int getHeadRotSpeed() {
      return 10;
   }

   public void lookAt(Entity entity, float f, float f1) {
      double d0 = entity.getX() - this.getX();
      double d1 = entity.getZ() - this.getZ();
      double d2;
      if (entity instanceof LivingEntity livingentity) {
         d2 = livingentity.getEyeY() - this.getEyeY();
      } else {
         d2 = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D - this.getEyeY();
      }

      double d4 = Math.sqrt(d0 * d0 + d1 * d1);
      float f2 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
      float f3 = (float)(-(Mth.atan2(d2, d4) * (double)(180F / (float)Math.PI)));
      this.setXRot(this.rotlerp(this.getXRot(), f3, f1));
      this.setYRot(this.rotlerp(this.getYRot(), f2, f));
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

   public static boolean checkMobSpawnRules(EntityType<? extends Mob> entitytype, LevelAccessor levelaccessor, MobSpawnType mobspawntype, BlockPos blockpos, RandomSource randomsource) {
      BlockPos blockpos1 = blockpos.below();
      return mobspawntype == MobSpawnType.SPAWNER || levelaccessor.getBlockState(blockpos1).isValidSpawn(levelaccessor, blockpos1, entitytype);
   }

   public boolean checkSpawnRules(LevelAccessor levelaccessor, MobSpawnType mobspawntype) {
      return true;
   }

   public boolean checkSpawnObstruction(LevelReader levelreader) {
      return !levelreader.containsAnyLiquid(this.getBoundingBox()) && levelreader.isUnobstructed(this);
   }

   public int getMaxSpawnClusterSize() {
      return 4;
   }

   public boolean isMaxGroupSizeReached(int i) {
      return false;
   }

   public int getMaxFallDistance() {
      if (this.getTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i -= (3 - this.level().getDifficulty().getId()) * 4;
         if (i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable<ItemStack> getHandSlots() {
      return this.handItems;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return this.armorItems;
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentslot) {
      switch (equipmentslot.getType()) {
         case HAND:
            return this.handItems.get(equipmentslot.getIndex());
         case ARMOR:
            return this.armorItems.get(equipmentslot.getIndex());
         default:
            return ItemStack.EMPTY;
      }
   }

   public void setItemSlot(EquipmentSlot equipmentslot, ItemStack itemstack) {
      this.verifyEquippedItem(itemstack);
      switch (equipmentslot.getType()) {
         case HAND:
            this.onEquipItem(equipmentslot, this.handItems.set(equipmentslot.getIndex(), itemstack), itemstack);
            break;
         case ARMOR:
            this.onEquipItem(equipmentslot, this.armorItems.set(equipmentslot.getIndex(), itemstack), itemstack);
      }

   }

   protected void dropCustomDeathLoot(DamageSource damagesource, int i, boolean flag) {
      super.dropCustomDeathLoot(damagesource, i, flag);

      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
         ItemStack itemstack = this.getItemBySlot(equipmentslot);
         float f = this.getEquipmentDropChance(equipmentslot);
         boolean flag1 = f > 1.0F;
         if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (flag || flag1) && Math.max(this.random.nextFloat() - (float)i * 0.01F, 0.0F) < f) {
            if (!flag1 && itemstack.isDamageableItem()) {
               itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
            }

            this.spawnAtLocation(itemstack);
            this.setItemSlot(equipmentslot, ItemStack.EMPTY);
         }
      }

   }

   protected float getEquipmentDropChance(EquipmentSlot equipmentslot) {
      float f;
      switch (equipmentslot.getType()) {
         case HAND:
            f = this.handDropChances[equipmentslot.getIndex()];
            break;
         case ARMOR:
            f = this.armorDropChances[equipmentslot.getIndex()];
            break;
         default:
            f = 0.0F;
      }

      return f;
   }

   protected void populateDefaultEquipmentSlots(RandomSource randomsource, DifficultyInstance difficultyinstance) {
      if (randomsource.nextFloat() < 0.15F * difficultyinstance.getSpecialMultiplier()) {
         int i = randomsource.nextInt(2);
         float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
         if (randomsource.nextFloat() < 0.095F) {
            ++i;
         }

         if (randomsource.nextFloat() < 0.095F) {
            ++i;
         }

         if (randomsource.nextFloat() < 0.095F) {
            ++i;
         }

         boolean flag = true;

         for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
               ItemStack itemstack = this.getItemBySlot(equipmentslot);
               if (!flag && randomsource.nextFloat() < f) {
                  break;
               }

               flag = false;
               if (itemstack.isEmpty()) {
                  Item item = getEquipmentForSlot(equipmentslot, i);
                  if (item != null) {
                     this.setItemSlot(equipmentslot, new ItemStack(item));
                  }
               }
            }
         }
      }

   }

   @Nullable
   public static Item getEquipmentForSlot(EquipmentSlot equipmentslot, int i) {
      switch (equipmentslot) {
         case HEAD:
            if (i == 0) {
               return Items.LEATHER_HELMET;
            } else if (i == 1) {
               return Items.GOLDEN_HELMET;
            } else if (i == 2) {
               return Items.CHAINMAIL_HELMET;
            } else if (i == 3) {
               return Items.IRON_HELMET;
            } else if (i == 4) {
               return Items.DIAMOND_HELMET;
            }
         case CHEST:
            if (i == 0) {
               return Items.LEATHER_CHESTPLATE;
            } else if (i == 1) {
               return Items.GOLDEN_CHESTPLATE;
            } else if (i == 2) {
               return Items.CHAINMAIL_CHESTPLATE;
            } else if (i == 3) {
               return Items.IRON_CHESTPLATE;
            } else if (i == 4) {
               return Items.DIAMOND_CHESTPLATE;
            }
         case LEGS:
            if (i == 0) {
               return Items.LEATHER_LEGGINGS;
            } else if (i == 1) {
               return Items.GOLDEN_LEGGINGS;
            } else if (i == 2) {
               return Items.CHAINMAIL_LEGGINGS;
            } else if (i == 3) {
               return Items.IRON_LEGGINGS;
            } else if (i == 4) {
               return Items.DIAMOND_LEGGINGS;
            }
         case FEET:
            if (i == 0) {
               return Items.LEATHER_BOOTS;
            } else if (i == 1) {
               return Items.GOLDEN_BOOTS;
            } else if (i == 2) {
               return Items.CHAINMAIL_BOOTS;
            } else if (i == 3) {
               return Items.IRON_BOOTS;
            } else if (i == 4) {
               return Items.DIAMOND_BOOTS;
            }
         default:
            return null;
      }
   }

   protected void populateDefaultEquipmentEnchantments(RandomSource randomsource, DifficultyInstance difficultyinstance) {
      float f = difficultyinstance.getSpecialMultiplier();
      this.enchantSpawnedWeapon(randomsource, f);

      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
         if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
            this.enchantSpawnedArmor(randomsource, f, equipmentslot);
         }
      }

   }

   protected void enchantSpawnedWeapon(RandomSource randomsource, float f) {
      if (!this.getMainHandItem().isEmpty() && randomsource.nextFloat() < 0.25F * f) {
         this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(randomsource, this.getMainHandItem(), (int)(5.0F + f * (float)randomsource.nextInt(18)), false));
      }

   }

   protected void enchantSpawnedArmor(RandomSource randomsource, float f, EquipmentSlot equipmentslot) {
      ItemStack itemstack = this.getItemBySlot(equipmentslot);
      if (!itemstack.isEmpty() && randomsource.nextFloat() < 0.5F * f) {
         this.setItemSlot(equipmentslot, EnchantmentHelper.enchantItem(randomsource, itemstack, (int)(5.0F + f * (float)randomsource.nextInt(18)), false));
      }

   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      RandomSource randomsource = serverlevelaccessor.getRandom();
      this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random spawn bonus", randomsource.triangle(0.0D, 0.11485000000000001D), AttributeModifier.Operation.MULTIPLY_BASE));
      if (randomsource.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return spawngroupdata;
   }

   public void setPersistenceRequired() {
      this.persistenceRequired = true;
   }

   public void setDropChance(EquipmentSlot equipmentslot, float f) {
      switch (equipmentslot.getType()) {
         case HAND:
            this.handDropChances[equipmentslot.getIndex()] = f;
            break;
         case ARMOR:
            this.armorDropChances[equipmentslot.getIndex()] = f;
      }

   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean flag) {
      this.canPickUpLoot = flag;
   }

   public boolean canTakeItem(ItemStack itemstack) {
      EquipmentSlot equipmentslot = getEquipmentSlotForItem(itemstack);
      return this.getItemBySlot(equipmentslot).isEmpty() && this.canPickUpLoot();
   }

   public boolean isPersistenceRequired() {
      return this.persistenceRequired;
   }

   public final InteractionResult interact(Player player, InteractionHand interactionhand) {
      if (!this.isAlive()) {
         return InteractionResult.PASS;
      } else if (this.getLeashHolder() == player) {
         this.dropLeash(true, !player.getAbilities().instabuild);
         this.gameEvent(GameEvent.ENTITY_INTERACT, player);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         InteractionResult interactionresult = this.checkAndHandleImportantInteractions(player, interactionhand);
         if (interactionresult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionresult;
         } else {
            interactionresult = this.mobInteract(player, interactionhand);
            if (interactionresult.consumesAction()) {
               this.gameEvent(GameEvent.ENTITY_INTERACT, player);
               return interactionresult;
            } else {
               return super.interact(player, interactionhand);
            }
         }
      }
   }

   private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (itemstack.is(Items.LEAD) && this.canBeLeashed(player)) {
         this.setLeashedTo(player, true);
         itemstack.shrink(1);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         if (itemstack.is(Items.NAME_TAG)) {
            InteractionResult interactionresult = itemstack.interactLivingEntity(player, this, interactionhand);
            if (interactionresult.consumesAction()) {
               return interactionresult;
            }
         }

         if (itemstack.getItem() instanceof SpawnEggItem) {
            if (this.level() instanceof ServerLevel) {
               SpawnEggItem spawneggitem = (SpawnEggItem)itemstack.getItem();
               Optional<Mob> optional = spawneggitem.spawnOffspringFromSpawnEgg(player, this, this.getType(), (ServerLevel)this.level(), this.position(), itemstack);
               optional.ifPresent((mob) -> this.onOffspringSpawnedFromEgg(player, mob));
               return optional.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            } else {
               return InteractionResult.CONSUME;
            }
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
   }

   protected InteractionResult mobInteract(Player player, InteractionHand interactionhand) {
      return InteractionResult.PASS;
   }

   public boolean isWithinRestriction() {
      return this.isWithinRestriction(this.blockPosition());
   }

   public boolean isWithinRestriction(BlockPos blockpos) {
      if (this.restrictRadius == -1.0F) {
         return true;
      } else {
         return this.restrictCenter.distSqr(blockpos) < (double)(this.restrictRadius * this.restrictRadius);
      }
   }

   public void restrictTo(BlockPos blockpos, int i) {
      this.restrictCenter = blockpos;
      this.restrictRadius = (float)i;
   }

   public BlockPos getRestrictCenter() {
      return this.restrictCenter;
   }

   public float getRestrictRadius() {
      return this.restrictRadius;
   }

   public void clearRestriction() {
      this.restrictRadius = -1.0F;
   }

   public boolean hasRestriction() {
      return this.restrictRadius != -1.0F;
   }

   @Nullable
   public <T extends Mob> T convertTo(EntityType<T> entitytype, boolean flag) {
      if (this.isRemoved()) {
         return (T)null;
      } else {
         T mob = entitytype.create(this.level());
         if (mob == null) {
            return (T)null;
         } else {
            mob.copyPosition(this);
            mob.setBaby(this.isBaby());
            mob.setNoAi(this.isNoAi());
            if (this.hasCustomName()) {
               mob.setCustomName(this.getCustomName());
               mob.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
               mob.setPersistenceRequired();
            }

            mob.setInvulnerable(this.isInvulnerable());
            if (flag) {
               mob.setCanPickUpLoot(this.canPickUpLoot());

               for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                  ItemStack itemstack = this.getItemBySlot(equipmentslot);
                  if (!itemstack.isEmpty()) {
                     mob.setItemSlot(equipmentslot, itemstack.copyAndClear());
                     mob.setDropChance(equipmentslot, this.getEquipmentDropChance(equipmentslot));
                  }
               }
            }

            this.level().addFreshEntity(mob);
            if (this.isPassenger()) {
               Entity entity = this.getVehicle();
               this.stopRiding();
               mob.startRiding(entity, true);
            }

            this.discard();
            return mob;
         }
      }
   }

   protected void tickLeash() {
      if (this.leashInfoTag != null) {
         this.restoreLeashFromSave();
      }

      if (this.leashHolder != null) {
         if (!this.isAlive() || !this.leashHolder.isAlive()) {
            this.dropLeash(true, true);
         }

      }
   }

   public void dropLeash(boolean flag, boolean flag1) {
      if (this.leashHolder != null) {
         this.leashHolder = null;
         this.leashInfoTag = null;
         if (!this.level().isClientSide && flag1) {
            this.spawnAtLocation(Items.LEAD);
         }

         if (!this.level().isClientSide && flag && this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashed(Player player) {
      return !this.isLeashed() && !(this instanceof Enemy);
   }

   public boolean isLeashed() {
      return this.leashHolder != null;
   }

   @Nullable
   public Entity getLeashHolder() {
      if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.level().isClientSide) {
         this.leashHolder = this.level().getEntity(this.delayedLeashHolderId);
      }

      return this.leashHolder;
   }

   public void setLeashedTo(Entity entity, boolean flag) {
      this.leashHolder = entity;
      this.leashInfoTag = null;
      if (!this.level().isClientSide && flag && this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, this.leashHolder));
      }

      if (this.isPassenger()) {
         this.stopRiding();
      }

   }

   public void setDelayedLeashHolderId(int i) {
      this.delayedLeashHolderId = i;
      this.dropLeash(false, false);
   }

   public boolean startRiding(Entity entity, boolean flag) {
      boolean flag1 = super.startRiding(entity, flag);
      if (flag1 && this.isLeashed()) {
         this.dropLeash(true, true);
      }

      return flag1;
   }

   private void restoreLeashFromSave() {
      if (this.leashInfoTag != null && this.level() instanceof ServerLevel) {
         if (this.leashInfoTag.hasUUID("UUID")) {
            UUID uuid = this.leashInfoTag.getUUID("UUID");
            Entity entity = ((ServerLevel)this.level()).getEntity(uuid);
            if (entity != null) {
               this.setLeashedTo(entity, true);
               return;
            }
         } else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
            BlockPos blockpos = NbtUtils.readBlockPos(this.leashInfoTag);
            this.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(this.level(), blockpos), true);
            return;
         }

         if (this.tickCount > 100) {
            this.spawnAtLocation(Items.LEAD);
            this.leashInfoTag = null;
         }
      }

   }

   public boolean isEffectiveAi() {
      return super.isEffectiveAi() && !this.isNoAi();
   }

   public void setNoAi(boolean flag) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, flag ? (byte)(b0 | 1) : (byte)(b0 & -2));
   }

   public void setLeftHanded(boolean flag) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, flag ? (byte)(b0 | 2) : (byte)(b0 & -3));
   }

   public void setAggressive(boolean flag) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, flag ? (byte)(b0 | 4) : (byte)(b0 & -5));
   }

   public boolean isNoAi() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
   }

   public boolean isLeftHanded() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
   }

   public boolean isAggressive() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
   }

   public void setBaby(boolean flag) {
   }

   public HumanoidArm getMainArm() {
      return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
   }

   public double getMeleeAttackRangeSqr(LivingEntity livingentity) {
      return (double)(this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + livingentity.getBbWidth());
   }

   public double getPerceivedTargetDistanceSquareForMeleeAttack(LivingEntity livingentity) {
      return Math.max(this.distanceToSqr(livingentity.getMeleeAttackReferencePosition()), this.distanceToSqr(livingentity.position()));
   }

   public boolean isWithinMeleeAttackRange(LivingEntity livingentity) {
      double d0 = this.getPerceivedTargetDistanceSquareForMeleeAttack(livingentity);
      return d0 <= this.getMeleeAttackRangeSqr(livingentity);
   }

   public boolean doHurtTarget(Entity entity) {
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      if (entity instanceof LivingEntity) {
         f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entity).getMobType());
         f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         entity.setSecondsOnFire(i * 4);
      }

      boolean flag = entity.hurt(this.damageSources().mobAttack(this), f);
      if (flag) {
         if (f1 > 0.0F && entity instanceof LivingEntity) {
            ((LivingEntity)entity).knockback((double)(f1 * 0.5F), (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
         }

         if (entity instanceof Player) {
            Player player = (Player)entity;
            this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
         }

         this.doEnchantDamageEffects(this, entity);
         this.setLastHurtMob(entity);
      }

      return flag;
   }

   private void maybeDisableShield(Player player, ItemStack itemstack, ItemStack itemstack1) {
      if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem() instanceof AxeItem && itemstack1.is(Items.SHIELD)) {
         float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            player.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent(player, (byte)30);
         }
      }

   }

   protected boolean isSunBurnTick() {
      if (this.level().isDay() && !this.level().isClientSide) {
         float f = this.getLightLevelDependentMagicValue();
         BlockPos blockpos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
         boolean flag = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
         if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && this.level().canSeeSky(blockpos)) {
            return true;
         }
      }

      return false;
   }

   protected void jumpInLiquid(TagKey<Fluid> tagkey) {
      if (this.getNavigation().canFloat()) {
         super.jumpInLiquid(tagkey);
      } else {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
      }

   }

   public void removeFreeWill() {
      this.removeAllGoals((goal) -> true);
      this.getBrain().removeAllBehaviors();
   }

   public void removeAllGoals(Predicate<Goal> predicate) {
      this.goalSelector.removeAllGoals(predicate);
   }

   protected void removeAfterChangingDimensions() {
      super.removeAfterChangingDimensions();
      this.dropLeash(true, false);
      this.getAllSlots().forEach((itemstack) -> {
         if (!itemstack.isEmpty()) {
            itemstack.setCount(0);
         }

      });
   }

   @Nullable
   public ItemStack getPickResult() {
      SpawnEggItem spawneggitem = SpawnEggItem.byId(this.getType());
      return spawneggitem == null ? null : new ItemStack(spawneggitem);
   }
}
