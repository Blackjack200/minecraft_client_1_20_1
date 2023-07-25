package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat(Instant timestamp, long heapUsed, GcHeapStat.Timing timing) {
   public static GcHeapStat from(RecordedEvent recordedevent) {
      return new GcHeapStat(recordedevent.getStartTime(), recordedevent.getLong("heapUsed"), recordedevent.getString("when").equalsIgnoreCase("before gc") ? GcHeapStat.Timing.BEFORE_GC : GcHeapStat.Timing.AFTER_GC);
   }

   public static GcHeapStat.Summary summary(Duration duration, List<GcHeapStat> list, Duration duration1, int i) {
      return new GcHeapStat.Summary(duration, duration1, i, calculateAllocationRatePerSecond(list));
   }

   private static double calculateAllocationRatePerSecond(List<GcHeapStat> list) {
      long i = 0L;
      Map<GcHeapStat.Timing, List<GcHeapStat>> map = list.stream().collect(Collectors.groupingBy((gcheapstat2) -> gcheapstat2.timing));
      List<GcHeapStat> list1 = map.get(GcHeapStat.Timing.BEFORE_GC);
      List<GcHeapStat> list2 = map.get(GcHeapStat.Timing.AFTER_GC);

      for(int j = 1; j < list1.size(); ++j) {
         GcHeapStat gcheapstat = list1.get(j);
         GcHeapStat gcheapstat1 = list2.get(j - 1);
         i += gcheapstat.heapUsed - gcheapstat1.heapUsed;
      }

      Duration duration = Duration.between((list.get(1)).timestamp, (list.get(list.size() - 1)).timestamp);
      return (double)i / (double)duration.getSeconds();
   }

   public static record Summary(Duration duration, Duration gcTotalDuration, int totalGCs, double allocationRateBytesPerSecond) {
      public float gcOverHead() {
         return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
      }
   }

   static enum Timing {
      BEFORE_GC,
      AFTER_GC;
   }
}
