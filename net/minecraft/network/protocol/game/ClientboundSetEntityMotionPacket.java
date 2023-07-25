package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundSetEntityMotionPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final int xa;
   private final int ya;
   private final int za;

   public ClientboundSetEntityMotionPacket(Entity entity) {
      this(entity.getId(), entity.getDeltaMovement());
   }

   public ClientboundSetEntityMotionPacket(int i, Vec3 vec3) {
      this.id = i;
      double d0 = 3.9D;
      double d1 = Mth.clamp(vec3.x, -3.9D, 3.9D);
      double d2 = Mth.clamp(vec3.y, -3.9D, 3.9D);
      double d3 = Mth.clamp(vec3.z, -3.9D, 3.9D);
      this.xa = (int)(d1 * 8000.0D);
      this.ya = (int)(d2 * 8000.0D);
      this.za = (int)(d3 * 8000.0D);
   }

   public ClientboundSetEntityMotionPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      this.xa = friendlybytebuf.readShort();
      this.ya = friendlybytebuf.readShort();
      this.za = friendlybytebuf.readShort();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeShort(this.xa);
      friendlybytebuf.writeShort(this.ya);
      friendlybytebuf.writeShort(this.za);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetEntityMotion(this);
   }

   public int getId() {
      return this.id;
   }

   public int getXa() {
      return this.xa;
   }

   public int getYa() {
      return this.ya;
   }

   public int getZa() {
      return this.za;
   }
}
