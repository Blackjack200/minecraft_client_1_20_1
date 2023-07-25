package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener> {
   private final int containerId;

   public ServerboundContainerClosePacket(int i) {
      this.containerId = i;
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleContainerClose(this);
   }

   public ServerboundContainerClosePacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
   }

   public int getContainerId() {
      return this.containerId;
   }
}
