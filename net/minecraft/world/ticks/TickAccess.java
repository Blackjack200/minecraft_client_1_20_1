package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public interface TickAccess<T> {
   void schedule(ScheduledTick<T> scheduledtick);

   boolean hasScheduledTick(BlockPos blockpos, T object);

   int count();
}
