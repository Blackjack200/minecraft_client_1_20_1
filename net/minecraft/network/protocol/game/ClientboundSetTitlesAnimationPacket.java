package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTitlesAnimationPacket implements Packet<ClientGamePacketListener> {
   private final int fadeIn;
   private final int stay;
   private final int fadeOut;

   public ClientboundSetTitlesAnimationPacket(int i, int j, int k) {
      this.fadeIn = i;
      this.stay = j;
      this.fadeOut = k;
   }

   public ClientboundSetTitlesAnimationPacket(FriendlyByteBuf friendlybytebuf) {
      this.fadeIn = friendlybytebuf.readInt();
      this.stay = friendlybytebuf.readInt();
      this.fadeOut = friendlybytebuf.readInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.fadeIn);
      friendlybytebuf.writeInt(this.stay);
      friendlybytebuf.writeInt(this.fadeOut);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.setTitlesAnimation(this);
   }

   public int getFadeIn() {
      return this.fadeIn;
   }

   public int getStay() {
      return this.stay;
   }

   public int getFadeOut() {
      return this.fadeOut;
   }
}
