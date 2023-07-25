package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
   private final boolean left;
   private final boolean right;

   public ServerboundPaddleBoatPacket(boolean flag, boolean flag1) {
      this.left = flag;
      this.right = flag1;
   }

   public ServerboundPaddleBoatPacket(FriendlyByteBuf friendlybytebuf) {
      this.left = friendlybytebuf.readBoolean();
      this.right = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBoolean(this.left);
      friendlybytebuf.writeBoolean(this.right);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePaddleBoat(this);
   }

   public boolean getLeft() {
      return this.left;
   }

   public boolean getRight() {
      return this.right;
   }
}
