package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener> {
   private final int containerId;

   public ClientboundContainerClosePacket(int i) {
      this.containerId = i;
   }

   public ClientboundContainerClosePacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readUnsignedByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleContainerClose(this);
   }

   public int getContainerId() {
      return this.containerId;
   }
}
