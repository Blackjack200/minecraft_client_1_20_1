package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
   private final long id;

   public ServerboundKeepAlivePacket(long i) {
      this.id = i;
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleKeepAlive(this);
   }

   public ServerboundKeepAlivePacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeLong(this.id);
   }

   public long getId() {
      return this.id;
   }
}
