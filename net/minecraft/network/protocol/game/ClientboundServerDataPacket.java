package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
   private final Component motd;
   private final Optional<byte[]> iconBytes;
   private final boolean enforcesSecureChat;

   public ClientboundServerDataPacket(Component component, Optional<byte[]> optional, boolean flag) {
      this.motd = component;
      this.iconBytes = optional;
      this.enforcesSecureChat = flag;
   }

   public ClientboundServerDataPacket(FriendlyByteBuf friendlybytebuf) {
      this.motd = friendlybytebuf.readComponent();
      this.iconBytes = friendlybytebuf.readOptional(FriendlyByteBuf::readByteArray);
      this.enforcesSecureChat = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.motd);
      friendlybytebuf.writeOptional(this.iconBytes, FriendlyByteBuf::writeByteArray);
      friendlybytebuf.writeBoolean(this.enforcesSecureChat);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleServerData(this);
   }

   public Component getMotd() {
      return this.motd;
   }

   public Optional<byte[]> getIconBytes() {
      return this.iconBytes;
   }

   public boolean enforcesSecureChat() {
      return this.enforcesSecureChat;
   }
}
