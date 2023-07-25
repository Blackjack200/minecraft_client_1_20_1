package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
   boolean setBlock(BlockPos blockpos, BlockState blockstate, int i, int j);

   default boolean setBlock(BlockPos blockpos, BlockState blockstate, int i) {
      return this.setBlock(blockpos, blockstate, i, 512);
   }

   boolean removeBlock(BlockPos blockpos, boolean flag);

   default boolean destroyBlock(BlockPos blockpos, boolean flag) {
      return this.destroyBlock(blockpos, flag, (Entity)null);
   }

   default boolean destroyBlock(BlockPos blockpos, boolean flag, @Nullable Entity entity) {
      return this.destroyBlock(blockpos, flag, entity, 512);
   }

   boolean destroyBlock(BlockPos blockpos, boolean flag, @Nullable Entity entity, int i);

   default boolean addFreshEntity(Entity entity) {
      return false;
   }
}
