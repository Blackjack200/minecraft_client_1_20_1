package net.minecraft.util.profiling;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class FilledProfileResults implements ProfileResults {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ProfilerPathEntry EMPTY = new ProfilerPathEntry() {
      public long getDuration() {
         return 0L;
      }

      public long getMaxDuration() {
         return 0L;
      }

      public long getCount() {
         return 0L;
      }

      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.emptyMap();
      }
   };
   private static final Splitter SPLITTER = Splitter.on('\u001e');
   private static final Comparator<Map.Entry<String, FilledProfileResults.CounterCollector>> COUNTER_ENTRY_COMPARATOR = Entry.<String, FilledProfileResults.CounterCollector>comparingByValue(Comparator.comparingLong((filledprofileresults_countercollector) -> filledprofileresults_countercollector.totalValue)).reversed();
   private final Map<String, ? extends ProfilerPathEntry> entries;
   private final long startTimeNano;
   private final int startTimeTicks;
   private final long endTimeNano;
   private final int endTimeTicks;
   private final int tickDuration;

   public FilledProfileResults(Map<String, ? extends ProfilerPathEntry> map, long i, int j, long k, int l) {
      this.entries = map;
      this.startTimeNano = i;
      this.startTimeTicks = j;
      this.endTimeNano = k;
      this.endTimeTicks = l;
      this.tickDuration = l - j;
   }

   private ProfilerPathEntry getEntry(String s) {
      ProfilerPathEntry profilerpathentry = this.entries.get(s);
      return profilerpathentry != null ? profilerpathentry : EMPTY;
   }

   public List<ResultField> getTimes(String s) {
      String s1 = s;
      ProfilerPathEntry profilerpathentry = this.getEntry("root");
      long i = profilerpathentry.getDuration();
      ProfilerPathEntry profilerpathentry1 = this.getEntry(s);
      long j = profilerpathentry1.getDuration();
      long k = profilerpathentry1.getCount();
      List<ResultField> list = Lists.newArrayList();
      if (!s.isEmpty()) {
         s = s + "\u001e";
      }

      long l = 0L;

      for(String s2 : this.entries.keySet()) {
         if (isDirectChild(s, s2)) {
            l += this.getEntry(s2).getDuration();
         }
      }

      float f = (float)l;
      if (l < j) {
         l = j;
      }

      if (i < l) {
         i = l;
      }

      for(String s3 : this.entries.keySet()) {
         if (isDirectChild(s, s3)) {
            ProfilerPathEntry profilerpathentry2 = this.getEntry(s3);
            long i1 = profilerpathentry2.getDuration();
            double d0 = (double)i1 * 100.0D / (double)l;
            double d1 = (double)i1 * 100.0D / (double)i;
            String s4 = s3.substring(s.length());
            list.add(new ResultField(s4, d0, d1, profilerpathentry2.getCount()));
         }
      }

      if ((float)l > f) {
         list.add(new ResultField("unspecified", (double)((float)l - f) * 100.0D / (double)l, (double)((float)l - f) * 100.0D / (double)i, k));
      }

      Collections.sort(list);
      list.add(0, new ResultField(s1, 100.0D, (double)l * 100.0D / (double)i, k));
      return list;
   }

   private static boolean isDirectChild(String s, String s1) {
      return s1.length() > s.length() && s1.startsWith(s) && s1.indexOf(30, s.length() + 1) < 0;
   }

   private Map<String, FilledProfileResults.CounterCollector> getCounterValues() {
      Map<String, FilledProfileResults.CounterCollector> map = Maps.newTreeMap();
      this.entries.forEach((s, profilerpathentry) -> {
         Object2LongMap<String> object2longmap = profilerpathentry.getCounters();
         if (!object2longmap.isEmpty()) {
            List<String> list = SPLITTER.splitToList(s);
            object2longmap.forEach((s1, olong) -> map.computeIfAbsent(s1, (s2) -> new FilledProfileResults.CounterCollector()).addValue(list.iterator(), olong));
         }

      });
      return map;
   }

   public long getStartTimeNano() {
      return this.startTimeNano;
   }

   public int getStartTimeTicks() {
      return this.startTimeTicks;
   }

   public long getEndTimeNano() {
      return this.endTimeNano;
   }

   public int getEndTimeTicks() {
      return this.endTimeTicks;
   }

   public boolean saveResults(Path path) {
      Writer writer = null;

      boolean var4;
      try {
         Files.createDirectories(path.getParent());
         writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
         writer.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
         return true;
      } catch (Throwable var8) {
         LOGGER.error("Could not save profiler results to {}", path, var8);
         var4 = false;
      } finally {
         IOUtils.closeQuietly(writer);
      }

      return var4;
   }

   protected String getProfilerResults(long i, int j) {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("---- Minecraft Profiler Results ----\n");
      stringbuilder.append("// ");
      stringbuilder.append(getComment());
      stringbuilder.append("\n\n");
      stringbuilder.append("Version: ").append(SharedConstants.getCurrentVersion().getId()).append('\n');
      stringbuilder.append("Time span: ").append(i / 1000000L).append(" ms\n");
      stringbuilder.append("Tick span: ").append(j).append(" ticks\n");
      stringbuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", (float)j / ((float)i / 1.0E9F))).append(" ticks per second. It should be ").append((int)20).append(" ticks per second\n\n");
      stringbuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
      this.appendProfilerResults(0, "root", stringbuilder);
      stringbuilder.append("--- END PROFILE DUMP ---\n\n");
      Map<String, FilledProfileResults.CounterCollector> map = this.getCounterValues();
      if (!map.isEmpty()) {
         stringbuilder.append("--- BEGIN COUNTER DUMP ---\n\n");
         this.appendCounters(map, stringbuilder, j);
         stringbuilder.append("--- END COUNTER DUMP ---\n\n");
      }

      return stringbuilder.toString();
   }

   public String getProfilerResults() {
      StringBuilder stringbuilder = new StringBuilder();
      this.appendProfilerResults(0, "root", stringbuilder);
      return stringbuilder.toString();
   }

   private static StringBuilder indentLine(StringBuilder stringbuilder, int i) {
      stringbuilder.append(String.format(Locale.ROOT, "[%02d] ", i));

      for(int j = 0; j < i; ++j) {
         stringbuilder.append("|   ");
      }

      return stringbuilder;
   }

   private void appendProfilerResults(int i, String s, StringBuilder stringbuilder) {
      List<ResultField> list = this.getTimes(s);
      Object2LongMap<String> object2longmap = ObjectUtils.firstNonNull(this.entries.get(s), EMPTY).getCounters();
      object2longmap.forEach((s1, olong) -> indentLine(stringbuilder, i).append('#').append(s1).append(' ').append((Object)olong).append('/').append(olong / (long)this.tickDuration).append('\n'));
      if (list.size() >= 3) {
         for(int j = 1; j < list.size(); ++j) {
            ResultField resultfield = list.get(j);
            indentLine(stringbuilder, i).append(resultfield.name).append('(').append(resultfield.count).append('/').append(String.format(Locale.ROOT, "%.0f", (float)resultfield.count / (float)this.tickDuration)).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", resultfield.percentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", resultfield.globalPercentage)).append("%\n");
            if (!"unspecified".equals(resultfield.name)) {
               try {
                  this.appendProfilerResults(i + 1, s + "\u001e" + resultfield.name, stringbuilder);
               } catch (Exception var9) {
                  stringbuilder.append("[[ EXCEPTION ").append((Object)var9).append(" ]]");
               }
            }
         }

      }
   }

   private void appendCounterResults(int i, String s, FilledProfileResults.CounterCollector filledprofileresults_countercollector, int j, StringBuilder stringbuilder) {
      indentLine(stringbuilder, i).append(s).append(" total:").append(filledprofileresults_countercollector.selfValue).append('/').append(filledprofileresults_countercollector.totalValue).append(" average: ").append(filledprofileresults_countercollector.selfValue / (long)j).append('/').append(filledprofileresults_countercollector.totalValue / (long)j).append('\n');
      filledprofileresults_countercollector.children.entrySet().stream().sorted(COUNTER_ENTRY_COMPARATOR).forEach((map_entry) -> this.appendCounterResults(i + 1, map_entry.getKey(), map_entry.getValue(), j, stringbuilder));
   }

   private void appendCounters(Map<String, FilledProfileResults.CounterCollector> map, StringBuilder stringbuilder, int i) {
      map.forEach((s, filledprofileresults_countercollector) -> {
         stringbuilder.append("-- Counter: ").append(s).append(" --\n");
         this.appendCounterResults(0, "root", filledprofileresults_countercollector.children.get("root"), i, stringbuilder);
         stringbuilder.append("\n\n");
      });
   }

   private static String getComment() {
      String[] astring = new String[]{"I'd Rather Be Surfing", "Shiny numbers!", "Am I not running fast enough? :(", "I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!", "Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers", "Now with the same numbers", "You should add flames to things, it makes them go faster!", "Do you feel the need for... optimization?", "*cracks redstone whip*", "Maybe if you treated it better then it'll have more motivation to work faster! Poor server."};

      try {
         return astring[(int)(Util.getNanos() % (long)astring.length)];
      } catch (Throwable var2) {
         return "Witty comment unavailable :(";
      }
   }

   public int getTickDuration() {
      return this.tickDuration;
   }

   static class CounterCollector {
      long selfValue;
      long totalValue;
      final Map<String, FilledProfileResults.CounterCollector> children = Maps.newHashMap();

      public void addValue(Iterator<String> iterator, long i) {
         this.totalValue += i;
         if (!iterator.hasNext()) {
            this.selfValue += i;
         } else {
            this.children.computeIfAbsent(iterator.next(), (s) -> new FilledProfileResults.CounterCollector()).addValue(iterator, i);
         }

      }
   }
}
