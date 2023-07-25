package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.profiling.metrics.storage.RecordedDeviation;

public class ActiveMetricsRecorder implements MetricsRecorder {
   public static final int PROFILING_MAX_DURATION_SECONDS = 10;
   @Nullable
   private static Consumer<Path> globalOnReportFinished = null;
   private final Map<MetricSampler, List<RecordedDeviation>> deviationsBySampler = new Object2ObjectOpenHashMap<>();
   private final ContinuousProfiler taskProfiler;
   private final Executor ioExecutor;
   private final MetricsPersister metricsPersister;
   private final Consumer<ProfileResults> onProfilingEnd;
   private final Consumer<Path> onReportFinished;
   private final MetricsSamplerProvider metricsSamplerProvider;
   private final LongSupplier wallTimeSource;
   private final long deadlineNano;
   private int currentTick;
   private ProfileCollector singleTickProfiler;
   private volatile boolean killSwitch;
   private Set<MetricSampler> thisTickSamplers = ImmutableSet.of();

   private ActiveMetricsRecorder(MetricsSamplerProvider metricssamplerprovider, LongSupplier longsupplier, Executor executor, MetricsPersister metricspersister, Consumer<ProfileResults> consumer, Consumer<Path> consumer1) {
      this.metricsSamplerProvider = metricssamplerprovider;
      this.wallTimeSource = longsupplier;
      this.taskProfiler = new ContinuousProfiler(longsupplier, () -> this.currentTick);
      this.ioExecutor = executor;
      this.metricsPersister = metricspersister;
      this.onProfilingEnd = consumer;
      this.onReportFinished = globalOnReportFinished == null ? consumer1 : consumer1.andThen(globalOnReportFinished);
      this.deadlineNano = longsupplier.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
      this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
      this.taskProfiler.enable();
   }

   public static ActiveMetricsRecorder createStarted(MetricsSamplerProvider metricssamplerprovider, LongSupplier longsupplier, Executor executor, MetricsPersister metricspersister, Consumer<ProfileResults> consumer, Consumer<Path> consumer1) {
      return new ActiveMetricsRecorder(metricssamplerprovider, longsupplier, executor, metricspersister, consumer, consumer1);
   }

   public synchronized void end() {
      if (this.isRecording()) {
         this.killSwitch = true;
      }
   }

   public synchronized void cancel() {
      if (this.isRecording()) {
         this.singleTickProfiler = InactiveProfiler.INSTANCE;
         this.onProfilingEnd.accept(EmptyProfileResults.EMPTY);
         this.cleanup(this.thisTickSamplers);
      }
   }

   public void startTick() {
      this.verifyStarted();
      this.thisTickSamplers = this.metricsSamplerProvider.samplers(() -> this.singleTickProfiler);

      for(MetricSampler metricsampler : this.thisTickSamplers) {
         metricsampler.onStartTick();
      }

      ++this.currentTick;
   }

   public void endTick() {
      this.verifyStarted();
      if (this.currentTick != 0) {
         for(MetricSampler metricsampler : this.thisTickSamplers) {
            metricsampler.onEndTick(this.currentTick);
            if (metricsampler.triggersThreshold()) {
               RecordedDeviation recordeddeviation = new RecordedDeviation(Instant.now(), this.currentTick, this.singleTickProfiler.getResults());
               this.deviationsBySampler.computeIfAbsent(metricsampler, (metricsampler1) -> Lists.newArrayList()).add(recordeddeviation);
            }
         }

         if (!this.killSwitch && this.wallTimeSource.getAsLong() <= this.deadlineNano) {
            this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
         } else {
            this.killSwitch = false;
            ProfileResults profileresults = this.taskProfiler.getResults();
            this.singleTickProfiler = InactiveProfiler.INSTANCE;
            this.onProfilingEnd.accept(profileresults);
            this.scheduleSaveResults(profileresults);
         }
      }
   }

   public boolean isRecording() {
      return this.taskProfiler.isEnabled();
   }

   public ProfilerFiller getProfiler() {
      return ProfilerFiller.tee(this.taskProfiler.getFiller(), this.singleTickProfiler);
   }

   private void verifyStarted() {
      if (!this.isRecording()) {
         throw new IllegalStateException("Not started!");
      }
   }

   private void scheduleSaveResults(ProfileResults profileresults) {
      HashSet<MetricSampler> hashset = new HashSet<>(this.thisTickSamplers);
      this.ioExecutor.execute(() -> {
         Path path = this.metricsPersister.saveReports(hashset, this.deviationsBySampler, profileresults);
         this.cleanup(hashset);
         this.onReportFinished.accept(path);
      });
   }

   private void cleanup(Collection<MetricSampler> collection) {
      for(MetricSampler metricsampler : collection) {
         metricsampler.onFinished();
      }

      this.deviationsBySampler.clear();
      this.taskProfiler.disable();
   }

   public static void registerGlobalCompletionCallback(Consumer<Path> consumer) {
      globalOnReportFinished = consumer;
   }
}
