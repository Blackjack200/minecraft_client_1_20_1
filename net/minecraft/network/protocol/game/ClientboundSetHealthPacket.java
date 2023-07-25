package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetHealthPacket implements Packet<ClientGamePacketListener> {
   private final float health;
   private final int food;
   private final float saturation;

   public ClientboundSetHealthPacket(float f, int i, float f1) {
      this.health = f;
      this.food = i;
      this.saturation = f1;
   }

   public ClientboundSetHealthPacket(FriendlyByteBuf friendlybytebuf) {
      this.health = friendlybytebuf.readFloat();
      this.food = friendlybytebuf.readVarInt();
      this.saturation = friendlybytebuf.readFloat();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeFloat(this.health);
      friendlybytebuf.writeVarInt(this.food);
      friendlybytebuf.writeFloat(this.saturation);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetHealth(this);
   }

   public float getHealth() {
      return this.health;
   }

   public int getFood() {
      return this.food;
   }

   public float getSaturation() {
      return this.saturation;
   }
}
