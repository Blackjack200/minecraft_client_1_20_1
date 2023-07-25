package net.minecraft.util.profiling.metrics.storage;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class MetricsPersister {
   public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling");
   public static final String METRICS_DIR_NAME = "metrics";
   public static final String DEVIATIONS_DIR_NAME = "deviations";
   public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String rootFolderName;

   public MetricsPersister(String s) {
      this.rootFolderName = s;
   }

   public Path saveReports(Set<MetricSampler> set, Map<MetricSampler, List<RecordedDeviation>> map, ProfileResults profileresults) {
      try {
         Files.createDirectories(PROFILING_RESULTS_DIR);
      } catch (IOException var8) {
         throw new UncheckedIOException(var8);
      }

      try {
         Path path = Files.createTempDirectory("minecraft-profiling");
         path.toFile().deleteOnExit();
         Files.createDirectories(PROFILING_RESULTS_DIR);
         Path path1 = path.resolve(this.rootFolderName);
         Path path2 = path1.resolve("metrics");
         this.saveMetrics(set, path2);
         if (!map.isEmpty()) {
            this.saveDeviations(map, path1.resolve("deviations"));
         }

         this.saveProfilingTaskExecutionResult(profileresults, path1);
         return path;
      } catch (IOException var7) {
         throw new UncheckedIOException(var7);
      }
   }

   private void saveMetrics(Set<MetricSampler> set, Path path) {
      if (set.isEmpty()) {
         throw new IllegalArgumentException("Expected at least one sampler to persist");
      } else {
         Map<MetricCategory, List<MetricSampler>> map = set.stream().collect(Collectors.groupingBy(MetricSampler::getCategory));
         map.forEach((metriccategory, list) -> this.saveCategory(metriccategory, list, path));
      }
   }

   private void saveCategory(MetricCategory metriccategory, List<MetricSampler> list, Path path) {
      Path path1 = path.resolve(Util.sanitizeName(metriccategory.getDescription(), ResourceLocation::validPathChar) + ".csv");
      Writer writer = null;

      try {
         Files.createDirectories(path1.getParent());
         writer = Files.newBufferedWriter(path1, StandardCharsets.UTF_8);
         CsvOutput.Builder csvoutput_builder = CsvOutput.builder();
         csvoutput_builder.addColumn("@tick");

         for(MetricSampler metricsampler : list) {
            csvoutput_builder.addColumn(metricsampler.getName());
         }

         CsvOutput csvoutput = csvoutput_builder.build(writer);
         List<MetricSampler.SamplerResult> list1 = list.stream().map(MetricSampler::result).collect(Collectors.toList());
         int i = list1.stream().mapToInt(MetricSampler.SamplerResult::getFirstTick).summaryStatistics().getMin();
         int j = list1.stream().mapToInt(MetricSampler.SamplerResult::getLastTick).summaryStatistics().getMax();

         for(int k = i; k <= j; ++k) {
            int l = k;
            Stream<String> stream = list1.stream().map((metricsampler_samplerresult) -> String.valueOf(metricsampler_samplerresult.valueAtTick(l)));
            Object[] aobject = Stream.concat(Stream.of(String.valueOf(k)), stream).toArray((i1) -> new String[i1]);
            csvoutput.writeRow(aobject);
         }

         LOGGER.info("Flushed metrics to {}", (Object)path1);
      } catch (Exception var18) {
         LOGGER.error("Could not save profiler results to {}", path1, var18);
      } finally {
         IOUtils.closeQuietly(writer);
      }

   }

   private void saveDeviations(Map<MetricSampler, List<RecordedDeviation>> map, Path path) {
      DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS", Locale.UK).withZone(ZoneId.systemDefault());
      map.forEach((metricsampler, list) -> list.forEach((recordeddeviation) -> {
            String s = datetimeformatter.format(recordeddeviation.timestamp);
            Path path3 = path.resolve(Util.sanitizeName(metricsampler.getName(), ResourceLocation::validPathChar)).resolve(String.format(Locale.ROOT, "%d@%s.txt", recordeddeviation.tick, s));
            recordeddeviation.profilerResultAtTick.saveResults(path3);
         }));
   }

   private void saveProfilingTaskExecutionResult(ProfileResults profileresults, Path path) {
      profileresults.saveResults(path.resolve("profiling.txt"));
   }
}
