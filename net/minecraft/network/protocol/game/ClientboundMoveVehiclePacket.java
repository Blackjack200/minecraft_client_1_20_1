package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundMoveVehiclePacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float yRot;
   private final float xRot;

   public ClientboundMoveVehiclePacket(Entity entity) {
      this.x = entity.getX();
      this.y = entity.getY();
      this.z = entity.getZ();
      this.yRot = entity.getYRot();
      this.xRot = entity.getXRot();
   }

   public ClientboundMoveVehiclePacket(FriendlyByteBuf friendlybytebuf) {
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.yRot = friendlybytebuf.readFloat();
      this.xRot = friendlybytebuf.readFloat();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeFloat(this.yRot);
      friendlybytebuf.writeFloat(this.xRot);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleMoveVehicle(this);
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getYRot() {
      return this.yRot;
   }

   public float getXRot() {
      return this.xRot;
   }
}
