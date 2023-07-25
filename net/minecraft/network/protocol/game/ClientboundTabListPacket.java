package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundTabListPacket implements Packet<ClientGamePacketListener> {
   private final Component header;
   private final Component footer;

   public ClientboundTabListPacket(Component component, Component component1) {
      this.header = component;
      this.footer = component1;
   }

   public ClientboundTabListPacket(FriendlyByteBuf friendlybytebuf) {
      this.header = friendlybytebuf.readComponent();
      this.footer = friendlybytebuf.readComponent();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.header);
      friendlybytebuf.writeComponent(this.footer);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleTabListCustomisation(this);
   }

   public Component getHeader() {
      return this.header;
   }

   public Component getFooter() {
      return this.footer;
   }
}
