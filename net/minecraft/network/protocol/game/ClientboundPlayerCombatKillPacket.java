package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
   private final int playerId;
   private final Component message;

   public ClientboundPlayerCombatKillPacket(int i, Component component) {
      this.playerId = i;
      this.message = component;
   }

   public ClientboundPlayerCombatKillPacket(FriendlyByteBuf friendlybytebuf) {
      this.playerId = friendlybytebuf.readVarInt();
      this.message = friendlybytebuf.readComponent();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.playerId);
      friendlybytebuf.writeComponent(this.message);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePlayerCombatKill(this);
   }

   public boolean isSkippable() {
      return true;
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public Component getMessage() {
      return this.message;
   }
}
