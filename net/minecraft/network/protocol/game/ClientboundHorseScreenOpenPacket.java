package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final int size;
   private final int entityId;

   public ClientboundHorseScreenOpenPacket(int i, int j, int k) {
      this.containerId = i;
      this.size = j;
      this.entityId = k;
   }

   public ClientboundHorseScreenOpenPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readUnsignedByte();
      this.size = friendlybytebuf.readVarInt();
      this.entityId = friendlybytebuf.readInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeVarInt(this.size);
      friendlybytebuf.writeInt(this.entityId);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleHorseScreenOpen(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSize() {
      return this.size;
   }

   public int getEntityId() {
      return this.entityId;
   }
}
