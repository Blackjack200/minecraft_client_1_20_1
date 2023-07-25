package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener> {
   private final int slot;

   public ServerboundSetCarriedItemPacket(int i) {
      this.slot = i;
   }

   public ServerboundSetCarriedItemPacket(FriendlyByteBuf friendlybytebuf) {
      this.slot = friendlybytebuf.readShort();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeShort(this.slot);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSetCarriedItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}
