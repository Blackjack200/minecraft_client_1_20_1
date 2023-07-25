package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundBlockEntityTagQuery implements Packet<ServerGamePacketListener> {
   private final int transactionId;
   private final BlockPos pos;

   public ServerboundBlockEntityTagQuery(int i, BlockPos blockpos) {
      this.transactionId = i;
      this.pos = blockpos;
   }

   public ServerboundBlockEntityTagQuery(FriendlyByteBuf friendlybytebuf) {
      this.transactionId = friendlybytebuf.readVarInt();
      this.pos = friendlybytebuf.readBlockPos();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.transactionId);
      friendlybytebuf.writeBlockPos(this.pos);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleBlockEntityTagQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
