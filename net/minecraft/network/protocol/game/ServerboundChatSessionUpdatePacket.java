package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener> {
   public ServerboundChatSessionUpdatePacket(FriendlyByteBuf friendlybytebuf) {
      this(RemoteChatSession.Data.read(friendlybytebuf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      RemoteChatSession.Data.write(friendlybytebuf, this.chatSession);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleChatSessionUpdate(this);
   }
}
