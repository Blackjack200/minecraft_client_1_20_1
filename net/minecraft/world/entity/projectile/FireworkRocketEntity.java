package net.minecraft.world.entity.projectile;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketEntity extends Projectile implements ItemSupplier {
   private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
   private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
   private int life;
   private int lifetime;
   @Nullable
   private LivingEntity attachedToEntity;

   public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entitytype, Level level) {
      super(entitytype, level);
   }

   public FireworkRocketEntity(Level level, double d0, double d1, double d2, ItemStack itemstack) {
      super(EntityType.FIREWORK_ROCKET, level);
      this.life = 0;
      this.setPos(d0, d1, d2);
      int i = 1;
      if (!itemstack.isEmpty() && itemstack.hasTag()) {
         this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemstack.copy());
         i += itemstack.getOrCreateTagElement("Fireworks").getByte("Flight");
      }

      this.setDeltaMovement(this.random.triangle(0.0D, 0.002297D), 0.05D, this.random.triangle(0.0D, 0.002297D));
      this.lifetime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
   }

   public FireworkRocketEntity(Level level, @Nullable Entity entity, double d0, double d1, double d2, ItemStack itemstack) {
      this(level, d0, d1, d2, itemstack);
      this.setOwner(entity);
   }

   public FireworkRocketEntity(Level level, ItemStack itemstack, LivingEntity livingentity) {
      this(level, livingentity, livingentity.getX(), livingentity.getY(), livingentity.getZ(), itemstack);
      this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(livingentity.getId()));
      this.attachedToEntity = livingentity;
   }

   public FireworkRocketEntity(Level level, ItemStack itemstack, double d0, double d1, double d2, boolean flag) {
      this(level, d0, d1, d2, itemstack);
      this.entityData.set(DATA_SHOT_AT_ANGLE, flag);
   }

   public FireworkRocketEntity(Level level, ItemStack itemstack, Entity entity, double d0, double d1, double d2, boolean flag) {
      this(level, itemstack, d0, d1, d2, flag);
      this.setOwner(entity);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_FIREWORKS_ITEM, ItemStack.EMPTY);
      this.entityData.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
      this.entityData.define(DATA_SHOT_AT_ANGLE, false);
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      return d0 < 4096.0D && !this.isAttachedToEntity();
   }

   public boolean shouldRender(double d0, double d1, double d2) {
      return super.shouldRender(d0, d1, d2) && !this.isAttachedToEntity();
   }

   public void tick() {
      super.tick();
      if (this.isAttachedToEntity()) {
         if (this.attachedToEntity == null) {
            this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent((i) -> {
               Entity entity = this.level().getEntity(i);
               if (entity instanceof LivingEntity) {
                  this.attachedToEntity = (LivingEntity)entity;
               }

            });
         }

         if (this.attachedToEntity != null) {
            Vec3 vec32;
            if (this.attachedToEntity.isFallFlying()) {
               Vec3 vec3 = this.attachedToEntity.getLookAngle();
               double d0 = 1.5D;
               double d1 = 0.1D;
               Vec3 vec31 = this.attachedToEntity.getDeltaMovement();
               this.attachedToEntity.setDeltaMovement(vec31.add(vec3.x * 0.1D + (vec3.x * 1.5D - vec31.x) * 0.5D, vec3.y * 0.1D + (vec3.y * 1.5D - vec31.y) * 0.5D, vec3.z * 0.1D + (vec3.z * 1.5D - vec31.z) * 0.5D));
               vec32 = this.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
            } else {
               vec32 = Vec3.ZERO;
            }

            this.setPos(this.attachedToEntity.getX() + vec32.x, this.attachedToEntity.getY() + vec32.y, this.attachedToEntity.getZ() + vec32.z);
            this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
         }
      } else {
         if (!this.isShotAtAngle()) {
            double d2 = this.horizontalCollision ? 1.0D : 1.15D;
            this.setDeltaMovement(this.getDeltaMovement().multiply(d2, 1.0D, d2).add(0.0D, 0.04D, 0.0D));
         }

         Vec3 vec34 = this.getDeltaMovement();
         this.move(MoverType.SELF, vec34);
         this.setDeltaMovement(vec34);
      }

      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      if (!this.noPhysics) {
         this.onHit(hitresult);
         this.hasImpulse = true;
      }

      this.updateRotation();
      if (this.life == 0 && !this.isSilent()) {
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
      }

      ++this.life;
      if (this.level().isClientSide && this.life % 2 < 2) {
         this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05D, -this.getDeltaMovement().y * 0.5D, this.random.nextGaussian() * 0.05D);
      }

      if (!this.level().isClientSide && this.life > this.lifetime) {
         this.explode();
      }

   }

   private void explode() {
      this.level().broadcastEntityEvent(this, (byte)17);
      this.gameEvent(GameEvent.EXPLODE, this.getOwner());
      this.dealExplosionDamage();
      this.discard();
   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      if (!this.level().isClientSide) {
         this.explode();
      }
   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      BlockPos blockpos = new BlockPos(blockhitresult.getBlockPos());
      this.level().getBlockState(blockpos).entityInside(this.level(), blockpos, this);
      if (!this.level().isClientSide() && this.hasExplosion()) {
         this.explode();
      }

      super.onHitBlock(blockhitresult);
   }

   private boolean hasExplosion() {
      ItemStack itemstack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
      CompoundTag compoundtag = itemstack.isEmpty() ? null : itemstack.getTagElement("Fireworks");
      ListTag listtag = compoundtag != null ? compoundtag.getList("Explosions", 10) : null;
      return listtag != null && !listtag.isEmpty();
   }

   private void dealExplosionDamage() {
      float f = 0.0F;
      ItemStack itemstack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
      CompoundTag compoundtag = itemstack.isEmpty() ? null : itemstack.getTagElement("Fireworks");
      ListTag listtag = compoundtag != null ? compoundtag.getList("Explosions", 10) : null;
      if (listtag != null && !listtag.isEmpty()) {
         f = 5.0F + (float)(listtag.size() * 2);
      }

      if (f > 0.0F) {
         if (this.attachedToEntity != null) {
            this.attachedToEntity.hurt(this.damageSources().fireworks(this, this.getOwner()), 5.0F + (float)(listtag.size() * 2));
         }

         double d0 = 5.0D;
         Vec3 vec3 = this.position();

         for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
            if (livingentity != this.attachedToEntity && !(this.distanceToSqr(livingentity) > 25.0D)) {
               boolean flag = false;

               for(int i = 0; i < 2; ++i) {
                  Vec3 vec31 = new Vec3(livingentity.getX(), livingentity.getY(0.5D * (double)i), livingentity.getZ());
                  HitResult hitresult = this.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                  if (hitresult.getType() == HitResult.Type.MISS) {
                     flag = true;
                     break;
                  }
               }

               if (flag) {
                  float f1 = f * (float)Math.sqrt((5.0D - (double)this.distanceTo(livingentity)) / 5.0D);
                  livingentity.hurt(this.damageSources().fireworks(this, this.getOwner()), f1);
               }
            }
         }
      }

   }

   private boolean isAttachedToEntity() {
      return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
   }

   public boolean isShotAtAngle() {
      return this.entityData.get(DATA_SHOT_AT_ANGLE);
   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 17 && this.level().isClientSide) {
         if (!this.hasExplosion()) {
            for(int i = 0; i < this.random.nextInt(3) + 2; ++i) {
               this.level().addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05D, 0.005D, this.random.nextGaussian() * 0.05D);
            }
         } else {
            ItemStack itemstack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
            CompoundTag compoundtag = itemstack.isEmpty() ? null : itemstack.getTagElement("Fireworks");
            Vec3 vec3 = this.getDeltaMovement();
            this.level().createFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, compoundtag);
         }
      }

      super.handleEntityEvent(b0);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Life", this.life);
      compoundtag.putInt("LifeTime", this.lifetime);
      ItemStack itemstack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
      if (!itemstack.isEmpty()) {
         compoundtag.put("FireworksItem", itemstack.save(new CompoundTag()));
      }

      compoundtag.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.life = compoundtag.getInt("Life");
      this.lifetime = compoundtag.getInt("LifeTime");
      ItemStack itemstack = ItemStack.of(compoundtag.getCompound("FireworksItem"));
      if (!itemstack.isEmpty()) {
         this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemstack);
      }

      if (compoundtag.contains("ShotAtAngle")) {
         this.entityData.set(DATA_SHOT_AT_ANGLE, compoundtag.getBoolean("ShotAtAngle"));
      }

   }

   public ItemStack getItem() {
      ItemStack itemstack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
      return itemstack.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : itemstack;
   }

   public boolean isAttackable() {
      return false;
   }
}
