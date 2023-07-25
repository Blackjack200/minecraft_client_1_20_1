package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public abstract class ClientboundMoveEntityPacket implements Packet<ClientGamePacketListener> {
   protected final int entityId;
   protected final short xa;
   protected final short ya;
   protected final short za;
   protected final byte yRot;
   protected final byte xRot;
   protected final boolean onGround;
   protected final boolean hasRot;
   protected final boolean hasPos;

   protected ClientboundMoveEntityPacket(int i, short short0, short short1, short short2, byte b0, byte b1, boolean flag, boolean flag1, boolean flag2) {
      this.entityId = i;
      this.xa = short0;
      this.ya = short1;
      this.za = short2;
      this.yRot = b0;
      this.xRot = b1;
      this.onGround = flag;
      this.hasRot = flag1;
      this.hasPos = flag2;
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleMoveEntity(this);
   }

   public String toString() {
      return "Entity_" + super.toString();
   }

   @Nullable
   public Entity getEntity(Level level) {
      return level.getEntity(this.entityId);
   }

   public short getXa() {
      return this.xa;
   }

   public short getYa() {
      return this.ya;
   }

   public short getZa() {
      return this.za;
   }

   public byte getyRot() {
      return this.yRot;
   }

   public byte getxRot() {
      return this.xRot;
   }

   public boolean hasRotation() {
      return this.hasRot;
   }

   public boolean hasPosition() {
      return this.hasPos;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public static class Pos extends ClientboundMoveEntityPacket {
      public Pos(int i, short short0, short short1, short short2, boolean flag) {
         super(i, short0, short1, short2, (byte)0, (byte)0, flag, false, true);
      }

      public static ClientboundMoveEntityPacket.Pos read(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt();
         short short0 = friendlybytebuf.readShort();
         short short1 = friendlybytebuf.readShort();
         short short2 = friendlybytebuf.readShort();
         boolean flag = friendlybytebuf.readBoolean();
         return new ClientboundMoveEntityPacket.Pos(i, short0, short1, short2, flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeVarInt(this.entityId);
         friendlybytebuf.writeShort(this.xa);
         friendlybytebuf.writeShort(this.ya);
         friendlybytebuf.writeShort(this.za);
         friendlybytebuf.writeBoolean(this.onGround);
      }
   }

   public static class PosRot extends ClientboundMoveEntityPacket {
      public PosRot(int i, short short0, short short1, short short2, byte b0, byte b1, boolean flag) {
         super(i, short0, short1, short2, b0, b1, flag, true, true);
      }

      public static ClientboundMoveEntityPacket.PosRot read(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt();
         short short0 = friendlybytebuf.readShort();
         short short1 = friendlybytebuf.readShort();
         short short2 = friendlybytebuf.readShort();
         byte b0 = friendlybytebuf.readByte();
         byte b1 = friendlybytebuf.readByte();
         boolean flag = friendlybytebuf.readBoolean();
         return new ClientboundMoveEntityPacket.PosRot(i, short0, short1, short2, b0, b1, flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeVarInt(this.entityId);
         friendlybytebuf.writeShort(this.xa);
         friendlybytebuf.writeShort(this.ya);
         friendlybytebuf.writeShort(this.za);
         friendlybytebuf.writeByte(this.yRot);
         friendlybytebuf.writeByte(this.xRot);
         friendlybytebuf.writeBoolean(this.onGround);
      }
   }

   public static class Rot extends ClientboundMoveEntityPacket {
      public Rot(int i, byte b0, byte b1, boolean flag) {
         super(i, (short)0, (short)0, (short)0, b0, b1, flag, true, false);
      }

      public static ClientboundMoveEntityPacket.Rot read(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt();
         byte b0 = friendlybytebuf.readByte();
         byte b1 = friendlybytebuf.readByte();
         boolean flag = friendlybytebuf.readBoolean();
         return new ClientboundMoveEntityPacket.Rot(i, b0, b1, flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeVarInt(this.entityId);
         friendlybytebuf.writeByte(this.yRot);
         friendlybytebuf.writeByte(this.xRot);
         friendlybytebuf.writeBoolean(this.onGround);
      }
   }
}
