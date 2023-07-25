package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTitleTextPacket implements Packet<ClientGamePacketListener> {
   private final Component text;

   public ClientboundSetTitleTextPacket(Component component) {
      this.text = component;
   }

   public ClientboundSetTitleTextPacket(FriendlyByteBuf friendlybytebuf) {
      this.text = friendlybytebuf.readComponent();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.text);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.setTitleText(this);
   }

   public Component getText() {
      return this.text;
   }
}
