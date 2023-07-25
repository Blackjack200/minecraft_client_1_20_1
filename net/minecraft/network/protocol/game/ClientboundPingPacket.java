package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPingPacket implements Packet<ClientGamePacketListener> {
   private final int id;

   public ClientboundPingPacket(int i) {
      this.id = i;
   }

   public ClientboundPingPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.id);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePing(this);
   }

   public int getId() {
      return this.id;
   }
}
