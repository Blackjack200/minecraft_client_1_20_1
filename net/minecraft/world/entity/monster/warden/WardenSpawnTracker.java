package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WardenSpawnTracker {
   public static final Codec<WardenSpawnTracker> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter((wardenspawntracker2) -> wardenspawntracker2.ticksSinceLastWarning), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter((wardenspawntracker1) -> wardenspawntracker1.warningLevel), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter((wardenspawntracker) -> wardenspawntracker.cooldownTicks)).apply(recordcodecbuilder_instance, WardenSpawnTracker::new));
   public static final int MAX_WARNING_LEVEL = 4;
   private static final double PLAYER_SEARCH_RADIUS = 16.0D;
   private static final int WARNING_CHECK_DIAMETER = 48;
   private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
   private static final int WARNING_LEVEL_INCREASE_COOLDOWN = 200;
   private int ticksSinceLastWarning;
   private int warningLevel;
   private int cooldownTicks;

   public WardenSpawnTracker(int i, int j, int k) {
      this.ticksSinceLastWarning = i;
      this.warningLevel = j;
      this.cooldownTicks = k;
   }

   public void tick() {
      if (this.ticksSinceLastWarning >= 12000) {
         this.decreaseWarningLevel();
         this.ticksSinceLastWarning = 0;
      } else {
         ++this.ticksSinceLastWarning;
      }

      if (this.cooldownTicks > 0) {
         --this.cooldownTicks;
      }

   }

   public void reset() {
      this.ticksSinceLastWarning = 0;
      this.warningLevel = 0;
      this.cooldownTicks = 0;
   }

   public static OptionalInt tryWarn(ServerLevel serverlevel, BlockPos blockpos, ServerPlayer serverplayer) {
      if (hasNearbyWarden(serverlevel, blockpos)) {
         return OptionalInt.empty();
      } else {
         List<ServerPlayer> list = getNearbyPlayers(serverlevel, blockpos);
         if (!list.contains(serverplayer)) {
            list.add(serverplayer);
         }

         if (list.stream().anyMatch((serverplayer3) -> serverplayer3.getWardenSpawnTracker().map(WardenSpawnTracker::onCooldown).orElse(false))) {
            return OptionalInt.empty();
         } else {
            Optional<WardenSpawnTracker> optional = list.stream().flatMap((serverplayer2) -> serverplayer2.getWardenSpawnTracker().stream()).max(Comparator.comparingInt(WardenSpawnTracker::getWarningLevel));
            if (optional.isPresent()) {
               WardenSpawnTracker wardenspawntracker = optional.get();
               wardenspawntracker.increaseWarningLevel();
               list.forEach((serverplayer1) -> serverplayer1.getWardenSpawnTracker().ifPresent((wardenspawntracker3) -> wardenspawntracker3.copyData(wardenspawntracker)));
               return OptionalInt.of(wardenspawntracker.warningLevel);
            } else {
               return OptionalInt.empty();
            }
         }
      }
   }

   private boolean onCooldown() {
      return this.cooldownTicks > 0;
   }

   private static boolean hasNearbyWarden(ServerLevel serverlevel, BlockPos blockpos) {
      AABB aabb = AABB.ofSize(Vec3.atCenterOf(blockpos), 48.0D, 48.0D, 48.0D);
      return !serverlevel.getEntitiesOfClass(Warden.class, aabb).isEmpty();
   }

   private static List<ServerPlayer> getNearbyPlayers(ServerLevel serverlevel, BlockPos blockpos) {
      Vec3 vec3 = Vec3.atCenterOf(blockpos);
      Predicate<ServerPlayer> predicate = (serverplayer) -> serverplayer.position().closerThan(vec3, 16.0D);
      return serverlevel.getPlayers(predicate.and(LivingEntity::isAlive).and(EntitySelector.NO_SPECTATORS));
   }

   private void increaseWarningLevel() {
      if (!this.onCooldown()) {
         this.ticksSinceLastWarning = 0;
         this.cooldownTicks = 200;
         this.setWarningLevel(this.getWarningLevel() + 1);
      }

   }

   private void decreaseWarningLevel() {
      this.setWarningLevel(this.getWarningLevel() - 1);
   }

   public void setWarningLevel(int i) {
      this.warningLevel = Mth.clamp(i, 0, 4);
   }

   public int getWarningLevel() {
      return this.warningLevel;
   }

   private void copyData(WardenSpawnTracker wardenspawntracker) {
      this.warningLevel = wardenspawntracker.warningLevel;
      this.cooldownTicks = wardenspawntracker.cooldownTicks;
      this.ticksSinceLastWarning = wardenspawntracker.ticksSinceLastWarning;
   }
}
