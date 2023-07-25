package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
   public CalibratedSculkSensorBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.CALIBRATED_SCULK_SENSOR, blockpos, blockstate);
   }

   public VibrationSystem.User createVibrationUser() {
      return new CalibratedSculkSensorBlockEntity.VibrationUser(this.getBlockPos());
   }

   protected class VibrationUser extends SculkSensorBlockEntity.VibrationUser {
      public VibrationUser(BlockPos blockpos) {
         super(blockpos);
      }

      public int getListenerRadius() {
         return 16;
      }

      public boolean canReceiveVibration(ServerLevel serverlevel, BlockPos blockpos, GameEvent gameevent, @Nullable GameEvent.Context gameevent_context) {
         int i = this.getBackSignal(serverlevel, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());
         return i != 0 && VibrationSystem.getGameEventFrequency(gameevent) != i ? false : super.canReceiveVibration(serverlevel, blockpos, gameevent, gameevent_context);
      }

      private int getBackSignal(Level level, BlockPos blockpos, BlockState blockstate) {
         Direction direction = blockstate.getValue(CalibratedSculkSensorBlock.FACING).getOpposite();
         return level.getSignal(blockpos.relative(direction), direction);
      }
   }
}
