package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSignUpdatePacket implements Packet<ServerGamePacketListener> {
   private static final int MAX_STRING_LENGTH = 384;
   private final BlockPos pos;
   private final String[] lines;
   private final boolean isFrontText;

   public ServerboundSignUpdatePacket(BlockPos blockpos, boolean flag, String s, String s1, String s2, String s3) {
      this.pos = blockpos;
      this.isFrontText = flag;
      this.lines = new String[]{s, s1, s2, s3};
   }

   public ServerboundSignUpdatePacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.isFrontText = friendlybytebuf.readBoolean();
      this.lines = new String[4];

      for(int i = 0; i < 4; ++i) {
         this.lines[i] = friendlybytebuf.readUtf(384);
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeBoolean(this.isFrontText);

      for(int i = 0; i < 4; ++i) {
         friendlybytebuf.writeUtf(this.lines[i]);
      }

   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSignUpdate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public boolean isFrontText() {
      return this.isFrontText;
   }

   public String[] getLines() {
      return this.lines;
   }
}
