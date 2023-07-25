package net.minecraft.util.profiling.jfr.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public final class NetworkPacketSummary {
   private final NetworkPacketSummary.PacketCountAndSize totalPacketCountAndSize;
   private final List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> largestSizeContributors;
   private final Duration recordingDuration;

   public NetworkPacketSummary(Duration duration, List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> list) {
      this.recordingDuration = duration;
      this.totalPacketCountAndSize = list.stream().map(Pair::getSecond).reduce(NetworkPacketSummary.PacketCountAndSize::add).orElseGet(() -> new NetworkPacketSummary.PacketCountAndSize(0L, 0L));
      this.largestSizeContributors = list.stream().sorted(Comparator.comparing(Pair::getSecond, NetworkPacketSummary.PacketCountAndSize.SIZE_THEN_COUNT)).limit(10L).toList();
   }

   public double getCountsPerSecond() {
      return (double)this.totalPacketCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
   }

   public double getSizePerSecond() {
      return (double)this.totalPacketCountAndSize.totalSize / (double)this.recordingDuration.getSeconds();
   }

   public long getTotalCount() {
      return this.totalPacketCountAndSize.totalCount;
   }

   public long getTotalSize() {
      return this.totalPacketCountAndSize.totalSize;
   }

   public List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> largestSizeContributors() {
      return this.largestSizeContributors;
   }

   public static record PacketCountAndSize(long totalCount, long totalSize) {
      final long totalCount;
      final long totalSize;
      static final Comparator<NetworkPacketSummary.PacketCountAndSize> SIZE_THEN_COUNT = Comparator.comparing(NetworkPacketSummary.PacketCountAndSize::totalSize).thenComparing(NetworkPacketSummary.PacketCountAndSize::totalCount).reversed();

      NetworkPacketSummary.PacketCountAndSize add(NetworkPacketSummary.PacketCountAndSize networkpacketsummary_packetcountandsize) {
         return new NetworkPacketSummary.PacketCountAndSize(this.totalCount + networkpacketsummary_packetcountandsize.totalCount, this.totalSize + networkpacketsummary_packetcountandsize.totalSize);
      }
   }

   public static record PacketIdentification(PacketFlow direction, int protocolId, int packetId) {
      private static final Map<NetworkPacketSummary.PacketIdentification, String> PACKET_NAME_BY_ID;

      public String packetName() {
         return PACKET_NAME_BY_ID.getOrDefault(this, "unknown");
      }

      public static NetworkPacketSummary.PacketIdentification from(RecordedEvent recordedevent) {
         return new NetworkPacketSummary.PacketIdentification(recordedevent.getEventType().getName().equals("minecraft.PacketSent") ? PacketFlow.CLIENTBOUND : PacketFlow.SERVERBOUND, recordedevent.getInt("protocolId"), recordedevent.getInt("packetId"));
      }

      static {
         ImmutableMap.Builder<NetworkPacketSummary.PacketIdentification, String> immutablemap_builder = ImmutableMap.builder();

         for(ConnectionProtocol connectionprotocol : ConnectionProtocol.values()) {
            for(PacketFlow packetflow : PacketFlow.values()) {
               Int2ObjectMap<Class<? extends Packet<?>>> int2objectmap = connectionprotocol.getPacketsByIds(packetflow);
               int2objectmap.forEach((integer, oclass) -> immutablemap_builder.put(new NetworkPacketSummary.PacketIdentification(packetflow, connectionprotocol.getId(), integer), oclass.getSimpleName()));
            }
         }

         PACKET_NAME_BY_ID = immutablemap_builder.build();
      }
   }
}
