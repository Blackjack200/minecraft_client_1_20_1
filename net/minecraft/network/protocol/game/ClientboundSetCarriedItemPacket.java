package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
   private final int slot;

   public ClientboundSetCarriedItemPacket(int i) {
      this.slot = i;
   }

   public ClientboundSetCarriedItemPacket(FriendlyByteBuf friendlybytebuf) {
      this.slot = friendlybytebuf.readByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.slot);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetCarriedItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}
