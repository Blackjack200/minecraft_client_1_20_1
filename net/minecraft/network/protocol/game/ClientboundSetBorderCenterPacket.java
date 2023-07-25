package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderCenterPacket implements Packet<ClientGamePacketListener> {
   private final double newCenterX;
   private final double newCenterZ;

   public ClientboundSetBorderCenterPacket(WorldBorder worldborder) {
      this.newCenterX = worldborder.getCenterX();
      this.newCenterZ = worldborder.getCenterZ();
   }

   public ClientboundSetBorderCenterPacket(FriendlyByteBuf friendlybytebuf) {
      this.newCenterX = friendlybytebuf.readDouble();
      this.newCenterZ = friendlybytebuf.readDouble();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.newCenterX);
      friendlybytebuf.writeDouble(this.newCenterZ);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetBorderCenter(this);
   }

   public double getNewCenterZ() {
      return this.newCenterZ;
   }

   public double getNewCenterX() {
      return this.newCenterX;
   }
}
