package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
   private final InteractionHand hand;
   private final int sequence;

   public ServerboundUseItemPacket(InteractionHand interactionhand, int i) {
      this.hand = interactionhand;
      this.sequence = i;
   }

   public ServerboundUseItemPacket(FriendlyByteBuf friendlybytebuf) {
      this.hand = friendlybytebuf.readEnum(InteractionHand.class);
      this.sequence = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.hand);
      friendlybytebuf.writeVarInt(this.sequence);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleUseItem(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public int getSequence() {
      return this.sequence;
   }
}
