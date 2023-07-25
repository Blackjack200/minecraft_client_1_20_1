package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public class BlackholeTickAccess {
   private static final TickContainerAccess<Object> CONTAINER_BLACKHOLE = new TickContainerAccess<Object>() {
      public void schedule(ScheduledTick<Object> scheduledtick) {
      }

      public boolean hasScheduledTick(BlockPos blockpos, Object object) {
         return false;
      }

      public int count() {
         return 0;
      }
   };
   private static final LevelTickAccess<Object> LEVEL_BLACKHOLE = new LevelTickAccess<Object>() {
      public void schedule(ScheduledTick<Object> scheduledtick) {
      }

      public boolean hasScheduledTick(BlockPos blockpos, Object object) {
         return false;
      }

      public boolean willTickThisTick(BlockPos blockpos, Object object) {
         return false;
      }

      public int count() {
         return 0;
      }
   };

   public static <T> TickContainerAccess<T> emptyContainer() {
      return CONTAINER_BLACKHOLE;
   }

   public static <T> LevelTickAccess<T> emptyLevelList() {
      return LEVEL_BLACKHOLE;
   }
}
