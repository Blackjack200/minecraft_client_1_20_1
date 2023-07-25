package net.minecraft.util.profiling.metrics;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class MetricsRegistry {
   public static final MetricsRegistry INSTANCE = new MetricsRegistry();
   private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap<>();

   private MetricsRegistry() {
   }

   public void add(ProfilerMeasured profilermeasured) {
      this.measuredInstances.put(profilermeasured, (Void)null);
   }

   public List<MetricSampler> getRegisteredSamplers() {
      Map<String, List<MetricSampler>> map = this.measuredInstances.keySet().stream().flatMap((profilermeasured) -> profilermeasured.profiledMetrics().stream()).collect(Collectors.groupingBy(MetricSampler::getName));
      return aggregateDuplicates(map);
   }

   private static List<MetricSampler> aggregateDuplicates(Map<String, List<MetricSampler>> map) {
      return map.entrySet().stream().map((map_entry) -> {
         String s = map_entry.getKey();
         List<MetricSampler> list = map_entry.getValue();
         return (MetricSampler)(list.size() > 1 ? new MetricsRegistry.AggregatedMetricSampler(s, list) : list.get(0));
      }).collect(Collectors.toList());
   }

   static class AggregatedMetricSampler extends MetricSampler {
      private final List<MetricSampler> delegates;

      AggregatedMetricSampler(String s, List<MetricSampler> list) {
         super(s, list.get(0).getCategory(), () -> averageValueFromDelegates(list), () -> beforeTick(list), thresholdTest(list));
         this.delegates = list;
      }

      private static MetricSampler.ThresholdTest thresholdTest(List<MetricSampler> list) {
         return (d0) -> list.stream().anyMatch((metricsampler) -> metricsampler.thresholdTest != null ? metricsampler.thresholdTest.test(d0) : false);
      }

      private static void beforeTick(List<MetricSampler> list) {
         for(MetricSampler metricsampler : list) {
            metricsampler.onStartTick();
         }

      }

      private static double averageValueFromDelegates(List<MetricSampler> list) {
         double d0 = 0.0D;

         for(MetricSampler metricsampler : list) {
            d0 += metricsampler.getSampler().getAsDouble();
         }

         return d0 / (double)list.size();
      }

      public boolean equals(@Nullable Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            if (!super.equals(object)) {
               return false;
            } else {
               MetricsRegistry.AggregatedMetricSampler metricsregistry_aggregatedmetricsampler = (MetricsRegistry.AggregatedMetricSampler)object;
               return this.delegates.equals(metricsregistry_aggregatedmetricsampler.delegates);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(super.hashCode(), this.delegates);
      }
   }
}
