package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class Entity implements Nameable, EntityAccess, CommandSource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String ID_TAG = "id";
   public static final String PASSENGERS_TAG = "Passengers";
   private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
   private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
   public static final int BOARDING_COOLDOWN = 60;
   public static final int TOTAL_AIR_SUPPLY = 300;
   public static final int MAX_ENTITY_TAG_COUNT = 1024;
   public static final float DELTA_AFFECTED_BY_BLOCKS_BELOW_0_2 = 0.2F;
   public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_0_5 = 0.500001D;
   public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_1_0 = 0.999999D;
   public static final float BREATHING_DISTANCE_BELOW_EYES = 0.11111111F;
   public static final int BASE_TICKS_REQUIRED_TO_FREEZE = 140;
   public static final int FREEZE_HURT_FREQUENCY = 40;
   private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static final double WATER_FLOW_SCALE = 0.014D;
   private static final double LAVA_FAST_FLOW_SCALE = 0.007D;
   private static final double LAVA_SLOW_FLOW_SCALE = 0.0023333333333333335D;
   public static final String UUID_TAG = "UUID";
   private static double viewScale = 1.0D;
   private final EntityType<?> type;
   private int id = ENTITY_COUNTER.incrementAndGet();
   public boolean blocksBuilding;
   private ImmutableList<Entity> passengers = ImmutableList.of();
   protected int boardingCooldown;
   @Nullable
   private Entity vehicle;
   private Level level;
   public double xo;
   public double yo;
   public double zo;
   private Vec3 position;
   private BlockPos blockPosition;
   private ChunkPos chunkPosition;
   private Vec3 deltaMovement = Vec3.ZERO;
   private float yRot;
   private float xRot;
   public float yRotO;
   public float xRotO;
   private AABB bb = INITIAL_AABB;
   private boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean verticalCollisionBelow;
   public boolean minorHorizontalCollision;
   public boolean hurtMarked;
   protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
   @Nullable
   private Entity.RemovalReason removalReason;
   public static final float DEFAULT_BB_WIDTH = 0.6F;
   public static final float DEFAULT_BB_HEIGHT = 1.8F;
   public float walkDistO;
   public float walkDist;
   public float moveDist;
   public float flyDist;
   public float fallDistance;
   private float nextStep = 1.0F;
   public double xOld;
   public double yOld;
   public double zOld;
   private float maxUpStep;
   public boolean noPhysics;
   protected final RandomSource random = RandomSource.create();
   public int tickCount;
   private int remainingFireTicks = -this.getFireImmuneTicks();
   protected boolean wasTouchingWater;
   protected Object2DoubleMap<TagKey<Fluid>> fluidHeight = new Object2DoubleArrayMap<>(2);
   protected boolean wasEyeInWater;
   private final Set<TagKey<Fluid>> fluidOnEyes = new HashSet<>();
   public int invulnerableTime;
   protected boolean firstTick = true;
   protected final SynchedEntityData entityData;
   protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
   protected static final int FLAG_ONFIRE = 0;
   private static final int FLAG_SHIFT_KEY_DOWN = 1;
   private static final int FLAG_SPRINTING = 3;
   private static final int FLAG_SWIMMING = 4;
   private static final int FLAG_INVISIBLE = 5;
   protected static final int FLAG_GLOWING = 6;
   protected static final int FLAG_FALL_FLYING = 7;
   private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
   private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Pose> DATA_POSE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.POSE);
   private static final EntityDataAccessor<Integer> DATA_TICKS_FROZEN = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
   private EntityInLevelCallback levelCallback = EntityInLevelCallback.NULL;
   private final VecDeltaCodec packetPositionCodec = new VecDeltaCodec();
   public boolean noCulling;
   public boolean hasImpulse;
   private int portalCooldown;
   protected boolean isInsidePortal;
   protected int portalTime;
   protected BlockPos portalEntrancePos;
   private boolean invulnerable;
   protected UUID uuid = Mth.createInsecureUUID(this.random);
   protected String stringUUID = this.uuid.toString();
   private boolean hasGlowingTag;
   private final Set<String> tags = Sets.newHashSet();
   private final double[] pistonDeltas = new double[]{0.0D, 0.0D, 0.0D};
   private long pistonDeltasGameTime;
   private EntityDimensions dimensions;
   private float eyeHeight;
   public boolean isInPowderSnow;
   public boolean wasInPowderSnow;
   public boolean wasOnFire;
   public Optional<BlockPos> mainSupportingBlockPos = Optional.empty();
   private boolean onGroundNoBlocks = false;
   private float crystalSoundIntensity;
   private int lastCrystalSoundPlayTick;
   private boolean hasVisualFire;
   @Nullable
   private BlockState feetBlockState = null;

   public Entity(EntityType<?> entitytype, Level level) {
      this.type = entitytype;
      this.level = level;
      this.dimensions = entitytype.getDimensions();
      this.position = Vec3.ZERO;
      this.blockPosition = BlockPos.ZERO;
      this.chunkPosition = ChunkPos.ZERO;
      this.entityData = new SynchedEntityData(this);
      this.entityData.define(DATA_SHARED_FLAGS_ID, (byte)0);
      this.entityData.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
      this.entityData.define(DATA_CUSTOM_NAME_VISIBLE, false);
      this.entityData.define(DATA_CUSTOM_NAME, Optional.empty());
      this.entityData.define(DATA_SILENT, false);
      this.entityData.define(DATA_NO_GRAVITY, false);
      this.entityData.define(DATA_POSE, Pose.STANDING);
      this.entityData.define(DATA_TICKS_FROZEN, 0);
      this.defineSynchedData();
      this.setPos(0.0D, 0.0D, 0.0D);
      this.eyeHeight = this.getEyeHeight(Pose.STANDING, this.dimensions);
   }

   public boolean isColliding(BlockPos blockpos, BlockState blockstate) {
      VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos, CollisionContext.of(this));
      VoxelShape voxelshape1 = voxelshape.move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
      return Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(this.getBoundingBox()), BooleanOp.AND);
   }

   public int getTeamColor() {
      Team team = this.getTeam();
      return team != null && team.getColor().getColor() != null ? team.getColor().getColor() : 16777215;
   }

   public boolean isSpectator() {
      return false;
   }

   public final void unRide() {
      if (this.isVehicle()) {
         this.ejectPassengers();
      }

      if (this.isPassenger()) {
         this.stopRiding();
      }

   }

   public void syncPacketPositionCodec(double d0, double d1, double d2) {
      this.packetPositionCodec.setBase(new Vec3(d0, d1, d2));
   }

   public VecDeltaCodec getPositionCodec() {
      return this.packetPositionCodec;
   }

   public EntityType<?> getType() {
      return this.type;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int i) {
      this.id = i;
   }

   public Set<String> getTags() {
      return this.tags;
   }

   public boolean addTag(String s) {
      return this.tags.size() >= 1024 ? false : this.tags.add(s);
   }

   public boolean removeTag(String s) {
      return this.tags.remove(s);
   }

   public void kill() {
      this.remove(Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
   }

   public final void discard() {
      this.remove(Entity.RemovalReason.DISCARDED);
   }

   protected abstract void defineSynchedData();

   public SynchedEntityData getEntityData() {
      return this.entityData;
   }

   public boolean equals(Object object) {
      if (object instanceof Entity) {
         return ((Entity)object).id == this.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }

   public void remove(Entity.RemovalReason entity_removalreason) {
      this.setRemoved(entity_removalreason);
   }

   public void onClientRemoval() {
   }

   public void setPose(Pose pose) {
      this.entityData.set(DATA_POSE, pose);
   }

   public Pose getPose() {
      return this.entityData.get(DATA_POSE);
   }

   public boolean hasPose(Pose pose) {
      return this.getPose() == pose;
   }

   public boolean closerThan(Entity entity, double d0) {
      return this.position().closerThan(entity.position(), d0);
   }

   public boolean closerThan(Entity entity, double d0, double d1) {
      double d2 = entity.getX() - this.getX();
      double d3 = entity.getY() - this.getY();
      double d4 = entity.getZ() - this.getZ();
      return Mth.lengthSquared(d2, d4) < Mth.square(d0) && Mth.square(d3) < Mth.square(d1);
   }

   protected void setRot(float f, float f1) {
      this.setYRot(f % 360.0F);
      this.setXRot(f1 % 360.0F);
   }

   public final void setPos(Vec3 vec3) {
      this.setPos(vec3.x(), vec3.y(), vec3.z());
   }

   public void setPos(double d0, double d1, double d2) {
      this.setPosRaw(d0, d1, d2);
      this.setBoundingBox(this.makeBoundingBox());
   }

   protected AABB makeBoundingBox() {
      return this.dimensions.makeBoundingBox(this.position);
   }

   protected void reapplyPosition() {
      this.setPos(this.position.x, this.position.y, this.position.z);
   }

   public void turn(double d0, double d1) {
      float f = (float)d1 * 0.15F;
      float f1 = (float)d0 * 0.15F;
      this.setXRot(this.getXRot() + f);
      this.setYRot(this.getYRot() + f1);
      this.setXRot(Mth.clamp(this.getXRot(), -90.0F, 90.0F));
      this.xRotO += f;
      this.yRotO += f1;
      this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
      if (this.vehicle != null) {
         this.vehicle.onPassengerTurned(this);
      }

   }

   public void tick() {
      this.baseTick();
   }

   public void baseTick() {
      this.level().getProfiler().push("entityBaseTick");
      this.feetBlockState = null;
      if (this.isPassenger() && this.getVehicle().isRemoved()) {
         this.stopRiding();
      }

      if (this.boardingCooldown > 0) {
         --this.boardingCooldown;
      }

      this.walkDistO = this.walkDist;
      this.xRotO = this.getXRot();
      this.yRotO = this.getYRot();
      this.handleNetherPortal();
      if (this.canSpawnSprintParticle()) {
         this.spawnSprintParticle();
      }

      this.wasInPowderSnow = this.isInPowderSnow;
      this.isInPowderSnow = false;
      this.updateInWaterStateAndDoFluidPushing();
      this.updateFluidOnEyes();
      this.updateSwimming();
      if (this.level().isClientSide) {
         this.clearFire();
      } else if (this.remainingFireTicks > 0) {
         if (this.fireImmune()) {
            this.setRemainingFireTicks(this.remainingFireTicks - 4);
            if (this.remainingFireTicks < 0) {
               this.clearFire();
            }
         } else {
            if (this.remainingFireTicks % 20 == 0 && !this.isInLava()) {
               this.hurt(this.damageSources().onFire(), 1.0F);
            }

            this.setRemainingFireTicks(this.remainingFireTicks - 1);
         }

         if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
            this.level().levelEvent((Player)null, 1009, this.blockPosition, 1);
         }
      }

      if (this.isInLava()) {
         this.lavaHurt();
         this.fallDistance *= 0.5F;
      }

      this.checkBelowWorld();
      if (!this.level().isClientSide) {
         this.setSharedFlagOnFire(this.remainingFireTicks > 0);
      }

      this.firstTick = false;
      this.level().getProfiler().pop();
   }

   public void setSharedFlagOnFire(boolean flag) {
      this.setSharedFlag(0, flag || this.hasVisualFire);
   }

   public void checkBelowWorld() {
      if (this.getY() < (double)(this.level().getMinBuildHeight() - 64)) {
         this.onBelowWorld();
      }

   }

   public void setPortalCooldown() {
      this.portalCooldown = this.getDimensionChangingDelay();
   }

   public void setPortalCooldown(int i) {
      this.portalCooldown = i;
   }

   public int getPortalCooldown() {
      return this.portalCooldown;
   }

   public boolean isOnPortalCooldown() {
      return this.portalCooldown > 0;
   }

   protected void processPortalCooldown() {
      if (this.isOnPortalCooldown()) {
         --this.portalCooldown;
      }

   }

   public int getPortalWaitTime() {
      return 0;
   }

   public void lavaHurt() {
      if (!this.fireImmune()) {
         this.setSecondsOnFire(15);
         if (this.hurt(this.damageSources().lava(), 4.0F)) {
            this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
         }

      }
   }

   public void setSecondsOnFire(int i) {
      int j = i * 20;
      if (this instanceof LivingEntity) {
         j = ProtectionEnchantment.getFireAfterDampener((LivingEntity)this, j);
      }

      if (this.remainingFireTicks < j) {
         this.setRemainingFireTicks(j);
      }

   }

   public void setRemainingFireTicks(int i) {
      this.remainingFireTicks = i;
   }

   public int getRemainingFireTicks() {
      return this.remainingFireTicks;
   }

   public void clearFire() {
      this.setRemainingFireTicks(0);
   }

   protected void onBelowWorld() {
      this.discard();
   }

   public boolean isFree(double d0, double d1, double d2) {
      return this.isFree(this.getBoundingBox().move(d0, d1, d2));
   }

   private boolean isFree(AABB aabb) {
      return this.level().noCollision(this, aabb) && !this.level().containsAnyLiquid(aabb);
   }

   public void setOnGround(boolean flag) {
      this.onGround = flag;
      this.checkSupportingBlock(flag, (Vec3)null);
   }

   public void setOnGroundWithKnownMovement(boolean flag, Vec3 vec3) {
      this.onGround = flag;
      this.checkSupportingBlock(flag, vec3);
   }

   public boolean isSupportedBy(BlockPos blockpos) {
      return this.mainSupportingBlockPos.isPresent() && this.mainSupportingBlockPos.get().equals(blockpos);
   }

   protected void checkSupportingBlock(boolean flag, @Nullable Vec3 vec3) {
      if (flag) {
         AABB aabb = this.getBoundingBox();
         AABB aabb1 = new AABB(aabb.minX, aabb.minY - 1.0E-6D, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
         Optional<BlockPos> optional = this.level.findSupportingBlock(this, aabb1);
         if (!optional.isPresent() && !this.onGroundNoBlocks) {
            if (vec3 != null) {
               AABB aabb2 = aabb1.move(-vec3.x, 0.0D, -vec3.z);
               optional = this.level.findSupportingBlock(this, aabb2);
               this.mainSupportingBlockPos = optional;
            }
         } else {
            this.mainSupportingBlockPos = optional;
         }

         this.onGroundNoBlocks = optional.isEmpty();
      } else {
         this.onGroundNoBlocks = false;
         if (this.mainSupportingBlockPos.isPresent()) {
            this.mainSupportingBlockPos = Optional.empty();
         }
      }

   }

   public boolean onGround() {
      return this.onGround;
   }

   public void move(MoverType movertype, Vec3 vec3) {
      if (this.noPhysics) {
         this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
      } else {
         this.wasOnFire = this.isOnFire();
         if (movertype == MoverType.PISTON) {
            vec3 = this.limitPistonMovement(vec3);
            if (vec3.equals(Vec3.ZERO)) {
               return;
            }
         }

         this.level().getProfiler().push("move");
         if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7D) {
            vec3 = vec3.multiply(this.stuckSpeedMultiplier);
            this.stuckSpeedMultiplier = Vec3.ZERO;
            this.setDeltaMovement(Vec3.ZERO);
         }

         vec3 = this.maybeBackOffFromEdge(vec3, movertype);
         Vec3 vec31 = this.collide(vec3);
         double d0 = vec31.lengthSqr();
         if (d0 > 1.0E-7D) {
            if (this.fallDistance != 0.0F && d0 >= 1.0D) {
               BlockHitResult blockhitresult = this.level().clip(new ClipContext(this.position(), this.position().add(vec31), ClipContext.Block.FALLDAMAGE_RESETTING, ClipContext.Fluid.WATER, this));
               if (blockhitresult.getType() != HitResult.Type.MISS) {
                  this.resetFallDistance();
               }
            }

            this.setPos(this.getX() + vec31.x, this.getY() + vec31.y, this.getZ() + vec31.z);
         }

         this.level().getProfiler().pop();
         this.level().getProfiler().push("rest");
         boolean flag = !Mth.equal(vec3.x, vec31.x);
         boolean flag1 = !Mth.equal(vec3.z, vec31.z);
         this.horizontalCollision = flag || flag1;
         this.verticalCollision = vec3.y != vec31.y;
         this.verticalCollisionBelow = this.verticalCollision && vec3.y < 0.0D;
         if (this.horizontalCollision) {
            this.minorHorizontalCollision = this.isHorizontalCollisionMinor(vec31);
         } else {
            this.minorHorizontalCollision = false;
         }

         this.setOnGroundWithKnownMovement(this.verticalCollisionBelow, vec31);
         BlockPos blockpos = this.getOnPosLegacy();
         BlockState blockstate = this.level().getBlockState(blockpos);
         this.checkFallDamage(vec31.y, this.onGround(), blockstate, blockpos);
         if (this.isRemoved()) {
            this.level().getProfiler().pop();
         } else {
            if (this.horizontalCollision) {
               Vec3 vec32 = this.getDeltaMovement();
               this.setDeltaMovement(flag ? 0.0D : vec32.x, vec32.y, flag1 ? 0.0D : vec32.z);
            }

            Block block = blockstate.getBlock();
            if (vec3.y != vec31.y) {
               block.updateEntityAfterFallOn(this.level(), this);
            }

            if (this.onGround()) {
               block.stepOn(this.level(), blockpos, blockstate, this);
            }

            Entity.MovementEmission entity_movementemission = this.getMovementEmission();
            if (entity_movementemission.emitsAnything() && !this.isPassenger()) {
               double d1 = vec31.x;
               double d2 = vec31.y;
               double d3 = vec31.z;
               this.flyDist += (float)(vec31.length() * 0.6D);
               BlockPos blockpos1 = this.getOnPos();
               BlockState blockstate1 = this.level().getBlockState(blockpos1);
               boolean flag2 = this.isStateClimbable(blockstate1);
               if (!flag2) {
                  d2 = 0.0D;
               }

               this.walkDist += (float)vec31.horizontalDistance() * 0.6F;
               this.moveDist += (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3) * 0.6F;
               if (this.moveDist > this.nextStep && !blockstate1.isAir()) {
                  boolean flag3 = blockpos1.equals(blockpos);
                  boolean flag4 = this.vibrationAndSoundEffectsFromBlock(blockpos, blockstate, entity_movementemission.emitsSounds(), flag3, vec3);
                  if (!flag3) {
                     flag4 |= this.vibrationAndSoundEffectsFromBlock(blockpos1, blockstate1, false, entity_movementemission.emitsEvents(), vec3);
                  }

                  if (flag4) {
                     this.nextStep = this.nextStep();
                  } else if (this.isInWater()) {
                     this.nextStep = this.nextStep();
                     if (entity_movementemission.emitsSounds()) {
                        this.waterSwimSound();
                     }

                     if (entity_movementemission.emitsEvents()) {
                        this.gameEvent(GameEvent.SWIM);
                     }
                  }
               } else if (blockstate1.isAir()) {
                  this.processFlappingMovement();
               }
            }

            this.tryCheckInsideBlocks();
            float f = this.getBlockSpeedFactor();
            this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 1.0D, (double)f));
            if (this.level().getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6D)).noneMatch((blockstate2) -> blockstate2.is(BlockTags.FIRE) || blockstate2.is(Blocks.LAVA))) {
               if (this.remainingFireTicks <= 0) {
                  this.setRemainingFireTicks(-this.getFireImmuneTicks());
               }

               if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
                  this.playEntityOnFireExtinguishedSound();
               }
            }

            if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
               this.setRemainingFireTicks(-this.getFireImmuneTicks());
            }

            this.level().getProfiler().pop();
         }
      }
   }

   private boolean isStateClimbable(BlockState blockstate) {
      return blockstate.is(BlockTags.CLIMBABLE) || blockstate.is(Blocks.POWDER_SNOW);
   }

   private boolean vibrationAndSoundEffectsFromBlock(BlockPos blockpos, BlockState blockstate, boolean flag, boolean flag1, Vec3 vec3) {
      if (blockstate.isAir()) {
         return false;
      } else {
         boolean flag2 = this.isStateClimbable(blockstate);
         if ((this.onGround() || flag2 || this.isCrouching() && vec3.y == 0.0D || this.isOnRails()) && !this.isSwimming()) {
            if (flag) {
               this.walkingStepSound(blockpos, blockstate);
            }

            if (flag1) {
               this.level().gameEvent(GameEvent.STEP, this.position(), GameEvent.Context.of(this, blockstate));
            }

            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean isHorizontalCollisionMinor(Vec3 vec3) {
      return false;
   }

   protected void tryCheckInsideBlocks() {
      try {
         this.checkInsideBlocks();
      } catch (Throwable var4) {
         CrashReport crashreport = CrashReport.forThrowable(var4, "Checking entity block collision");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being checked for collision");
         this.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   protected void playEntityOnFireExtinguishedSound() {
      this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   public void extinguishFire() {
      if (!this.level().isClientSide && this.wasOnFire) {
         this.playEntityOnFireExtinguishedSound();
      }

      this.clearFire();
   }

   protected void processFlappingMovement() {
      if (this.isFlapping()) {
         this.onFlap();
         if (this.getMovementEmission().emitsEvents()) {
            this.gameEvent(GameEvent.FLAP);
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public BlockPos getOnPosLegacy() {
      return this.getOnPos(0.2F);
   }

   protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return this.getOnPos(0.500001F);
   }

   public BlockPos getOnPos() {
      return this.getOnPos(1.0E-5F);
   }

   protected BlockPos getOnPos(float f) {
      if (this.mainSupportingBlockPos.isPresent()) {
         BlockPos blockpos = this.mainSupportingBlockPos.get();
         if (!(f > 1.0E-5F)) {
            return blockpos;
         } else {
            BlockState blockstate = this.level().getBlockState(blockpos);
            return (!((double)f <= 0.5D) || !blockstate.is(BlockTags.FENCES)) && !blockstate.is(BlockTags.WALLS) && !(blockstate.getBlock() instanceof FenceGateBlock) ? blockpos.atY(Mth.floor(this.position.y - (double)f)) : blockpos;
         }
      } else {
         int i = Mth.floor(this.position.x);
         int j = Mth.floor(this.position.y - (double)f);
         int k = Mth.floor(this.position.z);
         return new BlockPos(i, j, k);
      }
   }

   protected float getBlockJumpFactor() {
      float f = this.level().getBlockState(this.blockPosition()).getBlock().getJumpFactor();
      float f1 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
      return (double)f == 1.0D ? f1 : f;
   }

   protected float getBlockSpeedFactor() {
      BlockState blockstate = this.level().getBlockState(this.blockPosition());
      float f = blockstate.getBlock().getSpeedFactor();
      if (!blockstate.is(Blocks.WATER) && !blockstate.is(Blocks.BUBBLE_COLUMN)) {
         return (double)f == 1.0D ? this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : f;
      } else {
         return f;
      }
   }

   protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType movertype) {
      return vec3;
   }

   protected Vec3 limitPistonMovement(Vec3 vec3) {
      if (vec3.lengthSqr() <= 1.0E-7D) {
         return vec3;
      } else {
         long i = this.level().getGameTime();
         if (i != this.pistonDeltasGameTime) {
            Arrays.fill(this.pistonDeltas, 0.0D);
            this.pistonDeltasGameTime = i;
         }

         if (vec3.x != 0.0D) {
            double d0 = this.applyPistonMovementRestriction(Direction.Axis.X, vec3.x);
            return Math.abs(d0) <= (double)1.0E-5F ? Vec3.ZERO : new Vec3(d0, 0.0D, 0.0D);
         } else if (vec3.y != 0.0D) {
            double d1 = this.applyPistonMovementRestriction(Direction.Axis.Y, vec3.y);
            return Math.abs(d1) <= (double)1.0E-5F ? Vec3.ZERO : new Vec3(0.0D, d1, 0.0D);
         } else if (vec3.z != 0.0D) {
            double d2 = this.applyPistonMovementRestriction(Direction.Axis.Z, vec3.z);
            return Math.abs(d2) <= (double)1.0E-5F ? Vec3.ZERO : new Vec3(0.0D, 0.0D, d2);
         } else {
            return Vec3.ZERO;
         }
      }
   }

   private double applyPistonMovementRestriction(Direction.Axis direction_axis, double d0) {
      int i = direction_axis.ordinal();
      double d1 = Mth.clamp(d0 + this.pistonDeltas[i], -0.51D, 0.51D);
      d0 = d1 - this.pistonDeltas[i];
      this.pistonDeltas[i] = d1;
      return d0;
   }

   private Vec3 collide(Vec3 vec3) {
      AABB aabb = this.getBoundingBox();
      List<VoxelShape> list = this.level().getEntityCollisions(this, aabb.expandTowards(vec3));
      Vec3 vec31 = vec3.lengthSqr() == 0.0D ? vec3 : collideBoundingBox(this, vec3, aabb, this.level(), list);
      boolean flag = vec3.x != vec31.x;
      boolean flag1 = vec3.y != vec31.y;
      boolean flag2 = vec3.z != vec31.z;
      boolean flag3 = this.onGround() || flag1 && vec3.y < 0.0D;
      if (this.maxUpStep() > 0.0F && flag3 && (flag || flag2)) {
         Vec3 vec32 = collideBoundingBox(this, new Vec3(vec3.x, (double)this.maxUpStep(), vec3.z), aabb, this.level(), list);
         Vec3 vec33 = collideBoundingBox(this, new Vec3(0.0D, (double)this.maxUpStep(), 0.0D), aabb.expandTowards(vec3.x, 0.0D, vec3.z), this.level(), list);
         if (vec33.y < (double)this.maxUpStep()) {
            Vec3 vec34 = collideBoundingBox(this, new Vec3(vec3.x, 0.0D, vec3.z), aabb.move(vec33), this.level(), list).add(vec33);
            if (vec34.horizontalDistanceSqr() > vec32.horizontalDistanceSqr()) {
               vec32 = vec34;
            }
         }

         if (vec32.horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
            return vec32.add(collideBoundingBox(this, new Vec3(0.0D, -vec32.y + vec3.y, 0.0D), aabb.move(vec32), this.level(), list));
         }
      }

      return vec31;
   }

   public static Vec3 collideBoundingBox(@Nullable Entity entity, Vec3 vec3, AABB aabb, Level level, List<VoxelShape> list) {
      ImmutableList.Builder<VoxelShape> immutablelist_builder = ImmutableList.builderWithExpectedSize(list.size() + 1);
      if (!list.isEmpty()) {
         immutablelist_builder.addAll(list);
      }

      WorldBorder worldborder = level.getWorldBorder();
      boolean flag = entity != null && worldborder.isInsideCloseToBorder(entity, aabb.expandTowards(vec3));
      if (flag) {
         immutablelist_builder.add(worldborder.getCollisionShape());
      }

      immutablelist_builder.addAll(level.getBlockCollisions(entity, aabb.expandTowards(vec3)));
      return collideWithShapes(vec3, aabb, immutablelist_builder.build());
   }

   private static Vec3 collideWithShapes(Vec3 vec3, AABB aabb, List<VoxelShape> list) {
      if (list.isEmpty()) {
         return vec3;
      } else {
         double d0 = vec3.x;
         double d1 = vec3.y;
         double d2 = vec3.z;
         if (d1 != 0.0D) {
            d1 = Shapes.collide(Direction.Axis.Y, aabb, list, d1);
            if (d1 != 0.0D) {
               aabb = aabb.move(0.0D, d1, 0.0D);
            }
         }

         boolean flag = Math.abs(d0) < Math.abs(d2);
         if (flag && d2 != 0.0D) {
            d2 = Shapes.collide(Direction.Axis.Z, aabb, list, d2);
            if (d2 != 0.0D) {
               aabb = aabb.move(0.0D, 0.0D, d2);
            }
         }

         if (d0 != 0.0D) {
            d0 = Shapes.collide(Direction.Axis.X, aabb, list, d0);
            if (!flag && d0 != 0.0D) {
               aabb = aabb.move(d0, 0.0D, 0.0D);
            }
         }

         if (!flag && d2 != 0.0D) {
            d2 = Shapes.collide(Direction.Axis.Z, aabb, list, d2);
         }

         return new Vec3(d0, d1, d2);
      }
   }

   protected float nextStep() {
      return (float)((int)this.moveDist + 1);
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.GENERIC_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.GENERIC_SPLASH;
   }

   protected SoundEvent getSwimHighSpeedSplashSound() {
      return SoundEvents.GENERIC_SPLASH;
   }

   protected void checkInsideBlocks() {
      AABB aabb = this.getBoundingBox();
      BlockPos blockpos = BlockPos.containing(aabb.minX + 1.0E-7D, aabb.minY + 1.0E-7D, aabb.minZ + 1.0E-7D);
      BlockPos blockpos1 = BlockPos.containing(aabb.maxX - 1.0E-7D, aabb.maxY - 1.0E-7D, aabb.maxZ - 1.0E-7D);
      if (this.level().hasChunksAt(blockpos, blockpos1)) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
            for(int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
               for(int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                  blockpos_mutableblockpos.set(i, j, k);
                  BlockState blockstate = this.level().getBlockState(blockpos_mutableblockpos);

                  try {
                     blockstate.entityInside(this.level(), blockpos_mutableblockpos, this);
                     this.onInsideBlock(blockstate);
                  } catch (Throwable var12) {
                     CrashReport crashreport = CrashReport.forThrowable(var12, "Colliding entity with block");
                     CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                     CrashReportCategory.populateBlockDetails(crashreportcategory, this.level(), blockpos_mutableblockpos, blockstate);
                     throw new ReportedException(crashreport);
                  }
               }
            }
         }
      }

   }

   protected void onInsideBlock(BlockState blockstate) {
   }

   public void gameEvent(GameEvent gameevent, @Nullable Entity entity) {
      this.level().gameEvent(entity, gameevent, this.position);
   }

   public void gameEvent(GameEvent gameevent) {
      this.gameEvent(gameevent, this);
   }

   private void walkingStepSound(BlockPos blockpos, BlockState blockstate) {
      this.playStepSound(blockpos, blockstate);
      if (this.shouldPlayAmethystStepSound(blockstate)) {
         this.playAmethystStepSound();
      }

   }

   protected void waterSwimSound() {
      Entity entity = (Entity)(this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this);
      float f = entity == this ? 0.35F : 0.4F;
      Vec3 vec3 = entity.getDeltaMovement();
      float f1 = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * (double)0.2F + vec3.y * vec3.y + vec3.z * vec3.z * (double)0.2F) * f);
      this.playSwimSound(f1);
   }

   protected BlockPos getPrimaryStepSoundBlockPos(BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.above();
      BlockState blockstate = this.level().getBlockState(blockpos1);
      return !blockstate.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) && !blockstate.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS) ? blockpos : blockpos1;
   }

   protected void playCombinationStepSounds(BlockState blockstate, BlockState blockstate1) {
      SoundType soundtype = blockstate.getSoundType();
      this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
      this.playMuffledStepSound(blockstate1);
   }

   protected void playMuffledStepSound(BlockState blockstate) {
      SoundType soundtype = blockstate.getSoundType();
      this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.05F, soundtype.getPitch() * 0.8F);
   }

   protected void playStepSound(BlockPos blockpos, BlockState blockstate) {
      SoundType soundtype = blockstate.getSoundType();
      this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
   }

   private boolean shouldPlayAmethystStepSound(BlockState blockstate) {
      return blockstate.is(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.tickCount >= this.lastCrystalSoundPlayTick + 20;
   }

   private void playAmethystStepSound() {
      this.crystalSoundIntensity *= (float)Math.pow(0.997D, (double)(this.tickCount - this.lastCrystalSoundPlayTick));
      this.crystalSoundIntensity = Math.min(1.0F, this.crystalSoundIntensity + 0.07F);
      float f = 0.5F + this.crystalSoundIntensity * this.random.nextFloat() * 1.2F;
      float f1 = 0.1F + this.crystalSoundIntensity * 1.2F;
      this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, f1, f);
      this.lastCrystalSoundPlayTick = this.tickCount;
   }

   protected void playSwimSound(float f) {
      this.playSound(this.getSwimSound(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   protected void onFlap() {
   }

   protected boolean isFlapping() {
      return false;
   }

   public void playSound(SoundEvent soundevent, float f, float f1) {
      if (!this.isSilent()) {
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), f, f1);
      }

   }

   public void playSound(SoundEvent soundevent) {
      if (!this.isSilent()) {
         this.playSound(soundevent, 1.0F, 1.0F);
      }

   }

   public boolean isSilent() {
      return this.entityData.get(DATA_SILENT);
   }

   public void setSilent(boolean flag) {
      this.entityData.set(DATA_SILENT, flag);
   }

   public boolean isNoGravity() {
      return this.entityData.get(DATA_NO_GRAVITY);
   }

   public void setNoGravity(boolean flag) {
      this.entityData.set(DATA_NO_GRAVITY, flag);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.ALL;
   }

   public boolean dampensVibrations() {
      return false;
   }

   protected void checkFallDamage(double d0, boolean flag, BlockState blockstate, BlockPos blockpos) {
      if (flag) {
         if (this.fallDistance > 0.0F) {
            blockstate.getBlock().fallOn(this.level(), blockstate, blockpos, this, this.fallDistance);
            this.level().gameEvent(GameEvent.HIT_GROUND, this.position, GameEvent.Context.of(this, this.mainSupportingBlockPos.map((blockpos1) -> this.level().getBlockState(blockpos1)).orElse(blockstate)));
         }

         this.resetFallDistance();
      } else if (d0 < 0.0D) {
         this.fallDistance -= (float)d0;
      }

   }

   public boolean fireImmune() {
      return this.getType().fireImmune();
   }

   public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
      if (this.type.is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
         return false;
      } else {
         if (this.isVehicle()) {
            for(Entity entity : this.getPassengers()) {
               entity.causeFallDamage(f, f1, damagesource);
            }
         }

         return false;
      }
   }

   public boolean isInWater() {
      return this.wasTouchingWater;
   }

   private boolean isInRain() {
      BlockPos blockpos = this.blockPosition();
      return this.level().isRainingAt(blockpos) || this.level().isRainingAt(BlockPos.containing((double)blockpos.getX(), this.getBoundingBox().maxY, (double)blockpos.getZ()));
   }

   private boolean isInBubbleColumn() {
      return this.level().getBlockState(this.blockPosition()).is(Blocks.BUBBLE_COLUMN);
   }

   public boolean isInWaterOrRain() {
      return this.isInWater() || this.isInRain();
   }

   public boolean isInWaterRainOrBubble() {
      return this.isInWater() || this.isInRain() || this.isInBubbleColumn();
   }

   public boolean isInWaterOrBubble() {
      return this.isInWater() || this.isInBubbleColumn();
   }

   public boolean isUnderWater() {
      return this.wasEyeInWater && this.isInWater();
   }

   public void updateSwimming() {
      if (this.isSwimming()) {
         this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
      } else {
         this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger() && this.level().getFluidState(this.blockPosition).is(FluidTags.WATER));
      }

   }

   protected boolean updateInWaterStateAndDoFluidPushing() {
      this.fluidHeight.clear();
      this.updateInWaterStateAndDoWaterCurrentPushing();
      double d0 = this.level().dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
      boolean flag = this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, d0);
      return this.isInWater() || flag;
   }

   void updateInWaterStateAndDoWaterCurrentPushing() {
      Entity var2 = this.getVehicle();
      if (var2 instanceof Boat boat) {
         if (!boat.isUnderWater()) {
            this.wasTouchingWater = false;
            return;
         }
      }

      if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014D)) {
         if (!this.wasTouchingWater && !this.firstTick) {
            this.doWaterSplashEffect();
         }

         this.resetFallDistance();
         this.wasTouchingWater = true;
         this.clearFire();
      } else {
         this.wasTouchingWater = false;
      }

   }

   private void updateFluidOnEyes() {
      this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
      this.fluidOnEyes.clear();
      double d0 = this.getEyeY() - (double)0.11111111F;
      Entity entity = this.getVehicle();
      if (entity instanceof Boat boat) {
         if (!boat.isUnderWater() && boat.getBoundingBox().maxY >= d0 && boat.getBoundingBox().minY <= d0) {
            return;
         }
      }

      BlockPos blockpos = BlockPos.containing(this.getX(), d0, this.getZ());
      FluidState fluidstate = this.level().getFluidState(blockpos);
      double d1 = (double)((float)blockpos.getY() + fluidstate.getHeight(this.level(), blockpos));
      if (d1 > d0) {
         fluidstate.getTags().forEach(this.fluidOnEyes::add);
      }

   }

   protected void doWaterSplashEffect() {
      Entity entity = (Entity)(this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this);
      float f = entity == this ? 0.2F : 0.9F;
      Vec3 vec3 = entity.getDeltaMovement();
      float f1 = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * (double)0.2F + vec3.y * vec3.y + vec3.z * vec3.z * (double)0.2F) * f);
      if (f1 < 0.25F) {
         this.playSound(this.getSwimSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      } else {
         this.playSound(this.getSwimHighSpeedSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      }

      float f2 = (float)Mth.floor(this.getY());

      for(int i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; ++i) {
         double d0 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         double d1 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d0, (double)(f2 + 1.0F), this.getZ() + d1, vec3.x, vec3.y - this.random.nextDouble() * (double)0.2F, vec3.z);
      }

      for(int j = 0; (float)j < 1.0F + this.dimensions.width * 20.0F; ++j) {
         double d2 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         this.level().addParticle(ParticleTypes.SPLASH, this.getX() + d2, (double)(f2 + 1.0F), this.getZ() + d3, vec3.x, vec3.y, vec3.z);
      }

      this.gameEvent(GameEvent.SPLASH);
   }

   /** @deprecated */
   @Deprecated
   protected BlockState getBlockStateOnLegacy() {
      return this.level().getBlockState(this.getOnPosLegacy());
   }

   public BlockState getBlockStateOn() {
      return this.level().getBlockState(this.getOnPos());
   }

   public boolean canSpawnSprintParticle() {
      return this.isSprinting() && !this.isInWater() && !this.isSpectator() && !this.isCrouching() && !this.isInLava() && this.isAlive();
   }

   protected void spawnSprintParticle() {
      BlockPos blockpos = this.getOnPosLegacy();
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         Vec3 vec3 = this.getDeltaMovement();
         BlockPos blockpos1 = this.blockPosition();
         double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width;
         double d1 = this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width;
         if (blockpos1.getX() != blockpos.getX()) {
            d0 = Mth.clamp(d0, (double)blockpos.getX(), (double)blockpos.getX() + 1.0D);
         }

         if (blockpos1.getZ() != blockpos.getZ()) {
            d1 = Mth.clamp(d1, (double)blockpos.getZ(), (double)blockpos.getZ() + 1.0D);
         }

         this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), d0, this.getY() + 0.1D, d1, vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
      }

   }

   public boolean isEyeInFluid(TagKey<Fluid> tagkey) {
      return this.fluidOnEyes.contains(tagkey);
   }

   public boolean isInLava() {
      return !this.firstTick && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0D;
   }

   public void moveRelative(float f, Vec3 vec3) {
      Vec3 vec31 = getInputVector(vec3, f, this.getYRot());
      this.setDeltaMovement(this.getDeltaMovement().add(vec31));
   }

   private static Vec3 getInputVector(Vec3 vec3, float f, float f1) {
      double d0 = vec3.lengthSqr();
      if (d0 < 1.0E-7D) {
         return Vec3.ZERO;
      } else {
         Vec3 vec31 = (d0 > 1.0D ? vec3.normalize() : vec3).scale((double)f);
         float f2 = Mth.sin(f1 * ((float)Math.PI / 180F));
         float f3 = Mth.cos(f1 * ((float)Math.PI / 180F));
         return new Vec3(vec31.x * (double)f3 - vec31.z * (double)f2, vec31.y, vec31.z * (double)f3 + vec31.x * (double)f2);
      }
   }

   /** @deprecated */
   @Deprecated
   public float getLightLevelDependentMagicValue() {
      return this.level().hasChunkAt(this.getBlockX(), this.getBlockZ()) ? this.level().getLightLevelDependentMagicValue(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())) : 0.0F;
   }

   public void absMoveTo(double d0, double d1, double d2, float f, float f1) {
      this.absMoveTo(d0, d1, d2);
      this.setYRot(f % 360.0F);
      this.setXRot(Mth.clamp(f1, -90.0F, 90.0F) % 360.0F);
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   public void absMoveTo(double d0, double d1, double d2) {
      double d3 = Mth.clamp(d0, -3.0E7D, 3.0E7D);
      double d4 = Mth.clamp(d2, -3.0E7D, 3.0E7D);
      this.xo = d3;
      this.yo = d1;
      this.zo = d4;
      this.setPos(d3, d1, d4);
   }

   public void moveTo(Vec3 vec3) {
      this.moveTo(vec3.x, vec3.y, vec3.z);
   }

   public void moveTo(double d0, double d1, double d2) {
      this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
   }

   public void moveTo(BlockPos blockpos, float f, float f1) {
      this.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, f, f1);
   }

   public void moveTo(double d0, double d1, double d2, float f, float f1) {
      this.setPosRaw(d0, d1, d2);
      this.setYRot(f);
      this.setXRot(f1);
      this.setOldPosAndRot();
      this.reapplyPosition();
   }

   public final void setOldPosAndRot() {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      this.xo = d0;
      this.yo = d1;
      this.zo = d2;
      this.xOld = d0;
      this.yOld = d1;
      this.zOld = d2;
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   public float distanceTo(Entity entity) {
      float f = (float)(this.getX() - entity.getX());
      float f1 = (float)(this.getY() - entity.getY());
      float f2 = (float)(this.getZ() - entity.getZ());
      return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public double distanceToSqr(double d0, double d1, double d2) {
      double d3 = this.getX() - d0;
      double d4 = this.getY() - d1;
      double d5 = this.getZ() - d2;
      return d3 * d3 + d4 * d4 + d5 * d5;
   }

   public double distanceToSqr(Entity entity) {
      return this.distanceToSqr(entity.position());
   }

   public double distanceToSqr(Vec3 vec3) {
      double d0 = this.getX() - vec3.x;
      double d1 = this.getY() - vec3.y;
      double d2 = this.getZ() - vec3.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public void playerTouch(Player player) {
   }

   public void push(Entity entity) {
      if (!this.isPassengerOfSameVehicle(entity)) {
         if (!entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getX();
            double d1 = entity.getZ() - this.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= (double)0.01F) {
               d2 = Math.sqrt(d2);
               d0 /= d2;
               d1 /= d2;
               double d3 = 1.0D / d2;
               if (d3 > 1.0D) {
                  d3 = 1.0D;
               }

               d0 *= d3;
               d1 *= d3;
               d0 *= (double)0.05F;
               d1 *= (double)0.05F;
               if (!this.isVehicle() && this.isPushable()) {
                  this.push(-d0, 0.0D, -d1);
               }

               if (!entity.isVehicle() && entity.isPushable()) {
                  entity.push(d0, 0.0D, d1);
               }
            }

         }
      }
   }

   public void push(double d0, double d1, double d2) {
      this.setDeltaMovement(this.getDeltaMovement().add(d0, d1, d2));
      this.hasImpulse = true;
   }

   protected void markHurt() {
      this.hurtMarked = true;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else {
         this.markHurt();
         return false;
      }
   }

   public final Vec3 getViewVector(float f) {
      return this.calculateViewVector(this.getViewXRot(f), this.getViewYRot(f));
   }

   public float getViewXRot(float f) {
      return f == 1.0F ? this.getXRot() : Mth.lerp(f, this.xRotO, this.getXRot());
   }

   public float getViewYRot(float f) {
      return f == 1.0F ? this.getYRot() : Mth.lerp(f, this.yRotO, this.getYRot());
   }

   protected final Vec3 calculateViewVector(float f, float f1) {
      float f2 = f * ((float)Math.PI / 180F);
      float f3 = -f1 * ((float)Math.PI / 180F);
      float f4 = Mth.cos(f3);
      float f5 = Mth.sin(f3);
      float f6 = Mth.cos(f2);
      float f7 = Mth.sin(f2);
      return new Vec3((double)(f5 * f6), (double)(-f7), (double)(f4 * f6));
   }

   public final Vec3 getUpVector(float f) {
      return this.calculateUpVector(this.getViewXRot(f), this.getViewYRot(f));
   }

   protected final Vec3 calculateUpVector(float f, float f1) {
      return this.calculateViewVector(f - 90.0F, f1);
   }

   public final Vec3 getEyePosition() {
      return new Vec3(this.getX(), this.getEyeY(), this.getZ());
   }

   public final Vec3 getEyePosition(float f) {
      double d0 = Mth.lerp((double)f, this.xo, this.getX());
      double d1 = Mth.lerp((double)f, this.yo, this.getY()) + (double)this.getEyeHeight();
      double d2 = Mth.lerp((double)f, this.zo, this.getZ());
      return new Vec3(d0, d1, d2);
   }

   public Vec3 getLightProbePosition(float f) {
      return this.getEyePosition(f);
   }

   public final Vec3 getPosition(float f) {
      double d0 = Mth.lerp((double)f, this.xo, this.getX());
      double d1 = Mth.lerp((double)f, this.yo, this.getY());
      double d2 = Mth.lerp((double)f, this.zo, this.getZ());
      return new Vec3(d0, d1, d2);
   }

   public HitResult pick(double d0, float f, boolean flag) {
      Vec3 vec3 = this.getEyePosition(f);
      Vec3 vec31 = this.getViewVector(f);
      Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
      return this.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, flag ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
   }

   public boolean canBeHitByProjectile() {
      return this.isAlive() && this.isPickable();
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public void awardKillScore(Entity entity, int i, DamageSource damagesource) {
      if (entity instanceof ServerPlayer) {
         CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayer)entity, this, damagesource);
      }

   }

   public boolean shouldRender(double d0, double d1, double d2) {
      double d3 = this.getX() - d0;
      double d4 = this.getY() - d1;
      double d5 = this.getZ() - d2;
      double d6 = d3 * d3 + d4 * d4 + d5 * d5;
      return this.shouldRenderAtSqrDistance(d6);
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      double d1 = this.getBoundingBox().getSize();
      if (Double.isNaN(d1)) {
         d1 = 1.0D;
      }

      d1 *= 64.0D * viewScale;
      return d0 < d1 * d1;
   }

   public boolean saveAsPassenger(CompoundTag compoundtag) {
      if (this.removalReason != null && !this.removalReason.shouldSave()) {
         return false;
      } else {
         String s = this.getEncodeId();
         if (s == null) {
            return false;
         } else {
            compoundtag.putString("id", s);
            this.saveWithoutId(compoundtag);
            return true;
         }
      }
   }

   public boolean save(CompoundTag compoundtag) {
      return this.isPassenger() ? false : this.saveAsPassenger(compoundtag);
   }

   public CompoundTag saveWithoutId(CompoundTag compoundtag) {
      try {
         if (this.vehicle != null) {
            compoundtag.put("Pos", this.newDoubleList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
         } else {
            compoundtag.put("Pos", this.newDoubleList(this.getX(), this.getY(), this.getZ()));
         }

         Vec3 vec3 = this.getDeltaMovement();
         compoundtag.put("Motion", this.newDoubleList(vec3.x, vec3.y, vec3.z));
         compoundtag.put("Rotation", this.newFloatList(this.getYRot(), this.getXRot()));
         compoundtag.putFloat("FallDistance", this.fallDistance);
         compoundtag.putShort("Fire", (short)this.remainingFireTicks);
         compoundtag.putShort("Air", (short)this.getAirSupply());
         compoundtag.putBoolean("OnGround", this.onGround());
         compoundtag.putBoolean("Invulnerable", this.invulnerable);
         compoundtag.putInt("PortalCooldown", this.portalCooldown);
         compoundtag.putUUID("UUID", this.getUUID());
         Component component = this.getCustomName();
         if (component != null) {
            compoundtag.putString("CustomName", Component.Serializer.toJson(component));
         }

         if (this.isCustomNameVisible()) {
            compoundtag.putBoolean("CustomNameVisible", this.isCustomNameVisible());
         }

         if (this.isSilent()) {
            compoundtag.putBoolean("Silent", this.isSilent());
         }

         if (this.isNoGravity()) {
            compoundtag.putBoolean("NoGravity", this.isNoGravity());
         }

         if (this.hasGlowingTag) {
            compoundtag.putBoolean("Glowing", true);
         }

         int i = this.getTicksFrozen();
         if (i > 0) {
            compoundtag.putInt("TicksFrozen", this.getTicksFrozen());
         }

         if (this.hasVisualFire) {
            compoundtag.putBoolean("HasVisualFire", this.hasVisualFire);
         }

         if (!this.tags.isEmpty()) {
            ListTag listtag = new ListTag();

            for(String s : this.tags) {
               listtag.add(StringTag.valueOf(s));
            }

            compoundtag.put("Tags", listtag);
         }

         this.addAdditionalSaveData(compoundtag);
         if (this.isVehicle()) {
            ListTag listtag1 = new ListTag();

            for(Entity entity : this.getPassengers()) {
               CompoundTag compoundtag1 = new CompoundTag();
               if (entity.saveAsPassenger(compoundtag1)) {
                  listtag1.add(compoundtag1);
               }
            }

            if (!listtag1.isEmpty()) {
               compoundtag.put("Passengers", listtag1);
            }
         }

         return compoundtag;
      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.forThrowable(var9, "Saving entity NBT");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being saved");
         this.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   public void load(CompoundTag compoundtag) {
      try {
         ListTag listtag = compoundtag.getList("Pos", 6);
         ListTag listtag1 = compoundtag.getList("Motion", 6);
         ListTag listtag2 = compoundtag.getList("Rotation", 5);
         double d0 = listtag1.getDouble(0);
         double d1 = listtag1.getDouble(1);
         double d2 = listtag1.getDouble(2);
         this.setDeltaMovement(Math.abs(d0) > 10.0D ? 0.0D : d0, Math.abs(d1) > 10.0D ? 0.0D : d1, Math.abs(d2) > 10.0D ? 0.0D : d2);
         double d3 = 3.0000512E7D;
         this.setPosRaw(Mth.clamp(listtag.getDouble(0), -3.0000512E7D, 3.0000512E7D), Mth.clamp(listtag.getDouble(1), -2.0E7D, 2.0E7D), Mth.clamp(listtag.getDouble(2), -3.0000512E7D, 3.0000512E7D));
         this.setYRot(listtag2.getFloat(0));
         this.setXRot(listtag2.getFloat(1));
         this.setOldPosAndRot();
         this.setYHeadRot(this.getYRot());
         this.setYBodyRot(this.getYRot());
         this.fallDistance = compoundtag.getFloat("FallDistance");
         this.remainingFireTicks = compoundtag.getShort("Fire");
         if (compoundtag.contains("Air")) {
            this.setAirSupply(compoundtag.getShort("Air"));
         }

         this.onGround = compoundtag.getBoolean("OnGround");
         this.invulnerable = compoundtag.getBoolean("Invulnerable");
         this.portalCooldown = compoundtag.getInt("PortalCooldown");
         if (compoundtag.hasUUID("UUID")) {
            this.uuid = compoundtag.getUUID("UUID");
            this.stringUUID = this.uuid.toString();
         }

         if (Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ())) {
            if (Double.isFinite((double)this.getYRot()) && Double.isFinite((double)this.getXRot())) {
               this.reapplyPosition();
               this.setRot(this.getYRot(), this.getXRot());
               if (compoundtag.contains("CustomName", 8)) {
                  String s = compoundtag.getString("CustomName");

                  try {
                     this.setCustomName(Component.Serializer.fromJson(s));
                  } catch (Exception var16) {
                     LOGGER.warn("Failed to parse entity custom name {}", s, var16);
                  }
               }

               this.setCustomNameVisible(compoundtag.getBoolean("CustomNameVisible"));
               this.setSilent(compoundtag.getBoolean("Silent"));
               this.setNoGravity(compoundtag.getBoolean("NoGravity"));
               this.setGlowingTag(compoundtag.getBoolean("Glowing"));
               this.setTicksFrozen(compoundtag.getInt("TicksFrozen"));
               this.hasVisualFire = compoundtag.getBoolean("HasVisualFire");
               if (compoundtag.contains("Tags", 9)) {
                  this.tags.clear();
                  ListTag listtag3 = compoundtag.getList("Tags", 8);
                  int i = Math.min(listtag3.size(), 1024);

                  for(int j = 0; j < i; ++j) {
                     this.tags.add(listtag3.getString(j));
                  }
               }

               this.readAdditionalSaveData(compoundtag);
               if (this.repositionEntityAfterLoad()) {
                  this.reapplyPosition();
               }

            } else {
               throw new IllegalStateException("Entity has invalid rotation");
            }
         } else {
            throw new IllegalStateException("Entity has invalid position");
         }
      } catch (Throwable var17) {
         CrashReport crashreport = CrashReport.forThrowable(var17, "Loading entity NBT");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being loaded");
         this.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   protected boolean repositionEntityAfterLoad() {
      return true;
   }

   @Nullable
   protected final String getEncodeId() {
      EntityType<?> entitytype = this.getType();
      ResourceLocation resourcelocation = EntityType.getKey(entitytype);
      return entitytype.canSerialize() && resourcelocation != null ? resourcelocation.toString() : null;
   }

   protected abstract void readAdditionalSaveData(CompoundTag compoundtag);

   protected abstract void addAdditionalSaveData(CompoundTag compoundtag);

   protected ListTag newDoubleList(double... adouble) {
      ListTag listtag = new ListTag();

      for(double d0 : adouble) {
         listtag.add(DoubleTag.valueOf(d0));
      }

      return listtag;
   }

   protected ListTag newFloatList(float... afloat) {
      ListTag listtag = new ListTag();

      for(float f : afloat) {
         listtag.add(FloatTag.valueOf(f));
      }

      return listtag;
   }

   @Nullable
   public ItemEntity spawnAtLocation(ItemLike itemlike) {
      return this.spawnAtLocation(itemlike, 0);
   }

   @Nullable
   public ItemEntity spawnAtLocation(ItemLike itemlike, int i) {
      return this.spawnAtLocation(new ItemStack(itemlike), (float)i);
   }

   @Nullable
   public ItemEntity spawnAtLocation(ItemStack itemstack) {
      return this.spawnAtLocation(itemstack, 0.0F);
   }

   @Nullable
   public ItemEntity spawnAtLocation(ItemStack itemstack, float f) {
      if (itemstack.isEmpty()) {
         return null;
      } else if (this.level().isClientSide) {
         return null;
      } else {
         ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY() + (double)f, this.getZ(), itemstack);
         itementity.setDefaultPickUpDelay();
         this.level().addFreshEntity(itementity);
         return itementity;
      }
   }

   public boolean isAlive() {
      return !this.isRemoved();
   }

   public boolean isInWall() {
      if (this.noPhysics) {
         return false;
      } else {
         float f = this.dimensions.width * 0.8F;
         AABB aabb = AABB.ofSize(this.getEyePosition(), (double)f, 1.0E-6D, (double)f);
         return BlockPos.betweenClosedStream(aabb).anyMatch((blockpos) -> {
            BlockState blockstate = this.level().getBlockState(blockpos);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level(), blockpos) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level(), blockpos).move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()), Shapes.create(aabb), BooleanOp.AND);
         });
      }
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      return InteractionResult.PASS;
   }

   public boolean canCollideWith(Entity entity) {
      return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public void rideTick() {
      this.setDeltaMovement(Vec3.ZERO);
      this.tick();
      if (this.isPassenger()) {
         this.getVehicle().positionRider(this);
      }
   }

   public final void positionRider(Entity entity) {
      this.positionRider(entity, Entity::setPos);
   }

   protected void positionRider(Entity entity, Entity.MoveFunction entity_movefunction) {
      if (this.hasPassenger(entity)) {
         double d0 = this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset();
         entity_movefunction.accept(entity, this.getX(), d0, this.getZ());
      }
   }

   public void onPassengerTurned(Entity entity) {
   }

   public double getMyRidingOffset() {
      return 0.0D;
   }

   public double getPassengersRidingOffset() {
      return (double)this.dimensions.height * 0.75D;
   }

   public boolean startRiding(Entity entity) {
      return this.startRiding(entity, false);
   }

   public boolean showVehicleHealth() {
      return this instanceof LivingEntity;
   }

   public boolean startRiding(Entity entity, boolean flag) {
      if (entity == this.vehicle) {
         return false;
      } else if (!entity.couldAcceptPassenger()) {
         return false;
      } else {
         for(Entity entity1 = entity; entity1.vehicle != null; entity1 = entity1.vehicle) {
            if (entity1.vehicle == this) {
               return false;
            }
         }

         if (flag || this.canRide(entity) && entity.canAddPassenger(this)) {
            if (this.isPassenger()) {
               this.stopRiding();
            }

            this.setPose(Pose.STANDING);
            this.vehicle = entity;
            this.vehicle.addPassenger(this);
            entity.getIndirectPassengersStream().filter((entity3) -> entity3 instanceof ServerPlayer).forEach((entity2) -> CriteriaTriggers.START_RIDING_TRIGGER.trigger((ServerPlayer)entity2));
            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean canRide(Entity entity) {
      return !this.isShiftKeyDown() && this.boardingCooldown <= 0;
   }

   protected boolean canEnterPose(Pose pose) {
      return this.level().noCollision(this, this.getBoundingBoxForPose(pose).deflate(1.0E-7D));
   }

   public void ejectPassengers() {
      for(int i = this.passengers.size() - 1; i >= 0; --i) {
         this.passengers.get(i).stopRiding();
      }

   }

   public void removeVehicle() {
      if (this.vehicle != null) {
         Entity entity = this.vehicle;
         this.vehicle = null;
         entity.removePassenger(this);
      }

   }

   public void stopRiding() {
      this.removeVehicle();
   }

   protected void addPassenger(Entity entity) {
      if (entity.getVehicle() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         if (this.passengers.isEmpty()) {
            this.passengers = ImmutableList.of(entity);
         } else {
            List<Entity> list = Lists.newArrayList(this.passengers);
            if (!this.level().isClientSide && entity instanceof Player && !(this.getFirstPassenger() instanceof Player)) {
               list.add(0, entity);
            } else {
               list.add(entity);
            }

            this.passengers = ImmutableList.copyOf(list);
         }

         this.gameEvent(GameEvent.ENTITY_MOUNT, entity);
      }
   }

   protected void removePassenger(Entity entity) {
      if (entity.getVehicle() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         if (this.passengers.size() == 1 && this.passengers.get(0) == entity) {
            this.passengers = ImmutableList.of();
         } else {
            this.passengers = this.passengers.stream().filter((entity2) -> entity2 != entity).collect(ImmutableList.toImmutableList());
         }

         entity.boardingCooldown = 60;
         this.gameEvent(GameEvent.ENTITY_DISMOUNT, entity);
      }
   }

   protected boolean canAddPassenger(Entity entity) {
      return this.passengers.isEmpty();
   }

   protected boolean couldAcceptPassenger() {
      return true;
   }

   public void lerpTo(double d0, double d1, double d2, float f, float f1, int i, boolean flag) {
      this.setPos(d0, d1, d2);
      this.setRot(f, f1);
   }

   public void lerpHeadTo(float f, int i) {
      this.setYHeadRot(f);
   }

   public float getPickRadius() {
      return 0.0F;
   }

   public Vec3 getLookAngle() {
      return this.calculateViewVector(this.getXRot(), this.getYRot());
   }

   public Vec3 getHandHoldingItemAngle(Item item) {
      if (!(this instanceof Player player)) {
         return Vec3.ZERO;
      } else {
         boolean flag = player.getOffhandItem().is(item) && !player.getMainHandItem().is(item);
         HumanoidArm humanoidarm = flag ? player.getMainArm().getOpposite() : player.getMainArm();
         return this.calculateViewVector(0.0F, this.getYRot() + (float)(humanoidarm == HumanoidArm.RIGHT ? 80 : -80)).scale(0.5D);
      }
   }

   public Vec2 getRotationVector() {
      return new Vec2(this.getXRot(), this.getYRot());
   }

   public Vec3 getForward() {
      return Vec3.directionFromRotation(this.getRotationVector());
   }

   public void handleInsidePortal(BlockPos blockpos) {
      if (this.isOnPortalCooldown()) {
         this.setPortalCooldown();
      } else {
         if (!this.level().isClientSide && !blockpos.equals(this.portalEntrancePos)) {
            this.portalEntrancePos = blockpos.immutable();
         }

         this.isInsidePortal = true;
      }
   }

   protected void handleNetherPortal() {
      if (this.level() instanceof ServerLevel) {
         int i = this.getPortalWaitTime();
         ServerLevel serverlevel = (ServerLevel)this.level();
         if (this.isInsidePortal) {
            MinecraftServer minecraftserver = serverlevel.getServer();
            ResourceKey<Level> resourcekey = this.level().dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
            ServerLevel serverlevel1 = minecraftserver.getLevel(resourcekey);
            if (serverlevel1 != null && minecraftserver.isNetherEnabled() && !this.isPassenger() && this.portalTime++ >= i) {
               this.level().getProfiler().push("portal");
               this.portalTime = i;
               this.setPortalCooldown();
               this.changeDimension(serverlevel1);
               this.level().getProfiler().pop();
            }

            this.isInsidePortal = false;
         } else {
            if (this.portalTime > 0) {
               this.portalTime -= 4;
            }

            if (this.portalTime < 0) {
               this.portalTime = 0;
            }
         }

         this.processPortalCooldown();
      }
   }

   public int getDimensionChangingDelay() {
      return 300;
   }

   public void lerpMotion(double d0, double d1, double d2) {
      this.setDeltaMovement(d0, d1, d2);
   }

   public void handleDamageEvent(DamageSource damagesource) {
   }

   public void handleEntityEvent(byte b0) {
      switch (b0) {
         case 53:
            HoneyBlock.showSlideParticles(this);
         default:
      }
   }

   public void animateHurt(float f) {
   }

   public Iterable<ItemStack> getHandSlots() {
      return EMPTY_LIST;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return EMPTY_LIST;
   }

   public Iterable<ItemStack> getAllSlots() {
      return Iterables.concat(this.getHandSlots(), this.getArmorSlots());
   }

   public void setItemSlot(EquipmentSlot equipmentslot, ItemStack itemstack) {
   }

   public boolean isOnFire() {
      boolean flag = this.level() != null && this.level().isClientSide;
      return !this.fireImmune() && (this.remainingFireTicks > 0 || flag && this.getSharedFlag(0));
   }

   public boolean isPassenger() {
      return this.getVehicle() != null;
   }

   public boolean isVehicle() {
      return !this.passengers.isEmpty();
   }

   public boolean dismountsUnderwater() {
      return this.getType().is(EntityTypeTags.DISMOUNTS_UNDERWATER);
   }

   public void setShiftKeyDown(boolean flag) {
      this.setSharedFlag(1, flag);
   }

   public boolean isShiftKeyDown() {
      return this.getSharedFlag(1);
   }

   public boolean isSteppingCarefully() {
      return this.isShiftKeyDown();
   }

   public boolean isSuppressingBounce() {
      return this.isShiftKeyDown();
   }

   public boolean isDiscrete() {
      return this.isShiftKeyDown();
   }

   public boolean isDescending() {
      return this.isShiftKeyDown();
   }

   public boolean isCrouching() {
      return this.hasPose(Pose.CROUCHING);
   }

   public boolean isSprinting() {
      return this.getSharedFlag(3);
   }

   public void setSprinting(boolean flag) {
      this.setSharedFlag(3, flag);
   }

   public boolean isSwimming() {
      return this.getSharedFlag(4);
   }

   public boolean isVisuallySwimming() {
      return this.hasPose(Pose.SWIMMING);
   }

   public boolean isVisuallyCrawling() {
      return this.isVisuallySwimming() && !this.isInWater();
   }

   public void setSwimming(boolean flag) {
      this.setSharedFlag(4, flag);
   }

   public final boolean hasGlowingTag() {
      return this.hasGlowingTag;
   }

   public final void setGlowingTag(boolean flag) {
      this.hasGlowingTag = flag;
      this.setSharedFlag(6, this.isCurrentlyGlowing());
   }

   public boolean isCurrentlyGlowing() {
      return this.level().isClientSide() ? this.getSharedFlag(6) : this.hasGlowingTag;
   }

   public boolean isInvisible() {
      return this.getSharedFlag(5);
   }

   public boolean isInvisibleTo(Player player) {
      if (player.isSpectator()) {
         return false;
      } else {
         Team team = this.getTeam();
         return team != null && player != null && player.getTeam() == team && team.canSeeFriendlyInvisibles() ? false : this.isInvisible();
      }
   }

   public boolean isOnRails() {
      return false;
   }

   public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biconsumer) {
   }

   @Nullable
   public Team getTeam() {
      return this.level().getScoreboard().getPlayersTeam(this.getScoreboardName());
   }

   public boolean isAlliedTo(Entity entity) {
      return this.isAlliedTo(entity.getTeam());
   }

   public boolean isAlliedTo(Team team) {
      return this.getTeam() != null ? this.getTeam().isAlliedTo(team) : false;
   }

   public void setInvisible(boolean flag) {
      this.setSharedFlag(5, flag);
   }

   protected boolean getSharedFlag(int i) {
      return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << i) != 0;
   }

   protected void setSharedFlag(int i, boolean flag) {
      byte b0 = this.entityData.get(DATA_SHARED_FLAGS_ID);
      if (flag) {
         this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b0 | 1 << i));
      } else {
         this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b0 & ~(1 << i)));
      }

   }

   public int getMaxAirSupply() {
      return 300;
   }

   public int getAirSupply() {
      return this.entityData.get(DATA_AIR_SUPPLY_ID);
   }

   public void setAirSupply(int i) {
      this.entityData.set(DATA_AIR_SUPPLY_ID, i);
   }

   public int getTicksFrozen() {
      return this.entityData.get(DATA_TICKS_FROZEN);
   }

   public void setTicksFrozen(int i) {
      this.entityData.set(DATA_TICKS_FROZEN, i);
   }

   public float getPercentFrozen() {
      int i = this.getTicksRequiredToFreeze();
      return (float)Math.min(this.getTicksFrozen(), i) / (float)i;
   }

   public boolean isFullyFrozen() {
      return this.getTicksFrozen() >= this.getTicksRequiredToFreeze();
   }

   public int getTicksRequiredToFreeze() {
      return 140;
   }

   public void thunderHit(ServerLevel serverlevel, LightningBolt lightningbolt) {
      this.setRemainingFireTicks(this.remainingFireTicks + 1);
      if (this.remainingFireTicks == 0) {
         this.setSecondsOnFire(8);
      }

      this.hurt(this.damageSources().lightningBolt(), 5.0F);
   }

   public void onAboveBubbleCol(boolean flag) {
      Vec3 vec3 = this.getDeltaMovement();
      double d0;
      if (flag) {
         d0 = Math.max(-0.9D, vec3.y - 0.03D);
      } else {
         d0 = Math.min(1.8D, vec3.y + 0.1D);
      }

      this.setDeltaMovement(vec3.x, d0, vec3.z);
   }

   public void onInsideBubbleColumn(boolean flag) {
      Vec3 vec3 = this.getDeltaMovement();
      double d0;
      if (flag) {
         d0 = Math.max(-0.3D, vec3.y - 0.03D);
      } else {
         d0 = Math.min(0.7D, vec3.y + 0.06D);
      }

      this.setDeltaMovement(vec3.x, d0, vec3.z);
      this.resetFallDistance();
   }

   public boolean killedEntity(ServerLevel serverlevel, LivingEntity livingentity) {
      return true;
   }

   public void checkSlowFallDistance() {
      if (this.getDeltaMovement().y() > -0.5D && this.fallDistance > 1.0F) {
         this.fallDistance = 1.0F;
      }

   }

   public void resetFallDistance() {
      this.fallDistance = 0.0F;
   }

   protected void moveTowardsClosestSpace(double d0, double d1, double d2) {
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);
      Vec3 vec3 = new Vec3(d0 - (double)blockpos.getX(), d1 - (double)blockpos.getY(), d2 - (double)blockpos.getZ());
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      Direction direction = Direction.UP;
      double d3 = Double.MAX_VALUE;

      for(Direction direction1 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
         blockpos_mutableblockpos.setWithOffset(blockpos, direction1);
         if (!this.level().getBlockState(blockpos_mutableblockpos).isCollisionShapeFullBlock(this.level(), blockpos_mutableblockpos)) {
            double d4 = vec3.get(direction1.getAxis());
            double d5 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d4 : d4;
            if (d5 < d3) {
               d3 = d5;
               direction = direction1;
            }
         }
      }

      float f = this.random.nextFloat() * 0.2F + 0.1F;
      float f1 = (float)direction.getAxisDirection().getStep();
      Vec3 vec31 = this.getDeltaMovement().scale(0.75D);
      if (direction.getAxis() == Direction.Axis.X) {
         this.setDeltaMovement((double)(f1 * f), vec31.y, vec31.z);
      } else if (direction.getAxis() == Direction.Axis.Y) {
         this.setDeltaMovement(vec31.x, (double)(f1 * f), vec31.z);
      } else if (direction.getAxis() == Direction.Axis.Z) {
         this.setDeltaMovement(vec31.x, vec31.y, (double)(f1 * f));
      }

   }

   public void makeStuckInBlock(BlockState blockstate, Vec3 vec3) {
      this.resetFallDistance();
      this.stuckSpeedMultiplier = vec3;
   }

   private static Component removeAction(Component component) {
      MutableComponent mutablecomponent = component.plainCopy().setStyle(component.getStyle().withClickEvent((ClickEvent)null));

      for(Component component1 : component.getSiblings()) {
         mutablecomponent.append(removeAction(component1));
      }

      return mutablecomponent;
   }

   public Component getName() {
      Component component = this.getCustomName();
      return component != null ? removeAction(component) : this.getTypeName();
   }

   protected Component getTypeName() {
      return this.type.getDescription();
   }

   public boolean is(Entity entity) {
      return this == entity;
   }

   public float getYHeadRot() {
      return 0.0F;
   }

   public void setYHeadRot(float f) {
   }

   public void setYBodyRot(float f) {
   }

   public boolean isAttackable() {
      return true;
   }

   public boolean skipAttackInteraction(Entity entity) {
      return false;
   }

   public String toString() {
      String s = this.level() == null ? "~NULL~" : this.level().toString();
      return this.removalReason != null ? String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]", this.getClass().getSimpleName(), this.getName().getString(), this.id, s, this.getX(), this.getY(), this.getZ(), this.removalReason) : String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.id, s, this.getX(), this.getY(), this.getZ());
   }

   public boolean isInvulnerableTo(DamageSource damagesource) {
      return this.isRemoved() || this.invulnerable && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damagesource.isCreativePlayer() || damagesource.is(DamageTypeTags.IS_FIRE) && this.fireImmune() || damagesource.is(DamageTypeTags.IS_FALL) && this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE);
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public void setInvulnerable(boolean flag) {
      this.invulnerable = flag;
   }

   public void copyPosition(Entity entity) {
      this.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
   }

   public void restoreFrom(Entity entity) {
      CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
      compoundtag.remove("Dimension");
      this.load(compoundtag);
      this.portalCooldown = entity.portalCooldown;
      this.portalEntrancePos = entity.portalEntrancePos;
   }

   @Nullable
   public Entity changeDimension(ServerLevel serverlevel) {
      if (this.level() instanceof ServerLevel && !this.isRemoved()) {
         this.level().getProfiler().push("changeDimension");
         this.unRide();
         this.level().getProfiler().push("reposition");
         PortalInfo portalinfo = this.findDimensionEntryPoint(serverlevel);
         if (portalinfo == null) {
            return null;
         } else {
            this.level().getProfiler().popPush("reloading");
            Entity entity = this.getType().create(serverlevel);
            if (entity != null) {
               entity.restoreFrom(this);
               entity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, entity.getXRot());
               entity.setDeltaMovement(portalinfo.speed);
               serverlevel.addDuringTeleport(entity);
               if (serverlevel.dimension() == Level.END) {
                  ServerLevel.makeObsidianPlatform(serverlevel);
               }
            }

            this.removeAfterChangingDimensions();
            this.level().getProfiler().pop();
            ((ServerLevel)this.level()).resetEmptyTime();
            serverlevel.resetEmptyTime();
            this.level().getProfiler().pop();
            return entity;
         }
      } else {
         return null;
      }
   }

   protected void removeAfterChangingDimensions() {
      this.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
   }

   @Nullable
   protected PortalInfo findDimensionEntryPoint(ServerLevel serverlevel) {
      boolean flag = this.level().dimension() == Level.END && serverlevel.dimension() == Level.OVERWORLD;
      boolean flag1 = serverlevel.dimension() == Level.END;
      if (!flag && !flag1) {
         boolean flag2 = serverlevel.dimension() == Level.NETHER;
         if (this.level().dimension() != Level.NETHER && !flag2) {
            return null;
         } else {
            WorldBorder worldborder = serverlevel.getWorldBorder();
            double d0 = DimensionType.getTeleportationScale(this.level().dimensionType(), serverlevel.dimensionType());
            BlockPos blockpos2 = worldborder.clampToBounds(this.getX() * d0, this.getY(), this.getZ() * d0);
            return this.getExitPortal(serverlevel, blockpos2, flag2, worldborder).map((blockutil_foundrectangle) -> {
               BlockState blockstate = this.level().getBlockState(this.portalEntrancePos);
               Direction.Axis direction_axis;
               Vec3 vec3;
               if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                  direction_axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                  BlockUtil.FoundRectangle blockutil_foundrectangle1 = BlockUtil.getLargestRectangleAround(this.portalEntrancePos, direction_axis, 21, Direction.Axis.Y, 21, (blockpos3) -> this.level().getBlockState(blockpos3) == blockstate);
                  vec3 = this.getRelativePortalPosition(direction_axis, blockutil_foundrectangle1);
               } else {
                  direction_axis = Direction.Axis.X;
                  vec3 = new Vec3(0.5D, 0.0D, 0.0D);
               }

               return PortalShape.createPortalInfo(serverlevel, blockutil_foundrectangle, direction_axis, vec3, this, this.getDeltaMovement(), this.getYRot(), this.getXRot());
            }).orElse((PortalInfo)null);
         }
      } else {
         BlockPos blockpos;
         if (flag1) {
            blockpos = ServerLevel.END_SPAWN_POINT;
         } else {
            blockpos = serverlevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, serverlevel.getSharedSpawnPos());
         }

         return new PortalInfo(new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), this.getDeltaMovement(), this.getYRot(), this.getXRot());
      }
   }

   protected Vec3 getRelativePortalPosition(Direction.Axis direction_axis, BlockUtil.FoundRectangle blockutil_foundrectangle) {
      return PortalShape.getRelativePosition(blockutil_foundrectangle, direction_axis, this.position(), this.getDimensions(this.getPose()));
   }

   protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel serverlevel, BlockPos blockpos, boolean flag, WorldBorder worldborder) {
      return serverlevel.getPortalForcer().findPortalAround(blockpos, flag, worldborder);
   }

   public boolean canChangeDimensions() {
      return !this.isPassenger() && !this.isVehicle();
   }

   public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, FluidState fluidstate, float f) {
      return f;
   }

   public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, float f) {
      return true;
   }

   public int getMaxFallDistance() {
      return 3;
   }

   public boolean isIgnoringBlockTriggers() {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory crashreportcategory) {
      crashreportcategory.setDetail("Entity Type", () -> EntityType.getKey(this.getType()) + " (" + this.getClass().getCanonicalName() + ")");
      crashreportcategory.setDetail("Entity ID", this.id);
      crashreportcategory.setDetail("Entity Name", () -> this.getName().getString());
      crashreportcategory.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
      crashreportcategory.setDetail("Entity's Block location", CrashReportCategory.formatLocation(this.level(), Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())));
      Vec3 vec3 = this.getDeltaMovement();
      crashreportcategory.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vec3.x, vec3.y, vec3.z));
      crashreportcategory.setDetail("Entity's Passengers", () -> this.getPassengers().toString());
      crashreportcategory.setDetail("Entity's Vehicle", () -> String.valueOf((Object)this.getVehicle()));
   }

   public boolean displayFireAnimation() {
      return this.isOnFire() && !this.isSpectator();
   }

   public void setUUID(UUID uuid) {
      this.uuid = uuid;
      this.stringUUID = this.uuid.toString();
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public String getStringUUID() {
      return this.stringUUID;
   }

   public String getScoreboardName() {
      return this.stringUUID;
   }

   public boolean isPushedByFluid() {
      return true;
   }

   public static double getViewScale() {
      return viewScale;
   }

   public static void setViewScale(double d0) {
      viewScale = d0;
   }

   public Component getDisplayName() {
      return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName()).withStyle((style) -> style.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID()));
   }

   public void setCustomName(@Nullable Component component) {
      this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(component));
   }

   @Nullable
   public Component getCustomName() {
      return this.entityData.get(DATA_CUSTOM_NAME).orElse((Component)null);
   }

   public boolean hasCustomName() {
      return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
   }

   public void setCustomNameVisible(boolean flag) {
      this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, flag);
   }

   public boolean isCustomNameVisible() {
      return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
   }

   public final void teleportToWithTicket(double d0, double d1, double d2) {
      if (this.level() instanceof ServerLevel) {
         ChunkPos chunkpos = new ChunkPos(BlockPos.containing(d0, d1, d2));
         ((ServerLevel)this.level()).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 0, this.getId());
         this.level().getChunk(chunkpos.x, chunkpos.z);
         this.teleportTo(d0, d1, d2);
      }
   }

   public boolean teleportTo(ServerLevel serverlevel, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1) {
      float f2 = Mth.clamp(f1, -90.0F, 90.0F);
      if (serverlevel == this.level()) {
         this.moveTo(d0, d1, d2, f, f2);
         this.teleportPassengers();
         this.setYHeadRot(f);
      } else {
         this.unRide();
         Entity entity = this.getType().create(serverlevel);
         if (entity == null) {
            return false;
         }

         entity.restoreFrom(this);
         entity.moveTo(d0, d1, d2, f, f2);
         entity.setYHeadRot(f);
         this.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
         serverlevel.addDuringTeleport(entity);
      }

      return true;
   }

   public void dismountTo(double d0, double d1, double d2) {
      this.teleportTo(d0, d1, d2);
   }

   public void teleportTo(double d0, double d1, double d2) {
      if (this.level() instanceof ServerLevel) {
         this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
         this.teleportPassengers();
      }
   }

   private void teleportPassengers() {
      this.getSelfAndPassengers().forEach((entity) -> {
         for(Entity entity1 : entity.passengers) {
            entity.positionRider(entity1, Entity::moveTo);
         }

      });
   }

   public void teleportRelative(double d0, double d1, double d2) {
      this.teleportTo(this.getX() + d0, this.getY() + d1, this.getZ() + d2);
   }

   public boolean shouldShowName() {
      return this.isCustomNameVisible();
   }

   public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list) {
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_POSE.equals(entitydataaccessor)) {
         this.refreshDimensions();
      }

   }

   /** @deprecated */
   @Deprecated
   protected void fixupDimensions() {
      Pose pose = this.getPose();
      EntityDimensions entitydimensions = this.getDimensions(pose);
      this.dimensions = entitydimensions;
      this.eyeHeight = this.getEyeHeight(pose, entitydimensions);
   }

   public void refreshDimensions() {
      EntityDimensions entitydimensions = this.dimensions;
      Pose pose = this.getPose();
      EntityDimensions entitydimensions1 = this.getDimensions(pose);
      this.dimensions = entitydimensions1;
      this.eyeHeight = this.getEyeHeight(pose, entitydimensions1);
      this.reapplyPosition();
      boolean flag = (double)entitydimensions1.width <= 4.0D && (double)entitydimensions1.height <= 4.0D;
      if (!this.level().isClientSide && !this.firstTick && !this.noPhysics && flag && (entitydimensions1.width > entitydimensions.width || entitydimensions1.height > entitydimensions.height) && !(this instanceof Player)) {
         Vec3 vec3 = this.position().add(0.0D, (double)entitydimensions.height / 2.0D, 0.0D);
         double d0 = (double)Math.max(0.0F, entitydimensions1.width - entitydimensions.width) + 1.0E-6D;
         double d1 = (double)Math.max(0.0F, entitydimensions1.height - entitydimensions.height) + 1.0E-6D;
         VoxelShape voxelshape = Shapes.create(AABB.ofSize(vec3, d0, d1, d0));
         this.level().findFreePosition(this, voxelshape, vec3, (double)entitydimensions1.width, (double)entitydimensions1.height, (double)entitydimensions1.width).ifPresent((vec31) -> this.setPos(vec31.add(0.0D, (double)(-entitydimensions1.height) / 2.0D, 0.0D)));
      }

   }

   public Direction getDirection() {
      return Direction.fromYRot((double)this.getYRot());
   }

   public Direction getMotionDirection() {
      return this.getDirection();
   }

   protected HoverEvent createHoverEvent() {
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(this.getType(), this.getUUID(), this.getName()));
   }

   public boolean broadcastToPlayer(ServerPlayer serverplayer) {
      return true;
   }

   public final AABB getBoundingBox() {
      return this.bb;
   }

   public AABB getBoundingBoxForCulling() {
      return this.getBoundingBox();
   }

   protected AABB getBoundingBoxForPose(Pose pose) {
      EntityDimensions entitydimensions = this.getDimensions(pose);
      float f = entitydimensions.width / 2.0F;
      Vec3 vec3 = new Vec3(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
      Vec3 vec31 = new Vec3(this.getX() + (double)f, this.getY() + (double)entitydimensions.height, this.getZ() + (double)f);
      return new AABB(vec3, vec31);
   }

   public final void setBoundingBox(AABB aabb) {
      this.bb = aabb;
   }

   protected float getEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return entitydimensions.height * 0.85F;
   }

   public float getEyeHeight(Pose pose) {
      return this.getEyeHeight(pose, this.getDimensions(pose));
   }

   public final float getEyeHeight() {
      return this.eyeHeight;
   }

   public Vec3 getLeashOffset(float f) {
      return this.getLeashOffset();
   }

   protected Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)this.getEyeHeight(), (double)(this.getBbWidth() * 0.4F));
   }

   public SlotAccess getSlot(int i) {
      return SlotAccess.NULL;
   }

   public void sendSystemMessage(Component component) {
   }

   public Level getCommandSenderWorld() {
      return this.level();
   }

   @Nullable
   public MinecraftServer getServer() {
      return this.level().getServer();
   }

   public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionhand) {
      return InteractionResult.PASS;
   }

   public boolean ignoreExplosion() {
      return false;
   }

   public void doEnchantDamageEffects(LivingEntity livingentity, Entity entity) {
      if (entity instanceof LivingEntity) {
         EnchantmentHelper.doPostHurtEffects((LivingEntity)entity, livingentity);
      }

      EnchantmentHelper.doPostDamageEffects(livingentity, entity);
   }

   public void startSeenByPlayer(ServerPlayer serverplayer) {
   }

   public void stopSeenByPlayer(ServerPlayer serverplayer) {
   }

   public float rotate(Rotation rotation) {
      float f = Mth.wrapDegrees(this.getYRot());
      switch (rotation) {
         case CLOCKWISE_180:
            return f + 180.0F;
         case COUNTERCLOCKWISE_90:
            return f + 270.0F;
         case CLOCKWISE_90:
            return f + 90.0F;
         default:
            return f;
      }
   }

   public float mirror(Mirror mirror) {
      float f = Mth.wrapDegrees(this.getYRot());
      switch (mirror) {
         case FRONT_BACK:
            return -f;
         case LEFT_RIGHT:
            return 180.0F - f;
         default:
            return f;
      }
   }

   public boolean onlyOpCanSetNbt() {
      return false;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return null;
   }

   public final boolean hasControllingPassenger() {
      return this.getControllingPassenger() != null;
   }

   public final List<Entity> getPassengers() {
      return this.passengers;
   }

   @Nullable
   public Entity getFirstPassenger() {
      return this.passengers.isEmpty() ? null : this.passengers.get(0);
   }

   public boolean hasPassenger(Entity entity) {
      return this.passengers.contains(entity);
   }

   public boolean hasPassenger(Predicate<Entity> predicate) {
      for(Entity entity : this.passengers) {
         if (predicate.test(entity)) {
            return true;
         }
      }

      return false;
   }

   private Stream<Entity> getIndirectPassengersStream() {
      return this.passengers.stream().flatMap(Entity::getSelfAndPassengers);
   }

   public Stream<Entity> getSelfAndPassengers() {
      return Stream.concat(Stream.of(this), this.getIndirectPassengersStream());
   }

   public Stream<Entity> getPassengersAndSelf() {
      return Stream.concat(this.passengers.stream().flatMap(Entity::getPassengersAndSelf), Stream.of(this));
   }

   public Iterable<Entity> getIndirectPassengers() {
      return () -> this.getIndirectPassengersStream().iterator();
   }

   public boolean hasExactlyOnePlayerPassenger() {
      return this.getIndirectPassengersStream().filter((entity) -> entity instanceof Player).count() == 1L;
   }

   public Entity getRootVehicle() {
      Entity entity;
      for(entity = this; entity.isPassenger(); entity = entity.getVehicle()) {
      }

      return entity;
   }

   public boolean isPassengerOfSameVehicle(Entity entity) {
      return this.getRootVehicle() == entity.getRootVehicle();
   }

   public boolean hasIndirectPassenger(Entity entity) {
      if (!entity.isPassenger()) {
         return false;
      } else {
         Entity entity1 = entity.getVehicle();
         return entity1 == this ? true : this.hasIndirectPassenger(entity1);
      }
   }

   public boolean isControlledByLocalInstance() {
      LivingEntity var2 = this.getControllingPassenger();
      if (var2 instanceof Player player) {
         return player.isLocalPlayer();
      } else {
         return this.isEffectiveAi();
      }
   }

   public boolean isEffectiveAi() {
      return !this.level().isClientSide;
   }

   protected static Vec3 getCollisionHorizontalEscapeVector(double d0, double d1, float f) {
      double d2 = (d0 + d1 + (double)1.0E-5F) / 2.0D;
      float f1 = -Mth.sin(f * ((float)Math.PI / 180F));
      float f2 = Mth.cos(f * ((float)Math.PI / 180F));
      float f3 = Math.max(Math.abs(f1), Math.abs(f2));
      return new Vec3((double)f1 * d2 / (double)f3, 0.0D, (double)f2 * d2 / (double)f3);
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity livingentity) {
      return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   @Nullable
   public Entity getVehicle() {
      return this.vehicle;
   }

   @Nullable
   public Entity getControlledVehicle() {
      return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.NORMAL;
   }

   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   protected int getFireImmuneTicks() {
      return 1;
   }

   public CommandSourceStack createCommandSourceStack() {
      return new CommandSourceStack(this, this.position(), this.getRotationVector(), this.level() instanceof ServerLevel ? (ServerLevel)this.level() : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.level().getServer(), this);
   }

   protected int getPermissionLevel() {
      return 0;
   }

   public boolean hasPermissions(int i) {
      return this.getPermissionLevel() >= i;
   }

   public boolean acceptsSuccess() {
      return this.level().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
   }

   public boolean acceptsFailure() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return true;
   }

   public void lookAt(EntityAnchorArgument.Anchor entityanchorargument_anchor, Vec3 vec3) {
      Vec3 vec31 = entityanchorargument_anchor.apply(this);
      double d0 = vec3.x - vec31.x;
      double d1 = vec3.y - vec31.y;
      double d2 = vec3.z - vec31.z;
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      this.setXRot(Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI)))));
      this.setYRot(Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F));
      this.setYHeadRot(this.getYRot());
      this.xRotO = this.getXRot();
      this.yRotO = this.getYRot();
   }

   public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
      if (this.touchingUnloadedChunk()) {
         return false;
      } else {
         AABB aabb = this.getBoundingBox().deflate(0.001D);
         int i = Mth.floor(aabb.minX);
         int j = Mth.ceil(aabb.maxX);
         int k = Mth.floor(aabb.minY);
         int l = Mth.ceil(aabb.maxY);
         int i1 = Mth.floor(aabb.minZ);
         int j1 = Mth.ceil(aabb.maxZ);
         double d1 = 0.0D;
         boolean flag = this.isPushedByFluid();
         boolean flag1 = false;
         Vec3 vec3 = Vec3.ZERO;
         int k1 = 0;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int l1 = i; l1 < j; ++l1) {
            for(int i2 = k; i2 < l; ++i2) {
               for(int j2 = i1; j2 < j1; ++j2) {
                  blockpos_mutableblockpos.set(l1, i2, j2);
                  FluidState fluidstate = this.level().getFluidState(blockpos_mutableblockpos);
                  if (fluidstate.is(tagkey)) {
                     double d2 = (double)((float)i2 + fluidstate.getHeight(this.level(), blockpos_mutableblockpos));
                     if (d2 >= aabb.minY) {
                        flag1 = true;
                        d1 = Math.max(d2 - aabb.minY, d1);
                        if (flag) {
                           Vec3 vec31 = fluidstate.getFlow(this.level(), blockpos_mutableblockpos);
                           if (d1 < 0.4D) {
                              vec31 = vec31.scale(d1);
                           }

                           vec3 = vec3.add(vec31);
                           ++k1;
                        }
                     }
                  }
               }
            }
         }

         if (vec3.length() > 0.0D) {
            if (k1 > 0) {
               vec3 = vec3.scale(1.0D / (double)k1);
            }

            if (!(this instanceof Player)) {
               vec3 = vec3.normalize();
            }

            Vec3 vec32 = this.getDeltaMovement();
            vec3 = vec3.scale(d0 * 1.0D);
            double d3 = 0.003D;
            if (Math.abs(vec32.x) < 0.003D && Math.abs(vec32.z) < 0.003D && vec3.length() < 0.0045000000000000005D) {
               vec3 = vec3.normalize().scale(0.0045000000000000005D);
            }

            this.setDeltaMovement(this.getDeltaMovement().add(vec3));
         }

         this.fluidHeight.put(tagkey, d1);
         return flag1;
      }
   }

   public boolean touchingUnloadedChunk() {
      AABB aabb = this.getBoundingBox().inflate(1.0D);
      int i = Mth.floor(aabb.minX);
      int j = Mth.ceil(aabb.maxX);
      int k = Mth.floor(aabb.minZ);
      int l = Mth.ceil(aabb.maxZ);
      return !this.level().hasChunksAt(i, k, j, l);
   }

   public double getFluidHeight(TagKey<Fluid> tagkey) {
      return this.fluidHeight.getDouble(tagkey);
   }

   public double getFluidJumpThreshold() {
      return (double)this.getEyeHeight() < 0.4D ? 0.0D : 0.4D;
   }

   public final float getBbWidth() {
      return this.dimensions.width;
   }

   public final float getBbHeight() {
      return this.dimensions.height;
   }

   public float getNameTagOffsetY() {
      return this.getBbHeight() + 0.5F;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.type.getDimensions();
   }

   public Vec3 position() {
      return this.position;
   }

   public Vec3 trackingPosition() {
      return this.position();
   }

   public BlockPos blockPosition() {
      return this.blockPosition;
   }

   public BlockState getFeetBlockState() {
      if (this.feetBlockState == null) {
         this.feetBlockState = this.level().getBlockState(this.blockPosition());
      }

      return this.feetBlockState;
   }

   public ChunkPos chunkPosition() {
      return this.chunkPosition;
   }

   public Vec3 getDeltaMovement() {
      return this.deltaMovement;
   }

   public void setDeltaMovement(Vec3 vec3) {
      this.deltaMovement = vec3;
   }

   public void addDeltaMovement(Vec3 vec3) {
      this.setDeltaMovement(this.getDeltaMovement().add(vec3));
   }

   public void setDeltaMovement(double d0, double d1, double d2) {
      this.setDeltaMovement(new Vec3(d0, d1, d2));
   }

   public final int getBlockX() {
      return this.blockPosition.getX();
   }

   public final double getX() {
      return this.position.x;
   }

   public double getX(double d0) {
      return this.position.x + (double)this.getBbWidth() * d0;
   }

   public double getRandomX(double d0) {
      return this.getX((2.0D * this.random.nextDouble() - 1.0D) * d0);
   }

   public final int getBlockY() {
      return this.blockPosition.getY();
   }

   public final double getY() {
      return this.position.y;
   }

   public double getY(double d0) {
      return this.position.y + (double)this.getBbHeight() * d0;
   }

   public double getRandomY() {
      return this.getY(this.random.nextDouble());
   }

   public double getEyeY() {
      return this.position.y + (double)this.eyeHeight;
   }

   public final int getBlockZ() {
      return this.blockPosition.getZ();
   }

   public final double getZ() {
      return this.position.z;
   }

   public double getZ(double d0) {
      return this.position.z + (double)this.getBbWidth() * d0;
   }

   public double getRandomZ(double d0) {
      return this.getZ((2.0D * this.random.nextDouble() - 1.0D) * d0);
   }

   public final void setPosRaw(double d0, double d1, double d2) {
      if (this.position.x != d0 || this.position.y != d1 || this.position.z != d2) {
         this.position = new Vec3(d0, d1, d2);
         int i = Mth.floor(d0);
         int j = Mth.floor(d1);
         int k = Mth.floor(d2);
         if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
            this.blockPosition = new BlockPos(i, j, k);
            this.feetBlockState = null;
            if (SectionPos.blockToSectionCoord(i) != this.chunkPosition.x || SectionPos.blockToSectionCoord(k) != this.chunkPosition.z) {
               this.chunkPosition = new ChunkPos(this.blockPosition);
            }
         }

         this.levelCallback.onMove();
      }

   }

   public void checkDespawn() {
   }

   public Vec3 getRopeHoldPosition(float f) {
      return this.getPosition(f).add(0.0D, (double)this.eyeHeight * 0.7D, 0.0D);
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      int i = clientboundaddentitypacket.getId();
      double d0 = clientboundaddentitypacket.getX();
      double d1 = clientboundaddentitypacket.getY();
      double d2 = clientboundaddentitypacket.getZ();
      this.syncPacketPositionCodec(d0, d1, d2);
      this.moveTo(d0, d1, d2);
      this.setXRot(clientboundaddentitypacket.getXRot());
      this.setYRot(clientboundaddentitypacket.getYRot());
      this.setId(i);
      this.setUUID(clientboundaddentitypacket.getUUID());
   }

   @Nullable
   public ItemStack getPickResult() {
      return null;
   }

   public void setIsInPowderSnow(boolean flag) {
      this.isInPowderSnow = flag;
   }

   public boolean canFreeze() {
      return !this.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
   }

   public boolean isFreezing() {
      return (this.isInPowderSnow || this.wasInPowderSnow) && this.canFreeze();
   }

   public float getYRot() {
      return this.yRot;
   }

   public float getVisualRotationYInDegrees() {
      return this.getYRot();
   }

   public void setYRot(float f) {
      if (!Float.isFinite(f)) {
         Util.logAndPauseIfInIde("Invalid entity rotation: " + f + ", discarding.");
      } else {
         this.yRot = f;
      }
   }

   public float getXRot() {
      return this.xRot;
   }

   public void setXRot(float f) {
      if (!Float.isFinite(f)) {
         Util.logAndPauseIfInIde("Invalid entity rotation: " + f + ", discarding.");
      } else {
         this.xRot = f;
      }
   }

   public boolean canSprint() {
      return false;
   }

   public float maxUpStep() {
      return this.maxUpStep;
   }

   public void setMaxUpStep(float f) {
      this.maxUpStep = f;
   }

   public final boolean isRemoved() {
      return this.removalReason != null;
   }

   @Nullable
   public Entity.RemovalReason getRemovalReason() {
      return this.removalReason;
   }

   public final void setRemoved(Entity.RemovalReason entity_removalreason) {
      if (this.removalReason == null) {
         this.removalReason = entity_removalreason;
      }

      if (this.removalReason.shouldDestroy()) {
         this.stopRiding();
      }

      this.getPassengers().forEach(Entity::stopRiding);
      this.levelCallback.onRemove(entity_removalreason);
   }

   protected void unsetRemoved() {
      this.removalReason = null;
   }

   public void setLevelCallback(EntityInLevelCallback entityinlevelcallback) {
      this.levelCallback = entityinlevelcallback;
   }

   public boolean shouldBeSaved() {
      if (this.removalReason != null && !this.removalReason.shouldSave()) {
         return false;
      } else if (this.isPassenger()) {
         return false;
      } else {
         return !this.isVehicle() || !this.hasExactlyOnePlayerPassenger();
      }
   }

   public boolean isAlwaysTicking() {
      return false;
   }

   public boolean mayInteract(Level level, BlockPos blockpos) {
      return true;
   }

   public Level level() {
      return this.level;
   }

   protected void setLevel(Level level) {
      this.level = level;
   }

   public DamageSources damageSources() {
      return this.level().damageSources();
   }

   @FunctionalInterface
   public interface MoveFunction {
      void accept(Entity entity, double d0, double d1, double d2);
   }

   public static enum MovementEmission {
      NONE(false, false),
      SOUNDS(true, false),
      EVENTS(false, true),
      ALL(true, true);

      final boolean sounds;
      final boolean events;

      private MovementEmission(boolean flag, boolean flag1) {
         this.sounds = flag;
         this.events = flag1;
      }

      public boolean emitsAnything() {
         return this.events || this.sounds;
      }

      public boolean emitsEvents() {
         return this.events;
      }

      public boolean emitsSounds() {
         return this.sounds;
      }
   }

   public static enum RemovalReason {
      KILLED(true, false),
      DISCARDED(true, false),
      UNLOADED_TO_CHUNK(false, true),
      UNLOADED_WITH_PLAYER(false, false),
      CHANGED_DIMENSION(false, false);

      private final boolean destroy;
      private final boolean save;

      private RemovalReason(boolean flag, boolean flag1) {
         this.destroy = flag;
         this.save = flag1;
      }

      public boolean shouldDestroy() {
         return this.destroy;
      }

      public boolean shouldSave() {
         return this.save;
      }
   }
}
