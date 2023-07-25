package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundSwingPacket implements Packet<ServerGamePacketListener> {
   private final InteractionHand hand;

   public ServerboundSwingPacket(InteractionHand interactionhand) {
      this.hand = interactionhand;
   }

   public ServerboundSwingPacket(FriendlyByteBuf friendlybytebuf) {
      this.hand = friendlybytebuf.readEnum(InteractionHand.class);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.hand);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleAnimate(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }
}
