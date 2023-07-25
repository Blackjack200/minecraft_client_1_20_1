package net.minecraft.world.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

public abstract class LivingEntity extends Entity implements Attackable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final UUID SPEED_MODIFIER_SPRINTING_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
   private static final UUID SPEED_MODIFIER_SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
   private static final UUID SPEED_MODIFIER_POWDER_SNOW_UUID = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce");
   private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(SPEED_MODIFIER_SPRINTING_UUID, "Sprinting speed boost", (double)0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL);
   public static final int HAND_SLOTS = 2;
   public static final int ARMOR_SLOTS = 4;
   public static final int EQUIPMENT_SLOT_OFFSET = 98;
   public static final int ARMOR_SLOT_OFFSET = 100;
   public static final int SWING_DURATION = 6;
   public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
   private static final int DAMAGE_SOURCE_TIMEOUT = 40;
   public static final double MIN_MOVEMENT_DISTANCE = 0.003D;
   public static final double DEFAULT_BASE_GRAVITY = 0.08D;
   public static final int DEATH_DURATION = 20;
   private static final int WAIT_TICKS_BEFORE_ITEM_USE_EFFECTS = 7;
   private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
   private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
   public static final int USE_ITEM_INTERVAL = 4;
   private static final float BASE_JUMP_POWER = 0.42F;
   private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 128.0D;
   protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
   protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
   protected static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
   protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_EFFECT_COLOR_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
   protected static final float DEFAULT_EYE_HEIGHT = 1.74F;
   protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2F, 0.2F);
   public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5F;
   private static final int MAX_HEAD_ROTATION_RELATIVE_TO_BODY = 50;
   private final AttributeMap attributes;
   private final CombatTracker combatTracker = new CombatTracker(this);
   private final Map<MobEffect, MobEffectInstance> activeEffects = Maps.newHashMap();
   private final NonNullList<ItemStack> lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
   private final NonNullList<ItemStack> lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
   public boolean swinging;
   private boolean discardFriction = false;
   public InteractionHand swingingArm;
   public int swingTime;
   public int removeArrowTime;
   public int removeStingerTime;
   public int hurtTime;
   public int hurtDuration;
   public int deathTime;
   public float oAttackAnim;
   public float attackAnim;
   protected int attackStrengthTicker;
   public final WalkAnimationState walkAnimation = new WalkAnimationState();
   public final int invulnerableDuration = 20;
   public final float timeOffs;
   public final float rotA;
   public float yBodyRot;
   public float yBodyRotO;
   public float yHeadRot;
   public float yHeadRotO;
   @Nullable
   protected Player lastHurtByPlayer;
   protected int lastHurtByPlayerTime;
   protected boolean dead;
   protected int noActionTime;
   protected float oRun;
   protected float run;
   protected float animStep;
   protected float animStepO;
   protected float rotOffs;
   protected int deathScore;
   protected float lastHurt;
   protected boolean jumping;
   public float xxa;
   public float yya;
   public float zza;
   protected int lerpSteps;
   protected double lerpX;
   protected double lerpY;
   protected double lerpZ;
   protected double lerpYRot;
   protected double lerpXRot;
   protected double lyHeadRot;
   protected int lerpHeadSteps;
   private boolean effectsDirty = true;
   @Nullable
   private LivingEntity lastHurtByMob;
   private int lastHurtByMobTimestamp;
   private LivingEntity lastHurtMob;
   private int lastHurtMobTimestamp;
   private float speed;
   private int noJumpDelay;
   private float absorptionAmount;
   protected ItemStack useItem = ItemStack.EMPTY;
   protected int useItemRemaining;
   protected int fallFlyTicks;
   private BlockPos lastPos;
   private Optional<BlockPos> lastClimbablePos = Optional.empty();
   @Nullable
   private DamageSource lastDamageSource;
   private long lastDamageStamp;
   protected int autoSpinAttackTicks;
   private float swimAmount;
   private float swimAmountO;
   protected Brain<?> brain;
   private boolean skipDropExperience;

   protected LivingEntity(EntityType<? extends LivingEntity> entitytype, Level level) {
      super(entitytype, level);
      this.attributes = new AttributeMap(DefaultAttributes.getSupplier(entitytype));
      this.setHealth(this.getMaxHealth());
      this.blocksBuilding = true;
      this.rotA = (float)((Math.random() + 1.0D) * (double)0.01F);
      this.reapplyPosition();
      this.timeOffs = (float)Math.random() * 12398.0F;
      this.setYRot((float)(Math.random() * (double)((float)Math.PI * 2F)));
      this.yHeadRot = this.getYRot();
      this.setMaxUpStep(0.6F);
      NbtOps nbtops = NbtOps.INSTANCE;
      this.brain = this.makeBrain(new Dynamic<>(nbtops, nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), nbtops.emptyMap()))));
   }

   public Brain<?> getBrain() {
      return this.brain;
   }

   protected Brain.Provider<?> brainProvider() {
      return Brain.provider(ImmutableList.of(), ImmutableList.of());
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return this.brainProvider().makeBrain(dynamic);
   }

   public void kill() {
      this.hurt(this.damageSources().genericKill(), Float.MAX_VALUE);
   }

   public boolean canAttackType(EntityType<?> entitytype) {
      return true;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
      this.entityData.define(DATA_EFFECT_COLOR_ID, 0);
      this.entityData.define(DATA_EFFECT_AMBIENCE_ID, false);
      this.entityData.define(DATA_ARROW_COUNT_ID, 0);
      this.entityData.define(DATA_STINGER_COUNT_ID, 0);
      this.entityData.define(DATA_HEALTH_ID, 1.0F);
      this.entityData.define(SLEEPING_POS_ID, Optional.empty());
   }

   public static AttributeSupplier.Builder createLivingAttributes() {
      return AttributeSupplier.builder().add(Attributes.MAX_HEALTH).add(Attributes.KNOCKBACK_RESISTANCE).add(Attributes.MOVEMENT_SPEED).add(Attributes.ARMOR).add(Attributes.ARMOR_TOUGHNESS);
   }

   protected void checkFallDamage(double d0, boolean flag, BlockState blockstate, BlockPos blockpos) {
      if (!this.isInWater()) {
         this.updateInWaterStateAndDoWaterCurrentPushing();
      }

      if (!this.level().isClientSide && flag && this.fallDistance > 0.0F) {
         this.removeSoulSpeed();
         this.tryAddSoulSpeed();
      }

      if (!this.level().isClientSide && this.fallDistance > 3.0F && flag && !blockstate.isAir()) {
         double d1 = this.getX();
         double d2 = this.getY();
         double d3 = this.getZ();
         BlockPos blockpos1 = this.blockPosition();
         if (blockpos.getX() != blockpos1.getX() || blockpos.getZ() != blockpos1.getZ()) {
            double d4 = d1 - (double)blockpos.getX() - 0.5D;
            double d5 = d3 - (double)blockpos.getZ() - 0.5D;
            double d6 = Math.max(Math.abs(d4), Math.abs(d5));
            d1 = (double)blockpos.getX() + 0.5D + d4 / d6 * 0.5D;
            d3 = (double)blockpos.getZ() + 0.5D + d5 / d6 * 0.5D;
         }

         float f = (float)Mth.ceil(this.fallDistance - 3.0F);
         double d7 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
         int i = (int)(150.0D * d7);
         ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), d1, d2, d3, i, 0.0D, 0.0D, 0.0D, (double)0.15F);
      }

      super.checkFallDamage(d0, flag, blockstate, blockpos);
      if (flag) {
         this.lastClimbablePos = Optional.empty();
      }

   }

   public boolean canBreatheUnderwater() {
      return this.getMobType() == MobType.UNDEAD;
   }

   public float getSwimAmount(float f) {
      return Mth.lerp(f, this.swimAmountO, this.swimAmount);
   }

   public void baseTick() {
      this.oAttackAnim = this.attackAnim;
      if (this.firstTick) {
         this.getSleepingPos().ifPresent(this::setPosToBed);
      }

      if (this.canSpawnSoulSpeedParticle()) {
         this.spawnSoulSpeedParticle();
      }

      super.baseTick();
      this.level().getProfiler().push("livingEntityBaseTick");
      if (this.fireImmune() || this.level().isClientSide) {
         this.clearFire();
      }

      if (this.isAlive()) {
         boolean flag = this instanceof Player;
         if (!this.level().isClientSide) {
            if (this.isInWall()) {
               this.hurt(this.damageSources().inWall(), 1.0F);
            } else if (flag && !this.level().getWorldBorder().isWithinBounds(this.getBoundingBox())) {
               double d0 = this.level().getWorldBorder().getDistanceToBorder(this) + this.level().getWorldBorder().getDamageSafeZone();
               if (d0 < 0.0D) {
                  double d1 = this.level().getWorldBorder().getDamagePerBlock();
                  if (d1 > 0.0D) {
                     this.hurt(this.damageSources().outOfBorder(), (float)Math.max(1, Mth.floor(-d0 * d1)));
                  }
               }
            }
         }

         if (this.isEyeInFluid(FluidTags.WATER) && !this.level().getBlockState(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
            boolean flag1 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && (!flag || !((Player)this).getAbilities().invulnerable);
            if (flag1) {
               this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
               if (this.getAirSupply() == -20) {
                  this.setAirSupply(0);
                  Vec3 vec3 = this.getDeltaMovement();

                  for(int i = 0; i < 8; ++i) {
                     double d2 = this.random.nextDouble() - this.random.nextDouble();
                     double d3 = this.random.nextDouble() - this.random.nextDouble();
                     double d4 = this.random.nextDouble() - this.random.nextDouble();
                     this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d2, this.getY() + d3, this.getZ() + d4, vec3.x, vec3.y, vec3.z);
                  }

                  this.hurt(this.damageSources().drown(), 2.0F);
               }
            }

            if (!this.level().isClientSide && this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
               this.stopRiding();
            }
         } else if (this.getAirSupply() < this.getMaxAirSupply()) {
            this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
         }

         if (!this.level().isClientSide) {
            BlockPos blockpos = this.blockPosition();
            if (!Objects.equal(this.lastPos, blockpos)) {
               this.lastPos = blockpos;
               this.onChangedBlock(blockpos);
            }
         }
      }

      if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow)) {
         this.extinguishFire();
      }

      if (this.hurtTime > 0) {
         --this.hurtTime;
      }

      if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
         --this.invulnerableTime;
      }

      if (this.isDeadOrDying() && this.level().shouldTickDeath(this)) {
         this.tickDeath();
      }

      if (this.lastHurtByPlayerTime > 0) {
         --this.lastHurtByPlayerTime;
      } else {
         this.lastHurtByPlayer = null;
      }

      if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
         this.lastHurtMob = null;
      }

      if (this.lastHurtByMob != null) {
         if (!this.lastHurtByMob.isAlive()) {
            this.setLastHurtByMob((LivingEntity)null);
         } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
            this.setLastHurtByMob((LivingEntity)null);
         }
      }

      this.tickEffects();
      this.animStepO = this.animStep;
      this.yBodyRotO = this.yBodyRot;
      this.yHeadRotO = this.yHeadRot;
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
      this.level().getProfiler().pop();
   }

   public boolean canSpawnSoulSpeedParticle() {
      return this.tickCount % 5 == 0 && this.getDeltaMovement().x != 0.0D && this.getDeltaMovement().z != 0.0D && !this.isSpectator() && EnchantmentHelper.hasSoulSpeed(this) && this.onSoulSpeedBlock();
   }

   protected void spawnSoulSpeedParticle() {
      Vec3 vec3 = this.getDeltaMovement();
      this.level().addParticle(ParticleTypes.SOUL, this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(), this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(), vec3.x * -0.2D, 0.1D, vec3.z * -0.2D);
      float f = this.random.nextFloat() * 0.4F + this.random.nextFloat() > 0.9F ? 0.6F : 0.0F;
      this.playSound(SoundEvents.SOUL_ESCAPE, f, 0.6F + this.random.nextFloat() * 0.4F);
   }

   protected boolean onSoulSpeedBlock() {
      return this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.SOUL_SPEED_BLOCKS);
   }

   protected float getBlockSpeedFactor() {
      return this.onSoulSpeedBlock() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, this) > 0 ? 1.0F : super.getBlockSpeedFactor();
   }

   protected boolean shouldRemoveSoulSpeed(BlockState blockstate) {
      return !blockstate.isAir() || this.isFallFlying();
   }

   protected void removeSoulSpeed() {
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attributeinstance != null) {
         if (attributeinstance.getModifier(SPEED_MODIFIER_SOUL_SPEED_UUID) != null) {
            attributeinstance.removeModifier(SPEED_MODIFIER_SOUL_SPEED_UUID);
         }

      }
   }

   protected void tryAddSoulSpeed() {
      if (!this.getBlockStateOnLegacy().isAir()) {
         int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, this);
         if (i > 0 && this.onSoulSpeedBlock()) {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeinstance == null) {
               return;
            }

            attributeinstance.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_SOUL_SPEED_UUID, "Soul speed boost", (double)(0.03F * (1.0F + (float)i * 0.35F)), AttributeModifier.Operation.ADDITION));
            if (this.getRandom().nextFloat() < 0.04F) {
               ItemStack itemstack = this.getItemBySlot(EquipmentSlot.FEET);
               itemstack.hurtAndBreak(1, this, (livingentity) -> livingentity.broadcastBreakEvent(EquipmentSlot.FEET));
            }
         }
      }

   }

   protected void removeFrost() {
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attributeinstance != null) {
         if (attributeinstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_UUID) != null) {
            attributeinstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_UUID);
         }

      }
   }

   protected void tryAddFrost() {
      if (!this.getBlockStateOnLegacy().isAir()) {
         int i = this.getTicksFrozen();
         if (i > 0) {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeinstance == null) {
               return;
            }

            float f = -0.05F * this.getPercentFrozen();
            attributeinstance.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_POWDER_SNOW_UUID, "Powder snow slow", (double)f, AttributeModifier.Operation.ADDITION));
         }
      }

   }

   protected void onChangedBlock(BlockPos blockpos) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, this);
      if (i > 0) {
         FrostWalkerEnchantment.onEntityMoved(this, this.level(), blockpos, i);
      }

      if (this.shouldRemoveSoulSpeed(this.getBlockStateOnLegacy())) {
         this.removeSoulSpeed();
      }

      this.tryAddSoulSpeed();
   }

   public boolean isBaby() {
      return false;
   }

   public float getScale() {
      return this.isBaby() ? 0.5F : 1.0F;
   }

   protected boolean isAffectedByFluids() {
      return true;
   }

   protected void tickDeath() {
      ++this.deathTime;
      if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
         this.level().broadcastEntityEvent(this, (byte)60);
         this.remove(Entity.RemovalReason.KILLED);
      }

   }

   public boolean shouldDropExperience() {
      return !this.isBaby();
   }

   protected boolean shouldDropLoot() {
      return !this.isBaby();
   }

   protected int decreaseAirSupply(int i) {
      int j = EnchantmentHelper.getRespiration(this);
      return j > 0 && this.random.nextInt(j + 1) > 0 ? i : i - 1;
   }

   protected int increaseAirSupply(int i) {
      return Math.min(i + 4, this.getMaxAirSupply());
   }

   public int getExperienceReward() {
      return 0;
   }

   protected boolean isAlwaysExperienceDropper() {
      return false;
   }

   public RandomSource getRandom() {
      return this.random;
   }

   @Nullable
   public LivingEntity getLastHurtByMob() {
      return this.lastHurtByMob;
   }

   public LivingEntity getLastAttacker() {
      return this.getLastHurtByMob();
   }

   public int getLastHurtByMobTimestamp() {
      return this.lastHurtByMobTimestamp;
   }

   public void setLastHurtByPlayer(@Nullable Player player) {
      this.lastHurtByPlayer = player;
      this.lastHurtByPlayerTime = this.tickCount;
   }

   public void setLastHurtByMob(@Nullable LivingEntity livingentity) {
      this.lastHurtByMob = livingentity;
      this.lastHurtByMobTimestamp = this.tickCount;
   }

   @Nullable
   public LivingEntity getLastHurtMob() {
      return this.lastHurtMob;
   }

   public int getLastHurtMobTimestamp() {
      return this.lastHurtMobTimestamp;
   }

   public void setLastHurtMob(Entity entity) {
      if (entity instanceof LivingEntity) {
         this.lastHurtMob = (LivingEntity)entity;
      } else {
         this.lastHurtMob = null;
      }

      this.lastHurtMobTimestamp = this.tickCount;
   }

   public int getNoActionTime() {
      return this.noActionTime;
   }

   public void setNoActionTime(int i) {
      this.noActionTime = i;
   }

   public boolean shouldDiscardFriction() {
      return this.discardFriction;
   }

   public void setDiscardFriction(boolean flag) {
      this.discardFriction = flag;
   }

   protected boolean doesEmitEquipEvent(EquipmentSlot equipmentslot) {
      return true;
   }

   public void onEquipItem(EquipmentSlot equipmentslot, ItemStack itemstack, ItemStack itemstack1) {
      boolean flag = itemstack1.isEmpty() && itemstack.isEmpty();
      if (!flag && !ItemStack.isSameItemSameTags(itemstack, itemstack1) && !this.firstTick) {
         Equipable equipable = Equipable.get(itemstack1);
         if (equipable != null && !this.isSpectator() && equipable.getEquipmentSlot() == equipmentslot) {
            if (!this.level().isClientSide() && !this.isSilent()) {
               this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), equipable.getEquipSound(), this.getSoundSource(), 1.0F, 1.0F);
            }

            if (this.doesEmitEquipEvent(equipmentslot)) {
               this.gameEvent(GameEvent.EQUIP);
            }
         }

      }
   }

   public void remove(Entity.RemovalReason entity_removalreason) {
      super.remove(entity_removalreason);
      this.brain.clearMemories();
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putFloat("Health", this.getHealth());
      compoundtag.putShort("HurtTime", (short)this.hurtTime);
      compoundtag.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
      compoundtag.putShort("DeathTime", (short)this.deathTime);
      compoundtag.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
      compoundtag.put("Attributes", this.getAttributes().save());
      if (!this.activeEffects.isEmpty()) {
         ListTag listtag = new ListTag();

         for(MobEffectInstance mobeffectinstance : this.activeEffects.values()) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }

         compoundtag.put("ActiveEffects", listtag);
      }

      compoundtag.putBoolean("FallFlying", this.isFallFlying());
      this.getSleepingPos().ifPresent((blockpos) -> {
         compoundtag.putInt("SleepingX", blockpos.getX());
         compoundtag.putInt("SleepingY", blockpos.getY());
         compoundtag.putInt("SleepingZ", blockpos.getZ());
      });
      DataResult<Tag> dataresult = this.brain.serializeStart(NbtOps.INSTANCE);
      dataresult.resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("Brain", tag));
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      this.setAbsorptionAmount(compoundtag.getFloat("AbsorptionAmount"));
      if (compoundtag.contains("Attributes", 9) && this.level() != null && !this.level().isClientSide) {
         this.getAttributes().load(compoundtag.getList("Attributes", 10));
      }

      if (compoundtag.contains("ActiveEffects", 9)) {
         ListTag listtag = compoundtag.getList("ActiveEffects", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag1);
            if (mobeffectinstance != null) {
               this.activeEffects.put(mobeffectinstance.getEffect(), mobeffectinstance);
            }
         }
      }

      if (compoundtag.contains("Health", 99)) {
         this.setHealth(compoundtag.getFloat("Health"));
      }

      this.hurtTime = compoundtag.getShort("HurtTime");
      this.deathTime = compoundtag.getShort("DeathTime");
      this.lastHurtByMobTimestamp = compoundtag.getInt("HurtByTimestamp");
      if (compoundtag.contains("Team", 8)) {
         String s = compoundtag.getString("Team");
         PlayerTeam playerteam = this.level().getScoreboard().getPlayerTeam(s);
         boolean flag = playerteam != null && this.level().getScoreboard().addPlayerToTeam(this.getStringUUID(), playerteam);
         if (!flag) {
            LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", (Object)s);
         }
      }

      if (compoundtag.getBoolean("FallFlying")) {
         this.setSharedFlag(7, true);
      }

      if (compoundtag.contains("SleepingX", 99) && compoundtag.contains("SleepingY", 99) && compoundtag.contains("SleepingZ", 99)) {
         BlockPos blockpos = new BlockPos(compoundtag.getInt("SleepingX"), compoundtag.getInt("SleepingY"), compoundtag.getInt("SleepingZ"));
         this.setSleepingPos(blockpos);
         this.entityData.set(DATA_POSE, Pose.SLEEPING);
         if (!this.firstTick) {
            this.setPosToBed(blockpos);
         }
      }

      if (compoundtag.contains("Brain", 10)) {
         this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Brain")));
      }

   }

   protected void tickEffects() {
      Iterator<MobEffect> iterator = this.activeEffects.keySet().iterator();

      try {
         while(iterator.hasNext()) {
            MobEffect mobeffect = iterator.next();
            MobEffectInstance mobeffectinstance = this.activeEffects.get(mobeffect);
            if (!mobeffectinstance.tick(this, () -> this.onEffectUpdated(mobeffectinstance, true, (Entity)null))) {
               if (!this.level().isClientSide) {
                  iterator.remove();
                  this.onEffectRemoved(mobeffectinstance);
               }
            } else if (mobeffectinstance.getDuration() % 600 == 0) {
               this.onEffectUpdated(mobeffectinstance, false, (Entity)null);
            }
         }
      } catch (ConcurrentModificationException var11) {
      }

      if (this.effectsDirty) {
         if (!this.level().isClientSide) {
            this.updateInvisibilityStatus();
            this.updateGlowingStatus();
         }

         this.effectsDirty = false;
      }

      int i = this.entityData.get(DATA_EFFECT_COLOR_ID);
      boolean flag = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
      if (i > 0) {
         boolean flag1;
         if (this.isInvisible()) {
            flag1 = this.random.nextInt(15) == 0;
         } else {
            flag1 = this.random.nextBoolean();
         }

         if (flag) {
            flag1 &= this.random.nextInt(5) == 0;
         }

         if (flag1 && i > 0) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i >> 0 & 255) / 255.0D;
            this.level().addParticle(flag ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
         }
      }

   }

   protected void updateInvisibilityStatus() {
      if (this.activeEffects.isEmpty()) {
         this.removeEffectParticles();
         this.setInvisible(false);
      } else {
         Collection<MobEffectInstance> collection = this.activeEffects.values();
         this.entityData.set(DATA_EFFECT_AMBIENCE_ID, areAllEffectsAmbient(collection));
         this.entityData.set(DATA_EFFECT_COLOR_ID, PotionUtils.getColor(collection));
         this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
      }

   }

   private void updateGlowingStatus() {
      boolean flag = this.isCurrentlyGlowing();
      if (this.getSharedFlag(6) != flag) {
         this.setSharedFlag(6, flag);
      }

   }

   public double getVisibilityPercent(@Nullable Entity entity) {
      double d0 = 1.0D;
      if (this.isDiscrete()) {
         d0 *= 0.8D;
      }

      if (this.isInvisible()) {
         float f = this.getArmorCoverPercentage();
         if (f < 0.1F) {
            f = 0.1F;
         }

         d0 *= 0.7D * (double)f;
      }

      if (entity != null) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
         EntityType<?> entitytype = entity.getType();
         if (entitytype == EntityType.SKELETON && itemstack.is(Items.SKELETON_SKULL) || entitytype == EntityType.ZOMBIE && itemstack.is(Items.ZOMBIE_HEAD) || entitytype == EntityType.PIGLIN && itemstack.is(Items.PIGLIN_HEAD) || entitytype == EntityType.PIGLIN_BRUTE && itemstack.is(Items.PIGLIN_HEAD) || entitytype == EntityType.CREEPER && itemstack.is(Items.CREEPER_HEAD)) {
            d0 *= 0.5D;
         }
      }

      return d0;
   }

   public boolean canAttack(LivingEntity livingentity) {
      return livingentity instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL ? false : livingentity.canBeSeenAsEnemy();
   }

   public boolean canAttack(LivingEntity livingentity, TargetingConditions targetingconditions) {
      return targetingconditions.test(this, livingentity);
   }

   public boolean canBeSeenAsEnemy() {
      return !this.isInvulnerable() && this.canBeSeenByAnyone();
   }

   public boolean canBeSeenByAnyone() {
      return !this.isSpectator() && this.isAlive();
   }

   public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> collection) {
      for(MobEffectInstance mobeffectinstance : collection) {
         if (mobeffectinstance.isVisible() && !mobeffectinstance.isAmbient()) {
            return false;
         }
      }

      return true;
   }

   protected void removeEffectParticles() {
      this.entityData.set(DATA_EFFECT_AMBIENCE_ID, false);
      this.entityData.set(DATA_EFFECT_COLOR_ID, 0);
   }

   public boolean removeAllEffects() {
      if (this.level().isClientSide) {
         return false;
      } else {
         Iterator<MobEffectInstance> iterator = this.activeEffects.values().iterator();

         boolean flag;
         for(flag = false; iterator.hasNext(); flag = true) {
            this.onEffectRemoved(iterator.next());
            iterator.remove();
         }

         return flag;
      }
   }

   public Collection<MobEffectInstance> getActiveEffects() {
      return this.activeEffects.values();
   }

   public Map<MobEffect, MobEffectInstance> getActiveEffectsMap() {
      return this.activeEffects;
   }

   public boolean hasEffect(MobEffect mobeffect) {
      return this.activeEffects.containsKey(mobeffect);
   }

   @Nullable
   public MobEffectInstance getEffect(MobEffect mobeffect) {
      return this.activeEffects.get(mobeffect);
   }

   public final boolean addEffect(MobEffectInstance mobeffectinstance) {
      return this.addEffect(mobeffectinstance, (Entity)null);
   }

   public boolean addEffect(MobEffectInstance mobeffectinstance, @Nullable Entity entity) {
      if (!this.canBeAffected(mobeffectinstance)) {
         return false;
      } else {
         MobEffectInstance mobeffectinstance1 = this.activeEffects.get(mobeffectinstance.getEffect());
         if (mobeffectinstance1 == null) {
            this.activeEffects.put(mobeffectinstance.getEffect(), mobeffectinstance);
            this.onEffectAdded(mobeffectinstance, entity);
            return true;
         } else if (mobeffectinstance1.update(mobeffectinstance)) {
            this.onEffectUpdated(mobeffectinstance1, true, entity);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean canBeAffected(MobEffectInstance mobeffectinstance) {
      if (this.getMobType() == MobType.UNDEAD) {
         MobEffect mobeffect = mobeffectinstance.getEffect();
         if (mobeffect == MobEffects.REGENERATION || mobeffect == MobEffects.POISON) {
            return false;
         }
      }

      return true;
   }

   public void forceAddEffect(MobEffectInstance mobeffectinstance, @Nullable Entity entity) {
      if (this.canBeAffected(mobeffectinstance)) {
         MobEffectInstance mobeffectinstance1 = this.activeEffects.put(mobeffectinstance.getEffect(), mobeffectinstance);
         if (mobeffectinstance1 == null) {
            this.onEffectAdded(mobeffectinstance, entity);
         } else {
            this.onEffectUpdated(mobeffectinstance, true, entity);
         }

      }
   }

   public boolean isInvertedHealAndHarm() {
      return this.getMobType() == MobType.UNDEAD;
   }

   @Nullable
   public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect mobeffect) {
      return this.activeEffects.remove(mobeffect);
   }

   public boolean removeEffect(MobEffect mobeffect) {
      MobEffectInstance mobeffectinstance = this.removeEffectNoUpdate(mobeffect);
      if (mobeffectinstance != null) {
         this.onEffectRemoved(mobeffectinstance);
         return true;
      } else {
         return false;
      }
   }

   protected void onEffectAdded(MobEffectInstance mobeffectinstance, @Nullable Entity entity) {
      this.effectsDirty = true;
      if (!this.level().isClientSide) {
         mobeffectinstance.getEffect().addAttributeModifiers(this, this.getAttributes(), mobeffectinstance.getAmplifier());
         this.sendEffectToPassengers(mobeffectinstance);
      }

   }

   public void sendEffectToPassengers(MobEffectInstance mobeffectinstance) {
      for(Entity entity : this.getPassengers()) {
         if (entity instanceof ServerPlayer serverplayer) {
            serverplayer.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobeffectinstance));
         }
      }

   }

   protected void onEffectUpdated(MobEffectInstance mobeffectinstance, boolean flag, @Nullable Entity entity) {
      this.effectsDirty = true;
      if (flag && !this.level().isClientSide) {
         MobEffect mobeffect = mobeffectinstance.getEffect();
         mobeffect.removeAttributeModifiers(this, this.getAttributes(), mobeffectinstance.getAmplifier());
         mobeffect.addAttributeModifiers(this, this.getAttributes(), mobeffectinstance.getAmplifier());
      }

      if (!this.level().isClientSide) {
         this.sendEffectToPassengers(mobeffectinstance);
      }

   }

   protected void onEffectRemoved(MobEffectInstance mobeffectinstance) {
      this.effectsDirty = true;
      if (!this.level().isClientSide) {
         mobeffectinstance.getEffect().removeAttributeModifiers(this, this.getAttributes(), mobeffectinstance.getAmplifier());

         for(Entity entity : this.getPassengers()) {
            if (entity instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)entity;
               serverplayer.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobeffectinstance.getEffect()));
            }
         }
      }

   }

   public void heal(float f) {
      float f1 = this.getHealth();
      if (f1 > 0.0F) {
         this.setHealth(f1 + f);
      }

   }

   public float getHealth() {
      return this.entityData.get(DATA_HEALTH_ID);
   }

   public void setHealth(float f) {
      this.entityData.set(DATA_HEALTH_ID, Mth.clamp(f, 0.0F, this.getMaxHealth()));
   }

   public boolean isDeadOrDying() {
      return this.getHealth() <= 0.0F;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (this.level().isClientSide) {
         return false;
      } else if (this.isDeadOrDying()) {
         return false;
      } else if (damagesource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         return false;
      } else {
         if (this.isSleeping() && !this.level().isClientSide) {
            this.stopSleeping();
         }

         this.noActionTime = 0;
         float f1 = f;
         boolean flag = false;
         float f2 = 0.0F;
         if (f > 0.0F && this.isDamageSourceBlocked(damagesource)) {
            this.hurtCurrentlyUsedShield(f);
            f2 = f;
            f = 0.0F;
            if (!damagesource.is(DamageTypeTags.IS_PROJECTILE)) {
               Entity entity = damagesource.getDirectEntity();
               if (entity instanceof LivingEntity) {
                  LivingEntity livingentity = (LivingEntity)entity;
                  this.blockUsingShield(livingentity);
               }
            }

            flag = true;
         }

         if (damagesource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            f *= 5.0F;
         }

         this.walkAnimation.setSpeed(1.5F);
         boolean flag1 = true;
         if ((float)this.invulnerableTime > 10.0F && !damagesource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (f <= this.lastHurt) {
               return false;
            }

            this.actuallyHurt(damagesource, f - this.lastHurt);
            this.lastHurt = f;
            flag1 = false;
         } else {
            this.lastHurt = f;
            this.invulnerableTime = 20;
            this.actuallyHurt(damagesource, f);
            this.hurtDuration = 10;
            this.hurtTime = this.hurtDuration;
         }

         if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.hurtHelmet(damagesource, f);
            f *= 0.75F;
         }

         Entity entity1 = damagesource.getEntity();
         if (entity1 != null) {
            if (entity1 instanceof LivingEntity) {
               LivingEntity livingentity1 = (LivingEntity)entity1;
               if (!damagesource.is(DamageTypeTags.NO_ANGER)) {
                  this.setLastHurtByMob(livingentity1);
               }
            }

            if (entity1 instanceof Player) {
               Player player = (Player)entity1;
               this.lastHurtByPlayerTime = 100;
               this.lastHurtByPlayer = player;
            } else if (entity1 instanceof Wolf) {
               Wolf wolf = (Wolf)entity1;
               if (wolf.isTame()) {
                  this.lastHurtByPlayerTime = 100;
                  LivingEntity var11 = wolf.getOwner();
                  if (var11 instanceof Player) {
                     Player player1 = (Player)var11;
                     this.lastHurtByPlayer = player1;
                  } else {
                     this.lastHurtByPlayer = null;
                  }
               }
            }
         }

         if (flag1) {
            if (flag) {
               this.level().broadcastEntityEvent(this, (byte)29);
            } else {
               this.level().broadcastDamageEvent(this, damagesource);
            }

            if (!damagesource.is(DamageTypeTags.NO_IMPACT) && (!flag || f > 0.0F)) {
               this.markHurt();
            }

            if (entity1 != null && !damagesource.is(DamageTypeTags.IS_EXPLOSION)) {
               double d0 = entity1.getX() - this.getX();

               double d1;
               for(d1 = entity1.getZ() - this.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                  d0 = (Math.random() - Math.random()) * 0.01D;
               }

               this.knockback((double)0.4F, d0, d1);
               if (!flag) {
                  this.indicateDamage(d0, d1);
               }
            }
         }

         if (this.isDeadOrDying()) {
            if (!this.checkTotemDeathProtection(damagesource)) {
               SoundEvent soundevent = this.getDeathSound();
               if (flag1 && soundevent != null) {
                  this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
               }

               this.die(damagesource);
            }
         } else if (flag1) {
            this.playHurtSound(damagesource);
         }

         boolean flag2 = !flag || f > 0.0F;
         if (flag2) {
            this.lastDamageSource = damagesource;
            this.lastDamageStamp = this.level().getGameTime();
         }

         if (this instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)this, damagesource, f1, f, flag);
            if (f2 > 0.0F && f2 < 3.4028235E37F) {
               ((ServerPlayer)this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f2 * 10.0F));
            }
         }

         if (entity1 instanceof ServerPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity1, this, damagesource, f1, f, flag);
         }

         return flag2;
      }
   }

   protected void blockUsingShield(LivingEntity livingentity) {
      livingentity.blockedByShield(this);
   }

   protected void blockedByShield(LivingEntity livingentity) {
      livingentity.knockback(0.5D, livingentity.getX() - this.getX(), livingentity.getZ() - this.getZ());
   }

   private boolean checkTotemDeathProtection(DamageSource damagesource) {
      if (damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      } else {
         ItemStack itemstack = null;

         for(InteractionHand interactionhand : InteractionHand.values()) {
            ItemStack itemstack1 = this.getItemInHand(interactionhand);
            if (itemstack1.is(Items.TOTEM_OF_UNDYING)) {
               itemstack = itemstack1.copy();
               itemstack1.shrink(1);
               break;
            }
         }

         if (itemstack != null) {
            if (this instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)this;
               serverplayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
               CriteriaTriggers.USED_TOTEM.trigger(serverplayer, itemstack);
            }

            this.setHealth(1.0F);
            this.removeAllEffects();
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            this.level().broadcastEntityEvent(this, (byte)35);
         }

         return itemstack != null;
      }
   }

   @Nullable
   public DamageSource getLastDamageSource() {
      if (this.level().getGameTime() - this.lastDamageStamp > 40L) {
         this.lastDamageSource = null;
      }

      return this.lastDamageSource;
   }

   protected void playHurtSound(DamageSource damagesource) {
      SoundEvent soundevent = this.getHurtSound(damagesource);
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public boolean isDamageSourceBlocked(DamageSource damagesource) {
      Entity entity = damagesource.getDirectEntity();
      boolean flag = false;
      if (entity instanceof AbstractArrow abstractarrow) {
         if (abstractarrow.getPierceLevel() > 0) {
            flag = true;
         }
      }

      if (!damagesource.is(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !flag) {
         Vec3 vec3 = damagesource.getSourcePosition();
         if (vec3 != null) {
            Vec3 vec31 = this.getViewVector(1.0F);
            Vec3 vec32 = vec3.vectorTo(this.position()).normalize();
            vec32 = new Vec3(vec32.x, 0.0D, vec32.z);
            if (vec32.dot(vec31) < 0.0D) {
               return true;
            }
         }
      }

      return false;
   }

   private void breakItem(ItemStack itemstack) {
      if (!itemstack.isEmpty()) {
         if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, this.getSoundSource(), 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F, false);
         }

         this.spawnItemParticles(itemstack, 5);
      }

   }

   public void die(DamageSource damagesource) {
      if (!this.isRemoved() && !this.dead) {
         Entity entity = damagesource.getEntity();
         LivingEntity livingentity = this.getKillCredit();
         if (this.deathScore >= 0 && livingentity != null) {
            livingentity.awardKillScore(this, this.deathScore, damagesource);
         }

         if (this.isSleeping()) {
            this.stopSleeping();
         }

         if (!this.level().isClientSide && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", this, this.getCombatTracker().getDeathMessage().getString());
         }

         this.dead = true;
         this.getCombatTracker().recheckStatus();
         Level var5 = this.level();
         if (var5 instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)var5;
            if (entity == null || entity.killedEntity(serverlevel, this)) {
               this.gameEvent(GameEvent.ENTITY_DIE);
               this.dropAllDeathLoot(damagesource);
               this.createWitherRose(livingentity);
            }

            this.level().broadcastEntityEvent(this, (byte)3);
         }

         this.setPose(Pose.DYING);
      }
   }

   protected void createWitherRose(@Nullable LivingEntity livingentity) {
      if (!this.level().isClientSide) {
         boolean flag = false;
         if (livingentity instanceof WitherBoss) {
            if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
               BlockPos blockpos = this.blockPosition();
               BlockState blockstate = Blocks.WITHER_ROSE.defaultBlockState();
               if (this.level().getBlockState(blockpos).isAir() && blockstate.canSurvive(this.level(), blockpos)) {
                  this.level().setBlock(blockpos, blockstate, 3);
                  flag = true;
               }
            }

            if (!flag) {
               ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
               this.level().addFreshEntity(itementity);
            }
         }

      }
   }

   protected void dropAllDeathLoot(DamageSource damagesource) {
      Entity entity = damagesource.getEntity();
      int i;
      if (entity instanceof Player) {
         i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
      } else {
         i = 0;
      }

      boolean flag = this.lastHurtByPlayerTime > 0;
      if (this.shouldDropLoot() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         this.dropFromLootTable(damagesource, flag);
         this.dropCustomDeathLoot(damagesource, i, flag);
      }

      this.dropEquipment();
      this.dropExperience();
   }

   protected void dropEquipment() {
   }

   protected void dropExperience() {
      if (this.level() instanceof ServerLevel && !this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
         ExperienceOrb.award((ServerLevel)this.level(), this.position(), this.getExperienceReward());
      }

   }

   protected void dropCustomDeathLoot(DamageSource damagesource, int i, boolean flag) {
   }

   public ResourceLocation getLootTable() {
      return this.getType().getDefaultLootTable();
   }

   public long getLootTableSeed() {
      return 0L;
   }

   protected void dropFromLootTable(DamageSource damagesource, boolean flag) {
      ResourceLocation resourcelocation = this.getLootTable();
      LootTable loottable = this.level().getServer().getLootData().getLootTable(resourcelocation);
      LootParams.Builder lootparams_builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damagesource).withOptionalParameter(LootContextParams.KILLER_ENTITY, damagesource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damagesource.getDirectEntity());
      if (flag && this.lastHurtByPlayer != null) {
         lootparams_builder = lootparams_builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
      }

      LootParams lootparams = lootparams_builder.create(LootContextParamSets.ENTITY);
      loottable.getRandomItems(lootparams, this.getLootTableSeed(), this::spawnAtLocation);
   }

   public void knockback(double d0, double d1, double d2) {
      d0 *= 1.0D - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
      if (!(d0 <= 0.0D)) {
         this.hasImpulse = true;
         Vec3 vec3 = this.getDeltaMovement();
         Vec3 vec31 = (new Vec3(d1, 0.0D, d2)).normalize().scale(d0);
         this.setDeltaMovement(vec3.x / 2.0D - vec31.x, this.onGround() ? Math.min(0.4D, vec3.y / 2.0D + d0) : vec3.y, vec3.z / 2.0D - vec31.z);
      }
   }

   public void indicateDamage(double d0, double d1) {
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.GENERIC_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.GENERIC_DEATH;
   }

   private SoundEvent getFallDamageSound(int i) {
      return i > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
   }

   public void skipDropExperience() {
      this.skipDropExperience = true;
   }

   public boolean wasExperienceConsumed() {
      return this.skipDropExperience;
   }

   protected Vec3 getMeleeAttackReferencePosition() {
      Entity var2 = this.getVehicle();
      if (var2 instanceof RiderShieldingMount ridershieldingmount) {
         return this.position().add(0.0D, ridershieldingmount.getRiderShieldingHeight(), 0.0D);
      } else {
         return this.position();
      }
   }

   public float getHurtDir() {
      return 0.0F;
   }

   public LivingEntity.Fallsounds getFallSounds() {
      return new LivingEntity.Fallsounds(SoundEvents.GENERIC_SMALL_FALL, SoundEvents.GENERIC_BIG_FALL);
   }

   protected SoundEvent getDrinkingSound(ItemStack itemstack) {
      return itemstack.getDrinkingSound();
   }

   public SoundEvent getEatingSound(ItemStack itemstack) {
      return itemstack.getEatingSound();
   }

   public Optional<BlockPos> getLastClimbablePos() {
      return this.lastClimbablePos;
   }

   public boolean onClimbable() {
      if (this.isSpectator()) {
         return false;
      } else {
         BlockPos blockpos = this.blockPosition();
         BlockState blockstate = this.getFeetBlockState();
         if (blockstate.is(BlockTags.CLIMBABLE)) {
            this.lastClimbablePos = Optional.of(blockpos);
            return true;
         } else if (blockstate.getBlock() instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(blockpos, blockstate)) {
            this.lastClimbablePos = Optional.of(blockpos);
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean trapdoorUsableAsLadder(BlockPos blockpos, BlockState blockstate) {
      if (blockstate.getValue(TrapDoorBlock.OPEN)) {
         BlockState blockstate1 = this.level().getBlockState(blockpos.below());
         if (blockstate1.is(Blocks.LADDER) && blockstate1.getValue(LadderBlock.FACING) == blockstate.getValue(TrapDoorBlock.FACING)) {
            return true;
         }
      }

      return false;
   }

   public boolean isAlive() {
      return !this.isRemoved() && this.getHealth() > 0.0F;
   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      boolean flag = super.causeFallDamage(f, f1, damagesource);
      int i = this.calculateFallDamage(f, f1);
      if (i > 0) {
         this.playSound(this.getFallDamageSound(i), 1.0F, 1.0F);
         this.playBlockFallSound();
         this.hurt(damagesource, (float)i);
         return true;
      } else {
         return flag;
      }
   }

   protected int calculateFallDamage(float f, float f1) {
      if (this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
         return 0;
      } else {
         MobEffectInstance mobeffectinstance = this.getEffect(MobEffects.JUMP);
         float f2 = mobeffectinstance == null ? 0.0F : (float)(mobeffectinstance.getAmplifier() + 1);
         return Mth.ceil((f - 3.0F - f2) * f1);
      }
   }

   protected void playBlockFallSound() {
      if (!this.isSilent()) {
         int i = Mth.floor(this.getX());
         int j = Mth.floor(this.getY() - (double)0.2F);
         int k = Mth.floor(this.getZ());
         BlockState blockstate = this.level().getBlockState(new BlockPos(i, j, k));
         if (!blockstate.isAir()) {
            SoundType soundtype = blockstate.getSoundType();
            this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
         }

      }
   }

   public void animateHurt(float f) {
      this.hurtDuration = 10;
      this.hurtTime = this.hurtDuration;
   }

   public int getArmorValue() {
      return Mth.floor(this.getAttributeValue(Attributes.ARMOR));
   }

   protected void hurtArmor(DamageSource damagesource, float f) {
   }

   protected void hurtHelmet(DamageSource damagesource, float f) {
   }

   protected void hurtCurrentlyUsedShield(float f) {
   }

   protected float getDamageAfterArmorAbsorb(DamageSource damagesource, float f) {
      if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR)) {
         this.hurtArmor(damagesource, f);
         f = CombatRules.getDamageAfterAbsorb(f, (float)this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
      }

      return f;
   }

   protected float getDamageAfterMagicAbsorb(DamageSource damagesource, float f) {
      if (damagesource.is(DamageTypeTags.BYPASSES_EFFECTS)) {
         return f;
      } else {
         if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !damagesource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            int i = (this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f1 = f * (float)j;
            float f2 = f;
            f = Math.max(f1 / 25.0F, 0.0F);
            float f3 = f2 - f;
            if (f3 > 0.0F && f3 < 3.4028235E37F) {
               if (this instanceof ServerPlayer) {
                  ((ServerPlayer)this).awardStat(Stats.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
               } else if (damagesource.getEntity() instanceof ServerPlayer) {
                  ((ServerPlayer)damagesource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
               }
            }
         }

         if (f <= 0.0F) {
            return 0.0F;
         } else if (damagesource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return f;
         } else {
            int k = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), damagesource);
            if (k > 0) {
               f = CombatRules.getDamageAfterMagicAbsorb(f, (float)k);
            }

            return f;
         }
      }
   }

   protected void actuallyHurt(DamageSource damagesource, float f) {
      if (!this.isInvulnerableTo(damagesource)) {
         f = this.getDamageAfterArmorAbsorb(damagesource, f);
         f = this.getDamageAfterMagicAbsorb(damagesource, f);
         float var9 = Math.max(f - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - var9));
         float f2 = f - var9;
         if (f2 > 0.0F && f2 < 3.4028235E37F) {
            Entity var6 = damagesource.getEntity();
            if (var6 instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)var6;
               serverplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f2 * 10.0F));
            }
         }

         if (var9 != 0.0F) {
            this.getCombatTracker().recordDamage(damagesource, var9);
            this.setHealth(this.getHealth() - var9);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - var9);
            this.gameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   public CombatTracker getCombatTracker() {
      return this.combatTracker;
   }

   @Nullable
   public LivingEntity getKillCredit() {
      if (this.lastHurtByPlayer != null) {
         return this.lastHurtByPlayer;
      } else {
         return this.lastHurtByMob != null ? this.lastHurtByMob : null;
      }
   }

   public final float getMaxHealth() {
      return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
   }

   public final int getArrowCount() {
      return this.entityData.get(DATA_ARROW_COUNT_ID);
   }

   public final void setArrowCount(int i) {
      this.entityData.set(DATA_ARROW_COUNT_ID, i);
   }

   public final int getStingerCount() {
      return this.entityData.get(DATA_STINGER_COUNT_ID);
   }

   public final void setStingerCount(int i) {
      this.entityData.set(DATA_STINGER_COUNT_ID, i);
   }

   private int getCurrentSwingDuration() {
      if (MobEffectUtil.hasDigSpeed(this)) {
         return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
      } else {
         return this.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
      }
   }

   public void swing(InteractionHand interactionhand) {
      this.swing(interactionhand, false);
   }

   public void swing(InteractionHand interactionhand, boolean flag) {
      if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
         this.swingTime = -1;
         this.swinging = true;
         this.swingingArm = interactionhand;
         if (this.level() instanceof ServerLevel) {
            ClientboundAnimatePacket clientboundanimatepacket = new ClientboundAnimatePacket(this, interactionhand == InteractionHand.MAIN_HAND ? 0 : 3);
            ServerChunkCache serverchunkcache = ((ServerLevel)this.level()).getChunkSource();
            if (flag) {
               serverchunkcache.broadcastAndSend(this, clientboundanimatepacket);
            } else {
               serverchunkcache.broadcast(this, clientboundanimatepacket);
            }
         }
      }

   }

   public void handleDamageEvent(DamageSource damagesource) {
      this.walkAnimation.setSpeed(1.5F);
      this.invulnerableTime = 20;
      this.hurtDuration = 10;
      this.hurtTime = this.hurtDuration;
      SoundEvent soundevent = this.getHurtSound(damagesource);
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
      }

      this.hurt(this.damageSources().generic(), 0.0F);
      this.lastDamageSource = damagesource;
      this.lastDamageStamp = this.level().getGameTime();
   }

   public void handleEntityEvent(byte b0) {
      switch (b0) {
         case 3:
            SoundEvent soundevent = this.getDeathSound();
            if (soundevent != null) {
               this.playSound(soundevent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }

            if (!(this instanceof Player)) {
               this.setHealth(0.0F);
               this.die(this.damageSources().generic());
            }
            break;
         case 29:
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.level().random.nextFloat() * 0.4F);
            break;
         case 30:
            this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
            break;
         case 46:
            int i = 128;

            for(int j = 0; j < 128; ++j) {
               double d0 = (double)j / 127.0D;
               float f = (this.random.nextFloat() - 0.5F) * 0.2F;
               float f1 = (this.random.nextFloat() - 0.5F) * 0.2F;
               float f2 = (this.random.nextFloat() - 0.5F) * 0.2F;
               double d1 = Mth.lerp(d0, this.xo, this.getX()) + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth() * 2.0D;
               double d2 = Mth.lerp(d0, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
               double d3 = Mth.lerp(d0, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth() * 2.0D;
               this.level().addParticle(ParticleTypes.PORTAL, d1, d2, d3, (double)f, (double)f1, (double)f2);
            }
            break;
         case 47:
            this.breakItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            break;
         case 48:
            this.breakItem(this.getItemBySlot(EquipmentSlot.OFFHAND));
            break;
         case 49:
            this.breakItem(this.getItemBySlot(EquipmentSlot.HEAD));
            break;
         case 50:
            this.breakItem(this.getItemBySlot(EquipmentSlot.CHEST));
            break;
         case 51:
            this.breakItem(this.getItemBySlot(EquipmentSlot.LEGS));
            break;
         case 52:
            this.breakItem(this.getItemBySlot(EquipmentSlot.FEET));
            break;
         case 54:
            HoneyBlock.showJumpParticles(this);
            break;
         case 55:
            this.swapHandItems();
            break;
         case 60:
            this.makePoofParticles();
            break;
         default:
            super.handleEntityEvent(b0);
      }

   }

   private void makePoofParticles() {
      for(int i = 0; i < 20; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(ParticleTypes.POOF, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   private void swapHandItems() {
      ItemStack itemstack = this.getItemBySlot(EquipmentSlot.OFFHAND);
      this.setItemSlot(EquipmentSlot.OFFHAND, this.getItemBySlot(EquipmentSlot.MAINHAND));
      this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
   }

   protected void onBelowWorld() {
      this.hurt(this.damageSources().fellOutOfWorld(), 4.0F);
   }

   protected void updateSwingTime() {
      int i = this.getCurrentSwingDuration();
      if (this.swinging) {
         ++this.swingTime;
         if (this.swingTime >= i) {
            this.swingTime = 0;
            this.swinging = false;
         }
      } else {
         this.swingTime = 0;
      }

      this.attackAnim = (float)this.swingTime / (float)i;
   }

   @Nullable
   public AttributeInstance getAttribute(Attribute attribute) {
      return this.getAttributes().getInstance(attribute);
   }

   public double getAttributeValue(Holder<Attribute> holder) {
      return this.getAttributeValue(holder.value());
   }

   public double getAttributeValue(Attribute attribute) {
      return this.getAttributes().getValue(attribute);
   }

   public double getAttributeBaseValue(Holder<Attribute> holder) {
      return this.getAttributeBaseValue(holder.value());
   }

   public double getAttributeBaseValue(Attribute attribute) {
      return this.getAttributes().getBaseValue(attribute);
   }

   public AttributeMap getAttributes() {
      return this.attributes;
   }

   public MobType getMobType() {
      return MobType.UNDEFINED;
   }

   public ItemStack getMainHandItem() {
      return this.getItemBySlot(EquipmentSlot.MAINHAND);
   }

   public ItemStack getOffhandItem() {
      return this.getItemBySlot(EquipmentSlot.OFFHAND);
   }

   public boolean isHolding(Item item) {
      return this.isHolding((itemstack) -> itemstack.is(item));
   }

   public boolean isHolding(Predicate<ItemStack> predicate) {
      return predicate.test(this.getMainHandItem()) || predicate.test(this.getOffhandItem());
   }

   public ItemStack getItemInHand(InteractionHand interactionhand) {
      if (interactionhand == InteractionHand.MAIN_HAND) {
         return this.getItemBySlot(EquipmentSlot.MAINHAND);
      } else if (interactionhand == InteractionHand.OFF_HAND) {
         return this.getItemBySlot(EquipmentSlot.OFFHAND);
      } else {
         throw new IllegalArgumentException("Invalid hand " + interactionhand);
      }
   }

   public void setItemInHand(InteractionHand interactionhand, ItemStack itemstack) {
      if (interactionhand == InteractionHand.MAIN_HAND) {
         this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
      } else {
         if (interactionhand != InteractionHand.OFF_HAND) {
            throw new IllegalArgumentException("Invalid hand " + interactionhand);
         }

         this.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
      }

   }

   public boolean hasItemInSlot(EquipmentSlot equipmentslot) {
      return !this.getItemBySlot(equipmentslot).isEmpty();
   }

   public abstract Iterable<ItemStack> getArmorSlots();

   public abstract ItemStack getItemBySlot(EquipmentSlot equipmentslot);

   public abstract void setItemSlot(EquipmentSlot equipmentslot, ItemStack itemstack);

   protected void verifyEquippedItem(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null) {
         itemstack.getItem().verifyTagAfterLoad(compoundtag);
      }

   }

   public float getArmorCoverPercentage() {
      Iterable<ItemStack> iterable = this.getArmorSlots();
      int i = 0;
      int j = 0;

      for(ItemStack itemstack : iterable) {
         if (!itemstack.isEmpty()) {
            ++j;
         }

         ++i;
      }

      return i > 0 ? (float)j / (float)i : 0.0F;
   }

   public void setSprinting(boolean flag) {
      super.setSprinting(flag);
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attributeinstance.getModifier(SPEED_MODIFIER_SPRINTING_UUID) != null) {
         attributeinstance.removeModifier(SPEED_MODIFIER_SPRINTING);
      }

      if (flag) {
         attributeinstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
      }

   }

   protected float getSoundVolume() {
      return 1.0F;
   }

   public float getVoicePitch() {
      return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
   }

   protected boolean isImmobile() {
      return this.isDeadOrDying();
   }

   public void push(Entity entity) {
      if (!this.isSleeping()) {
         super.push(entity);
      }

   }

   private void dismountVehicle(Entity entity) {
      Vec3 vec3;
      if (this.isRemoved()) {
         vec3 = this.position();
      } else if (!entity.isRemoved() && !this.level().getBlockState(entity.blockPosition()).is(BlockTags.PORTALS)) {
         vec3 = entity.getDismountLocationForPassenger(this);
      } else {
         double d0 = Math.max(this.getY(), entity.getY());
         vec3 = new Vec3(this.getX(), d0, this.getZ());
      }

      this.dismountTo(vec3.x, vec3.y, vec3.z);
   }

   public boolean shouldShowName() {
      return this.isCustomNameVisible();
   }

   protected float getJumpPower() {
      return 0.42F * this.getBlockJumpFactor() + this.getJumpBoostPower();
   }

   public float getJumpBoostPower() {
      return this.hasEffect(MobEffects.JUMP) ? 0.1F * ((float)this.getEffect(MobEffects.JUMP).getAmplifier() + 1.0F) : 0.0F;
   }

   protected void jumpFromGround() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x, (double)this.getJumpPower(), vec3.z);
      if (this.isSprinting()) {
         float f = this.getYRot() * ((float)Math.PI / 180F);
         this.setDeltaMovement(this.getDeltaMovement().add((double)(-Mth.sin(f) * 0.2F), 0.0D, (double)(Mth.cos(f) * 0.2F)));
      }

      this.hasImpulse = true;
   }

   protected void goDownInWater() {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)-0.04F, 0.0D));
   }

   protected void jumpInLiquid(TagKey<Fluid> tagkey) {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)0.04F, 0.0D));
   }

   protected float getWaterSlowDown() {
      return 0.8F;
   }

   public boolean canStandOnFluid(FluidState fluidstate) {
      return false;
   }

   public void travel(Vec3 vec3) {
      if (this.isControlledByLocalInstance()) {
         double d0 = 0.08D;
         boolean flag = this.getDeltaMovement().y <= 0.0D;
         if (flag && this.hasEffect(MobEffects.SLOW_FALLING)) {
            d0 = 0.01D;
         }

         FluidState fluidstate = this.level().getFluidState(this.blockPosition());
         if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate)) {
            double d1 = this.getY();
            float f = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
            float f1 = 0.02F;
            float f2 = (float)EnchantmentHelper.getDepthStrider(this);
            if (f2 > 3.0F) {
               f2 = 3.0F;
            }

            if (!this.onGround()) {
               f2 *= 0.5F;
            }

            if (f2 > 0.0F) {
               f += (0.54600006F - f) * f2 / 3.0F;
               f1 += (this.getSpeed() - f1) * f2 / 3.0F;
            }

            if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
               f = 0.96F;
            }

            this.moveRelative(f1, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 vec31 = this.getDeltaMovement();
            if (this.horizontalCollision && this.onClimbable()) {
               vec31 = new Vec3(vec31.x, 0.2D, vec31.z);
            }

            this.setDeltaMovement(vec31.multiply((double)f, (double)0.8F, (double)f));
            Vec3 vec32 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
            this.setDeltaMovement(vec32);
            if (this.horizontalCollision && this.isFree(vec32.x, vec32.y + (double)0.6F - this.getY() + d1, vec32.z)) {
               this.setDeltaMovement(vec32.x, (double)0.3F, vec32.z);
            }
         } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate)) {
            double d2 = this.getY();
            this.moveRelative(0.02F, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, (double)0.8F, 0.5D));
               Vec3 vec33 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
               this.setDeltaMovement(vec33);
            } else {
               this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            }

            if (!this.isNoGravity()) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
            }

            Vec3 vec34 = this.getDeltaMovement();
            if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + (double)0.6F - this.getY() + d2, vec34.z)) {
               this.setDeltaMovement(vec34.x, (double)0.3F, vec34.z);
            }
         } else if (this.isFallFlying()) {
            this.checkSlowFallDistance();
            Vec3 vec35 = this.getDeltaMovement();
            Vec3 vec36 = this.getLookAngle();
            float f3 = this.getXRot() * ((float)Math.PI / 180F);
            double d3 = Math.sqrt(vec36.x * vec36.x + vec36.z * vec36.z);
            double d4 = vec35.horizontalDistance();
            double d5 = vec36.length();
            double d6 = Math.cos((double)f3);
            d6 = d6 * d6 * Math.min(1.0D, d5 / 0.4D);
            vec35 = this.getDeltaMovement().add(0.0D, d0 * (-1.0D + d6 * 0.75D), 0.0D);
            if (vec35.y < 0.0D && d3 > 0.0D) {
               double d7 = vec35.y * -0.1D * d6;
               vec35 = vec35.add(vec36.x * d7 / d3, d7, vec36.z * d7 / d3);
            }

            if (f3 < 0.0F && d3 > 0.0D) {
               double d8 = d4 * (double)(-Mth.sin(f3)) * 0.04D;
               vec35 = vec35.add(-vec36.x * d8 / d3, d8 * 3.2D, -vec36.z * d8 / d3);
            }

            if (d3 > 0.0D) {
               vec35 = vec35.add((vec36.x / d3 * d4 - vec35.x) * 0.1D, 0.0D, (vec36.z / d3 * d4 - vec35.z) * 0.1D);
            }

            this.setDeltaMovement(vec35.multiply((double)0.99F, (double)0.98F, (double)0.99F));
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.horizontalCollision && !this.level().isClientSide) {
               double d9 = this.getDeltaMovement().horizontalDistance();
               double d10 = d4 - d9;
               float f4 = (float)(d10 * 10.0D - 3.0D);
               if (f4 > 0.0F) {
                  this.playSound(this.getFallDamageSound((int)f4), 1.0F, 1.0F);
                  this.hurt(this.damageSources().flyIntoWall(), f4);
               }
            }

            if (this.onGround() && !this.level().isClientSide) {
               this.setSharedFlag(7, false);
            }
         } else {
            BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
            float f5 = this.level().getBlockState(blockpos).getBlock().getFriction();
            float f6 = this.onGround() ? f5 * 0.91F : 0.91F;
            Vec3 vec37 = this.handleRelativeFrictionAndCalculateMovement(vec3, f5);
            double d11 = vec37.y;
            if (this.hasEffect(MobEffects.LEVITATION)) {
               d11 += (0.05D * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec37.y) * 0.2D;
            } else if (this.level().isClientSide && !this.level().hasChunkAt(blockpos)) {
               if (this.getY() > (double)this.level().getMinBuildHeight()) {
                  d11 = -0.1D;
               } else {
                  d11 = 0.0D;
               }
            } else if (!this.isNoGravity()) {
               d11 -= d0;
            }

            if (this.shouldDiscardFriction()) {
               this.setDeltaMovement(vec37.x, d11, vec37.z);
            } else {
               this.setDeltaMovement(vec37.x * (double)f6, d11 * (double)0.98F, vec37.z * (double)f6);
            }
         }
      }

      this.calculateEntityAnimation(this instanceof FlyingAnimal);
   }

   private void travelRidden(Player player, Vec3 vec3) {
      Vec3 vec31 = this.getRiddenInput(player, vec3);
      this.tickRidden(player, vec31);
      if (this.isControlledByLocalInstance()) {
         this.setSpeed(this.getRiddenSpeed(player));
         this.travel(vec31);
      } else {
         this.calculateEntityAnimation(false);
         this.setDeltaMovement(Vec3.ZERO);
         this.tryCheckInsideBlocks();
      }

   }

   protected void tickRidden(Player player, Vec3 vec3) {
   }

   protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
      return vec3;
   }

   protected float getRiddenSpeed(Player player) {
      return this.getSpeed();
   }

   public void calculateEntityAnimation(boolean flag) {
      float f = (float)Mth.length(this.getX() - this.xo, flag ? this.getY() - this.yo : 0.0D, this.getZ() - this.zo);
      this.updateWalkAnimation(f);
   }

   protected void updateWalkAnimation(float f) {
      float f1 = Math.min(f * 4.0F, 1.0F);
      this.walkAnimation.update(f1, 0.4F);
   }

   public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 vec3, float f) {
      this.moveRelative(this.getFrictionInfluencedSpeed(f), vec3);
      this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
      this.move(MoverType.SELF, this.getDeltaMovement());
      Vec3 vec31 = this.getDeltaMovement();
      if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.getFeetBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
         vec31 = new Vec3(vec31.x, 0.2D, vec31.z);
      }

      return vec31;
   }

   public Vec3 getFluidFallingAdjustedMovement(double d0, boolean flag, Vec3 vec3) {
      if (!this.isNoGravity() && !this.isSprinting()) {
         double d1;
         if (flag && Math.abs(vec3.y - 0.005D) >= 0.003D && Math.abs(vec3.y - d0 / 16.0D) < 0.003D) {
            d1 = -0.003D;
         } else {
            d1 = vec3.y - d0 / 16.0D;
         }

         return new Vec3(vec3.x, d1, vec3.z);
      } else {
         return vec3;
      }
   }

   private Vec3 handleOnClimbable(Vec3 vec3) {
      if (this.onClimbable()) {
         this.resetFallDistance();
         float f = 0.15F;
         double d0 = Mth.clamp(vec3.x, (double)-0.15F, (double)0.15F);
         double d1 = Mth.clamp(vec3.z, (double)-0.15F, (double)0.15F);
         double d2 = Math.max(vec3.y, (double)-0.15F);
         if (d2 < 0.0D && !this.getFeetBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
            d2 = 0.0D;
         }

         vec3 = new Vec3(d0, d2, d1);
      }

      return vec3;
   }

   private float getFrictionInfluencedSpeed(float f) {
      return this.onGround() ? this.getSpeed() * (0.21600002F / (f * f * f)) : this.getFlyingSpeed();
   }

   protected float getFlyingSpeed() {
      return this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1F : 0.02F;
   }

   public float getSpeed() {
      return this.speed;
   }

   public void setSpeed(float f) {
      this.speed = f;
   }

   public boolean doHurtTarget(Entity entity) {
      this.setLastHurtMob(entity);
      return false;
   }

   public void tick() {
      super.tick();
      this.updatingUsingItem();
      this.updateSwimAmount();
      if (!this.level().isClientSide) {
         int i = this.getArrowCount();
         if (i > 0) {
            if (this.removeArrowTime <= 0) {
               this.removeArrowTime = 20 * (30 - i);
            }

            --this.removeArrowTime;
            if (this.removeArrowTime <= 0) {
               this.setArrowCount(i - 1);
            }
         }

         int j = this.getStingerCount();
         if (j > 0) {
            if (this.removeStingerTime <= 0) {
               this.removeStingerTime = 20 * (30 - j);
            }

            --this.removeStingerTime;
            if (this.removeStingerTime <= 0) {
               this.setStingerCount(j - 1);
            }
         }

         this.detectEquipmentUpdates();
         if (this.tickCount % 20 == 0) {
            this.getCombatTracker().recheckStatus();
         }

         if (this.isSleeping() && !this.checkBedExists()) {
            this.stopSleeping();
         }
      }

      if (!this.isRemoved()) {
         this.aiStep();
      }

      double d0 = this.getX() - this.xo;
      double d1 = this.getZ() - this.zo;
      float f = (float)(d0 * d0 + d1 * d1);
      float f1 = this.yBodyRot;
      float f2 = 0.0F;
      this.oRun = this.run;
      float f3 = 0.0F;
      if (f > 0.0025000002F) {
         f3 = 1.0F;
         f2 = (float)Math.sqrt((double)f) * 3.0F;
         float f4 = (float)Mth.atan2(d1, d0) * (180F / (float)Math.PI) - 90.0F;
         float f5 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - f4);
         if (95.0F < f5 && f5 < 265.0F) {
            f1 = f4 - 180.0F;
         } else {
            f1 = f4;
         }
      }

      if (this.attackAnim > 0.0F) {
         f1 = this.getYRot();
      }

      if (!this.onGround()) {
         f3 = 0.0F;
      }

      this.run += (f3 - this.run) * 0.3F;
      this.level().getProfiler().push("headTurn");
      f2 = this.tickHeadTurn(f1, f2);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("rangeChecks");

      while(this.getYRot() - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while(this.getYRot() - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      while(this.yBodyRot - this.yBodyRotO < -180.0F) {
         this.yBodyRotO -= 360.0F;
      }

      while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
         this.yBodyRotO += 360.0F;
      }

      while(this.getXRot() - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }

      while(this.getXRot() - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      while(this.yHeadRot - this.yHeadRotO < -180.0F) {
         this.yHeadRotO -= 360.0F;
      }

      while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
         this.yHeadRotO += 360.0F;
      }

      this.level().getProfiler().pop();
      this.animStep += f2;
      if (this.isFallFlying()) {
         ++this.fallFlyTicks;
      } else {
         this.fallFlyTicks = 0;
      }

      if (this.isSleeping()) {
         this.setXRot(0.0F);
      }

   }

   private void detectEquipmentUpdates() {
      Map<EquipmentSlot, ItemStack> map = this.collectEquipmentChanges();
      if (map != null) {
         this.handleHandSwap(map);
         if (!map.isEmpty()) {
            this.handleEquipmentChanges(map);
         }
      }

   }

   @Nullable
   private Map<EquipmentSlot, ItemStack> collectEquipmentChanges() {
      Map<EquipmentSlot, ItemStack> map = null;

      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
         ItemStack itemstack;
         switch (equipmentslot.getType()) {
            case HAND:
               itemstack = this.getLastHandItem(equipmentslot);
               break;
            case ARMOR:
               itemstack = this.getLastArmorItem(equipmentslot);
               break;
            default:
               continue;
         }

         ItemStack itemstack3 = this.getItemBySlot(equipmentslot);
         if (this.equipmentHasChanged(itemstack, itemstack3)) {
            if (map == null) {
               map = Maps.newEnumMap(EquipmentSlot.class);
            }

            map.put(equipmentslot, itemstack3);
            if (!itemstack.isEmpty()) {
               this.getAttributes().removeAttributeModifiers(itemstack.getAttributeModifiers(equipmentslot));
            }

            if (!itemstack3.isEmpty()) {
               this.getAttributes().addTransientAttributeModifiers(itemstack3.getAttributeModifiers(equipmentslot));
            }
         }
      }

      return map;
   }

   public boolean equipmentHasChanged(ItemStack itemstack, ItemStack itemstack1) {
      return !ItemStack.matches(itemstack1, itemstack);
   }

   private void handleHandSwap(Map<EquipmentSlot, ItemStack> map) {
      ItemStack itemstack = map.get(EquipmentSlot.MAINHAND);
      ItemStack itemstack1 = map.get(EquipmentSlot.OFFHAND);
      if (itemstack != null && itemstack1 != null && ItemStack.matches(itemstack, this.getLastHandItem(EquipmentSlot.OFFHAND)) && ItemStack.matches(itemstack1, this.getLastHandItem(EquipmentSlot.MAINHAND))) {
         ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundEntityEventPacket(this, (byte)55));
         map.remove(EquipmentSlot.MAINHAND);
         map.remove(EquipmentSlot.OFFHAND);
         this.setLastHandItem(EquipmentSlot.MAINHAND, itemstack.copy());
         this.setLastHandItem(EquipmentSlot.OFFHAND, itemstack1.copy());
      }

   }

   private void handleEquipmentChanges(Map<EquipmentSlot, ItemStack> map) {
      List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(map.size());
      map.forEach((equipmentslot, itemstack) -> {
         ItemStack itemstack1 = itemstack.copy();
         list.add(Pair.of(equipmentslot, itemstack1));
         switch (equipmentslot.getType()) {
            case HAND:
               this.setLastHandItem(equipmentslot, itemstack1);
               break;
            case ARMOR:
               this.setLastArmorItem(equipmentslot, itemstack1);
         }

      });
      ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEquipmentPacket(this.getId(), list));
   }

   private ItemStack getLastArmorItem(EquipmentSlot equipmentslot) {
      return this.lastArmorItemStacks.get(equipmentslot.getIndex());
   }

   private void setLastArmorItem(EquipmentSlot equipmentslot, ItemStack itemstack) {
      this.lastArmorItemStacks.set(equipmentslot.getIndex(), itemstack);
   }

   private ItemStack getLastHandItem(EquipmentSlot equipmentslot) {
      return this.lastHandItemStacks.get(equipmentslot.getIndex());
   }

   private void setLastHandItem(EquipmentSlot equipmentslot, ItemStack itemstack) {
      this.lastHandItemStacks.set(equipmentslot.getIndex(), itemstack);
   }

   protected float tickHeadTurn(float f, float f1) {
      float f2 = Mth.wrapDegrees(f - this.yBodyRot);
      this.yBodyRot += f2 * 0.3F;
      float f3 = Mth.wrapDegrees(this.getYRot() - this.yBodyRot);
      if (Math.abs(f3) > 50.0F) {
         this.yBodyRot += f3 - (float)(Mth.sign((double)f3) * 50);
      }

      boolean flag = f3 < -90.0F || f3 >= 90.0F;
      if (flag) {
         f1 *= -1.0F;
      }

      return f1;
   }

   public void aiStep() {
      if (this.noJumpDelay > 0) {
         --this.noJumpDelay;
      }

      if (this.isControlledByLocalInstance()) {
         this.lerpSteps = 0;
         this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
      }

      if (this.lerpSteps > 0) {
         double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
         double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
         double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
         double d3 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
         this.setYRot(this.getYRot() + (float)d3 / (float)this.lerpSteps);
         this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
         --this.lerpSteps;
         this.setPos(d0, d1, d2);
         this.setRot(this.getYRot(), this.getXRot());
      } else if (!this.isEffectiveAi()) {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
      }

      if (this.lerpHeadSteps > 0) {
         this.yHeadRot += (float)Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (float)this.lerpHeadSteps;
         --this.lerpHeadSteps;
      }

      Vec3 vec3 = this.getDeltaMovement();
      double d4 = vec3.x;
      double d5 = vec3.y;
      double d6 = vec3.z;
      if (Math.abs(vec3.x) < 0.003D) {
         d4 = 0.0D;
      }

      if (Math.abs(vec3.y) < 0.003D) {
         d5 = 0.0D;
      }

      if (Math.abs(vec3.z) < 0.003D) {
         d6 = 0.0D;
      }

      this.setDeltaMovement(d4, d5, d6);
      this.level().getProfiler().push("ai");
      if (this.isImmobile()) {
         this.jumping = false;
         this.xxa = 0.0F;
         this.zza = 0.0F;
      } else if (this.isEffectiveAi()) {
         this.level().getProfiler().push("newAi");
         this.serverAiStep();
         this.level().getProfiler().pop();
      }

      this.level().getProfiler().pop();
      this.level().getProfiler().push("jump");
      if (this.jumping && this.isAffectedByFluids()) {
         double d7;
         if (this.isInLava()) {
            d7 = this.getFluidHeight(FluidTags.LAVA);
         } else {
            d7 = this.getFluidHeight(FluidTags.WATER);
         }

         boolean flag = this.isInWater() && d7 > 0.0D;
         double d9 = this.getFluidJumpThreshold();
         if (!flag || this.onGround() && !(d7 > d9)) {
            if (!this.isInLava() || this.onGround() && !(d7 > d9)) {
               if ((this.onGround() || flag && d7 <= d9) && this.noJumpDelay == 0) {
                  this.jumpFromGround();
                  this.noJumpDelay = 10;
               }
            } else {
               this.jumpInLiquid(FluidTags.LAVA);
            }
         } else {
            this.jumpInLiquid(FluidTags.WATER);
         }
      } else {
         this.noJumpDelay = 0;
      }

      this.level().getProfiler().pop();
      this.level().getProfiler().push("travel");
      this.xxa *= 0.98F;
      this.zza *= 0.98F;
      this.updateFallFlying();
      AABB aabb = this.getBoundingBox();
      Vec3 vec31 = new Vec3((double)this.xxa, (double)this.yya, (double)this.zza);
      if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
         this.resetFallDistance();
      }

      label104: {
         LivingEntity var17 = this.getControllingPassenger();
         if (var17 instanceof Player player) {
            if (this.isAlive()) {
               this.travelRidden(player, vec31);
               break label104;
            }
         }

         this.travel(vec31);
      }

      this.level().getProfiler().pop();
      this.level().getProfiler().push("freezing");
      if (!this.level().isClientSide && !this.isDeadOrDying()) {
         int i = this.getTicksFrozen();
         if (this.isInPowderSnow && this.canFreeze()) {
            this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), i + 1));
         } else {
            this.setTicksFrozen(Math.max(0, i - 2));
         }
      }

      this.removeFrost();
      this.tryAddFrost();
      if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
         this.hurt(this.damageSources().freeze(), 1.0F);
      }

      this.level().getProfiler().pop();
      this.level().getProfiler().push("push");
      if (this.autoSpinAttackTicks > 0) {
         --this.autoSpinAttackTicks;
         this.checkAutoSpinAttack(aabb, this.getBoundingBox());
      }

      this.pushEntities();
      this.level().getProfiler().pop();
      if (!this.level().isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
         this.hurt(this.damageSources().drown(), 1.0F);
      }

   }

   public boolean isSensitiveToWater() {
      return false;
   }

   private void updateFallFlying() {
      boolean flag = this.getSharedFlag(7);
      if (flag && !this.onGround() && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
         if (itemstack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemstack)) {
            flag = true;
            int i = this.fallFlyTicks + 1;
            if (!this.level().isClientSide && i % 10 == 0) {
               int j = i / 10;
               if (j % 2 == 0) {
                  itemstack.hurtAndBreak(1, this, (livingentity) -> livingentity.broadcastBreakEvent(EquipmentSlot.CHEST));
               }

               this.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
         } else {
            flag = false;
         }
      } else {
         flag = false;
      }

      if (!this.level().isClientSide) {
         this.setSharedFlag(7, flag);
      }

   }

   protected void serverAiStep() {
   }

   protected void pushEntities() {
      if (this.level().isClientSide()) {
         this.level().getEntities(EntityTypeTest.forClass(Player.class), this.getBoundingBox(), EntitySelector.pushableBy(this)).forEach(this::doPush);
      } else {
         List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
         if (!list.isEmpty()) {
            int i = this.level().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
               int j = 0;

               for(int k = 0; k < list.size(); ++k) {
                  if (!list.get(k).isPassenger()) {
                     ++j;
                  }
               }

               if (j > i - 1) {
                  this.hurt(this.damageSources().cramming(), 6.0F);
               }
            }

            for(int l = 0; l < list.size(); ++l) {
               Entity entity = list.get(l);
               this.doPush(entity);
            }
         }

      }
   }

   protected void checkAutoSpinAttack(AABB aabb, AABB aabb1) {
      AABB aabb2 = aabb.minmax(aabb1);
      List<Entity> list = this.level().getEntities(this, aabb2);
      if (!list.isEmpty()) {
         for(int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if (entity instanceof LivingEntity) {
               this.doAutoAttackOnTouch((LivingEntity)entity);
               this.autoSpinAttackTicks = 0;
               this.setDeltaMovement(this.getDeltaMovement().scale(-0.2D));
               break;
            }
         }
      } else if (this.horizontalCollision) {
         this.autoSpinAttackTicks = 0;
      }

      if (!this.level().isClientSide && this.autoSpinAttackTicks <= 0) {
         this.setLivingEntityFlag(4, false);
      }

   }

   protected void doPush(Entity entity) {
      entity.push(this);
   }

   protected void doAutoAttackOnTouch(LivingEntity livingentity) {
   }

   public boolean isAutoSpinAttack() {
      return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
   }

   public void stopRiding() {
      Entity entity = this.getVehicle();
      super.stopRiding();
      if (entity != null && entity != this.getVehicle() && !this.level().isClientSide) {
         this.dismountVehicle(entity);
      }

   }

   public void rideTick() {
      super.rideTick();
      this.oRun = this.run;
      this.run = 0.0F;
      this.resetFallDistance();
   }

   public void lerpTo(double d0, double d1, double d2, float f, float f1, int i, boolean flag) {
      this.lerpX = d0;
      this.lerpY = d1;
      this.lerpZ = d2;
      this.lerpYRot = (double)f;
      this.lerpXRot = (double)f1;
      this.lerpSteps = i;
   }

   public void lerpHeadTo(float f, int i) {
      this.lyHeadRot = (double)f;
      this.lerpHeadSteps = i;
   }

   public void setJumping(boolean flag) {
      this.jumping = flag;
   }

   public void onItemPickup(ItemEntity itementity) {
      Entity entity = itementity.getOwner();
      if (entity instanceof ServerPlayer) {
         CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayer)entity, itementity.getItem(), this);
      }

   }

   public void take(Entity entity, int i) {
      if (!entity.isRemoved() && !this.level().isClientSide && (entity instanceof ItemEntity || entity instanceof AbstractArrow || entity instanceof ExperienceOrb)) {
         ((ServerLevel)this.level()).getChunkSource().broadcast(entity, new ClientboundTakeItemEntityPacket(entity.getId(), this.getId(), i));
      }

   }

   public boolean hasLineOfSight(Entity entity) {
      if (entity.level() != this.level()) {
         return false;
      } else {
         Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
         Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
         if (vec31.distanceTo(vec3) > 128.0D) {
            return false;
         } else {
            return this.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
         }
      }
   }

   public float getViewYRot(float f) {
      return f == 1.0F ? this.yHeadRot : Mth.lerp(f, this.yHeadRotO, this.yHeadRot);
   }

   public float getAttackAnim(float f) {
      float f1 = this.attackAnim - this.oAttackAnim;
      if (f1 < 0.0F) {
         ++f1;
      }

      return this.oAttackAnim + f1 * f;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean isPushable() {
      return this.isAlive() && !this.isSpectator() && !this.onClimbable();
   }

   public float getYHeadRot() {
      return this.yHeadRot;
   }

   public void setYHeadRot(float f) {
      this.yHeadRot = f;
   }

   public void setYBodyRot(float f) {
      this.yBodyRot = f;
   }

   protected Vec3 getRelativePortalPosition(Direction.Axis direction_axis, BlockUtil.FoundRectangle blockutil_foundrectangle) {
      return resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(direction_axis, blockutil_foundrectangle));
   }

   public static Vec3 resetForwardDirectionOfRelativePortalPosition(Vec3 vec3) {
      return new Vec3(vec3.x, vec3.y, 0.0D);
   }

   public float getAbsorptionAmount() {
      return this.absorptionAmount;
   }

   public void setAbsorptionAmount(float f) {
      if (f < 0.0F) {
         f = 0.0F;
      }

      this.absorptionAmount = f;
   }

   public void onEnterCombat() {
   }

   public void onLeaveCombat() {
   }

   protected void updateEffectVisibility() {
      this.effectsDirty = true;
   }

   public abstract HumanoidArm getMainArm();

   public boolean isUsingItem() {
      return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
   }

   public InteractionHand getUsedItemHand() {
      return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   private void updatingUsingItem() {
      if (this.isUsingItem()) {
         if (ItemStack.isSameItem(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
            this.useItem = this.getItemInHand(this.getUsedItemHand());
            this.updateUsingItem(this.useItem);
         } else {
            this.stopUsingItem();
         }
      }

   }

   protected void updateUsingItem(ItemStack itemstack) {
      itemstack.onUseTick(this.level(), this, this.getUseItemRemainingTicks());
      if (this.shouldTriggerItemUseEffects()) {
         this.triggerItemUseEffects(itemstack, 5);
      }

      if (--this.useItemRemaining == 0 && !this.level().isClientSide && !itemstack.useOnRelease()) {
         this.completeUsingItem();
      }

   }

   private boolean shouldTriggerItemUseEffects() {
      int i = this.getUseItemRemainingTicks();
      FoodProperties foodproperties = this.useItem.getItem().getFoodProperties();
      boolean flag = foodproperties != null && foodproperties.isFastFood();
      flag |= i <= this.useItem.getUseDuration() - 7;
      return flag && i % 4 == 0;
   }

   private void updateSwimAmount() {
      this.swimAmountO = this.swimAmount;
      if (this.isVisuallySwimming()) {
         this.swimAmount = Math.min(1.0F, this.swimAmount + 0.09F);
      } else {
         this.swimAmount = Math.max(0.0F, this.swimAmount - 0.09F);
      }

   }

   protected void setLivingEntityFlag(int i, boolean flag) {
      int j = this.entityData.get(DATA_LIVING_ENTITY_FLAGS);
      if (flag) {
         j |= i;
      } else {
         j &= ~i;
      }

      this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)j);
   }

   public void startUsingItem(InteractionHand interactionhand) {
      ItemStack itemstack = this.getItemInHand(interactionhand);
      if (!itemstack.isEmpty() && !this.isUsingItem()) {
         this.useItem = itemstack;
         this.useItemRemaining = itemstack.getUseDuration();
         if (!this.level().isClientSide) {
            this.setLivingEntityFlag(1, true);
            this.setLivingEntityFlag(2, interactionhand == InteractionHand.OFF_HAND);
            this.gameEvent(GameEvent.ITEM_INTERACT_START);
         }

      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      super.onSyncedDataUpdated(entitydataaccessor);
      if (SLEEPING_POS_ID.equals(entitydataaccessor)) {
         if (this.level().isClientSide) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
         }
      } else if (DATA_LIVING_ENTITY_FLAGS.equals(entitydataaccessor) && this.level().isClientSide) {
         if (this.isUsingItem() && this.useItem.isEmpty()) {
            this.useItem = this.getItemInHand(this.getUsedItemHand());
            if (!this.useItem.isEmpty()) {
               this.useItemRemaining = this.useItem.getUseDuration();
            }
         } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
            this.useItem = ItemStack.EMPTY;
            this.useItemRemaining = 0;
         }
      }

   }

   public void lookAt(EntityAnchorArgument.Anchor entityanchorargument_anchor, Vec3 vec3) {
      super.lookAt(entityanchorargument_anchor, vec3);
      this.yHeadRotO = this.yHeadRot;
      this.yBodyRot = this.yHeadRot;
      this.yBodyRotO = this.yBodyRot;
   }

   protected void triggerItemUseEffects(ItemStack itemstack, int i) {
      if (!itemstack.isEmpty() && this.isUsingItem()) {
         if (itemstack.getUseAnimation() == UseAnim.DRINK) {
            this.playSound(this.getDrinkingSound(itemstack), 0.5F, this.level().random.nextFloat() * 0.1F + 0.9F);
         }

         if (itemstack.getUseAnimation() == UseAnim.EAT) {
            this.spawnItemParticles(itemstack, i);
            this.playSound(this.getEatingSound(itemstack), 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         }

      }
   }

   private void spawnItemParticles(ItemStack itemstack, int i) {
      for(int j = 0; j < i; ++j) {
         Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
         vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180F));
         vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180F));
         double d0 = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
         Vec3 vec31 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
         vec31 = vec31.xRot(-this.getXRot() * ((float)Math.PI / 180F));
         vec31 = vec31.yRot(-this.getYRot() * ((float)Math.PI / 180F));
         vec31 = vec31.add(this.getX(), this.getEyeY(), this.getZ());
         this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemstack), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
      }

   }

   protected void completeUsingItem() {
      if (!this.level().isClientSide || this.isUsingItem()) {
         InteractionHand interactionhand = this.getUsedItemHand();
         if (!this.useItem.equals(this.getItemInHand(interactionhand))) {
            this.releaseUsingItem();
         } else {
            if (!this.useItem.isEmpty() && this.isUsingItem()) {
               this.triggerItemUseEffects(this.useItem, 16);
               ItemStack itemstack = this.useItem.finishUsingItem(this.level(), this);
               if (itemstack != this.useItem) {
                  this.setItemInHand(interactionhand, itemstack);
               }

               this.stopUsingItem();
            }

         }
      }
   }

   public ItemStack getUseItem() {
      return this.useItem;
   }

   public int getUseItemRemainingTicks() {
      return this.useItemRemaining;
   }

   public int getTicksUsingItem() {
      return this.isUsingItem() ? this.useItem.getUseDuration() - this.getUseItemRemainingTicks() : 0;
   }

   public void releaseUsingItem() {
      if (!this.useItem.isEmpty()) {
         this.useItem.releaseUsing(this.level(), this, this.getUseItemRemainingTicks());
         if (this.useItem.useOnRelease()) {
            this.updatingUsingItem();
         }
      }

      this.stopUsingItem();
   }

   public void stopUsingItem() {
      if (!this.level().isClientSide) {
         boolean flag = this.isUsingItem();
         this.setLivingEntityFlag(1, false);
         if (flag) {
            this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
         }
      }

      this.useItem = ItemStack.EMPTY;
      this.useItemRemaining = 0;
   }

   public boolean isBlocking() {
      if (this.isUsingItem() && !this.useItem.isEmpty()) {
         Item item = this.useItem.getItem();
         if (item.getUseAnimation(this.useItem) != UseAnim.BLOCK) {
            return false;
         } else {
            return item.getUseDuration(this.useItem) - this.useItemRemaining >= 5;
         }
      } else {
         return false;
      }
   }

   public boolean isSuppressingSlidingDownLadder() {
      return this.isShiftKeyDown();
   }

   public boolean isFallFlying() {
      return this.getSharedFlag(7);
   }

   public boolean isVisuallySwimming() {
      return super.isVisuallySwimming() || !this.isFallFlying() && this.hasPose(Pose.FALL_FLYING);
   }

   public int getFallFlyingTicks() {
      return this.fallFlyTicks;
   }

   public boolean randomTeleport(double d0, double d1, double d2, boolean flag) {
      double d3 = this.getX();
      double d4 = this.getY();
      double d5 = this.getZ();
      double d6 = d1;
      boolean flag1 = false;
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);
      Level level = this.level();
      if (level.hasChunkAt(blockpos)) {
         boolean flag2 = false;

         while(!flag2 && blockpos.getY() > level.getMinBuildHeight()) {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = level.getBlockState(blockpos1);
            if (blockstate.blocksMotion()) {
               flag2 = true;
            } else {
               --d6;
               blockpos = blockpos1;
            }
         }

         if (flag2) {
            this.teleportTo(d0, d6, d2);
            if (level.noCollision(this) && !level.containsAnyLiquid(this.getBoundingBox())) {
               flag1 = true;
            }
         }
      }

      if (!flag1) {
         this.teleportTo(d3, d4, d5);
         return false;
      } else {
         if (flag) {
            level.broadcastEntityEvent(this, (byte)46);
         }

         if (this instanceof PathfinderMob) {
            ((PathfinderMob)this).getNavigation().stop();
         }

         return true;
      }
   }

   public boolean isAffectedByPotions() {
      return true;
   }

   public boolean attackable() {
      return true;
   }

   public void setRecordPlayingNearby(BlockPos blockpos, boolean flag) {
   }

   public boolean canTakeItem(ItemStack itemstack) {
      return false;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return pose == Pose.SLEEPING ? SLEEPING_DIMENSIONS : super.getDimensions(pose).scale(this.getScale());
   }

   public ImmutableList<Pose> getDismountPoses() {
      return ImmutableList.of(Pose.STANDING);
   }

   public AABB getLocalBoundsForPose(Pose pose) {
      EntityDimensions entitydimensions = this.getDimensions(pose);
      return new AABB((double)(-entitydimensions.width / 2.0F), 0.0D, (double)(-entitydimensions.width / 2.0F), (double)(entitydimensions.width / 2.0F), (double)entitydimensions.height, (double)(entitydimensions.width / 2.0F));
   }

   public boolean canChangeDimensions() {
      return super.canChangeDimensions() && !this.isSleeping();
   }

   public Optional<BlockPos> getSleepingPos() {
      return this.entityData.get(SLEEPING_POS_ID);
   }

   public void setSleepingPos(BlockPos blockpos) {
      this.entityData.set(SLEEPING_POS_ID, Optional.of(blockpos));
   }

   public void clearSleepingPos() {
      this.entityData.set(SLEEPING_POS_ID, Optional.empty());
   }

   public boolean isSleeping() {
      return this.getSleepingPos().isPresent();
   }

   public void startSleeping(BlockPos blockpos) {
      if (this.isPassenger()) {
         this.stopRiding();
      }

      BlockState blockstate = this.level().getBlockState(blockpos);
      if (blockstate.getBlock() instanceof BedBlock) {
         this.level().setBlock(blockpos, blockstate.setValue(BedBlock.OCCUPIED, Boolean.valueOf(true)), 3);
      }

      this.setPose(Pose.SLEEPING);
      this.setPosToBed(blockpos);
      this.setSleepingPos(blockpos);
      this.setDeltaMovement(Vec3.ZERO);
      this.hasImpulse = true;
   }

   private void setPosToBed(BlockPos blockpos1) {
      this.setPos((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.6875D, (double)blockpos1.getZ() + 0.5D);
   }

   private boolean checkBedExists() {
      return this.getSleepingPos().map((blockpos) -> this.level().getBlockState(blockpos).getBlock() instanceof BedBlock).orElse(false);
   }

   public void stopSleeping() {
      this.getSleepingPos().filter(this.level()::hasChunkAt).ifPresent((blockpos) -> {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.getBlock() instanceof BedBlock) {
            Direction direction = blockstate.getValue(BedBlock.FACING);
            this.level().setBlock(blockpos, blockstate.setValue(BedBlock.OCCUPIED, Boolean.valueOf(false)), 3);
            Vec3 vec31 = BedBlock.findStandUpPosition(this.getType(), this.level(), blockpos, direction, this.getYRot()).orElseGet(() -> {
               BlockPos blockpos2 = blockpos.above();
               return new Vec3((double)blockpos2.getX() + 0.5D, (double)blockpos2.getY() + 0.1D, (double)blockpos2.getZ() + 0.5D);
            });
            Vec3 vec32 = Vec3.atBottomCenterOf(blockpos).subtract(vec31).normalize();
            float f = (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * (double)(180F / (float)Math.PI) - 90.0D);
            this.setPos(vec31.x, vec31.y, vec31.z);
            this.setYRot(f);
            this.setXRot(0.0F);
         }

      });
      Vec3 vec3 = this.position();
      this.setPose(Pose.STANDING);
      this.setPos(vec3.x, vec3.y, vec3.z);
      this.clearSleepingPos();
   }

   @Nullable
   public Direction getBedOrientation() {
      BlockPos blockpos = this.getSleepingPos().orElse((BlockPos)null);
      return blockpos != null ? BedBlock.getBedOrientation(this.level(), blockpos) : null;
   }

   public boolean isInWall() {
      return !this.isSleeping() && super.isInWall();
   }

   protected final float getEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return pose == Pose.SLEEPING ? 0.2F : this.getStandingEyeHeight(pose, entitydimensions);
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return super.getEyeHeight(pose, entitydimensions);
   }

   public ItemStack getProjectile(ItemStack itemstack) {
      return ItemStack.EMPTY;
   }

   public ItemStack eat(Level level, ItemStack itemstack) {
      if (itemstack.isEdible()) {
         level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(itemstack), SoundSource.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
         this.addEatEffect(itemstack, level, this);
         if (!(this instanceof Player) || !((Player)this).getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         this.gameEvent(GameEvent.EAT);
      }

      return itemstack;
   }

   private void addEatEffect(ItemStack itemstack, Level level, LivingEntity livingentity) {
      Item item = itemstack.getItem();
      if (item.isEdible()) {
         for(Pair<MobEffectInstance, Float> pair : item.getFoodProperties().getEffects()) {
            if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
               livingentity.addEffect(new MobEffectInstance(pair.getFirst()));
            }
         }
      }

   }

   private static byte entityEventForEquipmentBreak(EquipmentSlot equipmentslot) {
      switch (equipmentslot) {
         case MAINHAND:
            return 47;
         case OFFHAND:
            return 48;
         case HEAD:
            return 49;
         case CHEST:
            return 50;
         case FEET:
            return 52;
         case LEGS:
            return 51;
         default:
            return 47;
      }
   }

   public void broadcastBreakEvent(EquipmentSlot equipmentslot) {
      this.level().broadcastEntityEvent(this, entityEventForEquipmentBreak(equipmentslot));
   }

   public void broadcastBreakEvent(InteractionHand interactionhand) {
      this.broadcastBreakEvent(interactionhand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
   }

   public AABB getBoundingBoxForCulling() {
      if (this.getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD)) {
         float f = 0.5F;
         return this.getBoundingBox().inflate(0.5D, 0.5D, 0.5D);
      } else {
         return super.getBoundingBoxForCulling();
      }
   }

   public static EquipmentSlot getEquipmentSlotForItem(ItemStack itemstack) {
      Equipable equipable = Equipable.get(itemstack);
      return equipable != null ? equipable.getEquipmentSlot() : EquipmentSlot.MAINHAND;
   }

   private static SlotAccess createEquipmentSlotAccess(LivingEntity livingentity, EquipmentSlot equipmentslot) {
      return equipmentslot != EquipmentSlot.HEAD && equipmentslot != EquipmentSlot.MAINHAND && equipmentslot != EquipmentSlot.OFFHAND ? SlotAccess.forEquipmentSlot(livingentity, equipmentslot, (itemstack) -> itemstack.isEmpty() || Mob.getEquipmentSlotForItem(itemstack) == equipmentslot) : SlotAccess.forEquipmentSlot(livingentity, equipmentslot);
   }

   @Nullable
   private static EquipmentSlot getEquipmentSlot(int i) {
      if (i == 100 + EquipmentSlot.HEAD.getIndex()) {
         return EquipmentSlot.HEAD;
      } else if (i == 100 + EquipmentSlot.CHEST.getIndex()) {
         return EquipmentSlot.CHEST;
      } else if (i == 100 + EquipmentSlot.LEGS.getIndex()) {
         return EquipmentSlot.LEGS;
      } else if (i == 100 + EquipmentSlot.FEET.getIndex()) {
         return EquipmentSlot.FEET;
      } else if (i == 98) {
         return EquipmentSlot.MAINHAND;
      } else {
         return i == 99 ? EquipmentSlot.OFFHAND : null;
      }
   }

   public SlotAccess getSlot(int i) {
      EquipmentSlot equipmentslot = getEquipmentSlot(i);
      return equipmentslot != null ? createEquipmentSlotAccess(this, equipmentslot) : super.getSlot(i);
   }

   public boolean canFreeze() {
      if (this.isSpectator()) {
         return false;
      } else {
         boolean flag = !this.getItemBySlot(EquipmentSlot.HEAD).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.CHEST).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.LEGS).is(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getItemBySlot(EquipmentSlot.FEET).is(ItemTags.FREEZE_IMMUNE_WEARABLES);
         return flag && super.canFreeze();
      }
   }

   public boolean isCurrentlyGlowing() {
      return !this.level().isClientSide() && this.hasEffect(MobEffects.GLOWING) || super.isCurrentlyGlowing();
   }

   public float getVisualRotationYInDegrees() {
      return this.yBodyRot;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      double d0 = clientboundaddentitypacket.getX();
      double d1 = clientboundaddentitypacket.getY();
      double d2 = clientboundaddentitypacket.getZ();
      float f = clientboundaddentitypacket.getYRot();
      float f1 = clientboundaddentitypacket.getXRot();
      this.syncPacketPositionCodec(d0, d1, d2);
      this.yBodyRot = clientboundaddentitypacket.getYHeadRot();
      this.yHeadRot = clientboundaddentitypacket.getYHeadRot();
      this.yBodyRotO = this.yBodyRot;
      this.yHeadRotO = this.yHeadRot;
      this.setId(clientboundaddentitypacket.getId());
      this.setUUID(clientboundaddentitypacket.getUUID());
      this.absMoveTo(d0, d1, d2, f, f1);
      this.setDeltaMovement(clientboundaddentitypacket.getXa(), clientboundaddentitypacket.getYa(), clientboundaddentitypacket.getZa());
   }

   public boolean canDisableShield() {
      return this.getMainHandItem().getItem() instanceof AxeItem;
   }

   public float maxUpStep() {
      float f = super.maxUpStep();
      return this.getControllingPassenger() instanceof Player ? Math.max(f, 1.0F) : f;
   }

   public static record Fallsounds(SoundEvent small, SoundEvent big) {
   }
}
