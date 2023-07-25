package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
   public static FileIOStat.Summary summary(Duration duration, List<FileIOStat> list) {
      long i = list.stream().mapToLong((fileiostat3) -> fileiostat3.bytes).sum();
      return new FileIOStat.Summary(i, (double)i / (double)duration.getSeconds(), (long)list.size(), (double)list.size() / (double)duration.getSeconds(), list.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus), list.stream().filter((fileiostat2) -> fileiostat2.path != null).collect(Collectors.groupingBy((fileiostat1) -> fileiostat1.path, Collectors.summingLong((fileiostat) -> fileiostat.bytes))).entrySet().stream().sorted(Entry.<String, Long>comparingByValue().reversed()).map((map_entry) -> Pair.of(map_entry.getKey(), map_entry.getValue())).limit(10L).toList());
   }

   public static record Summary(long totalBytes, double bytesPerSecond, long counts, double countsPerSecond, Duration timeSpentInIO, List<Pair<String, Long>> topTenContributorsByTotalBytes) {
   }
}
