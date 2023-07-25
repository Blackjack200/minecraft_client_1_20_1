package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int WARNING_SOUND_RADIUS = 10;
   private static final int WARDEN_SPAWN_ATTEMPTS = 20;
   private static final int WARDEN_SPAWN_RANGE_XZ = 5;
   private static final int WARDEN_SPAWN_RANGE_Y = 6;
   private static final int DARKNESS_RADIUS = 40;
   private static final int SHRIEKING_TICKS = 90;
   private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), (int2objectopenhashmap) -> {
      int2objectopenhashmap.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
      int2objectopenhashmap.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
      int2objectopenhashmap.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
      int2objectopenhashmap.put(4, SoundEvents.WARDEN_LISTENING_ANGRY);
   });
   private int warningLevel;
   private final VibrationSystem.User vibrationUser = new SculkShriekerBlockEntity.VibrationUser();
   private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
   private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

   public SculkShriekerBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.SCULK_SHRIEKER, blockpos, blockstate);
   }

   public VibrationSystem.Data getVibrationData() {
      return this.vibrationData;
   }

   public VibrationSystem.User getVibrationUser() {
      return this.vibrationUser;
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("warning_level", 99)) {
         this.warningLevel = compoundtag.getInt("warning_level");
      }

      if (compoundtag.contains("listener", 10)) {
         VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent((vibrationsystem_data) -> this.vibrationData = vibrationsystem_data);
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putInt("warning_level", this.warningLevel);
      VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("listener", tag));
   }

   @Nullable
   public static ServerPlayer tryGetPlayer(@Nullable Entity entity) {
      if (entity instanceof ServerPlayer serverplayer) {
         return serverplayer;
      } else {
         if (entity != null) {
            LivingEntity serverplayer3 = entity.getControllingPassenger();
            if (serverplayer3 instanceof ServerPlayer) {
               ServerPlayer serverplayer1 = (ServerPlayer)serverplayer3;
               return serverplayer1;
            }
         }

         if (entity instanceof Projectile projectile) {
            Entity var3 = projectile.getOwner();
            if (var3 instanceof ServerPlayer serverplayer2) {
               return serverplayer2;
            }
         }

         if (entity instanceof ItemEntity itementity) {
            Entity var9 = itementity.getOwner();
            if (var9 instanceof ServerPlayer serverplayer3) {
               return serverplayer3;
            }
         }

         return null;
      }
   }

   public void tryShriek(ServerLevel serverlevel, @Nullable ServerPlayer serverplayer) {
      if (serverplayer != null) {
         BlockState blockstate = this.getBlockState();
         if (!blockstate.getValue(SculkShriekerBlock.SHRIEKING)) {
            this.warningLevel = 0;
            if (!this.canRespond(serverlevel) || this.tryToWarn(serverlevel, serverplayer)) {
               this.shriek(serverlevel, serverplayer);
            }
         }
      }
   }

   private boolean tryToWarn(ServerLevel serverlevel, ServerPlayer serverplayer) {
      OptionalInt optionalint = WardenSpawnTracker.tryWarn(serverlevel, this.getBlockPos(), serverplayer);
      optionalint.ifPresent((i) -> this.warningLevel = i);
      return optionalint.isPresent();
   }

   private void shriek(ServerLevel serverlevel, @Nullable Entity entity) {
      BlockPos blockpos = this.getBlockPos();
      BlockState blockstate = this.getBlockState();
      serverlevel.setBlock(blockpos, blockstate.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
      serverlevel.scheduleTick(blockpos, blockstate.getBlock(), 90);
      serverlevel.levelEvent(3007, blockpos, 0);
      serverlevel.gameEvent(GameEvent.SHRIEK, blockpos, GameEvent.Context.of(entity));
   }

   private boolean canRespond(ServerLevel serverlevel) {
      return this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON) && serverlevel.getDifficulty() != Difficulty.PEACEFUL && serverlevel.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
   }

   public void tryRespond(ServerLevel serverlevel) {
      if (this.canRespond(serverlevel) && this.warningLevel > 0) {
         if (!this.trySummonWarden(serverlevel)) {
            this.playWardenReplySound(serverlevel);
         }

         Warden.applyDarknessAround(serverlevel, Vec3.atCenterOf(this.getBlockPos()), (Entity)null, 40);
      }

   }

   private void playWardenReplySound(Level level) {
      SoundEvent soundevent = SOUND_BY_LEVEL.get(this.warningLevel);
      if (soundevent != null) {
         BlockPos blockpos = this.getBlockPos();
         int i = blockpos.getX() + Mth.randomBetweenInclusive(level.random, -10, 10);
         int j = blockpos.getY() + Mth.randomBetweenInclusive(level.random, -10, 10);
         int k = blockpos.getZ() + Mth.randomBetweenInclusive(level.random, -10, 10);
         level.playSound((Player)null, (double)i, (double)j, (double)k, soundevent, SoundSource.HOSTILE, 5.0F, 1.0F);
      }

   }

   private boolean trySummonWarden(ServerLevel serverlevel) {
      return this.warningLevel < 4 ? false : SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, serverlevel, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER).isPresent();
   }

   public VibrationSystem.Listener getListener() {
      return this.vibrationListener;
   }

   class VibrationUser implements VibrationSystem.User {
      private static final int LISTENER_RADIUS = 8;
      private final PositionSource positionSource = new BlockPositionSource(SculkShriekerBlockEntity.this.worldPosition);

      public VibrationUser() {
      }

      public int getListenerRadius() {
         return 8;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public TagKey<GameEvent> getListenableEvents() {
         return GameEventTags.SHRIEKER_CAN_LISTEN;
      }

      public boolean canReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, GameEvent.Context gameevent_context) {
         return !SculkShriekerBlockEntity.this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING) && SculkShriekerBlockEntity.tryGetPlayer(gameevent_context.sourceEntity()) != null;
      }

      public void onReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, @Nullable Entity entity, @Nullable Entity entity1, float f) {
         SculkShriekerBlockEntity.this.tryShriek(serverlevel, SculkShriekerBlockEntity.tryGetPlayer(entity1 != null ? entity1 : entity));
      }

      public void onDataChanged() {
         SculkShriekerBlockEntity.this.setChanged();
      }

      public boolean requiresAdjacentChunksToBeTicking() {
         return true;
      }
   }
}
