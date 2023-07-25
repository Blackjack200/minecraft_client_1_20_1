package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
   private final InteractionHand hand;

   public ClientboundOpenBookPacket(InteractionHand interactionhand) {
      this.hand = interactionhand;
   }

   public ClientboundOpenBookPacket(FriendlyByteBuf friendlybytebuf) {
      this.hand = friendlybytebuf.readEnum(InteractionHand.class);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.hand);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleOpenBook(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }
}
