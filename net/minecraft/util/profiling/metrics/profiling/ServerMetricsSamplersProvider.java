package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerMetricsSamplersProvider implements MetricsSamplerProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Set<MetricSampler> samplers = new ObjectOpenHashSet<>();
   private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

   public ServerMetricsSamplersProvider(LongSupplier longsupplier, boolean flag) {
      this.samplers.add(tickTimeSampler(longsupplier));
      if (flag) {
         this.samplers.addAll(runtimeIndependentSamplers());
      }

   }

   public static Set<MetricSampler> runtimeIndependentSamplers() {
      ImmutableSet.Builder<MetricSampler> immutableset_builder = ImmutableSet.builder();

      try {
         ServerMetricsSamplersProvider.CpuStats servermetricssamplersprovider_cpustats = new ServerMetricsSamplersProvider.CpuStats();
         IntStream.range(0, servermetricssamplersprovider_cpustats.nrOfCpus).mapToObj((i) -> MetricSampler.create("cpu#" + i, MetricCategory.CPU, () -> servermetricssamplersprovider_cpustats.loadForCpu(i))).forEach(immutableset_builder::add);
      } catch (Throwable var2) {
         LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", var2);
      }

      immutableset_builder.add(MetricSampler.create("heap MiB", MetricCategory.JVM, () -> (double)((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0F)));
      immutableset_builder.addAll(MetricsRegistry.INSTANCE.getRegisteredSamplers());
      return immutableset_builder.build();
   }

   public Set<MetricSampler> samplers(Supplier<ProfileCollector> supplier) {
      this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler(supplier));
      return this.samplers;
   }

   public static MetricSampler tickTimeSampler(final LongSupplier longsupplier) {
      Stopwatch stopwatch = Stopwatch.createUnstarted(new Ticker() {
         public long read() {
            return longsupplier.getAsLong();
         }
      });
      ToDoubleFunction<Stopwatch> todoublefunction = (stopwatch1) -> {
         if (stopwatch1.isRunning()) {
            stopwatch1.stop();
         }

         long i = stopwatch1.elapsed(TimeUnit.NANOSECONDS);
         stopwatch1.reset();
         return (double)i;
      };
      MetricSampler.ValueIncreasedByPercentage metricsampler_valueincreasedbypercentage = new MetricSampler.ValueIncreasedByPercentage(2.0F);
      return MetricSampler.builder("ticktime", MetricCategory.TICK_LOOP, todoublefunction, stopwatch).withBeforeTick(Stopwatch::start).withThresholdAlert(metricsampler_valueincreasedbypercentage).build();
   }

   static class CpuStats {
      private final SystemInfo systemInfo = new SystemInfo();
      private final CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
      public final int nrOfCpus = this.processor.getLogicalProcessorCount();
      private long[][] previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
      private double[] currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
      private long lastPollMs;

      public double loadForCpu(int i) {
         long j = System.currentTimeMillis();
         if (this.lastPollMs == 0L || this.lastPollMs + 501L < j) {
            this.currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
            this.previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
            this.lastPollMs = j;
         }

         return this.currentLoad[i] * 100.0D;
      }
   }
}
