package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public abstract class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
   protected final double x;
   protected final double y;
   protected final double z;
   protected final float yRot;
   protected final float xRot;
   protected final boolean onGround;
   protected final boolean hasPos;
   protected final boolean hasRot;

   protected ServerboundMovePlayerPacket(double d0, double d1, double d2, float f, float f1, boolean flag, boolean flag1, boolean flag2) {
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.yRot = f;
      this.xRot = f1;
      this.onGround = flag;
      this.hasPos = flag1;
      this.hasRot = flag2;
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleMovePlayer(this);
   }

   public double getX(double d0) {
      return this.hasPos ? this.x : d0;
   }

   public double getY(double d0) {
      return this.hasPos ? this.y : d0;
   }

   public double getZ(double d0) {
      return this.hasPos ? this.z : d0;
   }

   public float getYRot(float f) {
      return this.hasRot ? this.yRot : f;
   }

   public float getXRot(float f) {
      return this.hasRot ? this.xRot : f;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public boolean hasPosition() {
      return this.hasPos;
   }

   public boolean hasRotation() {
      return this.hasRot;
   }

   public static class Pos extends ServerboundMovePlayerPacket {
      public Pos(double d0, double d1, double d2, boolean flag) {
         super(d0, d1, d2, 0.0F, 0.0F, flag, true, false);
      }

      public static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf friendlybytebuf) {
         double d0 = friendlybytebuf.readDouble();
         double d1 = friendlybytebuf.readDouble();
         double d2 = friendlybytebuf.readDouble();
         boolean flag = friendlybytebuf.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.Pos(d0, d1, d2, flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeDouble(this.x);
         friendlybytebuf.writeDouble(this.y);
         friendlybytebuf.writeDouble(this.z);
         friendlybytebuf.writeByte(this.onGround ? 1 : 0);
      }
   }

   public static class PosRot extends ServerboundMovePlayerPacket {
      public PosRot(double d0, double d1, double d2, float f, float f1, boolean flag) {
         super(d0, d1, d2, f, f1, flag, true, true);
      }

      public static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf friendlybytebuf) {
         double d0 = friendlybytebuf.readDouble();
         double d1 = friendlybytebuf.readDouble();
         double d2 = friendlybytebuf.readDouble();
         float f = friendlybytebuf.readFloat();
         float f1 = friendlybytebuf.readFloat();
         boolean flag = friendlybytebuf.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.PosRot(d0, d1, d2, f, f1, flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeDouble(this.x);
         friendlybytebuf.writeDouble(this.y);
         friendlybytebuf.writeDouble(this.z);
         friendlybytebuf.writeFloat(this.yRot);
         friendlybytebuf.writeFloat(this.xRot);
         friendlybytebuf.writeByte(this.onGround ? 1 : 0);
      }
   }

   public static class Rot extends ServerboundMovePlayerPacket {
      public Rot(float f, float f1, boolean flag) {
         super(0.0D, 0.0D, 0.0D, f, f1, flag, false, true);
      }

      public static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf friendlybytebuf) {
         float f = friendlybytebuf.readFloat();
         float f1 = friendlybytebuf.readFloat();
         boolean flag = friendlybytebuf.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.Rot(f, f1, flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeFloat(this.yRot);
         friendlybytebuf.writeFloat(this.xRot);
         friendlybytebuf.writeByte(this.onGround ? 1 : 0);
      }
   }

   public static class StatusOnly extends ServerboundMovePlayerPacket {
      public StatusOnly(boolean flag) {
         super(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, flag, false, false);
      }

      public static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf friendlybytebuf) {
         boolean flag = friendlybytebuf.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.StatusOnly(flag);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeByte(this.onGround ? 1 : 0);
      }
   }
}
