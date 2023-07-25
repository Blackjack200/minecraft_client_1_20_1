package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import net.minecraft.Util;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.NetworkPacketSummary;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public class JfrResultJsonSerializer {
   private static final String BYTES_PER_SECOND = "bytesPerSecond";
   private static final String COUNT = "count";
   private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
   private static final String TOTAL_BYTES = "totalBytes";
   private static final String COUNT_PER_SECOND = "countPerSecond";
   final Gson gson = (new GsonBuilder()).setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

   public String format(JfrStatsResult jfrstatsresult) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("startedEpoch", jfrstatsresult.recordingStarted().toEpochMilli());
      jsonobject.addProperty("endedEpoch", jfrstatsresult.recordingEnded().toEpochMilli());
      jsonobject.addProperty("durationMs", jfrstatsresult.recordingDuration().toMillis());
      Duration duration = jfrstatsresult.worldCreationDuration();
      if (duration != null) {
         jsonobject.addProperty("worldGenDurationMs", duration.toMillis());
      }

      jsonobject.add("heap", this.heap(jfrstatsresult.heapSummary()));
      jsonobject.add("cpuPercent", this.cpu(jfrstatsresult.cpuLoadStats()));
      jsonobject.add("network", this.network(jfrstatsresult));
      jsonobject.add("fileIO", this.fileIO(jfrstatsresult));
      jsonobject.add("serverTick", this.serverTicks(jfrstatsresult.tickTimes()));
      jsonobject.add("threadAllocation", this.threadAllocations(jfrstatsresult.threadAllocationSummary()));
      jsonobject.add("chunkGen", this.chunkGen(jfrstatsresult.chunkGenSummary()));
      return this.gson.toJson((JsonElement)jsonobject);
   }

   private JsonElement heap(GcHeapStat.Summary gcheapstat_summary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("allocationRateBytesPerSecond", gcheapstat_summary.allocationRateBytesPerSecond());
      jsonobject.addProperty("gcCount", gcheapstat_summary.totalGCs());
      jsonobject.addProperty("gcOverHeadPercent", gcheapstat_summary.gcOverHead());
      jsonobject.addProperty("gcTotalDurationMs", gcheapstat_summary.gcTotalDuration().toMillis());
      return jsonobject;
   }

   private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("durationNanosTotal", list.stream().mapToDouble((pair1) -> (double)pair1.getSecond().totalDuration().toNanos()).sum());
      JsonArray jsonarray = Util.make(new JsonArray(), (jsonarray1) -> jsonobject.add("status", jsonarray1));

      for(Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair : list) {
         TimedStatSummary<ChunkGenStat> timedstatsummary = pair.getSecond();
         JsonObject jsonobject1 = Util.make(new JsonObject(), jsonarray::add);
         jsonobject1.addProperty("state", pair.getFirst().toString());
         jsonobject1.addProperty("count", timedstatsummary.count());
         jsonobject1.addProperty("durationNanosTotal", timedstatsummary.totalDuration().toNanos());
         jsonobject1.addProperty("durationNanosAvg", timedstatsummary.totalDuration().toNanos() / (long)timedstatsummary.count());
         JsonObject jsonobject2 = Util.make(new JsonObject(), (jsonobject6) -> jsonobject1.add("durationNanosPercentiles", jsonobject6));
         timedstatsummary.percentilesNanos().forEach((integer, odouble) -> jsonobject2.addProperty("p" + integer, odouble));
         Function<ChunkGenStat, JsonElement> function = (chunkgenstat) -> {
            JsonObject jsonobject3 = new JsonObject();
            jsonobject3.addProperty("durationNanos", chunkgenstat.duration().toNanos());
            jsonobject3.addProperty("level", chunkgenstat.level());
            jsonobject3.addProperty("chunkPosX", chunkgenstat.chunkPos().x);
            jsonobject3.addProperty("chunkPosZ", chunkgenstat.chunkPos().z);
            jsonobject3.addProperty("worldPosX", chunkgenstat.worldPos().x());
            jsonobject3.addProperty("worldPosZ", chunkgenstat.worldPos().z());
            return jsonobject3;
         };
         jsonobject1.add("fastest", function.apply(timedstatsummary.fastest()));
         jsonobject1.add("slowest", function.apply(timedstatsummary.slowest()));
         jsonobject1.add("secondSlowest", (JsonElement)(timedstatsummary.secondSlowest() != null ? function.apply(timedstatsummary.secondSlowest()) : JsonNull.INSTANCE));
      }

      return jsonobject;
   }

   private JsonElement threadAllocations(ThreadAllocationStat.Summary threadallocationstat_summary) {
      JsonArray jsonarray = new JsonArray();
      threadallocationstat_summary.allocationsPerSecondByThread().forEach((s, odouble) -> jsonarray.add(Util.make(new JsonObject(), (jsonobject) -> {
            jsonobject.addProperty("thread", s);
            jsonobject.addProperty("bytesPerSecond", odouble);
         })));
      return jsonarray;
   }

   private JsonElement serverTicks(List<TickTimeStat> list) {
      if (list.isEmpty()) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         double[] adouble = list.stream().mapToDouble((ticktimestat) -> (double)ticktimestat.currentAverage().toNanos() / 1000000.0D).toArray();
         DoubleSummaryStatistics doublesummarystatistics = DoubleStream.of(adouble).summaryStatistics();
         jsonobject.addProperty("minMs", doublesummarystatistics.getMin());
         jsonobject.addProperty("averageMs", doublesummarystatistics.getAverage());
         jsonobject.addProperty("maxMs", doublesummarystatistics.getMax());
         Map<Integer, Double> map = Percentiles.evaluate(adouble);
         map.forEach((integer, odouble) -> jsonobject.addProperty("p" + integer, odouble));
         return jsonobject;
      }
   }

   private JsonElement fileIO(JfrStatsResult jfrstatsresult) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("write", this.fileIoSummary(jfrstatsresult.fileWrites()));
      jsonobject.add("read", this.fileIoSummary(jfrstatsresult.fileReads()));
      return jsonobject;
   }

   private JsonElement fileIoSummary(FileIOStat.Summary fileiostat_summary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("totalBytes", fileiostat_summary.totalBytes());
      jsonobject.addProperty("count", fileiostat_summary.counts());
      jsonobject.addProperty("bytesPerSecond", fileiostat_summary.bytesPerSecond());
      jsonobject.addProperty("countPerSecond", fileiostat_summary.countsPerSecond());
      JsonArray jsonarray = new JsonArray();
      jsonobject.add("topContributors", jsonarray);
      fileiostat_summary.topTenContributorsByTotalBytes().forEach((pair) -> {
         JsonObject jsonobject1 = new JsonObject();
         jsonarray.add(jsonobject1);
         jsonobject1.addProperty("path", pair.getFirst());
         jsonobject1.addProperty("totalBytes", pair.getSecond());
      });
      return jsonobject;
   }

   private JsonElement network(JfrStatsResult jfrstatsresult) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("sent", this.packets(jfrstatsresult.sentPacketsSummary()));
      jsonobject.add("received", this.packets(jfrstatsresult.receivedPacketsSummary()));
      return jsonobject;
   }

   private JsonElement packets(NetworkPacketSummary networkpacketsummary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("totalBytes", networkpacketsummary.getTotalSize());
      jsonobject.addProperty("count", networkpacketsummary.getTotalCount());
      jsonobject.addProperty("bytesPerSecond", networkpacketsummary.getSizePerSecond());
      jsonobject.addProperty("countPerSecond", networkpacketsummary.getCountsPerSecond());
      JsonArray jsonarray = new JsonArray();
      jsonobject.add("topContributors", jsonarray);
      networkpacketsummary.largestSizeContributors().forEach((pair) -> {
         JsonObject jsonobject1 = new JsonObject();
         jsonarray.add(jsonobject1);
         NetworkPacketSummary.PacketIdentification networkpacketsummary_packetidentification = pair.getFirst();
         NetworkPacketSummary.PacketCountAndSize networkpacketsummary_packetcountandsize = pair.getSecond();
         jsonobject1.addProperty("protocolId", networkpacketsummary_packetidentification.protocolId());
         jsonobject1.addProperty("packetId", networkpacketsummary_packetidentification.packetId());
         jsonobject1.addProperty("packetName", networkpacketsummary_packetidentification.packetName());
         jsonobject1.addProperty("totalBytes", networkpacketsummary_packetcountandsize.totalSize());
         jsonobject1.addProperty("count", networkpacketsummary_packetcountandsize.totalCount());
      });
      return jsonobject;
   }

   private JsonElement cpu(List<CpuLoadStat> list) {
      JsonObject jsonobject = new JsonObject();
      BiFunction<List<CpuLoadStat>, ToDoubleFunction<CpuLoadStat>, JsonObject> bifunction = (list1, todoublefunction) -> {
         JsonObject jsonobject1 = new JsonObject();
         DoubleSummaryStatistics doublesummarystatistics = list1.stream().mapToDouble(todoublefunction).summaryStatistics();
         jsonobject1.addProperty("min", doublesummarystatistics.getMin());
         jsonobject1.addProperty("average", doublesummarystatistics.getAverage());
         jsonobject1.addProperty("max", doublesummarystatistics.getMax());
         return jsonobject1;
      };
      jsonobject.add("jvm", bifunction.apply(list, CpuLoadStat::jvm));
      jsonobject.add("userJvm", bifunction.apply(list, CpuLoadStat::userJvm));
      jsonobject.add("system", bifunction.apply(list, CpuLoadStat::system));
      return jsonobject;
   }
}
