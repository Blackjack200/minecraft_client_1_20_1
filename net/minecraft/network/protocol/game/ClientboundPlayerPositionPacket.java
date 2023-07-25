package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.RelativeMovement;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float yRot;
   private final float xRot;
   private final Set<RelativeMovement> relativeArguments;
   private final int id;

   public ClientboundPlayerPositionPacket(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set, int i) {
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.yRot = f;
      this.xRot = f1;
      this.relativeArguments = set;
      this.id = i;
   }

   public ClientboundPlayerPositionPacket(FriendlyByteBuf friendlybytebuf) {
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.yRot = friendlybytebuf.readFloat();
      this.xRot = friendlybytebuf.readFloat();
      this.relativeArguments = RelativeMovement.unpack(friendlybytebuf.readUnsignedByte());
      this.id = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeFloat(this.yRot);
      friendlybytebuf.writeFloat(this.xRot);
      friendlybytebuf.writeByte(RelativeMovement.pack(this.relativeArguments));
      friendlybytebuf.writeVarInt(this.id);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleMovePlayer(this);
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

   public int getId() {
      return this.id;
   }

   public Set<RelativeMovement> getRelativeArguments() {
      return this.relativeArguments;
   }
}
