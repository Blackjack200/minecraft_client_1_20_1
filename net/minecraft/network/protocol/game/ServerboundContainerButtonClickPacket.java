package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
   private final int containerId;
   private final int buttonId;

   public ServerboundContainerButtonClickPacket(int i, int j) {
      this.containerId = i;
      this.buttonId = j;
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleContainerButtonClick(this);
   }

   public ServerboundContainerButtonClickPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readByte();
      this.buttonId = friendlybytebuf.readByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeByte(this.buttonId);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getButtonId() {
      return this.buttonId;
   }
}
