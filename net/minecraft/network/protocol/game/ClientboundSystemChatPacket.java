package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, boolean overlay) implements Packet<ClientGamePacketListener> {
   public ClientboundSystemChatPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readComponent(), friendlybytebuf.readBoolean());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.content);
      friendlybytebuf.writeBoolean(this.overlay);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSystemChat(this);
   }

   public boolean isSkippable() {
      return true;
   }
}
