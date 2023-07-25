package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginCompressionPacket implements Packet<ClientLoginPacketListener> {
   private final int compressionThreshold;

   public ClientboundLoginCompressionPacket(int i) {
      this.compressionThreshold = i;
   }

   public ClientboundLoginCompressionPacket(FriendlyByteBuf friendlybytebuf) {
      this.compressionThreshold = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.compressionThreshold);
   }

   public void handle(ClientLoginPacketListener clientloginpacketlistener) {
      clientloginpacketlistener.handleCompression(this);
   }

   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }
}
