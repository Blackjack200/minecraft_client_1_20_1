package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTimePacket implements Packet<ClientGamePacketListener> {
   private final long gameTime;
   private final long dayTime;

   public ClientboundSetTimePacket(long i, long j, boolean flag) {
      this.gameTime = i;
      long k = j;
      if (!flag) {
         k = -j;
         if (k == 0L) {
            k = -1L;
         }
      }

      this.dayTime = k;
   }

   public ClientboundSetTimePacket(FriendlyByteBuf friendlybytebuf) {
      this.gameTime = friendlybytebuf.readLong();
      this.dayTime = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeLong(this.gameTime);
      friendlybytebuf.writeLong(this.dayTime);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetTime(this);
   }

   public long getGameTime() {
      return this.gameTime;
   }

   public long getDayTime() {
      return this.dayTime;
   }
}
