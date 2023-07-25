package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ItemBasedSteering {
   private static final int MIN_BOOST_TIME = 140;
   private static final int MAX_BOOST_TIME = 700;
   private final SynchedEntityData entityData;
   private final EntityDataAccessor<Integer> boostTimeAccessor;
   private final EntityDataAccessor<Boolean> hasSaddleAccessor;
   private boolean boosting;
   private int boostTime;

   public ItemBasedSteering(SynchedEntityData synchedentitydata, EntityDataAccessor<Integer> entitydataaccessor, EntityDataAccessor<Boolean> entitydataaccessor1) {
      this.entityData = synchedentitydata;
      this.boostTimeAccessor = entitydataaccessor;
      this.hasSaddleAccessor = entitydataaccessor1;
   }

   public void onSynced() {
      this.boosting = true;
      this.boostTime = 0;
   }

   public boolean boost(RandomSource randomsource) {
      if (this.boosting) {
         return false;
      } else {
         this.boosting = true;
         this.boostTime = 0;
         this.entityData.set(this.boostTimeAccessor, randomsource.nextInt(841) + 140);
         return true;
      }
   }

   public void tickBoost() {
      if (this.boosting && this.boostTime++ > this.boostTimeTotal()) {
         this.boosting = false;
      }

   }

   public float boostFactor() {
      return this.boosting ? 1.0F + 1.15F * Mth.sin((float)this.boostTime / (float)this.boostTimeTotal() * (float)Math.PI) : 1.0F;
   }

   private int boostTimeTotal() {
      return this.entityData.get(this.boostTimeAccessor);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putBoolean("Saddle", this.hasSaddle());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      this.setSaddle(compoundtag.getBoolean("Saddle"));
   }

   public void setSaddle(boolean flag) {
      this.entityData.set(this.hasSaddleAccessor, flag);
   }

   public boolean hasSaddle() {
      return this.entityData.get(this.hasSaddleAccessor);
   }
}
