package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

public abstract class TamableAnimal extends Animal implements OwnableEntity {
   protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID);
   private boolean orderedToSit;

   protected TamableAnimal(EntityType<? extends TamableAnimal> entitytype, Level level) {
      super(entitytype, level);
      this.reassessTameGoals();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
      this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      if (this.getOwnerUUID() != null) {
         compoundtag.putUUID("Owner", this.getOwnerUUID());
      }

      compoundtag.putBoolean("Sitting", this.orderedToSit);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      UUID uuid;
      if (compoundtag.hasUUID("Owner")) {
         uuid = compoundtag.getUUID("Owner");
      } else {
         String s = compoundtag.getString("Owner");
         uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
      }

      if (uuid != null) {
         try {
            this.setOwnerUUID(uuid);
            this.setTame(true);
         } catch (Throwable var4) {
            this.setTame(false);
         }
      }

      this.orderedToSit = compoundtag.getBoolean("Sitting");
      this.setInSittingPose(this.orderedToSit);
   }

   public boolean canBeLeashed(Player player) {
      return !this.isLeashed();
   }

   protected void spawnTamingParticles(boolean flag) {
      ParticleOptions particleoptions = ParticleTypes.HEART;
      if (!flag) {
         particleoptions = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 7) {
         this.spawnTamingParticles(true);
      } else if (b0 == 6) {
         this.spawnTamingParticles(false);
      } else {
         super.handleEntityEvent(b0);
      }

   }

   public boolean isTame() {
      return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
   }

   public void setTame(boolean flag) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (flag) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
      }

      this.reassessTameGoals();
   }

   protected void reassessTameGoals() {
   }

   public boolean isInSittingPose() {
      return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   public void setInSittingPose(boolean flag) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (flag) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
      }

   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID)null);
   }

   public void setOwnerUUID(@Nullable UUID uuid) {
      this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
   }

   public void tame(Player player) {
      this.setTame(true);
      this.setOwnerUUID(player.getUUID());
      if (player instanceof ServerPlayer) {
         CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
      }

   }

   public boolean canAttack(LivingEntity livingentity) {
      return this.isOwnedBy(livingentity) ? false : super.canAttack(livingentity);
   }

   public boolean isOwnedBy(LivingEntity livingentity) {
      return livingentity == this.getOwner();
   }

   public boolean wantsToAttack(LivingEntity livingentity, LivingEntity livingentity1) {
      return true;
   }

   public Team getTeam() {
      if (this.isTame()) {
         LivingEntity livingentity = this.getOwner();
         if (livingentity != null) {
            return livingentity.getTeam();
         }
      }

      return super.getTeam();
   }

   public boolean isAlliedTo(Entity entity) {
      if (this.isTame()) {
         LivingEntity livingentity = this.getOwner();
         if (entity == livingentity) {
            return true;
         }

         if (livingentity != null) {
            return livingentity.isAlliedTo(entity);
         }
      }

      return super.isAlliedTo(entity);
   }

   public void die(DamageSource damagesource) {
      if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
         this.getOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
      }

      super.die(damagesource);
   }

   public boolean isOrderedToSit() {
      return this.orderedToSit;
   }

   public void setOrderedToSit(boolean flag) {
      this.orderedToSit = flag;
   }
}
