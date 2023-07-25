package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
   private final long time;

   public ClientboundPongResponsePacket(long i) {
      this.time = i;
   }

   public ClientboundPongResponsePacket(FriendlyByteBuf friendlybytebuf) {
      this.time = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeLong(this.time);
   }

   public void handle(ClientStatusPacketListener clientstatuspacketlistener) {
      clientstatuspacketlistener.handlePongResponse(this);
   }

   public long getTime() {
      return this.time;
   }
}
