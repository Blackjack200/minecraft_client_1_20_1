package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public record ScheduledTick<T>(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
   public static final Comparator<ScheduledTick<?>> DRAIN_ORDER = (scheduledtick, scheduledtick1) -> {
      int i = Long.compare(scheduledtick.triggerTick, scheduledtick1.triggerTick);
      if (i != 0) {
         return i;
      } else {
         i = scheduledtick.priority.compareTo(scheduledtick1.priority);
         return i != 0 ? i : Long.compare(scheduledtick.subTickOrder, scheduledtick1.subTickOrder);
      }
   };
   public static final Comparator<ScheduledTick<?>> INTRA_TICK_DRAIN_ORDER = (scheduledtick, scheduledtick1) -> {
      int i = scheduledtick.priority.compareTo(scheduledtick1.priority);
      return i != 0 ? i : Long.compare(scheduledtick.subTickOrder, scheduledtick1.subTickOrder);
   };
   public static final Hash.Strategy<ScheduledTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<ScheduledTick<?>>() {
      public int hashCode(ScheduledTick<?> scheduledtick) {
         return 31 * scheduledtick.pos().hashCode() + scheduledtick.type().hashCode();
      }

      public boolean equals(@Nullable ScheduledTick<?> scheduledtick, @Nullable ScheduledTick<?> scheduledtick1) {
         if (scheduledtick == scheduledtick1) {
            return true;
         } else if (scheduledtick != null && scheduledtick1 != null) {
            return scheduledtick.type() == scheduledtick1.type() && scheduledtick.pos().equals(scheduledtick1.pos());
         } else {
            return false;
         }
      }
   };

   public ScheduledTick(T object, BlockPos blockpos, long i, long j) {
      this(object, blockpos, i, TickPriority.NORMAL, j);
   }

   public ScheduledTick {
      blockpos = blockpos.immutable();
   }

   public static <T> ScheduledTick<T> probe(T object, BlockPos blockpos) {
      return new ScheduledTick<>(object, blockpos, 0L, TickPriority.NORMAL, 0L);
   }
}
