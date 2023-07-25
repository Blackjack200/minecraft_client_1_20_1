package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPongPacket implements Packet<ServerGamePacketListener> {
   private final int id;

   public ServerboundPongPacket(int i) {
      this.id = i;
   }

   public ServerboundPongPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.id);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePong(this);
   }

   public int getId() {
      return this.id;
   }
}
