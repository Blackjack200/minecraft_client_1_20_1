package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class ServerboundSetCommandBlockPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_TRACK_OUTPUT = 1;
   private static final int FLAG_CONDITIONAL = 2;
   private static final int FLAG_AUTOMATIC = 4;
   private final BlockPos pos;
   private final String command;
   private final boolean trackOutput;
   private final boolean conditional;
   private final boolean automatic;
   private final CommandBlockEntity.Mode mode;

   public ServerboundSetCommandBlockPacket(BlockPos blockpos, String s, CommandBlockEntity.Mode commandblockentity_mode, boolean flag, boolean flag1, boolean flag2) {
      this.pos = blockpos;
      this.command = s;
      this.trackOutput = flag;
      this.conditional = flag1;
      this.automatic = flag2;
      this.mode = commandblockentity_mode;
   }

   public ServerboundSetCommandBlockPacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.command = friendlybytebuf.readUtf();
      this.mode = friendlybytebuf.readEnum(CommandBlockEntity.Mode.class);
      int i = friendlybytebuf.readByte();
      this.trackOutput = (i & 1) != 0;
      this.conditional = (i & 2) != 0;
      this.automatic = (i & 4) != 0;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeUtf(this.command);
      friendlybytebuf.writeEnum(this.mode);
      int i = 0;
      if (this.trackOutput) {
         i |= 1;
      }

      if (this.conditional) {
         i |= 2;
      }

      if (this.automatic) {
         i |= 4;
      }

      friendlybytebuf.writeByte(i);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSetCommandBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public boolean isConditional() {
      return this.conditional;
   }

   public boolean isAutomatic() {
      return this.automatic;
   }

   public CommandBlockEntity.Mode getMode() {
      return this.mode;
   }
}
