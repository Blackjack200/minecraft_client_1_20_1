package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
   private final int duration;

   public ClientboundPlayerCombatEndPacket(CombatTracker combattracker) {
      this(combattracker.getCombatDuration());
   }

   public ClientboundPlayerCombatEndPacket(int i) {
      this.duration = i;
   }

   public ClientboundPlayerCombatEndPacket(FriendlyByteBuf friendlybytebuf) {
      this.duration = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.duration);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePlayerCombatEnd(this);
   }
}
