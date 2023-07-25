package net.minecraft.world.entity.vehicle;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Boat extends Entity implements VariantHolder<Boat.Type> {
   private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
   public static final int PADDLE_LEFT = 0;
   public static final int PADDLE_RIGHT = 1;
   private static final int TIME_TO_EJECT = 60;
   private static final float PADDLE_SPEED = ((float)Math.PI / 8F);
   public static final double PADDLE_SOUND_TIME = (double)((float)Math.PI / 4F);
   public static final int BUBBLE_TIME = 60;
   private final float[] paddlePositions = new float[2];
   private float invFriction;
   private float outOfControlTicks;
   private float deltaRotation;
   private int lerpSteps;
   private double lerpX;
   private double lerpY;
   private double lerpZ;
   private double lerpYRot;
   private double lerpXRot;
   private boolean inputLeft;
   private boolean inputRight;
   private boolean inputUp;
   private boolean inputDown;
   private double waterLevel;
   private float landFriction;
   private Boat.Status status;
   private Boat.Status oldStatus;
   private double lastYd;
   private boolean isAboveBubbleColumn;
   private boolean bubbleColumnDirectionIsDown;
   private float bubbleMultiplier;
   private float bubbleAngle;
   private float bubbleAngleO;

   public Boat(EntityType<? extends Boat> entitytype, Level level) {
      super(entitytype, level);
      this.blocksBuilding = true;
   }

   public Boat(Level level, double d0, double d1, double d2) {
      this(EntityType.BOAT, level);
      this.setPos(d0, d1, d2);
      this.xo = d0;
      this.yo = d1;
      this.zo = d2;
   }

   protected float getEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return entitydimensions.height;
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_HURT, 0);
      this.entityData.define(DATA_ID_HURTDIR, 1);
      this.entityData.define(DATA_ID_DAMAGE, 0.0F);
      this.entityData.define(DATA_ID_TYPE, Boat.Type.OAK.ordinal());
      this.entityData.define(DATA_ID_PADDLE_LEFT, false);
      this.entityData.define(DATA_ID_PADDLE_RIGHT, false);
      this.entityData.define(DATA_ID_BUBBLE_TIME, 0);
   }

   public boolean canCollideWith(Entity entity) {
      return canVehicleCollide(this, entity);
   }

   public static boolean canVehicleCollide(Entity entity, Entity entity1) {
      return (entity1.canBeCollidedWith() || entity1.isPushable()) && !entity.isPassengerOfSameVehicle(entity1);
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean isPushable() {
      return true;
   }

   protected Vec3 getRelativePortalPosition(Direction.Axis direction_axis, BlockUtil.FoundRectangle blockutil_foundrectangle) {
      return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(direction_axis, blockutil_foundrectangle));
   }

   public double getPassengersRidingOffset() {
      return this.getVariant() == Boat.Type.BAMBOO ? 0.25D : -0.1D;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (!this.level().isClientSide && !this.isRemoved()) {
         this.setHurtDir(-this.getHurtDir());
         this.setHurtTime(10);
         this.setDamage(this.getDamage() + f * 10.0F);
         this.markHurt();
         this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
         boolean flag = damagesource.getEntity() instanceof Player && ((Player)damagesource.getEntity()).getAbilities().instabuild;
         if (flag || this.getDamage() > 40.0F) {
            if (!flag && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
               this.destroy(damagesource);
            }

            this.discard();
         }

         return true;
      } else {
         return true;
      }
   }

   protected void destroy(DamageSource damagesource) {
      this.spawnAtLocation(this.getDropItem());
   }

   public void onAboveBubbleCol(boolean flag) {
      if (!this.level().isClientSide) {
         this.isAboveBubbleColumn = true;
         this.bubbleColumnDirectionIsDown = flag;
         if (this.getBubbleTime() == 0) {
            this.setBubbleTime(60);
         }
      }

      this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7D, this.getZ() + (double)this.random.nextFloat(), 0.0D, 0.0D, 0.0D);
      if (this.random.nextInt(20) == 0) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
         this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
      }

   }

   public void push(Entity entity) {
      if (entity instanceof Boat) {
         if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            super.push(entity);
         }
      } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
         super.push(entity);
      }

   }

   public Item getDropItem() {
      Item var10000;
      switch (this.getVariant()) {
         case SPRUCE:
            var10000 = Items.SPRUCE_BOAT;
            break;
         case BIRCH:
            var10000 = Items.BIRCH_BOAT;
            break;
         case JUNGLE:
            var10000 = Items.JUNGLE_BOAT;
            break;
         case ACACIA:
            var10000 = Items.ACACIA_BOAT;
            break;
         case CHERRY:
            var10000 = Items.CHERRY_BOAT;
            break;
         case DARK_OAK:
            var10000 = Items.DARK_OAK_BOAT;
            break;
         case MANGROVE:
            var10000 = Items.MANGROVE_BOAT;
            break;
         case BAMBOO:
            var10000 = Items.BAMBOO_RAFT;
            break;
         default:
            var10000 = Items.OAK_BOAT;
      }

      return var10000;
   }

   public void animateHurt(float f) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() * 11.0F);
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public void lerpTo(double d0, double d1, double d2, float f, float f1, int i, boolean flag) {
      this.lerpX = d0;
      this.lerpY = d1;
      this.lerpZ = d2;
      this.lerpYRot = (double)f;
      this.lerpXRot = (double)f1;
      this.lerpSteps = 10;
   }

   public Direction getMotionDirection() {
      return this.getDirection().getClockWise();
   }

   public void tick() {
      this.oldStatus = this.status;
      this.status = this.getStatus();
      if (this.status != Boat.Status.UNDER_WATER && this.status != Boat.Status.UNDER_FLOWING_WATER) {
         this.outOfControlTicks = 0.0F;
      } else {
         ++this.outOfControlTicks;
      }

      if (!this.level().isClientSide && this.outOfControlTicks >= 60.0F) {
         this.ejectPassengers();
      }

      if (this.getHurtTime() > 0) {
         this.setHurtTime(this.getHurtTime() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      super.tick();
      this.tickLerp();
      if (this.isControlledByLocalInstance()) {
         if (!(this.getFirstPassenger() instanceof Player)) {
            this.setPaddleState(false, false);
         }

         this.floatBoat();
         if (this.level().isClientSide) {
            this.controlBoat();
            this.level().sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
         }

         this.move(MoverType.SELF, this.getDeltaMovement());
      } else {
         this.setDeltaMovement(Vec3.ZERO);
      }

      this.tickBubbleColumn();

      for(int i = 0; i <= 1; ++i) {
         if (this.getPaddleState(i)) {
            if (!this.isSilent() && (double)(this.paddlePositions[i] % ((float)Math.PI * 2F)) <= (double)((float)Math.PI / 4F) && (double)((this.paddlePositions[i] + ((float)Math.PI / 8F)) % ((float)Math.PI * 2F)) >= (double)((float)Math.PI / 4F)) {
               SoundEvent soundevent = this.getPaddleSound();
               if (soundevent != null) {
                  Vec3 vec3 = this.getViewVector(1.0F);
                  double d0 = i == 1 ? -vec3.z : vec3.z;
                  double d1 = i == 1 ? vec3.x : -vec3.x;
                  this.level().playSound((Player)null, this.getX() + d0, this.getY(), this.getZ() + d1, soundevent, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
               }
            }

            this.paddlePositions[i] += ((float)Math.PI / 8F);
         } else {
            this.paddlePositions[i] = 0.0F;
         }
      }

      this.checkInsideBlocks();
      List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate((double)0.2F, (double)-0.01F, (double)0.2F), EntitySelector.pushableBy(this));
      if (!list.isEmpty()) {
         boolean flag = !this.level().isClientSide && !(this.getControllingPassenger() instanceof Player);

         for(int j = 0; j < list.size(); ++j) {
            Entity entity = list.get(j);
            if (!entity.hasPassenger(this)) {
               if (flag && this.getPassengers().size() < this.getMaxPassengers() && !entity.isPassenger() && this.hasEnoughSpaceFor(entity) && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player)) {
                  entity.startRiding(this);
               } else {
                  this.push(entity);
               }
            }
         }
      }

   }

   private void tickBubbleColumn() {
      if (this.level().isClientSide) {
         int i = this.getBubbleTime();
         if (i > 0) {
            this.bubbleMultiplier += 0.05F;
         } else {
            this.bubbleMultiplier -= 0.1F;
         }

         this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0F, 1.0F);
         this.bubbleAngleO = this.bubbleAngle;
         this.bubbleAngle = 10.0F * (float)Math.sin((double)(0.5F * (float)this.level().getGameTime())) * this.bubbleMultiplier;
      } else {
         if (!this.isAboveBubbleColumn) {
            this.setBubbleTime(0);
         }

         int j = this.getBubbleTime();
         if (j > 0) {
            --j;
            this.setBubbleTime(j);
            int k = 60 - j - 1;
            if (k > 0 && j == 0) {
               this.setBubbleTime(0);
               Vec3 vec3 = this.getDeltaMovement();
               if (this.bubbleColumnDirectionIsDown) {
                  this.setDeltaMovement(vec3.add(0.0D, -0.7D, 0.0D));
                  this.ejectPassengers();
               } else {
                  this.setDeltaMovement(vec3.x, this.hasPassenger((entity) -> entity instanceof Player) ? 2.7D : 0.6D, vec3.z);
               }
            }

            this.isAboveBubbleColumn = false;
         }
      }

   }

   @Nullable
   protected SoundEvent getPaddleSound() {
      switch (this.getStatus()) {
         case IN_WATER:
         case UNDER_WATER:
         case UNDER_FLOWING_WATER:
            return SoundEvents.BOAT_PADDLE_WATER;
         case ON_LAND:
            return SoundEvents.BOAT_PADDLE_LAND;
         case IN_AIR:
         default:
            return null;
      }
   }

   private void tickLerp() {
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
      }
   }

   public void setPaddleState(boolean flag, boolean flag1) {
      this.entityData.set(DATA_ID_PADDLE_LEFT, flag);
      this.entityData.set(DATA_ID_PADDLE_RIGHT, flag1);
   }

   public float getRowingTime(int i, float f) {
      return this.getPaddleState(i) ? Mth.clampedLerp(this.paddlePositions[i] - ((float)Math.PI / 8F), this.paddlePositions[i], f) : 0.0F;
   }

   private Boat.Status getStatus() {
      Boat.Status boat_status = this.isUnderwater();
      if (boat_status != null) {
         this.waterLevel = this.getBoundingBox().maxY;
         return boat_status;
      } else if (this.checkInWater()) {
         return Boat.Status.IN_WATER;
      } else {
         float f = this.getGroundFriction();
         if (f > 0.0F) {
            this.landFriction = f;
            return Boat.Status.ON_LAND;
         } else {
            return Boat.Status.IN_AIR;
         }
      }
   }

   public float getWaterLevelAbove() {
      AABB aabb = this.getBoundingBox();
      int i = Mth.floor(aabb.minX);
      int j = Mth.ceil(aabb.maxX);
      int k = Mth.floor(aabb.maxY);
      int l = Mth.ceil(aabb.maxY - this.lastYd);
      int i1 = Mth.floor(aabb.minZ);
      int j1 = Mth.ceil(aabb.maxZ);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      label39:
      for(int k1 = k; k1 < l; ++k1) {
         float f = 0.0F;

         for(int l1 = i; l1 < j; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               blockpos_mutableblockpos.set(l1, k1, i2);
               FluidState fluidstate = this.level().getFluidState(blockpos_mutableblockpos);
               if (fluidstate.is(FluidTags.WATER)) {
                  f = Math.max(f, fluidstate.getHeight(this.level(), blockpos_mutableblockpos));
               }

               if (f >= 1.0F) {
                  continue label39;
               }
            }
         }

         if (f < 1.0F) {
            return (float)blockpos_mutableblockpos.getY() + f;
         }
      }

      return (float)(l + 1);
   }

   public float getGroundFriction() {
      AABB aabb = this.getBoundingBox();
      AABB aabb1 = new AABB(aabb.minX, aabb.minY - 0.001D, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
      int i = Mth.floor(aabb1.minX) - 1;
      int j = Mth.ceil(aabb1.maxX) + 1;
      int k = Mth.floor(aabb1.minY) - 1;
      int l = Mth.ceil(aabb1.maxY) + 1;
      int i1 = Mth.floor(aabb1.minZ) - 1;
      int j1 = Mth.ceil(aabb1.maxZ) + 1;
      VoxelShape voxelshape = Shapes.create(aabb1);
      float f = 0.0F;
      int k1 = 0;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l1 = i; l1 < j; ++l1) {
         for(int i2 = i1; i2 < j1; ++i2) {
            int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
            if (j2 != 2) {
               for(int k2 = k; k2 < l; ++k2) {
                  if (j2 <= 0 || k2 != k && k2 != l - 1) {
                     blockpos_mutableblockpos.set(l1, k2, i2);
                     BlockState blockstate = this.level().getBlockState(blockpos_mutableblockpos);
                     if (!(blockstate.getBlock() instanceof WaterlilyBlock) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level(), blockpos_mutableblockpos).move((double)l1, (double)k2, (double)i2), voxelshape, BooleanOp.AND)) {
                        f += blockstate.getBlock().getFriction();
                        ++k1;
                     }
                  }
               }
            }
         }
      }

      return f / (float)k1;
   }

   private boolean checkInWater() {
      AABB aabb = this.getBoundingBox();
      int i = Mth.floor(aabb.minX);
      int j = Mth.ceil(aabb.maxX);
      int k = Mth.floor(aabb.minY);
      int l = Mth.ceil(aabb.minY + 0.001D);
      int i1 = Mth.floor(aabb.minZ);
      int j1 = Mth.ceil(aabb.maxZ);
      boolean flag = false;
      this.waterLevel = -Double.MAX_VALUE;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               blockpos_mutableblockpos.set(k1, l1, i2);
               FluidState fluidstate = this.level().getFluidState(blockpos_mutableblockpos);
               if (fluidstate.is(FluidTags.WATER)) {
                  float f = (float)l1 + fluidstate.getHeight(this.level(), blockpos_mutableblockpos);
                  this.waterLevel = Math.max((double)f, this.waterLevel);
                  flag |= aabb.minY < (double)f;
               }
            }
         }
      }

      return flag;
   }

   @Nullable
   private Boat.Status isUnderwater() {
      AABB aabb = this.getBoundingBox();
      double d0 = aabb.maxY + 0.001D;
      int i = Mth.floor(aabb.minX);
      int j = Mth.ceil(aabb.maxX);
      int k = Mth.floor(aabb.maxY);
      int l = Mth.ceil(d0);
      int i1 = Mth.floor(aabb.minZ);
      int j1 = Mth.ceil(aabb.maxZ);
      boolean flag = false;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               blockpos_mutableblockpos.set(k1, l1, i2);
               FluidState fluidstate = this.level().getFluidState(blockpos_mutableblockpos);
               if (fluidstate.is(FluidTags.WATER) && d0 < (double)((float)blockpos_mutableblockpos.getY() + fluidstate.getHeight(this.level(), blockpos_mutableblockpos))) {
                  if (!fluidstate.isSource()) {
                     return Boat.Status.UNDER_FLOWING_WATER;
                  }

                  flag = true;
               }
            }
         }
      }

      return flag ? Boat.Status.UNDER_WATER : null;
   }

   private void floatBoat() {
      double d0 = (double)-0.04F;
      double d1 = this.isNoGravity() ? 0.0D : (double)-0.04F;
      double d2 = 0.0D;
      this.invFriction = 0.05F;
      if (this.oldStatus == Boat.Status.IN_AIR && this.status != Boat.Status.IN_AIR && this.status != Boat.Status.ON_LAND) {
         this.waterLevel = this.getY(1.0D);
         this.setPos(this.getX(), (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101D, this.getZ());
         this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
         this.lastYd = 0.0D;
         this.status = Boat.Status.IN_WATER;
      } else {
         if (this.status == Boat.Status.IN_WATER) {
            d2 = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
            this.invFriction = 0.9F;
         } else if (this.status == Boat.Status.UNDER_FLOWING_WATER) {
            d1 = -7.0E-4D;
            this.invFriction = 0.9F;
         } else if (this.status == Boat.Status.UNDER_WATER) {
            d2 = (double)0.01F;
            this.invFriction = 0.45F;
         } else if (this.status == Boat.Status.IN_AIR) {
            this.invFriction = 0.9F;
         } else if (this.status == Boat.Status.ON_LAND) {
            this.invFriction = this.landFriction;
            if (this.getControllingPassenger() instanceof Player) {
               this.landFriction /= 2.0F;
            }
         }

         Vec3 vec3 = this.getDeltaMovement();
         this.setDeltaMovement(vec3.x * (double)this.invFriction, vec3.y + d1, vec3.z * (double)this.invFriction);
         this.deltaRotation *= this.invFriction;
         if (d2 > 0.0D) {
            Vec3 vec31 = this.getDeltaMovement();
            this.setDeltaMovement(vec31.x, (vec31.y + d2 * 0.06153846016296973D) * 0.75D, vec31.z);
         }
      }

   }

   private void controlBoat() {
      if (this.isVehicle()) {
         float f = 0.0F;
         if (this.inputLeft) {
            --this.deltaRotation;
         }

         if (this.inputRight) {
            ++this.deltaRotation;
         }

         if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            f += 0.005F;
         }

         this.setYRot(this.getYRot() + this.deltaRotation);
         if (this.inputUp) {
            f += 0.04F;
         }

         if (this.inputDown) {
            f -= 0.005F;
         }

         this.setDeltaMovement(this.getDeltaMovement().add((double)(Mth.sin(-this.getYRot() * ((float)Math.PI / 180F)) * f), 0.0D, (double)(Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * f)));
         this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
      }
   }

   protected float getSinglePassengerXOffset() {
      return 0.0F;
   }

   public boolean hasEnoughSpaceFor(Entity entity) {
      return entity.getBbWidth() < this.getBbWidth();
   }

   protected void positionRider(Entity entity, Entity.MoveFunction entity_movefunction) {
      if (this.hasPassenger(entity)) {
         float f = this.getSinglePassengerXOffset();
         float f1 = (float)((this.isRemoved() ? (double)0.01F : this.getPassengersRidingOffset()) + entity.getMyRidingOffset());
         if (this.getPassengers().size() > 1) {
            int i = this.getPassengers().indexOf(entity);
            if (i == 0) {
               f = 0.2F;
            } else {
               f = -0.6F;
            }

            if (entity instanceof Animal) {
               f += 0.2F;
            }
         }

         Vec3 vec3 = (new Vec3((double)f, 0.0D, 0.0D)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         entity_movefunction.accept(entity, this.getX() + vec3.x, this.getY() + (double)f1, this.getZ() + vec3.z);
         entity.setYRot(entity.getYRot() + this.deltaRotation);
         entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
         this.clampRotation(entity);
         if (entity instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
            int j = entity.getId() % 2 == 0 ? 90 : 270;
            entity.setYBodyRot(((Animal)entity).yBodyRot + (float)j);
            entity.setYHeadRot(entity.getYHeadRot() + (float)j);
         }

      }
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity livingentity) {
      Vec3 vec3 = getCollisionHorizontalEscapeVector((double)(this.getBbWidth() * Mth.SQRT_OF_TWO), (double)livingentity.getBbWidth(), livingentity.getYRot());
      double d0 = this.getX() + vec3.x;
      double d1 = this.getZ() + vec3.z;
      BlockPos blockpos = BlockPos.containing(d0, this.getBoundingBox().maxY, d1);
      BlockPos blockpos1 = blockpos.below();
      if (!this.level().isWaterAt(blockpos1)) {
         List<Vec3> list = Lists.newArrayList();
         double d2 = this.level().getBlockFloorHeight(blockpos);
         if (DismountHelper.isBlockFloorValid(d2)) {
            list.add(new Vec3(d0, (double)blockpos.getY() + d2, d1));
         }

         double d3 = this.level().getBlockFloorHeight(blockpos1);
         if (DismountHelper.isBlockFloorValid(d3)) {
            list.add(new Vec3(d0, (double)blockpos1.getY() + d3, d1));
         }

         for(Pose pose : livingentity.getDismountPoses()) {
            for(Vec3 vec31 : list) {
               if (DismountHelper.canDismountTo(this.level(), vec31, livingentity, pose)) {
                  livingentity.setPose(pose);
                  return vec31;
               }
            }
         }
      }

      return super.getDismountLocationForPassenger(livingentity);
   }

   protected void clampRotation(Entity entity) {
      entity.setYBodyRot(this.getYRot());
      float f = Mth.wrapDegrees(entity.getYRot() - this.getYRot());
      float f1 = Mth.clamp(f, -105.0F, 105.0F);
      entity.yRotO += f1 - f;
      entity.setYRot(entity.getYRot() + f1 - f);
      entity.setYHeadRot(entity.getYRot());
   }

   public void onPassengerTurned(Entity entity) {
      this.clampRotation(entity);
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putString("Type", this.getVariant().getSerializedName());
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.contains("Type", 8)) {
         this.setVariant(Boat.Type.byName(compoundtag.getString("Type")));
      }

   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      if (player.isSecondaryUseActive()) {
         return InteractionResult.PASS;
      } else if (this.outOfControlTicks < 60.0F) {
         if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
         } else {
            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   protected void checkFallDamage(double d0, boolean flag, BlockState blockstate, BlockPos blockpos) {
      this.lastYd = this.getDeltaMovement().y;
      if (!this.isPassenger()) {
         if (flag) {
            if (this.fallDistance > 3.0F) {
               if (this.status != Boat.Status.ON_LAND) {
                  this.resetFallDistance();
                  return;
               }

               this.causeFallDamage(this.fallDistance, 1.0F, this.damageSources().fall());
               if (!this.level().isClientSide && !this.isRemoved()) {
                  this.kill();
                  if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                     for(int i = 0; i < 3; ++i) {
                        this.spawnAtLocation(this.getVariant().getPlanks());
                     }

                     for(int j = 0; j < 2; ++j) {
                        this.spawnAtLocation(Items.STICK);
                     }
                  }
               }
            }

            this.resetFallDistance();
         } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && d0 < 0.0D) {
            this.fallDistance -= (float)d0;
         }

      }
   }

   public boolean getPaddleState(int i) {
      return this.entityData.<Boolean>get(i == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) && this.getControllingPassenger() != null;
   }

   public void setDamage(float f) {
      this.entityData.set(DATA_ID_DAMAGE, f);
   }

   public float getDamage() {
      return this.entityData.get(DATA_ID_DAMAGE);
   }

   public void setHurtTime(int i) {
      this.entityData.set(DATA_ID_HURT, i);
   }

   public int getHurtTime() {
      return this.entityData.get(DATA_ID_HURT);
   }

   private void setBubbleTime(int i) {
      this.entityData.set(DATA_ID_BUBBLE_TIME, i);
   }

   private int getBubbleTime() {
      return this.entityData.get(DATA_ID_BUBBLE_TIME);
   }

   public float getBubbleAngle(float f) {
      return Mth.lerp(f, this.bubbleAngleO, this.bubbleAngle);
   }

   public void setHurtDir(int i) {
      this.entityData.set(DATA_ID_HURTDIR, i);
   }

   public int getHurtDir() {
      return this.entityData.get(DATA_ID_HURTDIR);
   }

   public void setVariant(Boat.Type boat_type) {
      this.entityData.set(DATA_ID_TYPE, boat_type.ordinal());
   }

   public Boat.Type getVariant() {
      return Boat.Type.byId(this.entityData.get(DATA_ID_TYPE));
   }

   protected boolean canAddPassenger(Entity entity) {
      return this.getPassengers().size() < this.getMaxPassengers() && !this.isEyeInFluid(FluidTags.WATER);
   }

   protected int getMaxPassengers() {
      return 2;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity var2 = this.getFirstPassenger();
      LivingEntity var10000;
      if (var2 instanceof LivingEntity livingentity) {
         var10000 = livingentity;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public void setInput(boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      this.inputLeft = flag;
      this.inputRight = flag1;
      this.inputUp = flag2;
      this.inputDown = flag3;
   }

   public boolean isUnderWater() {
      return this.status == Boat.Status.UNDER_WATER || this.status == Boat.Status.UNDER_FLOWING_WATER;
   }

   public ItemStack getPickResult() {
      return new ItemStack(this.getDropItem());
   }

   public static enum Status {
      IN_WATER,
      UNDER_WATER,
      UNDER_FLOWING_WATER,
      ON_LAND,
      IN_AIR;
   }

   public static enum Type implements StringRepresentable {
      OAK(Blocks.OAK_PLANKS, "oak"),
      SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
      BIRCH(Blocks.BIRCH_PLANKS, "birch"),
      JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
      ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
      CHERRY(Blocks.CHERRY_PLANKS, "cherry"),
      DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak"),
      MANGROVE(Blocks.MANGROVE_PLANKS, "mangrove"),
      BAMBOO(Blocks.BAMBOO_PLANKS, "bamboo");

      private final String name;
      private final Block planks;
      public static final StringRepresentable.EnumCodec<Boat.Type> CODEC = StringRepresentable.fromEnum(Boat.Type::values);
      private static final IntFunction<Boat.Type> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

      private Type(Block block, String s) {
         this.name = s;
         this.planks = block;
      }

      public String getSerializedName() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }

      public Block getPlanks() {
         return this.planks;
      }

      public String toString() {
         return this.name;
      }

      public static Boat.Type byId(int i) {
         return BY_ID.apply(i);
      }

      public static Boat.Type byName(String s) {
         return CODEC.byName(s, OAK);
      }
   }
}
