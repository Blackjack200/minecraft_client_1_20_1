package net.minecraft.world.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Skeleton extends AbstractSkeleton {
   private static final int TOTAL_CONVERSION_TIME = 300;
   private static final EntityDataAccessor<Boolean> DATA_STRAY_CONVERSION_ID = SynchedEntityData.defineId(Skeleton.class, EntityDataSerializers.BOOLEAN);
   public static final String CONVERSION_TAG = "StrayConversionTime";
   private int inPowderSnowTime;
   private int conversionTime;

   public Skeleton(EntityType<? extends Skeleton> entitytype, Level level) {
      super(entitytype, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_STRAY_CONVERSION_ID, false);
   }

   public boolean isFreezeConverting() {
      return this.getEntityData().get(DATA_STRAY_CONVERSION_ID);
   }

   public void setFreezeConverting(boolean flag) {
      this.entityData.set(DATA_STRAY_CONVERSION_ID, flag);
   }

   public boolean isShaking() {
      return this.isFreezeConverting();
   }

   public void tick() {
      if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
         if (this.isInPowderSnow) {
            if (this.isFreezeConverting()) {
               --this.conversionTime;
               if (this.conversionTime < 0) {
                  this.doFreezeConversion();
               }
            } else {
               ++this.inPowderSnowTime;
               if (this.inPowderSnowTime >= 140) {
                  this.startFreezeConversion(300);
               }
            }
         } else {
            this.inPowderSnowTime = -1;
            this.setFreezeConverting(false);
         }
      }

      super.tick();
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("StrayConversionTime", this.isFreezeConverting() ? this.conversionTime : -1);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("StrayConversionTime", 99) && compoundtag.getInt("StrayConversionTime") > -1) {
         this.startFreezeConversion(compoundtag.getInt("StrayConversionTime"));
      }

   }

   private void startFreezeConversion(int i) {
      this.conversionTime = i;
      this.setFreezeConverting(true);
   }

   protected void doFreezeConversion() {
      this.convertTo(EntityType.STRAY, true);
      if (!this.isSilent()) {
         this.level().levelEvent((Player)null, 1048, this.blockPosition(), 0);
      }

   }

   public boolean canFreeze() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SKELETON_DEATH;
   }

   SoundEvent getStepSound() {
      return SoundEvents.SKELETON_STEP;
   }

   protected void dropCustomDeathLoot(DamageSource damagesource, int i, boolean flag) {
      super.dropCustomDeathLoot(damagesource, i, flag);
      Entity entity = damagesource.getEntity();
      if (entity instanceof Creeper creeper) {
         if (creeper.canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(Items.SKELETON_SKULL);
         }
      }

   }
}
