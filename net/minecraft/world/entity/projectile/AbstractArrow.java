package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractArrow extends Projectile {
   private static final double ARROW_BASE_DAMAGE = 2.0D;
   private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
   private static final int FLAG_CRIT = 1;
   private static final int FLAG_NOPHYSICS = 2;
   private static final int FLAG_CROSSBOW = 4;
   @Nullable
   private BlockState lastState;
   protected boolean inGround;
   protected int inGroundTime;
   public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
   public int shakeTime;
   private int life;
   private double baseDamage = 2.0D;
   private int knockback;
   private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
   @Nullable
   private IntOpenHashSet piercingIgnoreEntityIds;
   @Nullable
   private List<Entity> piercedAndKilledEntities;

   protected AbstractArrow(EntityType<? extends AbstractArrow> entitytype, Level level) {
      super(entitytype, level);
   }

   protected AbstractArrow(EntityType<? extends AbstractArrow> entitytype, double d0, double d1, double d2, Level level) {
      this(entitytype, level);
      this.setPos(d0, d1, d2);
   }

   protected AbstractArrow(EntityType<? extends AbstractArrow> entitytype, LivingEntity livingentity, Level level) {
      this(entitytype, livingentity.getX(), livingentity.getEyeY() - (double)0.1F, livingentity.getZ(), level);
      this.setOwner(livingentity);
      if (livingentity instanceof Player) {
         this.pickup = AbstractArrow.Pickup.ALLOWED;
      }

   }

   public void setSoundEvent(SoundEvent soundevent) {
      this.soundEvent = soundevent;
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      double d1 = this.getBoundingBox().getSize() * 10.0D;
      if (Double.isNaN(d1)) {
         d1 = 1.0D;
      }

      d1 *= 64.0D * getViewScale();
      return d0 < d1 * d1;
   }

   protected void defineSynchedData() {
      this.entityData.define(ID_FLAGS, (byte)0);
      this.entityData.define(PIERCE_LEVEL, (byte)0);
   }

   public void shoot(double d0, double d1, double d2, float f, float f1) {
      super.shoot(d0, d1, d2, f, f1);
      this.life = 0;
   }

   public void lerpTo(double d0, double d1, double d2, float f, float f1, int i, boolean flag) {
      this.setPos(d0, d1, d2);
      this.setRot(f, f1);
   }

   public void lerpMotion(double d0, double d1, double d2) {
      super.lerpMotion(d0, d1, d2);
      this.life = 0;
   }

   public void tick() {
      super.tick();
      boolean flag = this.isNoPhysics();
      Vec3 vec3 = this.getDeltaMovement();
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         double d0 = vec3.horizontalDistance();
         this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
         this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }

      BlockPos blockpos = this.blockPosition();
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (!blockstate.isAir() && !flag) {
         VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
         if (!voxelshape.isEmpty()) {
            Vec3 vec31 = this.position();

            for(AABB aabb : voxelshape.toAabbs()) {
               if (aabb.move(blockpos).contains(vec31)) {
                  this.inGround = true;
                  break;
               }
            }
         }
      }

      if (this.shakeTime > 0) {
         --this.shakeTime;
      }

      if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
         this.clearFire();
      }

      if (this.inGround && !flag) {
         if (this.lastState != blockstate && this.shouldFall()) {
            this.startFalling();
         } else if (!this.level().isClientSide) {
            this.tickDespawn();
         }

         ++this.inGroundTime;
      } else {
         this.inGroundTime = 0;
         Vec3 vec32 = this.position();
         Vec3 vec33 = vec32.add(vec3);
         HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
         if (hitresult.getType() != HitResult.Type.MISS) {
            vec33 = hitresult.getLocation();
         }

         while(!this.isRemoved()) {
            EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
            if (entityhitresult != null) {
               hitresult = entityhitresult;
            }

            if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
               Entity entity = ((EntityHitResult)hitresult).getEntity();
               Entity entity1 = this.getOwner();
               if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                  hitresult = null;
                  entityhitresult = null;
               }
            }

            if (hitresult != null && !flag) {
               this.onHit(hitresult);
               this.hasImpulse = true;
            }

            if (entityhitresult == null || this.getPierceLevel() <= 0) {
               break;
            }

            hitresult = null;
         }

         vec3 = this.getDeltaMovement();
         double d1 = vec3.x;
         double d2 = vec3.y;
         double d3 = vec3.z;
         if (this.isCritArrow()) {
            for(int i = 0; i < 4; ++i) {
               this.level().addParticle(ParticleTypes.CRIT, this.getX() + d1 * (double)i / 4.0D, this.getY() + d2 * (double)i / 4.0D, this.getZ() + d3 * (double)i / 4.0D, -d1, -d2 + 0.2D, -d3);
            }
         }

         double d4 = this.getX() + d1;
         double d5 = this.getY() + d2;
         double d6 = this.getZ() + d3;
         double d7 = vec3.horizontalDistance();
         if (flag) {
            this.setYRot((float)(Mth.atan2(-d1, -d3) * (double)(180F / (float)Math.PI)));
         } else {
            this.setYRot((float)(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI)));
         }

         this.setXRot((float)(Mth.atan2(d2, d7) * (double)(180F / (float)Math.PI)));
         this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
         this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
         float f = 0.99F;
         float f1 = 0.05F;
         if (this.isInWater()) {
            for(int j = 0; j < 4; ++j) {
               float f2 = 0.25F;
               this.level().addParticle(ParticleTypes.BUBBLE, d4 - d1 * 0.25D, d5 - d2 * 0.25D, d6 - d3 * 0.25D, d1, d2, d3);
            }

            f = this.getWaterInertia();
         }

         this.setDeltaMovement(vec3.scale((double)f));
         if (!this.isNoGravity() && !flag) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double)0.05F, vec34.z);
         }

         this.setPos(d4, d5, d6);
         this.checkInsideBlocks();
      }
   }

   private boolean shouldFall() {
      return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
   }

   private void startFalling() {
      this.inGround = false;
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
      this.life = 0;
   }

   public void move(MoverType movertype, Vec3 vec3) {
      super.move(movertype, vec3);
      if (movertype != MoverType.SELF && this.shouldFall()) {
         this.startFalling();
      }

   }

   protected void tickDespawn() {
      ++this.life;
      if (this.life >= 1200) {
         this.discard();
      }

   }

   private void resetPiercedEntities() {
      if (this.piercedAndKilledEntities != null) {
         this.piercedAndKilledEntities.clear();
      }

      if (this.piercingIgnoreEntityIds != null) {
         this.piercingIgnoreEntityIds.clear();
      }

   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      Entity entity = entityhitresult.getEntity();
      float f = (float)this.getDeltaMovement().length();
      int i = Mth.ceil(Mth.clamp((double)f * this.baseDamage, 0.0D, 2.147483647E9D));
      if (this.getPierceLevel() > 0) {
         if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
         }

         if (this.piercedAndKilledEntities == null) {
            this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
         }

         if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
            this.discard();
            return;
         }

         this.piercingIgnoreEntityIds.add(entity.getId());
      }

      if (this.isCritArrow()) {
         long j = (long)this.random.nextInt(i / 2 + 2);
         i = (int)Math.min(j + (long)i, 2147483647L);
      }

      Entity entity1 = this.getOwner();
      DamageSource damagesource;
      if (entity1 == null) {
         damagesource = this.damageSources().arrow(this, this);
      } else {
         damagesource = this.damageSources().arrow(this, entity1);
         if (entity1 instanceof LivingEntity) {
            ((LivingEntity)entity1).setLastHurtMob(entity);
         }
      }

      boolean flag = entity.getType() == EntityType.ENDERMAN;
      int k = entity.getRemainingFireTicks();
      if (this.isOnFire() && !flag) {
         entity.setSecondsOnFire(5);
      }

      if (entity.hurt(damagesource, (float)i)) {
         if (flag) {
            return;
         }

         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
               livingentity.setArrowCount(livingentity.getArrowCount() + 1);
            }

            if (this.knockback > 0) {
               double d0 = Math.max(0.0D, 1.0D - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
               Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockback * 0.6D * d0);
               if (vec3.lengthSqr() > 0.0D) {
                  livingentity.push(vec3.x, 0.1D, vec3.z);
               }
            }

            if (!this.level().isClientSide && entity1 instanceof LivingEntity) {
               EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
               EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity);
            }

            this.doPostHurtEffects(livingentity);
            if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
               ((ServerPlayer)entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
            }

            if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
               this.piercedAndKilledEntities.add(livingentity);
            }

            if (!this.level().isClientSide && entity1 instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)entity1;
               if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
               } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(entity));
               }
            }
         }

         this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
         if (this.getPierceLevel() <= 0) {
            this.discard();
         }
      } else {
         entity.setRemainingFireTicks(k);
         this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
         this.setYRot(this.getYRot() + 180.0F);
         this.yRotO += 180.0F;
         if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
            if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
               this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.discard();
         }
      }

   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      this.lastState = this.level().getBlockState(blockhitresult.getBlockPos());
      super.onHitBlock(blockhitresult);
      Vec3 vec3 = blockhitresult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
      this.setDeltaMovement(vec3);
      Vec3 vec31 = vec3.normalize().scale((double)0.05F);
      this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
      this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      this.inGround = true;
      this.shakeTime = 7;
      this.setCritArrow(false);
      this.setPierceLevel((byte)0);
      this.setSoundEvent(SoundEvents.ARROW_HIT);
      this.setShotFromCrossbow(false);
      this.resetPiercedEntities();
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.ARROW_HIT;
   }

   protected final SoundEvent getHitGroundSoundEvent() {
      return this.soundEvent;
   }

   protected void doPostHurtEffects(LivingEntity livingentity) {
   }

   @Nullable
   protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec31) {
      return ProjectileUtil.getEntityHitResult(this.level(), this, vec3, vec31, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
   }

   protected boolean canHitEntity(Entity entity) {
      return super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putShort("life", (short)this.life);
      if (this.lastState != null) {
         compoundtag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
      }

      compoundtag.putByte("shake", (byte)this.shakeTime);
      compoundtag.putBoolean("inGround", this.inGround);
      compoundtag.putByte("pickup", (byte)this.pickup.ordinal());
      compoundtag.putDouble("damage", this.baseDamage);
      compoundtag.putBoolean("crit", this.isCritArrow());
      compoundtag.putByte("PierceLevel", this.getPierceLevel());
      compoundtag.putString("SoundEvent", BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent).toString());
      compoundtag.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.life = compoundtag.getShort("life");
      if (compoundtag.contains("inBlockState", 10)) {
         this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundtag.getCompound("inBlockState"));
      }

      this.shakeTime = compoundtag.getByte("shake") & 255;
      this.inGround = compoundtag.getBoolean("inGround");
      if (compoundtag.contains("damage", 99)) {
         this.baseDamage = compoundtag.getDouble("damage");
      }

      this.pickup = AbstractArrow.Pickup.byOrdinal(compoundtag.getByte("pickup"));
      this.setCritArrow(compoundtag.getBoolean("crit"));
      this.setPierceLevel(compoundtag.getByte("PierceLevel"));
      if (compoundtag.contains("SoundEvent", 8)) {
         this.soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(new ResourceLocation(compoundtag.getString("SoundEvent"))).orElse(this.getDefaultHitGroundSoundEvent());
      }

      this.setShotFromCrossbow(compoundtag.getBoolean("ShotFromCrossbow"));
   }

   public void setOwner(@Nullable Entity entity) {
      super.setOwner(entity);
      if (entity instanceof Player) {
         this.pickup = ((Player)entity).getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
      }

   }

   public void playerTouch(Player player) {
      if (!this.level().isClientSide && (this.inGround || this.isNoPhysics()) && this.shakeTime <= 0) {
         if (this.tryPickup(player)) {
            player.take(this, 1);
            this.discard();
         }

      }
   }

   protected boolean tryPickup(Player player) {
      switch (this.pickup) {
         case ALLOWED:
            return player.getInventory().add(this.getPickupItem());
         case CREATIVE_ONLY:
            return player.getAbilities().instabuild;
         default:
            return false;
      }
   }

   protected abstract ItemStack getPickupItem();

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   public void setBaseDamage(double d0) {
      this.baseDamage = d0;
   }

   public double getBaseDamage() {
      return this.baseDamage;
   }

   public void setKnockback(int i) {
      this.knockback = i;
   }

   public int getKnockback() {
      return this.knockback;
   }

   public boolean isAttackable() {
      return false;
   }

   protected float getEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return 0.13F;
   }

   public void setCritArrow(boolean flag) {
      this.setFlag(1, flag);
   }

   public void setPierceLevel(byte b0) {
      this.entityData.set(PIERCE_LEVEL, b0);
   }

   private void setFlag(int i, boolean flag) {
      byte b0 = this.entityData.get(ID_FLAGS);
      if (flag) {
         this.entityData.set(ID_FLAGS, (byte)(b0 | i));
      } else {
         this.entityData.set(ID_FLAGS, (byte)(b0 & ~i));
      }

   }

   public boolean isCritArrow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 1) != 0;
   }

   public boolean shotFromCrossbow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 4) != 0;
   }

   public byte getPierceLevel() {
      return this.entityData.get(PIERCE_LEVEL);
   }

   public void setEnchantmentEffectsFromEntity(LivingEntity livingentity, float f) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, livingentity);
      int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, livingentity);
      this.setBaseDamage((double)(f * 2.0F) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11D, 0.57425D));
      if (i > 0) {
         this.setBaseDamage(this.getBaseDamage() + (double)i * 0.5D + 0.5D);
      }

      if (j > 0) {
         this.setKnockback(j);
      }

      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, livingentity) > 0) {
         this.setSecondsOnFire(100);
      }

   }

   protected float getWaterInertia() {
      return 0.6F;
   }

   public void setNoPhysics(boolean flag) {
      this.noPhysics = flag;
      this.setFlag(2, flag);
   }

   public boolean isNoPhysics() {
      if (!this.level().isClientSide) {
         return this.noPhysics;
      } else {
         return (this.entityData.get(ID_FLAGS) & 2) != 0;
      }
   }

   public void setShotFromCrossbow(boolean flag) {
      this.setFlag(4, flag);
   }

   public static enum Pickup {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static AbstractArrow.Pickup byOrdinal(int i) {
         if (i < 0 || i > values().length) {
            i = 0;
         }

         return values()[i];
      }
   }
}
