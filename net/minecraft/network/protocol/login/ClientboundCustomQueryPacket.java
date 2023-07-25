package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomQueryPacket implements Packet<ClientLoginPacketListener> {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   private final int transactionId;
   private final ResourceLocation identifier;
   private final FriendlyByteBuf data;

   public ClientboundCustomQueryPacket(int i, ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
      this.transactionId = i;
      this.identifier = resourcelocation;
      this.data = friendlybytebuf;
   }

   public ClientboundCustomQueryPacket(FriendlyByteBuf friendlybytebuf) {
      this.transactionId = friendlybytebuf.readVarInt();
      this.identifier = friendlybytebuf.readResourceLocation();
      int i = friendlybytebuf.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.data = new FriendlyByteBuf(friendlybytebuf.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.transactionId);
      friendlybytebuf.writeResourceLocation(this.identifier);
      friendlybytebuf.writeBytes(this.data.copy());
   }

   public void handle(ClientLoginPacketListener clientloginpacketlistener) {
      clientloginpacketlistener.handleCustomQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public ResourceLocation getIdentifier() {
      return this.identifier;
   }

   public FriendlyByteBuf getData() {
      return this.data;
   }
}
