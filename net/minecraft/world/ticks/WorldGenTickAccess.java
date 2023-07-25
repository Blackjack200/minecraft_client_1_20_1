package net.minecraft.world.ticks;

import java.util.function.Function;
import net.minecraft.core.BlockPos;

public class WorldGenTickAccess<T> implements LevelTickAccess<T> {
   private final Function<BlockPos, TickContainerAccess<T>> containerGetter;

   public WorldGenTickAccess(Function<BlockPos, TickContainerAccess<T>> function) {
      this.containerGetter = function;
   }

   public boolean hasScheduledTick(BlockPos blockpos, T object) {
      return this.containerGetter.apply(blockpos).hasScheduledTick(blockpos, object);
   }

   public void schedule(ScheduledTick<T> scheduledtick) {
      this.containerGetter.apply(scheduledtick.pos()).schedule(scheduledtick);
   }

   public boolean willTickThisTick(BlockPos blockpos, T object) {
      return false;
   }

   public int count() {
      return 0;
   }
}
