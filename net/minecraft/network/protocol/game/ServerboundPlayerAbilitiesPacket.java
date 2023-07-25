package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ServerboundPlayerAbilitiesPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_FLYING = 2;
   private final boolean isFlying;

   public ServerboundPlayerAbilitiesPacket(Abilities abilities) {
      this.isFlying = abilities.flying;
   }

   public ServerboundPlayerAbilitiesPacket(FriendlyByteBuf friendlybytebuf) {
      byte b0 = friendlybytebuf.readByte();
      this.isFlying = (b0 & 2) != 0;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      byte b0 = 0;
      if (this.isFlying) {
         b0 = (byte)(b0 | 2);
      }

      friendlybytebuf.writeByte(b0);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePlayerAbilities(this);
   }

   public boolean isFlying() {
      return this.isFlying;
   }
}
