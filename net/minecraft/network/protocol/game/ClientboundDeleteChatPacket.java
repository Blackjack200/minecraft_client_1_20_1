package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener> {
   public ClientboundDeleteChatPacket(FriendlyByteBuf friendlybytebuf) {
      this(MessageSignature.Packed.read(friendlybytebuf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      MessageSignature.Packed.write(friendlybytebuf, this.messageSignature);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleDeleteChat(this);
   }
}
