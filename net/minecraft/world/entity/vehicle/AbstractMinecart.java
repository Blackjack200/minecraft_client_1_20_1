package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecart extends Entity {
   private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
   private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1));
   protected static final float WATER_SLOWDOWN_FACTOR = 0.95F;
   private boolean flipped;
   private boolean onRails;
   private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), (enummap) -> {
      Vec3i vec3i = Direction.WEST.getNormal();
      Vec3i vec3i1 = Direction.EAST.getNormal();
      Vec3i vec3i2 = Direction.NORTH.getNormal();
      Vec3i vec3i3 = Direction.SOUTH.getNormal();
      Vec3i vec3i4 = vec3i.below();
      Vec3i vec3i5 = vec3i1.below();
      Vec3i vec3i6 = vec3i2.below();
      Vec3i vec3i7 = vec3i3.below();
      enummap.put(RailShape.NORTH_SOUTH, Pair.of(vec3i2, vec3i3));
      enummap.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i1));
      enummap.put(RailShape.ASCENDING_EAST, Pair.of(vec3i4, vec3i1));
      enummap.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i5));
      enummap.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i2, vec3i7));
      enummap.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i6, vec3i3));
      enummap.put(RailShape.SOUTH_EAST, Pair.of(vec3i3, vec3i1));
      enummap.put(RailShape.SOUTH_WEST, Pair.of(vec3i3, vec3i));
      enummap.put(RailShape.NORTH_WEST, Pair.of(vec3i2, vec3i));
      enummap.put(RailShape.NORTH_EAST, Pair.of(vec3i2, vec3i1));
   });
   private int lSteps;
   private double lx;
   private double ly;
   private double lz;
   private double lyr;
   private double lxr;
   private double lxd;
   private double lyd;
   private double lzd;

   protected AbstractMinecart(EntityType<?> entitytype, Level level) {
      super(entitytype, level);
      this.blocksBuilding = true;
   }

   protected AbstractMinecart(EntityType<?> entitytype, Level level, double d0, double d1, double d2) {
      this(entitytype, level);
      this.setPos(d0, d1, d2);
      this.xo = d0;
      this.yo = d1;
      this.zo = d2;
   }

   public static AbstractMinecart createMinecart(Level level, double d0, double d1, double d2, AbstractMinecart.Type abstractminecart_type) {
      if (abstractminecart_type == AbstractMinecart.Type.CHEST) {
         return new MinecartChest(level, d0, d1, d2);
      } else if (abstractminecart_type == AbstractMinecart.Type.FURNACE) {
         return new MinecartFurnace(level, d0, d1, d2);
      } else if (abstractminecart_type == AbstractMinecart.Type.TNT) {
         return new MinecartTNT(level, d0, d1, d2);
      } else if (abstractminecart_type == AbstractMinecart.Type.SPAWNER) {
         return new MinecartSpawner(level, d0, d1, d2);
      } else if (abstractminecart_type == AbstractMinecart.Type.HOPPER) {
         return new MinecartHopper(level, d0, d1, d2);
      } else {
         return (AbstractMinecart)(abstractminecart_type == AbstractMinecart.Type.COMMAND_BLOCK ? new MinecartCommandBlock(level, d0, d1, d2) : new Minecart(level, d0, d1, d2));
      }
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_HURT, 0);
      this.entityData.define(DATA_ID_HURTDIR, 1);
      this.entityData.define(DATA_ID_DAMAGE, 0.0F);
      this.entityData.define(DATA_ID_DISPLAY_BLOCK, Block.getId(Blocks.AIR.defaultBlockState()));
      this.entityData.define(DATA_ID_DISPLAY_OFFSET, 6);
      this.entityData.define(DATA_ID_CUSTOM_DISPLAY, false);
   }

   public boolean canCollideWith(Entity entity) {
      return Boat.canVehicleCollide(this, entity);
   }

   public boolean isPushable() {
      return true;
   }

   protected Vec3 getRelativePortalPosition(Direction.Axis direction_axis, BlockUtil.FoundRectangle blockutil_foundrectangle) {
      return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(direction_axis, blockutil_foundrectangle));
   }

   public double getPassengersRidingOffset() {
      return 0.0D;
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity livingentity) {
      Direction direction = this.getMotionDirection();
      if (direction.getAxis() == Direction.Axis.Y) {
         return super.getDismountLocationForPassenger(livingentity);
      } else {
         int[][] aint = DismountHelper.offsetsForDirection(direction);
         BlockPos blockpos = this.blockPosition();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
         ImmutableList<Pose> immutablelist = livingentity.getDismountPoses();

         for(Pose pose : immutablelist) {
            EntityDimensions entitydimensions = livingentity.getDimensions(pose);
            float f = Math.min(entitydimensions.width, 1.0F) / 2.0F;

            for(int i : POSE_DISMOUNT_HEIGHTS.get(pose)) {
               for(int[] aint1 : aint) {
                  blockpos_mutableblockpos.set(blockpos.getX() + aint1[0], blockpos.getY() + i, blockpos.getZ() + aint1[1]);
                  double d0 = this.level().getBlockFloorHeight(DismountHelper.nonClimbableShape(this.level(), blockpos_mutableblockpos), () -> DismountHelper.nonClimbableShape(this.level(), blockpos_mutableblockpos.below()));
                  if (DismountHelper.isBlockFloorValid(d0)) {
                     AABB aabb = new AABB((double)(-f), 0.0D, (double)(-f), (double)f, (double)entitydimensions.height, (double)f);
                     Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos_mutableblockpos, d0);
                     if (DismountHelper.canDismountTo(this.level(), livingentity, aabb.move(vec3))) {
                        livingentity.setPose(pose);
                        return vec3;
                     }
                  }
               }
            }
         }

         double d1 = this.getBoundingBox().maxY;
         blockpos_mutableblockpos.set((double)blockpos.getX(), d1, (double)blockpos.getZ());

         for(Pose pose1 : immutablelist) {
            double d2 = (double)livingentity.getDimensions(pose1).height;
            int j = Mth.ceil(d1 - (double)blockpos_mutableblockpos.getY() + d2);
            double d3 = DismountHelper.findCeilingFrom(blockpos_mutableblockpos, j, (blockpos1) -> this.level().getBlockState(blockpos1).getCollisionShape(this.level(), blockpos1));
            if (d1 + d2 <= d3) {
               livingentity.setPose(pose1);
               break;
            }
         }

         return super.getDismountLocationForPassenger(livingentity);
      }
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (!this.level().isClientSide && !this.isRemoved()) {
         if (this.isInvulnerableTo(damagesource)) {
            return false;
         } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + f * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
            boolean flag = damagesource.getEntity() instanceof Player && ((Player)damagesource.getEntity()).getAbilities().instabuild;
            if (flag || this.getDamage() > 40.0F) {
               this.ejectPassengers();
               if (flag && !this.hasCustomName()) {
                  this.discard();
               } else {
                  this.destroy(damagesource);
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   protected float getBlockSpeedFactor() {
      BlockState blockstate = this.level().getBlockState(this.blockPosition());
      return blockstate.is(BlockTags.RAILS) ? 1.0F : super.getBlockSpeedFactor();
   }

   public void destroy(DamageSource damagesource) {
      this.kill();
      if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         ItemStack itemstack = new ItemStack(this.getDropItem());
         if (this.hasCustomName()) {
            itemstack.setHoverName(this.getCustomName());
         }

         this.spawnAtLocation(itemstack);
      }

   }

   abstract Item getDropItem();

   public void animateHurt(float f) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   private static Pair<Vec3i, Vec3i> exits(RailShape railshape) {
      return EXITS.get(railshape);
   }

   public Direction getMotionDirection() {
      return this.flipped ? this.getDirection().getOpposite().getClockWise() : this.getDirection().getClockWise();
   }

   public void tick() {
      if (this.getHurtTime() > 0) {
         this.setHurtTime(this.getHurtTime() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      this.checkBelowWorld();
      this.handleNetherPortal();
      if (this.level().isClientSide) {
         if (this.lSteps > 0) {
            double d0 = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
            double d1 = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
            double d2 = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
            double d3 = Mth.wrapDegrees(this.lyr - (double)this.getYRot());
            this.setYRot(this.getYRot() + (float)d3 / (float)this.lSteps);
            this.setXRot(this.getXRot() + (float)(this.lxr - (double)this.getXRot()) / (float)this.lSteps);
            --this.lSteps;
            this.setPos(d0, d1, d2);
            this.setRot(this.getYRot(), this.getXRot());
         } else {
            this.reapplyPosition();
            this.setRot(this.getYRot(), this.getXRot());
         }

      } else {
         if (!this.isNoGravity()) {
            double d4 = this.isInWater() ? -0.005D : -0.04D;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d4, 0.0D));
         }

         int i = Mth.floor(this.getX());
         int j = Mth.floor(this.getY());
         int k = Mth.floor(this.getZ());
         if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
            --j;
         }

         BlockPos blockpos = new BlockPos(i, j, k);
         BlockState blockstate = this.level().getBlockState(blockpos);
         this.onRails = BaseRailBlock.isRail(blockstate);
         if (this.onRails) {
            this.moveAlongTrack(blockpos, blockstate);
            if (blockstate.is(Blocks.ACTIVATOR_RAIL)) {
               this.activateMinecart(i, j, k, blockstate.getValue(PoweredRailBlock.POWERED));
            }
         } else {
            this.comeOffTrack();
         }

         this.checkInsideBlocks();
         this.setXRot(0.0F);
         double d5 = this.xo - this.getX();
         double d6 = this.zo - this.getZ();
         if (d5 * d5 + d6 * d6 > 0.001D) {
            this.setYRot((float)(Mth.atan2(d6, d5) * 180.0D / Math.PI));
            if (this.flipped) {
               this.setYRot(this.getYRot() + 180.0F);
            }
         }

         double d7 = (double)Mth.wrapDegrees(this.getYRot() - this.yRotO);
         if (d7 < -170.0D || d7 >= 170.0D) {
            this.setYRot(this.getYRot() + 180.0F);
            this.flipped = !this.flipped;
         }

         this.setRot(this.getYRot(), this.getXRot());
         if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
            List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate((double)0.2F, 0.0D, (double)0.2F), EntitySelector.pushableBy(this));
            if (!list.isEmpty()) {
               for(int l = 0; l < list.size(); ++l) {
                  Entity entity = list.get(l);
                  if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.isVehicle() && !entity.isPassenger()) {
                     entity.startRiding(this);
                  } else {
                     entity.push(this);
                  }
               }
            }
         } else {
            for(Entity entity1 : this.level().getEntities(this, this.getBoundingBox().inflate((double)0.2F, 0.0D, (double)0.2F))) {
               if (!this.hasPassenger(entity1) && entity1.isPushable() && entity1 instanceof AbstractMinecart) {
                  entity1.push(this);
               }
            }
         }

         this.updateInWaterStateAndDoFluidPushing();
         if (this.isInLava()) {
            this.lavaHurt();
            this.fallDistance *= 0.5F;
         }

         this.firstTick = false;
      }
   }

   protected double getMaxSpeed() {
      return (this.isInWater() ? 4.0D : 8.0D) / 20.0D;
   }

   public void activateMinecart(int i, int j, int k, boolean flag) {
   }

   protected void comeOffTrack() {
      double d0 = this.getMaxSpeed();
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(Mth.clamp(vec3.x, -d0, d0), vec3.y, Mth.clamp(vec3.z, -d0, d0));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      if (!this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.95D));
      }

   }

   protected void moveAlongTrack(BlockPos blockpos, BlockState blockstate) {
      this.resetFallDistance();
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      Vec3 vec3 = this.getPos(d0, d1, d2);
      d1 = (double)blockpos.getY();
      boolean flag = false;
      boolean flag1 = false;
      if (blockstate.is(Blocks.POWERED_RAIL)) {
         flag = blockstate.getValue(PoweredRailBlock.POWERED);
         flag1 = !flag;
      }

      double d3 = 0.0078125D;
      if (this.isInWater()) {
         d3 *= 0.2D;
      }

      Vec3 vec31 = this.getDeltaMovement();
      RailShape railshape = blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty());
      switch (railshape) {
         case ASCENDING_EAST:
            this.setDeltaMovement(vec31.add(-d3, 0.0D, 0.0D));
            ++d1;
            break;
         case ASCENDING_WEST:
            this.setDeltaMovement(vec31.add(d3, 0.0D, 0.0D));
            ++d1;
            break;
         case ASCENDING_NORTH:
            this.setDeltaMovement(vec31.add(0.0D, 0.0D, d3));
            ++d1;
            break;
         case ASCENDING_SOUTH:
            this.setDeltaMovement(vec31.add(0.0D, 0.0D, -d3));
            ++d1;
      }

      vec31 = this.getDeltaMovement();
      Pair<Vec3i, Vec3i> pair = exits(railshape);
      Vec3i vec3i = pair.getFirst();
      Vec3i vec3i1 = pair.getSecond();
      double d4 = (double)(vec3i1.getX() - vec3i.getX());
      double d5 = (double)(vec3i1.getZ() - vec3i.getZ());
      double d6 = Math.sqrt(d4 * d4 + d5 * d5);
      double d7 = vec31.x * d4 + vec31.z * d5;
      if (d7 < 0.0D) {
         d4 = -d4;
         d5 = -d5;
      }

      double d8 = Math.min(2.0D, vec31.horizontalDistance());
      vec31 = new Vec3(d8 * d4 / d6, vec31.y, d8 * d5 / d6);
      this.setDeltaMovement(vec31);
      Entity entity = this.getFirstPassenger();
      if (entity instanceof Player) {
         Vec3 vec32 = entity.getDeltaMovement();
         double d9 = vec32.horizontalDistanceSqr();
         double d10 = this.getDeltaMovement().horizontalDistanceSqr();
         if (d9 > 1.0E-4D && d10 < 0.01D) {
            this.setDeltaMovement(this.getDeltaMovement().add(vec32.x * 0.1D, 0.0D, vec32.z * 0.1D));
            flag1 = false;
         }
      }

      if (flag1) {
         double d11 = this.getDeltaMovement().horizontalDistance();
         if (d11 < 0.03D) {
            this.setDeltaMovement(Vec3.ZERO);
         } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
         }
      }

      double d12 = (double)blockpos.getX() + 0.5D + (double)vec3i.getX() * 0.5D;
      double d13 = (double)blockpos.getZ() + 0.5D + (double)vec3i.getZ() * 0.5D;
      double d14 = (double)blockpos.getX() + 0.5D + (double)vec3i1.getX() * 0.5D;
      double d15 = (double)blockpos.getZ() + 0.5D + (double)vec3i1.getZ() * 0.5D;
      d4 = d14 - d12;
      d5 = d15 - d13;
      double d16;
      if (d4 == 0.0D) {
         d16 = d2 - (double)blockpos.getZ();
      } else if (d5 == 0.0D) {
         d16 = d0 - (double)blockpos.getX();
      } else {
         double d18 = d0 - d12;
         double d19 = d2 - d13;
         d16 = (d18 * d4 + d19 * d5) * 2.0D;
      }

      d0 = d12 + d4 * d16;
      d2 = d13 + d5 * d16;
      this.setPos(d0, d1, d2);
      double d21 = this.isVehicle() ? 0.75D : 1.0D;
      double d22 = this.getMaxSpeed();
      vec31 = this.getDeltaMovement();
      this.move(MoverType.SELF, new Vec3(Mth.clamp(d21 * vec31.x, -d22, d22), 0.0D, Mth.clamp(d21 * vec31.z, -d22, d22)));
      if (vec3i.getY() != 0 && Mth.floor(this.getX()) - blockpos.getX() == vec3i.getX() && Mth.floor(this.getZ()) - blockpos.getZ() == vec3i.getZ()) {
         this.setPos(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
      } else if (vec3i1.getY() != 0 && Mth.floor(this.getX()) - blockpos.getX() == vec3i1.getX() && Mth.floor(this.getZ()) - blockpos.getZ() == vec3i1.getZ()) {
         this.setPos(this.getX(), this.getY() + (double)vec3i1.getY(), this.getZ());
      }

      this.applyNaturalSlowdown();
      Vec3 vec33 = this.getPos(this.getX(), this.getY(), this.getZ());
      if (vec33 != null && vec3 != null) {
         double d23 = (vec3.y - vec33.y) * 0.05D;
         Vec3 vec34 = this.getDeltaMovement();
         double d24 = vec34.horizontalDistance();
         if (d24 > 0.0D) {
            this.setDeltaMovement(vec34.multiply((d24 + d23) / d24, 1.0D, (d24 + d23) / d24));
         }

         this.setPos(this.getX(), vec33.y, this.getZ());
      }

      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getZ());
      if (i != blockpos.getX() || j != blockpos.getZ()) {
         Vec3 vec35 = this.getDeltaMovement();
         double d25 = vec35.horizontalDistance();
         this.setDeltaMovement(d25 * (double)(i - blockpos.getX()), vec35.y, d25 * (double)(j - blockpos.getZ()));
      }

      if (flag) {
         Vec3 vec36 = this.getDeltaMovement();
         double d26 = vec36.horizontalDistance();
         if (d26 > 0.01D) {
            double d27 = 0.06D;
            this.setDeltaMovement(vec36.add(vec36.x / d26 * 0.06D, 0.0D, vec36.z / d26 * 0.06D));
         } else {
            Vec3 vec37 = this.getDeltaMovement();
            double d28 = vec37.x;
            double d29 = vec37.z;
            if (railshape == RailShape.EAST_WEST) {
               if (this.isRedstoneConductor(blockpos.west())) {
                  d28 = 0.02D;
               } else if (this.isRedstoneConductor(blockpos.east())) {
                  d28 = -0.02D;
               }
            } else {
               if (railshape != RailShape.NORTH_SOUTH) {
                  return;
               }

               if (this.isRedstoneConductor(blockpos.north())) {
                  d29 = 0.02D;
               } else if (this.isRedstoneConductor(blockpos.south())) {
                  d29 = -0.02D;
               }
            }

            this.setDeltaMovement(d28, vec37.y, d29);
         }
      }

   }

   public boolean isOnRails() {
      return this.onRails;
   }

   private boolean isRedstoneConductor(BlockPos blockpos) {
      return this.level().getBlockState(blockpos).isRedstoneConductor(this.level(), blockpos);
   }

   protected void applyNaturalSlowdown() {
      double d0 = this.isVehicle() ? 0.997D : 0.96D;
      Vec3 vec3 = this.getDeltaMovement();
      vec3 = vec3.multiply(d0, 0.0D, d0);
      if (this.isInWater()) {
         vec3 = vec3.scale((double)0.95F);
      }

      this.setDeltaMovement(vec3);
   }

   @Nullable
   public Vec3 getPosOffs(double d0, double d1, double d2, double d3) {
      int i = Mth.floor(d0);
      int j = Mth.floor(d1);
      int k = Mth.floor(d2);
      if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
         --j;
      }

      BlockState blockstate = this.level().getBlockState(new BlockPos(i, j, k));
      if (BaseRailBlock.isRail(blockstate)) {
         RailShape railshape = blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty());
         d1 = (double)j;
         if (railshape.isAscending()) {
            d1 = (double)(j + 1);
         }

         Pair<Vec3i, Vec3i> pair = exits(railshape);
         Vec3i vec3i = pair.getFirst();
         Vec3i vec3i1 = pair.getSecond();
         double d4 = (double)(vec3i1.getX() - vec3i.getX());
         double d5 = (double)(vec3i1.getZ() - vec3i.getZ());
         double d6 = Math.sqrt(d4 * d4 + d5 * d5);
         d4 /= d6;
         d5 /= d6;
         d0 += d4 * d3;
         d2 += d5 * d3;
         if (vec3i.getY() != 0 && Mth.floor(d0) - i == vec3i.getX() && Mth.floor(d2) - k == vec3i.getZ()) {
            d1 += (double)vec3i.getY();
         } else if (vec3i1.getY() != 0 && Mth.floor(d0) - i == vec3i1.getX() && Mth.floor(d2) - k == vec3i1.getZ()) {
            d1 += (double)vec3i1.getY();
         }

         return this.getPos(d0, d1, d2);
      } else {
         return null;
      }
   }

   @Nullable
   public Vec3 getPos(double d0, double d1, double d2) {
      int i = Mth.floor(d0);
      int j = Mth.floor(d1);
      int k = Mth.floor(d2);
      if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
         --j;
      }

      BlockState blockstate = this.level().getBlockState(new BlockPos(i, j, k));
      if (BaseRailBlock.isRail(blockstate)) {
         RailShape railshape = blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty());
         Pair<Vec3i, Vec3i> pair = exits(railshape);
         Vec3i vec3i = pair.getFirst();
         Vec3i vec3i1 = pair.getSecond();
         double d3 = (double)i + 0.5D + (double)vec3i.getX() * 0.5D;
         double d4 = (double)j + 0.0625D + (double)vec3i.getY() * 0.5D;
         double d5 = (double)k + 0.5D + (double)vec3i.getZ() * 0.5D;
         double d6 = (double)i + 0.5D + (double)vec3i1.getX() * 0.5D;
         double d7 = (double)j + 0.0625D + (double)vec3i1.getY() * 0.5D;
         double d8 = (double)k + 0.5D + (double)vec3i1.getZ() * 0.5D;
         double d9 = d6 - d3;
         double d10 = (d7 - d4) * 2.0D;
         double d11 = d8 - d5;
         double d12;
         if (d9 == 0.0D) {
            d12 = d2 - (double)k;
         } else if (d11 == 0.0D) {
            d12 = d0 - (double)i;
         } else {
            double d14 = d0 - d3;
            double d15 = d2 - d5;
            d12 = (d14 * d9 + d15 * d11) * 2.0D;
         }

         d0 = d3 + d9 * d12;
         d1 = d4 + d10 * d12;
         d2 = d5 + d11 * d12;
         if (d10 < 0.0D) {
            ++d1;
         } else if (d10 > 0.0D) {
            d1 += 0.5D;
         }

         return new Vec3(d0, d1, d2);
      } else {
         return null;
      }
   }

   public AABB getBoundingBoxForCulling() {
      AABB aabb = this.getBoundingBox();
      return this.hasCustomDisplay() ? aabb.inflate((double)Math.abs(this.getDisplayOffset()) / 16.0D) : aabb;
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.getBoolean("CustomDisplayTile")) {
         this.setDisplayBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundtag.getCompound("DisplayState")));
         this.setDisplayOffset(compoundtag.getInt("DisplayOffset"));
      }

   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      if (this.hasCustomDisplay()) {
         compoundtag.putBoolean("CustomDisplayTile", true);
         compoundtag.put("DisplayState", NbtUtils.writeBlockState(this.getDisplayBlockState()));
         compoundtag.putInt("DisplayOffset", this.getDisplayOffset());
      }

   }

   public void push(Entity entity) {
      if (!this.level().isClientSide) {
         if (!entity.noPhysics && !this.noPhysics) {
            if (!this.hasPassenger(entity)) {
               double d0 = entity.getX() - this.getX();
               double d1 = entity.getZ() - this.getZ();
               double d2 = d0 * d0 + d1 * d1;
               if (d2 >= (double)1.0E-4F) {
                  d2 = Math.sqrt(d2);
                  d0 /= d2;
                  d1 /= d2;
                  double d3 = 1.0D / d2;
                  if (d3 > 1.0D) {
                     d3 = 1.0D;
                  }

                  d0 *= d3;
                  d1 *= d3;
                  d0 *= (double)0.1F;
                  d1 *= (double)0.1F;
                  d0 *= 0.5D;
                  d1 *= 0.5D;
                  if (entity instanceof AbstractMinecart) {
                     double d4 = entity.getX() - this.getX();
                     double d5 = entity.getZ() - this.getZ();
                     Vec3 vec3 = (new Vec3(d4, 0.0D, d5)).normalize();
                     Vec3 vec31 = (new Vec3((double)Mth.cos(this.getYRot() * ((float)Math.PI / 180F)), 0.0D, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)))).normalize();
                     double d6 = Math.abs(vec3.dot(vec31));
                     if (d6 < (double)0.8F) {
                        return;
                     }

                     Vec3 vec32 = this.getDeltaMovement();
                     Vec3 vec33 = entity.getDeltaMovement();
                     if (((AbstractMinecart)entity).getMinecartType() == AbstractMinecart.Type.FURNACE && this.getMinecartType() != AbstractMinecart.Type.FURNACE) {
                        this.setDeltaMovement(vec32.multiply(0.2D, 1.0D, 0.2D));
                        this.push(vec33.x - d0, 0.0D, vec33.z - d1);
                        entity.setDeltaMovement(vec33.multiply(0.95D, 1.0D, 0.95D));
                     } else if (((AbstractMinecart)entity).getMinecartType() != AbstractMinecart.Type.FURNACE && this.getMinecartType() == AbstractMinecart.Type.FURNACE) {
                        entity.setDeltaMovement(vec33.multiply(0.2D, 1.0D, 0.2D));
                        entity.push(vec32.x + d0, 0.0D, vec32.z + d1);
                        this.setDeltaMovement(vec32.multiply(0.95D, 1.0D, 0.95D));
                     } else {
                        double d7 = (vec33.x + vec32.x) / 2.0D;
                        double d8 = (vec33.z + vec32.z) / 2.0D;
                        this.setDeltaMovement(vec32.multiply(0.2D, 1.0D, 0.2D));
                        this.push(d7 - d0, 0.0D, d8 - d1);
                        entity.setDeltaMovement(vec33.multiply(0.2D, 1.0D, 0.2D));
                        entity.push(d7 + d0, 0.0D, d8 + d1);
                     }
                  } else {
                     this.push(-d0, 0.0D, -d1);
                     entity.push(d0 / 4.0D, 0.0D, d1 / 4.0D);
                  }
               }

            }
         }
      }
   }

   public void lerpTo(double d0, double d1, double d2, float f, float f1, int i, boolean flag) {
      this.lx = d0;
      this.ly = d1;
      this.lz = d2;
      this.lyr = (double)f;
      this.lxr = (double)f1;
      this.lSteps = i + 2;
      this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
   }

   public void lerpMotion(double d0, double d1, double d2) {
      this.lxd = d0;
      this.lyd = d1;
      this.lzd = d2;
      this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
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

   public void setHurtDir(int i) {
      this.entityData.set(DATA_ID_HURTDIR, i);
   }

   public int getHurtDir() {
      return this.entityData.get(DATA_ID_HURTDIR);
   }

   public abstract AbstractMinecart.Type getMinecartType();

   public BlockState getDisplayBlockState() {
      return !this.hasCustomDisplay() ? this.getDefaultDisplayBlockState() : Block.stateById(this.getEntityData().get(DATA_ID_DISPLAY_BLOCK));
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.AIR.defaultBlockState();
   }

   public int getDisplayOffset() {
      return !this.hasCustomDisplay() ? this.getDefaultDisplayOffset() : this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
   }

   public int getDefaultDisplayOffset() {
      return 6;
   }

   public void setDisplayBlockState(BlockState blockstate) {
      this.getEntityData().set(DATA_ID_DISPLAY_BLOCK, Block.getId(blockstate));
      this.setCustomDisplay(true);
   }

   public void setDisplayOffset(int i) {
      this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, i);
      this.setCustomDisplay(true);
   }

   public boolean hasCustomDisplay() {
      return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY);
   }

   public void setCustomDisplay(boolean flag) {
      this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY, flag);
   }

   public ItemStack getPickResult() {
      Item item;
      switch (this.getMinecartType()) {
         case FURNACE:
            item = Items.FURNACE_MINECART;
            break;
         case CHEST:
            item = Items.CHEST_MINECART;
            break;
         case TNT:
            item = Items.TNT_MINECART;
            break;
         case HOPPER:
            item = Items.HOPPER_MINECART;
            break;
         case COMMAND_BLOCK:
            item = Items.COMMAND_BLOCK_MINECART;
            break;
         default:
            item = Items.MINECART;
      }

      return new ItemStack(item);
   }

   public static enum Type {
      RIDEABLE,
      CHEST,
      FURNACE,
      TNT,
      SPAWNER,
      HOPPER,
      COMMAND_BLOCK;
   }
}
