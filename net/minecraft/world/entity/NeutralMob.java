package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface NeutralMob {
   String TAG_ANGER_TIME = "AngerTime";
   String TAG_ANGRY_AT = "AngryAt";

   int getRemainingPersistentAngerTime();

   void setRemainingPersistentAngerTime(int i);

   @Nullable
   UUID getPersistentAngerTarget();

   void setPersistentAngerTarget(@Nullable UUID uuid);

   void startPersistentAngerTimer();

   default void addPersistentAngerSaveData(CompoundTag compoundtag) {
      compoundtag.putInt("AngerTime", this.getRemainingPersistentAngerTime());
      if (this.getPersistentAngerTarget() != null) {
         compoundtag.putUUID("AngryAt", this.getPersistentAngerTarget());
      }

   }

   default void readPersistentAngerSaveData(Level level, CompoundTag compoundtag) {
      this.setRemainingPersistentAngerTime(compoundtag.getInt("AngerTime"));
      if (level instanceof ServerLevel) {
         if (!compoundtag.hasUUID("AngryAt")) {
            this.setPersistentAngerTarget((UUID)null);
         } else {
            UUID uuid = compoundtag.getUUID("AngryAt");
            this.setPersistentAngerTarget(uuid);
            Entity entity = ((ServerLevel)level).getEntity(uuid);
            if (entity != null) {
               if (entity instanceof Mob) {
                  this.setLastHurtByMob((Mob)entity);
               }

               if (entity.getType() == EntityType.PLAYER) {
                  this.setLastHurtByPlayer((Player)entity);
               }

            }
         }
      }
   }

   default void updatePersistentAnger(ServerLevel serverlevel, boolean flag) {
      LivingEntity livingentity = this.getTarget();
      UUID uuid = this.getPersistentAngerTarget();
      if ((livingentity == null || livingentity.isDeadOrDying()) && uuid != null && serverlevel.getEntity(uuid) instanceof Mob) {
         this.stopBeingAngry();
      } else {
         if (livingentity != null && !Objects.equals(uuid, livingentity.getUUID())) {
            this.setPersistentAngerTarget(livingentity.getUUID());
            this.startPersistentAngerTimer();
         }

         if (this.getRemainingPersistentAngerTime() > 0 && (livingentity == null || livingentity.getType() != EntityType.PLAYER || !flag)) {
            this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
            if (this.getRemainingPersistentAngerTime() == 0) {
               this.stopBeingAngry();
            }
         }

      }
   }

   default boolean isAngryAt(LivingEntity livingentity) {
      if (!this.canAttack(livingentity)) {
         return false;
      } else {
         return livingentity.getType() == EntityType.PLAYER && this.isAngryAtAllPlayers(livingentity.level()) ? true : livingentity.getUUID().equals(this.getPersistentAngerTarget());
      }
   }

   default boolean isAngryAtAllPlayers(Level level) {
      return level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
   }

   default boolean isAngry() {
      return this.getRemainingPersistentAngerTime() > 0;
   }

   default void playerDied(Player player) {
      if (player.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
         if (player.getUUID().equals(this.getPersistentAngerTarget())) {
            this.stopBeingAngry();
         }
      }
   }

   default void forgetCurrentTargetAndRefreshUniversalAnger() {
      this.stopBeingAngry();
      this.startPersistentAngerTimer();
   }

   default void stopBeingAngry() {
      this.setLastHurtByMob((LivingEntity)null);
      this.setPersistentAngerTarget((UUID)null);
      this.setTarget((LivingEntity)null);
      this.setRemainingPersistentAngerTime(0);
   }

   @Nullable
   LivingEntity getLastHurtByMob();

   void setLastHurtByMob(@Nullable LivingEntity livingentity);

   void setLastHurtByPlayer(@Nullable Player player);

   void setTarget(@Nullable LivingEntity livingentity);

   boolean canAttack(LivingEntity livingentity);

   @Nullable
   LivingEntity getTarget();
}
