package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelEventPacket implements Packet<ClientGamePacketListener> {
   private final int type;
   private final BlockPos pos;
   private final int data;
   private final boolean globalEvent;

   public ClientboundLevelEventPacket(int i, BlockPos blockpos, int j, boolean flag) {
      this.type = i;
      this.pos = blockpos.immutable();
      this.data = j;
      this.globalEvent = flag;
   }

   public ClientboundLevelEventPacket(FriendlyByteBuf friendlybytebuf) {
      this.type = friendlybytebuf.readInt();
      this.pos = friendlybytebuf.readBlockPos();
      this.data = friendlybytebuf.readInt();
      this.globalEvent = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.type);
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeInt(this.data);
      friendlybytebuf.writeBoolean(this.globalEvent);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleLevelEvent(this);
   }

   public boolean isGlobalEvent() {
      return this.globalEvent;
   }

   public int getType() {
      return this.type;
   }

   public int getData() {
      return this.data;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
