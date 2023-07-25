package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundKeepAlivePacket implements Packet<ClientGamePacketListener> {
   private final long id;

   public ClientboundKeepAlivePacket(long i) {
      this.id = i;
   }

   public ClientboundKeepAlivePacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeLong(this.id);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleKeepAlive(this);
   }

   public long getId() {
      return this.id;
   }
}
