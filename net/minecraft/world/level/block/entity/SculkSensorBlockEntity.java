package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {
   private static final Logger LOGGER = LogUtils.getLogger();
   private VibrationSystem.Data vibrationData;
   private final VibrationSystem.Listener vibrationListener;
   private final VibrationSystem.User vibrationUser = this.createVibrationUser();
   private int lastVibrationFrequency;

   protected SculkSensorBlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate) {
      super(blockentitytype, blockpos, blockstate);
      this.vibrationData = new VibrationSystem.Data();
      this.vibrationListener = new VibrationSystem.Listener(this);
   }

   public SculkSensorBlockEntity(BlockPos blockpos, BlockState blockstate) {
      this(BlockEntityType.SCULK_SENSOR, blockpos, blockstate);
   }

   public VibrationSystem.User createVibrationUser() {
      return new SculkSensorBlockEntity.VibrationUser(this.getBlockPos());
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.lastVibrationFrequency = compoundtag.getInt("last_vibration_frequency");
      if (compoundtag.contains("listener", 10)) {
         VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent((vibrationsystem_data) -> this.vibrationData = vibrationsystem_data);
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putInt("last_vibration_frequency", this.lastVibrationFrequency);
      VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("listener", tag));
   }

   public VibrationSystem.Data getVibrationData() {
      return this.vibrationData;
   }

   public VibrationSystem.User getVibrationUser() {
      return this.vibrationUser;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public void setLastVibrationFrequency(int i) {
      this.lastVibrationFrequency = i;
   }

   public VibrationSystem.Listener getListener() {
      return this.vibrationListener;
   }

   protected class VibrationUser implements VibrationSystem.User {
      public static final int LISTENER_RANGE = 8;
      protected final BlockPos blockPos;
      private final PositionSource positionSource;

      public VibrationUser(BlockPos blockpos) {
         this.blockPos = blockpos;
         this.positionSource = new BlockPositionSource(blockpos);
      }

      public int getListenerRadius() {
         return 8;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public boolean canTriggerAvoidVibration() {
         return true;
      }

      public boolean canReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, @Nullable GameEvent.Context gameevent_context) {
         return !blockpos.equals(this.blockPos) || gameevent != GameEvent.BLOCK_DESTROY && gameevent != GameEvent.BLOCK_PLACE ? SculkSensorBlock.canActivate(SculkSensorBlockEntity.this.getBlockState()) : false;
      }

      public void onReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, @Nullable Entity entity, @Nullable Entity entity1, float f) {
         BlockState blockstate = SculkSensorBlockEntity.this.getBlockState();
         if (SculkSensorBlock.canActivate(blockstate)) {
            SculkSensorBlockEntity.this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(gameevent));
            int i = VibrationSystem.getRedstoneStrengthForDistance(f, this.getListenerRadius());
            Block var10 = blockstate.getBlock();
            if (var10 instanceof SculkSensorBlock) {
               SculkSensorBlock sculksensorblock = (SculkSensorBlock)var10;
               sculksensorblock.activate(entity, serverlevel, this.blockPos, blockstate, i, SculkSensorBlockEntity.this.getLastVibrationFrequency());
            }
         }

      }

      public void onDataChanged() {
         SculkSensorBlockEntity.this.setChanged();
      }

      public boolean requiresAdjacentChunksToBeTicking() {
         return true;
      }
   }
}
