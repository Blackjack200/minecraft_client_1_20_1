package net.minecraft.world.entity.decoration;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStand extends LivingEntity {
   public static final int WOBBLE_TIME = 5;
   private static final boolean ENABLE_ARMS = true;
   private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
   private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0F, 0.0F, 0.0F);
   private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
   private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0F, 0.0F, 10.0F);
   private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0F, 0.0F, -1.0F);
   private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0F, 0.0F, 1.0F);
   private static final EntityDimensions MARKER_DIMENSIONS = new EntityDimensions(0.0F, 0.0F, true);
   private static final EntityDimensions BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5F);
   private static final double FEET_OFFSET = 0.1D;
   private static final double CHEST_OFFSET = 0.9D;
   private static final double LEGS_OFFSET = 0.4D;
   private static final double HEAD_OFFSET = 1.6D;
   public static final int DISABLE_TAKING_OFFSET = 8;
   public static final int DISABLE_PUTTING_OFFSET = 16;
   public static final int CLIENT_FLAG_SMALL = 1;
   public static final int CLIENT_FLAG_SHOW_ARMS = 4;
   public static final int CLIENT_FLAG_NO_BASEPLATE = 8;
   public static final int CLIENT_FLAG_MARKER = 16;
   public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.BYTE);
   public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   private static final Predicate<Entity> RIDABLE_MINECARTS = (entity) -> entity instanceof AbstractMinecart && ((AbstractMinecart)entity).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
   private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
   private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
   private boolean invisible;
   public long lastHit;
   private int disabledSlots;
   private Rotations headPose = DEFAULT_HEAD_POSE;
   private Rotations bodyPose = DEFAULT_BODY_POSE;
   private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
   private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;
   private Rotations leftLegPose = DEFAULT_LEFT_LEG_POSE;
   private Rotations rightLegPose = DEFAULT_RIGHT_LEG_POSE;

   public ArmorStand(EntityType<? extends ArmorStand> entitytype, Level level) {
      super(entitytype, level);
      this.setMaxUpStep(0.0F);
   }

   public ArmorStand(Level level, double d0, double d1, double d2) {
      this(EntityType.ARMOR_STAND, level);
      this.setPos(d0, d1, d2);
   }

   public void refreshDimensions() {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      super.refreshDimensions();
      this.setPos(d0, d1, d2);
   }

   private boolean hasPhysics() {
      return !this.isMarker() && !this.isNoGravity();
   }

   public boolean isEffectiveAi() {
      return super.isEffectiveAi() && this.hasPhysics();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_CLIENT_FLAGS, (byte)0);
      this.entityData.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
      this.entityData.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
      this.entityData.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
      this.entityData.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
      this.entityData.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
      this.entityData.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
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

   public boolean canTakeItem(ItemStack itemstack) {
      EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
      return this.getItemBySlot(equipmentslot).isEmpty() && !this.isDisabled(equipmentslot);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
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
      compoundtag.putBoolean("Invisible", this.isInvisible());
      compoundtag.putBoolean("Small", this.isSmall());
      compoundtag.putBoolean("ShowArms", this.isShowArms());
      compoundtag.putInt("DisabledSlots", this.disabledSlots);
      compoundtag.putBoolean("NoBasePlate", this.isNoBasePlate());
      if (this.isMarker()) {
         compoundtag.putBoolean("Marker", this.isMarker());
      }

      compoundtag.put("Pose", this.writePose());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
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

      this.setInvisible(compoundtag.getBoolean("Invisible"));
      this.setSmall(compoundtag.getBoolean("Small"));
      this.setShowArms(compoundtag.getBoolean("ShowArms"));
      this.disabledSlots = compoundtag.getInt("DisabledSlots");
      this.setNoBasePlate(compoundtag.getBoolean("NoBasePlate"));
      this.setMarker(compoundtag.getBoolean("Marker"));
      this.noPhysics = !this.hasPhysics();
      CompoundTag compoundtag1 = compoundtag.getCompound("Pose");
      this.readPose(compoundtag1);
   }

   private void readPose(CompoundTag compoundtag) {
      ListTag listtag = compoundtag.getList("Head", 5);
      this.setHeadPose(listtag.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(listtag));
      ListTag listtag1 = compoundtag.getList("Body", 5);
      this.setBodyPose(listtag1.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(listtag1));
      ListTag listtag2 = compoundtag.getList("LeftArm", 5);
      this.setLeftArmPose(listtag2.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(listtag2));
      ListTag listtag3 = compoundtag.getList("RightArm", 5);
      this.setRightArmPose(listtag3.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(listtag3));
      ListTag listtag4 = compoundtag.getList("LeftLeg", 5);
      this.setLeftLegPose(listtag4.isEmpty() ? DEFAULT_LEFT_LEG_POSE : new Rotations(listtag4));
      ListTag listtag5 = compoundtag.getList("RightLeg", 5);
      this.setRightLegPose(listtag5.isEmpty() ? DEFAULT_RIGHT_LEG_POSE : new Rotations(listtag5));
   }

   private CompoundTag writePose() {
      CompoundTag compoundtag = new CompoundTag();
      if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
         compoundtag.put("Head", this.headPose.save());
      }

      if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
         compoundtag.put("Body", this.bodyPose.save());
      }

      if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
         compoundtag.put("LeftArm", this.leftArmPose.save());
      }

      if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
         compoundtag.put("RightArm", this.rightArmPose.save());
      }

      if (!DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
         compoundtag.put("LeftLeg", this.leftLegPose.save());
      }

      if (!DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
         compoundtag.put("RightLeg", this.rightLegPose.save());
      }

      return compoundtag;
   }

   public boolean isPushable() {
      return false;
   }

   protected void doPush(Entity entity) {
   }

   protected void pushEntities() {
      List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);

      for(int i = 0; i < list.size(); ++i) {
         Entity entity = list.get(i);
         if (this.distanceToSqr(entity) <= 0.2D) {
            entity.push(this);
         }
      }

   }

   public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (!this.isMarker() && !itemstack.is(Items.NAME_TAG)) {
         if (player.isSpectator()) {
            return InteractionResult.SUCCESS;
         } else if (player.level().isClientSide) {
            return InteractionResult.CONSUME;
         } else {
            EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
            if (itemstack.isEmpty()) {
               EquipmentSlot equipmentslot1 = this.getClickedSlot(vec3);
               EquipmentSlot equipmentslot2 = this.isDisabled(equipmentslot1) ? equipmentslot : equipmentslot1;
               if (this.hasItemInSlot(equipmentslot2) && this.swapItem(player, equipmentslot2, itemstack, interactionhand)) {
                  return InteractionResult.SUCCESS;
               }
            } else {
               if (this.isDisabled(equipmentslot)) {
                  return InteractionResult.FAIL;
               }

               if (equipmentslot.getType() == EquipmentSlot.Type.HAND && !this.isShowArms()) {
                  return InteractionResult.FAIL;
               }

               if (this.swapItem(player, equipmentslot, itemstack, interactionhand)) {
                  return InteractionResult.SUCCESS;
               }
            }

            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private EquipmentSlot getClickedSlot(Vec3 vec3) {
      EquipmentSlot equipmentslot = EquipmentSlot.MAINHAND;
      boolean flag = this.isSmall();
      double d0 = flag ? vec3.y * 2.0D : vec3.y;
      EquipmentSlot equipmentslot1 = EquipmentSlot.FEET;
      if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(equipmentslot1)) {
         equipmentslot = EquipmentSlot.FEET;
      } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D) && this.hasItemInSlot(EquipmentSlot.CHEST)) {
         equipmentslot = EquipmentSlot.CHEST;
      } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EquipmentSlot.LEGS)) {
         equipmentslot = EquipmentSlot.LEGS;
      } else if (d0 >= 1.6D && this.hasItemInSlot(EquipmentSlot.HEAD)) {
         equipmentslot = EquipmentSlot.HEAD;
      } else if (!this.hasItemInSlot(EquipmentSlot.MAINHAND) && this.hasItemInSlot(EquipmentSlot.OFFHAND)) {
         equipmentslot = EquipmentSlot.OFFHAND;
      }

      return equipmentslot;
   }

   private boolean isDisabled(EquipmentSlot equipmentslot) {
      return (this.disabledSlots & 1 << equipmentslot.getFilterFlag()) != 0 || equipmentslot.getType() == EquipmentSlot.Type.HAND && !this.isShowArms();
   }

   private boolean swapItem(Player player, EquipmentSlot equipmentslot, ItemStack itemstack, InteractionHand interactionhand) {
      ItemStack itemstack1 = this.getItemBySlot(equipmentslot);
      if (!itemstack1.isEmpty() && (this.disabledSlots & 1 << equipmentslot.getFilterFlag() + 8) != 0) {
         return false;
      } else if (itemstack1.isEmpty() && (this.disabledSlots & 1 << equipmentslot.getFilterFlag() + 16) != 0) {
         return false;
      } else if (player.getAbilities().instabuild && itemstack1.isEmpty() && !itemstack.isEmpty()) {
         this.setItemSlot(equipmentslot, itemstack.copyWithCount(1));
         return true;
      } else if (!itemstack.isEmpty() && itemstack.getCount() > 1) {
         if (!itemstack1.isEmpty()) {
            return false;
         } else {
            this.setItemSlot(equipmentslot, itemstack.split(1));
            return true;
         }
      } else {
         this.setItemSlot(equipmentslot, itemstack);
         player.setItemInHand(interactionhand, itemstack1);
         return true;
      }
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (!this.level().isClientSide && !this.isRemoved()) {
         if (damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill();
            return false;
         } else if (!this.isInvulnerableTo(damagesource) && !this.invisible && !this.isMarker()) {
            if (damagesource.is(DamageTypeTags.IS_EXPLOSION)) {
               this.brokenByAnything(damagesource);
               this.kill();
               return false;
            } else if (damagesource.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
               if (this.isOnFire()) {
                  this.causeDamage(damagesource, 0.15F);
               } else {
                  this.setSecondsOnFire(5);
               }

               return false;
            } else if (damagesource.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
               this.causeDamage(damagesource, 4.0F);
               return false;
            } else {
               boolean flag = damagesource.getDirectEntity() instanceof AbstractArrow;
               boolean flag1 = flag && ((AbstractArrow)damagesource.getDirectEntity()).getPierceLevel() > 0;
               boolean flag2 = "player".equals(damagesource.getMsgId());
               if (!flag2 && !flag) {
                  return false;
               } else {
                  Entity var7 = damagesource.getEntity();
                  if (var7 instanceof Player) {
                     Player player = (Player)var7;
                     if (!player.getAbilities().mayBuild) {
                        return false;
                     }
                  }

                  if (damagesource.isCreativePlayer()) {
                     this.playBrokenSound();
                     this.showBreakingParticles();
                     this.kill();
                     return flag1;
                  } else {
                     long i = this.level().getGameTime();
                     if (i - this.lastHit > 5L && !flag) {
                        this.level().broadcastEntityEvent(this, (byte)32);
                        this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
                        this.lastHit = i;
                     } else {
                        this.brokenByPlayer(damagesource);
                        this.showBreakingParticles();
                        this.kill();
                     }

                     return true;
                  }
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 32) {
         if (this.level().isClientSide) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
            this.lastHit = this.level().getGameTime();
         }
      } else {
         super.handleEntityEvent(b0);
      }

   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      double d1 = this.getBoundingBox().getSize() * 4.0D;
      if (Double.isNaN(d1) || d1 == 0.0D) {
         d1 = 4.0D;
      }

      d1 *= 64.0D;
      return d0 < d1 * d1;
   }

   private void showBreakingParticles() {
      if (this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.6666666666666666D), this.getZ(), 10, (double)(this.getBbWidth() / 4.0F), (double)(this.getBbHeight() / 4.0F), (double)(this.getBbWidth() / 4.0F), 0.05D);
      }

   }

   private void causeDamage(DamageSource damagesource, float f) {
      float f1 = this.getHealth();
      f1 -= f;
      if (f1 <= 0.5F) {
         this.brokenByAnything(damagesource);
         this.kill();
      } else {
         this.setHealth(f1);
         this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
      }

   }

   private void brokenByPlayer(DamageSource damagesource) {
      ItemStack itemstack = new ItemStack(Items.ARMOR_STAND);
      if (this.hasCustomName()) {
         itemstack.setHoverName(this.getCustomName());
      }

      Block.popResource(this.level(), this.blockPosition(), itemstack);
      this.brokenByAnything(damagesource);
   }

   private void brokenByAnything(DamageSource damagesource) {
      this.playBrokenSound();
      this.dropAllDeathLoot(damagesource);

      for(int i = 0; i < this.handItems.size(); ++i) {
         ItemStack itemstack = this.handItems.get(i);
         if (!itemstack.isEmpty()) {
            Block.popResource(this.level(), this.blockPosition().above(), itemstack);
            this.handItems.set(i, ItemStack.EMPTY);
         }
      }

      for(int j = 0; j < this.armorItems.size(); ++j) {
         ItemStack itemstack1 = this.armorItems.get(j);
         if (!itemstack1.isEmpty()) {
            Block.popResource(this.level(), this.blockPosition().above(), itemstack1);
            this.armorItems.set(j, ItemStack.EMPTY);
         }
      }

   }

   private void playBrokenSound() {
      this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
   }

   protected float tickHeadTurn(float f, float f1) {
      this.yBodyRotO = this.yRotO;
      this.yBodyRot = this.getYRot();
      return 0.0F;
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return entitydimensions.height * (this.isBaby() ? 0.5F : 0.9F);
   }

   public double getMyRidingOffset() {
      return this.isMarker() ? 0.0D : (double)0.1F;
   }

   public void travel(Vec3 vec3) {
      if (this.hasPhysics()) {
         super.travel(vec3);
      }
   }

   public void setYBodyRot(float f) {
      this.yBodyRotO = this.yRotO = f;
      this.yHeadRotO = this.yHeadRot = f;
   }

   public void setYHeadRot(float f) {
      this.yBodyRotO = this.yRotO = f;
      this.yHeadRotO = this.yHeadRot = f;
   }

   public void tick() {
      super.tick();
      Rotations rotations = this.entityData.get(DATA_HEAD_POSE);
      if (!this.headPose.equals(rotations)) {
         this.setHeadPose(rotations);
      }

      Rotations rotations1 = this.entityData.get(DATA_BODY_POSE);
      if (!this.bodyPose.equals(rotations1)) {
         this.setBodyPose(rotations1);
      }

      Rotations rotations2 = this.entityData.get(DATA_LEFT_ARM_POSE);
      if (!this.leftArmPose.equals(rotations2)) {
         this.setLeftArmPose(rotations2);
      }

      Rotations rotations3 = this.entityData.get(DATA_RIGHT_ARM_POSE);
      if (!this.rightArmPose.equals(rotations3)) {
         this.setRightArmPose(rotations3);
      }

      Rotations rotations4 = this.entityData.get(DATA_LEFT_LEG_POSE);
      if (!this.leftLegPose.equals(rotations4)) {
         this.setLeftLegPose(rotations4);
      }

      Rotations rotations5 = this.entityData.get(DATA_RIGHT_LEG_POSE);
      if (!this.rightLegPose.equals(rotations5)) {
         this.setRightLegPose(rotations5);
      }

   }

   protected void updateInvisibilityStatus() {
      this.setInvisible(this.invisible);
   }

   public void setInvisible(boolean flag) {
      this.invisible = flag;
      super.setInvisible(flag);
   }

   public boolean isBaby() {
      return this.isSmall();
   }

   public void kill() {
      this.remove(Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
   }

   public boolean ignoreExplosion() {
      return this.isInvisible();
   }

   public PushReaction getPistonPushReaction() {
      return this.isMarker() ? PushReaction.IGNORE : super.getPistonPushReaction();
   }

   public boolean isIgnoringBlockTriggers() {
      return this.isMarker();
   }

   private void setSmall(boolean flag) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 1, flag));
   }

   public boolean isSmall() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 1) != 0;
   }

   public void setShowArms(boolean flag) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 4, flag));
   }

   public boolean isShowArms() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
   }

   public void setNoBasePlate(boolean flag) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 8, flag));
   }

   public boolean isNoBasePlate() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 8) != 0;
   }

   private void setMarker(boolean flag) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 16, flag));
   }

   public boolean isMarker() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 16) != 0;
   }

   private byte setBit(byte b0, int i, boolean flag) {
      if (flag) {
         b0 = (byte)(b0 | i);
      } else {
         b0 = (byte)(b0 & ~i);
      }

      return b0;
   }

   public void setHeadPose(Rotations rotations) {
      this.headPose = rotations;
      this.entityData.set(DATA_HEAD_POSE, rotations);
   }

   public void setBodyPose(Rotations rotations) {
      this.bodyPose = rotations;
      this.entityData.set(DATA_BODY_POSE, rotations);
   }

   public void setLeftArmPose(Rotations rotations) {
      this.leftArmPose = rotations;
      this.entityData.set(DATA_LEFT_ARM_POSE, rotations);
   }

   public void setRightArmPose(Rotations rotations) {
      this.rightArmPose = rotations;
      this.entityData.set(DATA_RIGHT_ARM_POSE, rotations);
   }

   public void setLeftLegPose(Rotations rotations) {
      this.leftLegPose = rotations;
      this.entityData.set(DATA_LEFT_LEG_POSE, rotations);
   }

   public void setRightLegPose(Rotations rotations) {
      this.rightLegPose = rotations;
      this.entityData.set(DATA_RIGHT_LEG_POSE, rotations);
   }

   public Rotations getHeadPose() {
      return this.headPose;
   }

   public Rotations getBodyPose() {
      return this.bodyPose;
   }

   public Rotations getLeftArmPose() {
      return this.leftArmPose;
   }

   public Rotations getRightArmPose() {
      return this.rightArmPose;
   }

   public Rotations getLeftLegPose() {
      return this.leftLegPose;
   }

   public Rotations getRightLegPose() {
      return this.rightLegPose;
   }

   public boolean isPickable() {
      return super.isPickable() && !this.isMarker();
   }

   public boolean skipAttackInteraction(Entity entity) {
      return entity instanceof Player && !this.level().mayInteract((Player)entity, this.blockPosition());
   }

   public HumanoidArm getMainArm() {
      return HumanoidArm.RIGHT;
   }

   public LivingEntity.Fallsounds getFallSounds() {
      return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.ARMOR_STAND_HIT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ARMOR_STAND_BREAK;
   }

   public void thunderHit(ServerLevel serverlevel, LightningBolt lightningbolt) {
   }

   public boolean isAffectedByPotions() {
      return false;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_CLIENT_FLAGS.equals(entitydataaccessor)) {
         this.refreshDimensions();
         this.blocksBuilding = !this.isMarker();
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   public boolean attackable() {
      return false;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.getDimensionsMarker(this.isMarker());
   }

   private EntityDimensions getDimensionsMarker(boolean flag) {
      if (flag) {
         return MARKER_DIMENSIONS;
      } else {
         return this.isBaby() ? BABY_DIMENSIONS : this.getType().getDimensions();
      }
   }

   public Vec3 getLightProbePosition(float f) {
      if (this.isMarker()) {
         AABB aabb = this.getDimensionsMarker(false).makeBoundingBox(this.position());
         BlockPos blockpos = this.blockPosition();
         int i = Integer.MIN_VALUE;

         for(BlockPos blockpos1 : BlockPos.betweenClosed(BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ), BlockPos.containing(aabb.maxX, aabb.maxY, aabb.maxZ))) {
            int j = Math.max(this.level().getBrightness(LightLayer.BLOCK, blockpos1), this.level().getBrightness(LightLayer.SKY, blockpos1));
            if (j == 15) {
               return Vec3.atCenterOf(blockpos1);
            }

            if (j > i) {
               i = j;
               blockpos = blockpos1.immutable();
            }
         }

         return Vec3.atCenterOf(blockpos);
      } else {
         return super.getLightProbePosition(f);
      }
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.ARMOR_STAND);
   }

   public boolean canBeSeenByAnyone() {
      return !this.isInvisible() && !this.isMarker();
   }
}
