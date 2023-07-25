package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderSizePacket implements Packet<ClientGamePacketListener> {
   private final double size;

   public ClientboundSetBorderSizePacket(WorldBorder worldborder) {
      this.size = worldborder.getLerpTarget();
   }

   public ClientboundSetBorderSizePacket(FriendlyByteBuf friendlybytebuf) {
      this.size = friendlybytebuf.readDouble();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.size);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetBorderSize(this);
   }

   public double getSize() {
      return this.size;
   }
}
