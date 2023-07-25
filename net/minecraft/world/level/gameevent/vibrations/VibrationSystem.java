package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface VibrationSystem {
   GameEvent[] RESONANCE_EVENTS = new GameEvent[]{GameEvent.RESONATE_1, GameEvent.RESONATE_2, GameEvent.RESONATE_3, GameEvent.RESONATE_4, GameEvent.RESONATE_5, GameEvent.RESONATE_6, GameEvent.RESONATE_7, GameEvent.RESONATE_8, GameEvent.RESONATE_9, GameEvent.RESONATE_10, GameEvent.RESONATE_11, GameEvent.RESONATE_12, GameEvent.RESONATE_13, GameEvent.RESONATE_14, GameEvent.RESONATE_15};
   ToIntFunction<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Util.make(new Object2IntOpenHashMap<>(), (object2intopenhashmap) -> {
      object2intopenhashmap.defaultReturnValue(0);
      object2intopenhashmap.put(GameEvent.STEP, 1);
      object2intopenhashmap.put(GameEvent.SWIM, 1);
      object2intopenhashmap.put(GameEvent.FLAP, 1);
      object2intopenhashmap.put(GameEvent.PROJECTILE_LAND, 2);
      object2intopenhashmap.put(GameEvent.HIT_GROUND, 2);
      object2intopenhashmap.put(GameEvent.SPLASH, 2);
      object2intopenhashmap.put(GameEvent.ITEM_INTERACT_FINISH, 3);
      object2intopenhashmap.put(GameEvent.PROJECTILE_SHOOT, 3);
      object2intopenhashmap.put(GameEvent.INSTRUMENT_PLAY, 3);
      object2intopenhashmap.put(GameEvent.ENTITY_ROAR, 4);
      object2intopenhashmap.put(GameEvent.ENTITY_SHAKE, 4);
      object2intopenhashmap.put(GameEvent.ELYTRA_GLIDE, 4);
      object2intopenhashmap.put(GameEvent.ENTITY_DISMOUNT, 5);
      object2intopenhashmap.put(GameEvent.EQUIP, 5);
      object2intopenhashmap.put(GameEvent.ENTITY_INTERACT, 6);
      object2intopenhashmap.put(GameEvent.SHEAR, 6);
      object2intopenhashmap.put(GameEvent.ENTITY_MOUNT, 6);
      object2intopenhashmap.put(GameEvent.ENTITY_DAMAGE, 7);
      object2intopenhashmap.put(GameEvent.DRINK, 8);
      object2intopenhashmap.put(GameEvent.EAT, 8);
      object2intopenhashmap.put(GameEvent.CONTAINER_CLOSE, 9);
      object2intopenhashmap.put(GameEvent.BLOCK_CLOSE, 9);
      object2intopenhashmap.put(GameEvent.BLOCK_DEACTIVATE, 9);
      object2intopenhashmap.put(GameEvent.BLOCK_DETACH, 9);
      object2intopenhashmap.put(GameEvent.CONTAINER_OPEN, 10);
      object2intopenhashmap.put(GameEvent.BLOCK_OPEN, 10);
      object2intopenhashmap.put(GameEvent.BLOCK_ACTIVATE, 10);
      object2intopenhashmap.put(GameEvent.BLOCK_ATTACH, 10);
      object2intopenhashmap.put(GameEvent.PRIME_FUSE, 10);
      object2intopenhashmap.put(GameEvent.NOTE_BLOCK_PLAY, 10);
      object2intopenhashmap.put(GameEvent.BLOCK_CHANGE, 11);
      object2intopenhashmap.put(GameEvent.BLOCK_DESTROY, 12);
      object2intopenhashmap.put(GameEvent.FLUID_PICKUP, 12);
      object2intopenhashmap.put(GameEvent.BLOCK_PLACE, 13);
      object2intopenhashmap.put(GameEvent.FLUID_PLACE, 13);
      object2intopenhashmap.put(GameEvent.ENTITY_PLACE, 14);
      object2intopenhashmap.put(GameEvent.LIGHTNING_STRIKE, 14);
      object2intopenhashmap.put(GameEvent.TELEPORT, 14);
      object2intopenhashmap.put(GameEvent.ENTITY_DIE, 15);
      object2intopenhashmap.put(GameEvent.EXPLODE, 15);

      for(int i = 1; i <= 15; ++i) {
         object2intopenhashmap.put(getResonanceEventByFrequency(i), i);
      }

   });

   VibrationSystem.Data getVibrationData();

   VibrationSystem.User getVibrationUser();

   static int getGameEventFrequency(GameEvent gameevent) {
      return VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(gameevent);
   }

   static GameEvent getResonanceEventByFrequency(int i) {
      return RESONANCE_EVENTS[i - 1];
   }

   static int getRedstoneStrengthForDistance(float f, int i) {
      double d0 = 15.0D / (double)i;
      return Math.max(1, 15 - Mth.floor(d0 * (double)f));
   }

   public static final class Data {
      public static Codec<VibrationSystem.Data> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(VibrationInfo.CODEC.optionalFieldOf("event").forGetter((vibrationsystem_data) -> Optional.ofNullable(vibrationsystem_data.currentVibration)), VibrationSelector.CODEC.fieldOf("selector").forGetter(VibrationSystem.Data::getSelectionStrategy), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(VibrationSystem.Data::getTravelTimeInTicks)).apply(recordcodecbuilder_instance, (optional, vibrationselector, integer) -> new VibrationSystem.Data(optional.orElse((VibrationInfo)null), vibrationselector, integer, true)));
      public static final String NBT_TAG_KEY = "listener";
      @Nullable
      VibrationInfo currentVibration;
      private int travelTimeInTicks;
      final VibrationSelector selectionStrategy;
      private boolean reloadVibrationParticle;

      private Data(@Nullable VibrationInfo vibrationinfo, VibrationSelector vibrationselector, int i, boolean flag) {
         this.currentVibration = vibrationinfo;
         this.travelTimeInTicks = i;
         this.selectionStrategy = vibrationselector;
         this.reloadVibrationParticle = flag;
      }

      public Data() {
         this((VibrationInfo)null, new VibrationSelector(), 0, false);
      }

      public VibrationSelector getSelectionStrategy() {
         return this.selectionStrategy;
      }

      @Nullable
      public VibrationInfo getCurrentVibration() {
         return this.currentVibration;
      }

      public void setCurrentVibration(@Nullable VibrationInfo vibrationinfo) {
         this.currentVibration = vibrationinfo;
      }

      public int getTravelTimeInTicks() {
         return this.travelTimeInTicks;
      }

      public void setTravelTimeInTicks(int i) {
         this.travelTimeInTicks = i;
      }

      public void decrementTravelTime() {
         this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
      }

      public boolean shouldReloadVibrationParticle() {
         return this.reloadVibrationParticle;
      }

      public void setReloadVibrationParticle(boolean flag) {
         this.reloadVibrationParticle = flag;
      }
   }

   public static class Listener implements GameEventListener {
      private final VibrationSystem system;

      public Listener(VibrationSystem vibrationsystem) {
         this.system = vibrationsystem;
      }

      public PositionSource getListenerSource() {
         return this.system.getVibrationUser().getPositionSource();
      }

      public int getListenerRadius() {
         return this.system.getVibrationUser().getListenerRadius();
      }

      public boolean handleGameEvent(ServerLevel serverlevel, GameEvent gameevent, GameEvent.Context gameevent_context, Vec3 vec3) {
         VibrationSystem.Data vibrationsystem_data = this.system.getVibrationData();
         VibrationSystem.User vibrationsystem_user = this.system.getVibrationUser();
         if (vibrationsystem_data.getCurrentVibration() != null) {
            return false;
         } else if (!vibrationsystem_user.isValidVibration(gameevent, gameevent_context)) {
            return false;
         } else {
            Optional<Vec3> optional = vibrationsystem_user.getPositionSource().getPosition(serverlevel);
            if (optional.isEmpty()) {
               return false;
            } else {
               Vec3 vec31 = optional.get();
               if (!vibrationsystem_user.canReceiveVibration(serverlevel, BlockPos.containing(vec3), gameevent, gameevent_context)) {
                  return false;
               } else if (isOccluded(serverlevel, vec3, vec31)) {
                  return false;
               } else {
                  this.scheduleVibration(serverlevel, vibrationsystem_data, gameevent, gameevent_context, vec3, vec31);
                  return true;
               }
            }
         }
      }

      public void forceScheduleVibration(ServerLevel serverlevel, GameEvent gameevent, GameEvent.Context gameevent_context, Vec3 vec3) {
         this.system.getVibrationUser().getPositionSource().getPosition(serverlevel).ifPresent((vec32) -> this.scheduleVibration(serverlevel, this.system.getVibrationData(), gameevent, gameevent_context, vec3, vec32));
      }

      private void scheduleVibration(ServerLevel serverlevel, VibrationSystem.Data vibrationsystem_data, GameEvent gameevent, GameEvent.Context gameevent_context, Vec3 vec3, Vec3 vec31) {
         vibrationsystem_data.selectionStrategy.addCandidate(new VibrationInfo(gameevent, (float)vec3.distanceTo(vec31), vec3, gameevent_context.sourceEntity()), serverlevel.getGameTime());
      }

      public static float distanceBetweenInBlocks(BlockPos blockpos, BlockPos blockpos1) {
         return (float)Math.sqrt(blockpos.distSqr(blockpos1));
      }

      private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec31) {
         Vec3 vec32 = new Vec3((double)Mth.floor(vec3.x) + 0.5D, (double)Mth.floor(vec3.y) + 0.5D, (double)Mth.floor(vec3.z) + 0.5D);
         Vec3 vec33 = new Vec3((double)Mth.floor(vec31.x) + 0.5D, (double)Mth.floor(vec31.y) + 0.5D, (double)Mth.floor(vec31.z) + 0.5D);

         for(Direction direction : Direction.values()) {
            Vec3 vec34 = vec32.relative(direction, (double)1.0E-5F);
            if (level.isBlockInLine(new ClipBlockStateContext(vec34, vec33, (blockstate) -> blockstate.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType() != HitResult.Type.BLOCK) {
               return false;
            }
         }

         return true;
      }
   }

   public interface Ticker {
      static void tick(Level level, VibrationSystem.Data vibrationsystem_data, VibrationSystem.User vibrationsystem_user) {
         if (level instanceof ServerLevel serverlevel) {
            if (vibrationsystem_data.currentVibration == null) {
               trySelectAndScheduleVibration(serverlevel, vibrationsystem_data, vibrationsystem_user);
            }

            if (vibrationsystem_data.currentVibration != null) {
               boolean flag = vibrationsystem_data.getTravelTimeInTicks() > 0;
               tryReloadVibrationParticle(serverlevel, vibrationsystem_data, vibrationsystem_user);
               vibrationsystem_data.decrementTravelTime();
               if (vibrationsystem_data.getTravelTimeInTicks() <= 0) {
                  flag = receiveVibration(serverlevel, vibrationsystem_data, vibrationsystem_user, vibrationsystem_data.currentVibration);
               }

               if (flag) {
                  vibrationsystem_user.onDataChanged();
               }

            }
         }
      }

      private static void trySelectAndScheduleVibration(ServerLevel serverlevel, VibrationSystem.Data vibrationsystem_data, VibrationSystem.User vibrationsystem_user) {
         vibrationsystem_data.getSelectionStrategy().chosenCandidate(serverlevel.getGameTime()).ifPresent((vibrationinfo) -> {
            vibrationsystem_data.setCurrentVibration(vibrationinfo);
            Vec3 vec3 = vibrationinfo.pos();
            vibrationsystem_data.setTravelTimeInTicks(vibrationsystem_user.calculateTravelTimeInTicks(vibrationinfo.distance()));
            serverlevel.sendParticles(new VibrationParticleOption(vibrationsystem_user.getPositionSource(), vibrationsystem_data.getTravelTimeInTicks()), vec3.x, vec3.y, vec3.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            vibrationsystem_user.onDataChanged();
            vibrationsystem_data.getSelectionStrategy().startOver();
         });
      }

      private static void tryReloadVibrationParticle(ServerLevel serverlevel, VibrationSystem.Data vibrationsystem_data, VibrationSystem.User vibrationsystem_user) {
         if (vibrationsystem_data.shouldReloadVibrationParticle()) {
            if (vibrationsystem_data.currentVibration == null) {
               vibrationsystem_data.setReloadVibrationParticle(false);
            } else {
               Vec3 vec3 = vibrationsystem_data.currentVibration.pos();
               PositionSource positionsource = vibrationsystem_user.getPositionSource();
               Vec3 vec31 = positionsource.getPosition(serverlevel).orElse(vec3);
               int i = vibrationsystem_data.getTravelTimeInTicks();
               int j = vibrationsystem_user.calculateTravelTimeInTicks(vibrationsystem_data.currentVibration.distance());
               double d0 = 1.0D - (double)i / (double)j;
               double d1 = Mth.lerp(d0, vec3.x, vec31.x);
               double d2 = Mth.lerp(d0, vec3.y, vec31.y);
               double d3 = Mth.lerp(d0, vec3.z, vec31.z);
               boolean flag = serverlevel.sendParticles(new VibrationParticleOption(positionsource, i), d1, d2, d3, 1, 0.0D, 0.0D, 0.0D, 0.0D) > 0;
               if (flag) {
                  vibrationsystem_data.setReloadVibrationParticle(false);
               }

            }
         }
      }

      private static boolean receiveVibration(ServerLevel serverlevel, VibrationSystem.Data vibrationsystem_data, VibrationSystem.User vibrationsystem_user, VibrationInfo vibrationinfo) {
         BlockPos blockpos = BlockPos.containing(vibrationinfo.pos());
         BlockPos blockpos1 = vibrationsystem_user.getPositionSource().getPosition(serverlevel).map(BlockPos::containing).orElse(blockpos);
         if (vibrationsystem_user.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking(serverlevel, blockpos1)) {
            return false;
         } else {
            vibrationsystem_user.onReceiveVibration(serverlevel, blockpos, vibrationinfo.gameEvent(), vibrationinfo.getEntity(serverlevel).orElse((Entity)null), vibrationinfo.getProjectileOwner(serverlevel).orElse((Entity)null), VibrationSystem.Listener.distanceBetweenInBlocks(blockpos, blockpos1));
            vibrationsystem_data.setCurrentVibration((VibrationInfo)null);
            return true;
         }
      }

      private static boolean areAdjacentChunksTicking(Level level, BlockPos blockpos) {
         ChunkPos chunkpos = new ChunkPos(blockpos);

         for(int i = chunkpos.x - 1; i < chunkpos.x + 1; ++i) {
            for(int j = chunkpos.z - 1; j < chunkpos.z + 1; ++j) {
               ChunkAccess chunkaccess = level.getChunkSource().getChunkNow(i, j);
               if (chunkaccess == null || !level.shouldTickBlocksAt(chunkaccess.getPos().toLong())) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public interface User {
      int getListenerRadius();

      PositionSource getPositionSource();

      boolean canReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, GameEvent.Context gameevent_context);

      void onReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, @Nullable Entity entity, @Nullable Entity entity1, float f);

      default TagKey<GameEvent> getListenableEvents() {
         return GameEventTags.VIBRATIONS;
      }

      default boolean canTriggerAvoidVibration() {
         return false;
      }

      default boolean requiresAdjacentChunksToBeTicking() {
         return false;
      }

      default int calculateTravelTimeInTicks(float f) {
         return Mth.floor(f);
      }

      default boolean isValidVibration(GameEvent gameevent, GameEvent.Context gameevent_context) {
         if (!gameevent.is(this.getListenableEvents())) {
            return false;
         } else {
            Entity entity = gameevent_context.sourceEntity();
            if (entity != null) {
               if (entity.isSpectator()) {
                  return false;
               }

               if (entity.isSteppingCarefully() && gameevent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                  if (this.canTriggerAvoidVibration() && entity instanceof ServerPlayer) {
                     ServerPlayer serverplayer = (ServerPlayer)entity;
                     CriteriaTriggers.AVOID_VIBRATION.trigger(serverplayer);
                  }

                  return false;
               }

               if (entity.dampensVibrations()) {
                  return false;
               }
            }

            if (gameevent_context.affectedState() != null) {
               return !gameevent_context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
            } else {
               return true;
            }
         }
      }

      default void onDataChanged() {
      }
   }
}
