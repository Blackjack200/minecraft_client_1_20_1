package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action action, List<String> entries) implements Packet<ClientGamePacketListener> {
   public ClientboundCustomChatCompletionsPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readEnum(ClientboundCustomChatCompletionsPacket.Action.class), friendlybytebuf.readList(FriendlyByteBuf::readUtf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.action);
      friendlybytebuf.writeCollection(this.entries, FriendlyByteBuf::writeUtf);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleCustomChatCompletions(this);
   }

   public static enum Action {
      ADD,
      REMOVE,
      SET;
   }
}
