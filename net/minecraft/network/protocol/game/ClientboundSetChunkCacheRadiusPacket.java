package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetChunkCacheRadiusPacket implements Packet<ClientGamePacketListener> {
   private final int radius;

   public ClientboundSetChunkCacheRadiusPacket(int i) {
      this.radius = i;
   }

   public ClientboundSetChunkCacheRadiusPacket(FriendlyByteBuf friendlybytebuf) {
      this.radius = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.radius);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetChunkCacheRadius(this);
   }

   public int getRadius() {
      return this.radius;
   }
}
