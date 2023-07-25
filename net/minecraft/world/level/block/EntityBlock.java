package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;

public interface EntityBlock {
   @Nullable
   BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate);

   @Nullable
   default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return null;
   }

   @Nullable
   default <T extends BlockEntity> GameEventListener getListener(ServerLevel serverlevel, T blockentity) {
      if (blockentity instanceof GameEventListener.Holder<?> gameeventlistener_holder) {
         return gameeventlistener_holder.getListener();
      } else {
         return null;
      }
   }
}
