package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final BlockPos pos;
   private final int progress;

   public ClientboundBlockDestructionPacket(int i, BlockPos blockpos, int j) {
      this.id = i;
      this.pos = blockpos;
      this.progress = j;
   }

   public ClientboundBlockDestructionPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      this.pos = friendlybytebuf.readBlockPos();
      this.progress = friendlybytebuf.readUnsignedByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeByte(this.progress);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleBlockDestruction(this);
   }

   public int getId() {
      return this.id;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getProgress() {
      return this.progress;
   }
}
