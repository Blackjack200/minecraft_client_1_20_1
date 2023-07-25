package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerSetDataPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final int id;
   private final int value;

   public ClientboundContainerSetDataPacket(int i, int j, int k) {
      this.containerId = i;
      this.id = j;
      this.value = k;
   }

   public ClientboundContainerSetDataPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readUnsignedByte();
      this.id = friendlybytebuf.readShort();
      this.value = friendlybytebuf.readShort();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeShort(this.id);
      friendlybytebuf.writeShort(this.value);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleContainerSetData(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getId() {
      return this.id;
   }

   public int getValue() {
      return this.value;
   }
}
