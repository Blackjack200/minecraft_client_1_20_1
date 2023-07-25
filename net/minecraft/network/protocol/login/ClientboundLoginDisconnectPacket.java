package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
   private final Component reason;

   public ClientboundLoginDisconnectPacket(Component component) {
      this.reason = component;
   }

   public ClientboundLoginDisconnectPacket(FriendlyByteBuf friendlybytebuf) {
      this.reason = Component.Serializer.fromJsonLenient(friendlybytebuf.readUtf(262144));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.reason);
   }

   public void handle(ClientLoginPacketListener clientloginpacketlistener) {
      clientloginpacketlistener.handleDisconnect(this);
   }

   public Component getReason() {
      return this.reason;
   }
}
