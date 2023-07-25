package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundDisguisedChatPacket(Component message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
   public ClientboundDisguisedChatPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readComponent(), new ChatType.BoundNetwork(friendlybytebuf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.message);
      this.chatType.write(friendlybytebuf);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleDisguisedChat(this);
   }

   public boolean isSkippable() {
      return true;
   }
}
