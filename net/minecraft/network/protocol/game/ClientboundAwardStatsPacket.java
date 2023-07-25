package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener> {
   private final Object2IntMap<Stat<?>> stats;

   public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> object2intmap) {
      this.stats = object2intmap;
   }

   public ClientboundAwardStatsPacket(FriendlyByteBuf friendlybytebuf) {
      this.stats = friendlybytebuf.readMap(Object2IntOpenHashMap::new, (friendlybytebuf2) -> {
         StatType<?> stattype = friendlybytebuf2.readById(BuiltInRegistries.STAT_TYPE);
         return readStatCap(friendlybytebuf, stattype);
      }, FriendlyByteBuf::readVarInt);
   }

   private static <T> Stat<T> readStatCap(FriendlyByteBuf friendlybytebuf, StatType<T> stattype) {
      return stattype.get(friendlybytebuf.readById(stattype.getRegistry()));
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAwardStats(this);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeMap(this.stats, ClientboundAwardStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
   }

   private static <T> void writeStatCap(FriendlyByteBuf friendlybytebuf1, Stat<T> stat) {
      friendlybytebuf1.writeId(BuiltInRegistries.STAT_TYPE, stat.getType());
      friendlybytebuf1.writeId(stat.getType().getRegistry(), stat.getValue());
   }

   public Map<Stat<?>, Integer> getStats() {
      return this.stats;
   }
}
