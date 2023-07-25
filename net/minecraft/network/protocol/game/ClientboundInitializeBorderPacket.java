package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundInitializeBorderPacket implements Packet<ClientGamePacketListener> {
   private final double newCenterX;
   private final double newCenterZ;
   private final double oldSize;
   private final double newSize;
   private final long lerpTime;
   private final int newAbsoluteMaxSize;
   private final int warningBlocks;
   private final int warningTime;

   public ClientboundInitializeBorderPacket(FriendlyByteBuf friendlybytebuf) {
      this.newCenterX = friendlybytebuf.readDouble();
      this.newCenterZ = friendlybytebuf.readDouble();
      this.oldSize = friendlybytebuf.readDouble();
      this.newSize = friendlybytebuf.readDouble();
      this.lerpTime = friendlybytebuf.readVarLong();
      this.newAbsoluteMaxSize = friendlybytebuf.readVarInt();
      this.warningBlocks = friendlybytebuf.readVarInt();
      this.warningTime = friendlybytebuf.readVarInt();
   }

   public ClientboundInitializeBorderPacket(WorldBorder worldborder) {
      this.newCenterX = worldborder.getCenterX();
      this.newCenterZ = worldborder.getCenterZ();
      this.oldSize = worldborder.getSize();
      this.newSize = worldborder.getLerpTarget();
      this.lerpTime = worldborder.getLerpRemainingTime();
      this.newAbsoluteMaxSize = worldborder.getAbsoluteMaxSize();
      this.warningBlocks = worldborder.getWarningBlocks();
      this.warningTime = worldborder.getWarningTime();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.newCenterX);
      friendlybytebuf.writeDouble(this.newCenterZ);
      friendlybytebuf.writeDouble(this.oldSize);
      friendlybytebuf.writeDouble(this.newSize);
      friendlybytebuf.writeVarLong(this.lerpTime);
      friendlybytebuf.writeVarInt(this.newAbsoluteMaxSize);
      friendlybytebuf.writeVarInt(this.warningBlocks);
      friendlybytebuf.writeVarInt(this.warningTime);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleInitializeBorder(this);
   }

   public double getNewCenterX() {
      return this.newCenterX;
   }

   public double getNewCenterZ() {
      return this.newCenterZ;
   }

   public double getNewSize() {
      return this.newSize;
   }

   public double getOldSize() {
      return this.oldSize;
   }

   public long getLerpTime() {
      return this.lerpTime;
   }

   public int getNewAbsoluteMaxSize() {
      return this.newAbsoluteMaxSize;
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }
}
