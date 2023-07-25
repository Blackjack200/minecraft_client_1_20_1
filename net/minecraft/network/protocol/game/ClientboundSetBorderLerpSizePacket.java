package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderLerpSizePacket implements Packet<ClientGamePacketListener> {
   private final double oldSize;
   private final double newSize;
   private final long lerpTime;

   public ClientboundSetBorderLerpSizePacket(WorldBorder worldborder) {
      this.oldSize = worldborder.getSize();
      this.newSize = worldborder.getLerpTarget();
      this.lerpTime = worldborder.getLerpRemainingTime();
   }

   public ClientboundSetBorderLerpSizePacket(FriendlyByteBuf friendlybytebuf) {
      this.oldSize = friendlybytebuf.readDouble();
      this.newSize = friendlybytebuf.readDouble();
      this.lerpTime = friendlybytebuf.readVarLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.oldSize);
      friendlybytebuf.writeDouble(this.newSize);
      friendlybytebuf.writeVarLong(this.lerpTime);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetBorderLerpSize(this);
   }

   public double getOldSize() {
      return this.oldSize;
   }

   public double getNewSize() {
      return this.newSize;
   }

   public long getLerpTime() {
      return this.lerpTime;
   }
}
