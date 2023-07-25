package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetActionBarTextPacket implements Packet<ClientGamePacketListener> {
   private final Component text;

   public ClientboundSetActionBarTextPacket(Component component) {
      this.text = component;
   }

   public ClientboundSetActionBarTextPacket(FriendlyByteBuf friendlybytebuf) {
      this.text = friendlybytebuf.readComponent();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.text);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.setActionBarText(this);
   }

   public Component getText() {
      return this.text;
   }
}
