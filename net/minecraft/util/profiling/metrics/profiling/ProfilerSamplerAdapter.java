package net.minecraft.util.profiling.metrics.profiling;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;

public class ProfilerSamplerAdapter {
   private final Set<String> previouslyFoundSamplerNames = new ObjectOpenHashSet<>();

   public Set<MetricSampler> newSamplersFoundInProfiler(Supplier<ProfileCollector> supplier) {
      Set<MetricSampler> set = supplier.get().getChartedPaths().stream().filter((pair1) -> !this.previouslyFoundSamplerNames.contains(pair1.getLeft())).map((pair) -> samplerForProfilingPath(supplier, pair.getLeft(), pair.getRight())).collect(Collectors.toSet());

      for(MetricSampler metricsampler : set) {
         this.previouslyFoundSamplerNames.add(metricsampler.getName());
      }

      return set;
   }

   private static MetricSampler samplerForProfilingPath(Supplier<ProfileCollector> supplier, String s, MetricCategory metriccategory) {
      return MetricSampler.create(s, metriccategory, () -> {
         ActiveProfiler.PathEntry activeprofiler_pathentry = supplier.get().getEntry(s);
         return activeprofiler_pathentry == null ? 0.0D : (double)activeprofiler_pathentry.getMaxDuration() / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND;
      });
   }
}
