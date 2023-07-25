package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener> {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   private final int transactionId;
   @Nullable
   private final FriendlyByteBuf data;

   public ServerboundCustomQueryPacket(int i, @Nullable FriendlyByteBuf friendlybytebuf) {
      this.transactionId = i;
      this.data = friendlybytebuf;
   }

   public ServerboundCustomQueryPacket(FriendlyByteBuf friendlybytebuf) {
      this.transactionId = friendlybytebuf.readVarInt();
      this.data = friendlybytebuf.readNullable((friendlybytebuf1) -> {
         int i = friendlybytebuf1.readableBytes();
         if (i >= 0 && i <= 1048576) {
            return new FriendlyByteBuf(friendlybytebuf1.readBytes(i));
         } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
         }
      });
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.transactionId);
      friendlybytebuf.writeNullable(this.data, (friendlybytebuf1, friendlybytebuf2) -> friendlybytebuf1.writeBytes(friendlybytebuf2.slice()));
   }

   public void handle(ServerLoginPacketListener serverloginpacketlistener) {
      serverloginpacketlistener.handleCustomQueryPacket(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   @Nullable
   public FriendlyByteBuf getData() {
      return this.data;
   }
}
