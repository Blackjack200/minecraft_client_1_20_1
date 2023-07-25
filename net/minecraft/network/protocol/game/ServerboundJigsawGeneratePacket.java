package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
   private final BlockPos pos;
   private final int levels;
   private final boolean keepJigsaws;

   public ServerboundJigsawGeneratePacket(BlockPos blockpos, int i, boolean flag) {
      this.pos = blockpos;
      this.levels = i;
      this.keepJigsaws = flag;
   }

   public ServerboundJigsawGeneratePacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.levels = friendlybytebuf.readVarInt();
      this.keepJigsaws = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeVarInt(this.levels);
      friendlybytebuf.writeBoolean(this.keepJigsaws);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleJigsawGenerate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int levels() {
      return this.levels;
   }

   public boolean keepJigsaws() {
      return this.keepJigsaws;
   }
}
