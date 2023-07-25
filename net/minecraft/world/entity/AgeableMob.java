package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AgeableMob extends PathfinderMob {
   private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgeableMob.class, EntityDataSerializers.BOOLEAN);
   public static final int BABY_START_AGE = -24000;
   private static final int FORCED_AGE_PARTICLE_TICKS = 40;
   protected int age;
   protected int forcedAge;
   protected int forcedAgeTimer;

   protected AgeableMob(EntityType<? extends AgeableMob> entitytype, Level level) {
      super(entitytype, level);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      if (spawngroupdata == null) {
         spawngroupdata = new AgeableMob.AgeableMobGroupData(true);
      }

      AgeableMob.AgeableMobGroupData ageablemob_ageablemobgroupdata = (AgeableMob.AgeableMobGroupData)spawngroupdata;
      if (ageablemob_ageablemobgroupdata.isShouldSpawnBaby() && ageablemob_ageablemobgroupdata.getGroupSize() > 0 && serverlevelaccessor.getRandom().nextFloat() <= ageablemob_ageablemobgroupdata.getBabySpawnChance()) {
         this.setAge(-24000);
      }

      ageablemob_ageablemobgroupdata.increaseGroupSizeByOne();
      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   @Nullable
   public abstract AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob);

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
   }

   public boolean canBreed() {
      return false;
   }

   public int getAge() {
      if (this.level().isClientSide) {
         return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
      } else {
         return this.age;
      }
   }

   public void ageUp(int i, boolean flag) {
      int j = this.getAge();
      j += i * 20;
      if (j > 0) {
         j = 0;
      }

      int l = j - j;
      this.setAge(j);
      if (flag) {
         this.forcedAge += l;
         if (this.forcedAgeTimer == 0) {
            this.forcedAgeTimer = 40;
         }
      }

      if (this.getAge() == 0) {
         this.setAge(this.forcedAge);
      }

   }

   public void ageUp(int i) {
      this.ageUp(i, false);
   }

   public void setAge(int i) {
      int j = this.getAge();
      this.age = i;
      if (j < 0 && i >= 0 || j >= 0 && i < 0) {
         this.entityData.set(DATA_BABY_ID, i < 0);
         this.ageBoundaryReached();
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Age", this.getAge());
      compoundtag.putInt("ForcedAge", this.forcedAge);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.setAge(compoundtag.getInt("Age"));
      this.forcedAge = compoundtag.getInt("ForcedAge");
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_BABY_ID.equals(entitydataaccessor)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide) {
         if (this.forcedAgeTimer > 0) {
            if (this.forcedAgeTimer % 4 == 0) {
               this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            }

            --this.forcedAgeTimer;
         }
      } else if (this.isAlive()) {
         int i = this.getAge();
         if (i < 0) {
            ++i;
            this.setAge(i);
         } else if (i > 0) {
            --i;
            this.setAge(i);
         }
      }

   }

   protected void ageBoundaryReached() {
      if (!this.isBaby() && this.isPassenger()) {
         Entity var2 = this.getVehicle();
         if (var2 instanceof Boat) {
            Boat boat = (Boat)var2;
            if (!boat.hasEnoughSpaceFor(this)) {
               this.stopRiding();
            }
         }
      }

   }

   public boolean isBaby() {
      return this.getAge() < 0;
   }

   public void setBaby(boolean flag) {
      this.setAge(flag ? -24000 : 0);
   }

   public static int getSpeedUpSecondsWhenFeeding(int i) {
      return (int)((float)(i / 20) * 0.1F);
   }

   public static class AgeableMobGroupData implements SpawnGroupData {
      private int groupSize;
      private final boolean shouldSpawnBaby;
      private final float babySpawnChance;

      private AgeableMobGroupData(boolean flag, float f) {
         this.shouldSpawnBaby = flag;
         this.babySpawnChance = f;
      }

      public AgeableMobGroupData(boolean flag) {
         this(flag, 0.05F);
      }

      public AgeableMobGroupData(float f) {
         this(true, f);
      }

      public int getGroupSize() {
         return this.groupSize;
      }

      public void increaseGroupSizeByOne() {
         ++this.groupSize;
      }

      public boolean isShouldSpawnBaby() {
         return this.shouldSpawnBaby;
      }

      public float getBabySpawnChance() {
         return this.babySpawnChance;
      }
   }
}
