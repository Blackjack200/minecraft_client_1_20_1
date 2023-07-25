package net.minecraft.util.profiling;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.slf4j.Logger;

public class SingleTickProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final LongSupplier realTime;
   private final long saveThreshold;
   private int tick;
   private final File location;
   private ProfileCollector profiler = InactiveProfiler.INSTANCE;

   public SingleTickProfiler(LongSupplier longsupplier, String s, long i) {
      this.realTime = longsupplier;
      this.location = new File("debug", s);
      this.saveThreshold = i;
   }

   public ProfilerFiller startTick() {
      this.profiler = new ActiveProfiler(this.realTime, () -> this.tick, false);
      ++this.tick;
      return this.profiler;
   }

   public void endTick() {
      if (this.profiler != InactiveProfiler.INSTANCE) {
         ProfileResults profileresults = this.profiler.getResults();
         this.profiler = InactiveProfiler.INSTANCE;
         if (profileresults.getNanoDuration() >= this.saveThreshold) {
            File file = new File(this.location, "tick-results-" + Util.getFilenameFormattedDateTime() + ".txt");
            profileresults.saveResults(file.toPath());
            LOGGER.info("Recorded long tick -- wrote info to: {}", (Object)file.getAbsolutePath());
         }

      }
   }

   @Nullable
   public static SingleTickProfiler createTickProfiler(String s) {
      return null;
   }

   public static ProfilerFiller decorateFiller(ProfilerFiller profilerfiller, @Nullable SingleTickProfiler singletickprofiler) {
      return singletickprofiler != null ? ProfilerFiller.tee(singletickprofiler.startTick(), profilerfiller) : profilerfiller;
   }
}
