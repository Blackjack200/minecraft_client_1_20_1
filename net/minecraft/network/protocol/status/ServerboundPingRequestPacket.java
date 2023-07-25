package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPingRequestPacket implements Packet<ServerStatusPacketListener> {
   private final long time;

   public ServerboundPingRequestPacket(long i) {
      this.time = i;
   }

   public ServerboundPingRequestPacket(FriendlyByteBuf friendlybytebuf) {
      this.time = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeLong(this.time);
   }

   public void handle(ServerStatusPacketListener serverstatuspacketlistener) {
      serverstatuspacketlistener.handlePingRequest(this);
   }

   public long getTime() {
      return this.time;
   }
}
