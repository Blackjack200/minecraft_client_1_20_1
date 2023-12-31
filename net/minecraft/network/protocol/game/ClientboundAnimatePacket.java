package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundAnimatePacket implements Packet<ClientGamePacketListener> {
   public static final int SWING_MAIN_HAND = 0;
   public static final int WAKE_UP = 2;
   public static final int SWING_OFF_HAND = 3;
   public static final int CRITICAL_HIT = 4;
   public static final int MAGIC_CRITICAL_HIT = 5;
   private final int id;
   private final int action;

   public ClientboundAnimatePacket(Entity entity, int i) {
      this.id = entity.getId();
      this.action = i;
   }

   public ClientboundAnimatePacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      this.action = friendlybytebuf.readUnsignedByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeByte(this.action);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAnimate(this);
   }

   public int getId() {
      return this.id;
   }

   public int getAction() {
      return this.action;
   }
}
