package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   private final int b0;
   private final int b1;
   private final Block block;

   public ClientboundBlockEventPacket(BlockPos blockpos, Block block, int i, int j) {
      this.pos = blockpos;
      this.block = block;
      this.b0 = i;
      this.b1 = j;
   }

   public ClientboundBlockEventPacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.b0 = friendlybytebuf.readUnsignedByte();
      this.b1 = friendlybytebuf.readUnsignedByte();
      this.block = friendlybytebuf.readById(BuiltInRegistries.BLOCK);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeByte(this.b0);
      friendlybytebuf.writeByte(this.b1);
      friendlybytebuf.writeId(BuiltInRegistries.BLOCK, this.block);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleBlockEvent(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getB0() {
      return this.b0;
   }

   public int getB1() {
      return this.b1;
   }

   public Block getBlock() {
      return this.block;
   }
}
