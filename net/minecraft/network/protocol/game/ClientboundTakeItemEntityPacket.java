package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundTakeItemEntityPacket implements Packet<ClientGamePacketListener> {
   private final int itemId;
   private final int playerId;
   private final int amount;

   public ClientboundTakeItemEntityPacket(int i, int j, int k) {
      this.itemId = i;
      this.playerId = j;
      this.amount = k;
   }

   public ClientboundTakeItemEntityPacket(FriendlyByteBuf friendlybytebuf) {
      this.itemId = friendlybytebuf.readVarInt();
      this.playerId = friendlybytebuf.readVarInt();
      this.amount = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.itemId);
      friendlybytebuf.writeVarInt(this.playerId);
      friendlybytebuf.writeVarInt(this.amount);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleTakeItemEntity(this);
   }

   public int getItemId() {
      return this.itemId;
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public int getAmount() {
      return this.amount;
   }
}
